package cz.incad.cdk.cdkharvester;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class Utils {
	
	public static final String API_POSTFIX = "api/v5.0/item/";
	
	public static boolean checkExists(String baseUrl, String pid) {
		String url = baseUrl+(baseUrl.endsWith("/") ? "" : "/")+API_POSTFIX+pid;
		Client c = Client.create();
        WebResource r = c.resource(url);
        ClientResponse response = r.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        return response.getStatus() == ClientResponse.Status.OK.getStatusCode();
	}

	public static void main(String[] args) {
        //p.start("http://vmkramerius.incad.cz:8080/search", "vmkramerius", "vc:534b8b98-82d8-49c7-a751-33e88aaeeea9", "krameriusAdmin", "krameriusAdmin");
		boolean checkExists = checkExists("http://cdk.lib.cas.cz/search", "uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22");
		System.out.println(checkExists);

    	if (Utils.checkExists("http://cdk.lib.cas.cz/search", "uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22")) {
    		System.out.println("RETURNING .... ");
    	}
	}
}
