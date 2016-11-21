package org.cas.lib.cdl;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;


/**
 * Connection utils 
 * @author pstastny
 */
public class PublicConnectUtils {

    public static final String CDK_SERVER = "http://cdk.lib.cas.cz/search/";
    public static final String SOURCE_KRAMERIUS = "http://kramerius.lib.cas.cz/search/";

    /**
     * HEAD request to source kramerius
     * @param pid Requested pid
     * @return
     */
    public static boolean headFullImgSourcecKramerius(String pid, String sourceKramerius) {
        Client c = Client.create();
        WebResource r = c
                .resource(sourceKramerius+"img?uuid="
                        + pid+"&stream=IMG_FULL&action=GETRAW");
        ClientResponse head = r.accept(MediaType.APPLICATION_JSON).head();
        Status status = head.getClientResponseStatus();
        return (status.equals(Status.OK) || status.equals(Status.NOT_MODIFIED));
   }

    public static boolean headFullImgSourcecKramerius(String pid) {
        return headFullImgSourcecKramerius(pid, SOURCE_KRAMERIUS);
    }
    
    
    /**
     * Searching in source kramerius
     * @param query
     * @return
     */
    public static String searchInSourceKramerius(String query) {
        Client c = Client.create();
        WebResource r = c
                .resource(SOURCE_KRAMERIUS+"api/v5.0/search?q="
                        + query);
        String t = r.accept(MediaType.APPLICATION_JSON).get(String.class);
        return t;
    }

    /**
     * Searching in CDK
     * @param query
     * @return
     */
    public static String searchInCDK(String query) {
        Client c = Client.create();
        WebResource r = c
                .resource(CDK_SERVER+"api/v5.0/search?q="
                        + query);
        String t = r.accept(MediaType.APPLICATION_JSON).get(String.class);
        return t;
    }
    
}
