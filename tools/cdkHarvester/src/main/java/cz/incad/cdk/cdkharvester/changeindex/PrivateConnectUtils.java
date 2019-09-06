package cz.incad.cdk.cdkharvester.changeindex;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.ClientResponse.Status;

import cz.incad.kramerius.utils.conf.KConfiguration;

public class PrivateConnectUtils {

    public static final String SOLR_UDATE_ENDPOINT = "http://localhost:8983/solr/kramerius/update?commit=true";
    public static final String SOLR_SELECT_ENDPOINT = "http://localhost:8983/solr/kramerius/select";

    
    public static JSONObject indexDocument(String solrUpdateEndpoint, String pid, JSONObject doc) {
        JSONObject add = new JSONObject();
        add.put("add", doc);

        Client c = Client.create();
        WebResource r = c
                .resource(solrUpdateEndpoint);

        String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON)
                .entity(add.toString(), MediaType.APPLICATION_JSON)
                .post(String.class);
        
        JSONObject jsonObject = new JSONObject(t);
        return jsonObject;
   }
    public static JSONObject indexDocument(String pid, JSONObject doc) {
        return indexDocument(SOLR_UDATE_ENDPOINT, pid, doc);
    }    


    public static JSONObject findDoc(String solrSelectEndpoint, String pid) throws UnsupportedEncodingException, URISyntaxException {
        String q="?q=PID:"+URLEncoder.encode("\"","UTF-8")+pid+URLEncoder.encode("\"","UTF-8")+"*&wt=json";
        String u = solrSelectEndpoint+q;
        Client c = Client.create();
        WebResource r = c
                .resource(u);

        String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON)
                .get(String.class);
        System.out.println(t);
        JSONObject jsonObject = new JSONObject(t);
        return jsonObject;
    }

    
    public static void main(String[] args) throws UnsupportedEncodingException, URISyntaxException {
        //String solrEndpoint = KConfiguration.getInstance().getSolrHost()+"/select";
        String solrEndpoint = "http://cdk.lib.cas.cz/search/api/v5.0/search";
        //JSONObject results = findDoc(solrEndpoint, "uuid:376e1df7-e2a0-4930-8a4e-ad357c2b979b");
        JSONObject results = findDoc(solrEndpoint, "uuid:4eac74b0-e92c-11dc-9fa1-000d606f5dc7");

        if (ResultsUtils.docsExists(results)) {
            System.out.println("Exists ");
            if (ResultsUtils.collectionExists(results)) {
                System.out.println(" collection exists ");
                List<String> disectCollections = ResultsUtils.disectCollections(results);
                System.out.println(" Disect collections "+disectCollections);
            }
        } else {
            System.out.println("Doesnt exists");
        }

    }

    // TODO:Move
    public static List<String> disectCollections(String solrEndpoint, String pid) throws UnsupportedEncodingException, URISyntaxException {
        JSONObject findDoc = findDoc(solrEndpoint, pid);
        return ResultsUtils.disectCollections(findDoc);
    }
}
