package org.cas.lib.cdl;

import javax.ws.rs.core.MediaType;

import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.ClientResponse.Status;

public class PrivateConnectUtils {

    public static final String SOLR_UDATE_ENDPOINT = "http://localhost:8983/solr/kramerius/update?commit=true";

    
    public static JSONObject indexDocument(String solrEndpoint, String pid, JSONObject doc) {
        JSONObject add = new JSONObject();
        add.put("add", doc);

        System.out.println(add.toString());
        Client c = Client.create();
        WebResource r = c
                .resource(solrEndpoint);

        String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON)
                .entity(add.toString(), MediaType.APPLICATION_JSON)
                .post(String.class);
        
        JSONObject jsonObject = new JSONObject(t);
        return jsonObject;
   }
    public static JSONObject indexDocument(String pid, JSONObject doc) {
        return indexDocument(SOLR_UDATE_ENDPOINT, pid, doc);
    }    
}
