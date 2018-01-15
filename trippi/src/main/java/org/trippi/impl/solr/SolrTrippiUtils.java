package org.trippi.impl.solr;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class SolrTrippiUtils {

    public static final String SUBJECT = "subject";
    public static final String PREDICATE = "predicate";
    public static final String OBJECT = "object";
    public static final String OBJECT_TYPE = "object_type";

    public static final String LITERAL_TYPE = "literal";

    public static String solrQueryingPoint(String solrPoint, String query, int offset, int rows) {
        return solrPoint+(solrPoint.endsWith("/") ? "" : "/")+"select?indent=on&wt=json&q="+query+"&rows="+rows+"&start="+offset;
    }

    public static String solrUpdatingPoint(String solrPoint) {
        return solrPoint+(solrPoint.endsWith("/") ? "" : "/")+"update";
    }

    public static JSONObject clientGET(DefaultHttpClient httpclient, String url) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        HttpResponse execute = httpclient.execute(httpGet);
        byte[] bytes = EntityUtils.toByteArray(execute.getEntity());
        return new JSONObject(new String(bytes, "UTF-8"));
    }

    public static JSONObject clientPOST(DefaultHttpClient httpclient, String url, JSONObject data) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");

        JSONObject docWrapper = new JSONObject();
        docWrapper.put("doc",data);

        JSONObject updateJSON = new JSONObject();
        updateJSON.put("add", docWrapper);

        String s = updateJSON.toString();
        StringEntity entity = new StringEntity(s, "UTF-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);


        HttpResponse response = httpclient.execute(httpPost);
        return new JSONObject(EntityUtils.toString(response.getEntity()));
    }

}
