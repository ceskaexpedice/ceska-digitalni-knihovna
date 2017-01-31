package cz.incad.cdk.cdkharvester;

import java.util.List;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.utils.conf.KConfiguration;

public class Utils {
	
	public static final String API_POSTFIX = "api/v5.0/item/";
	
	public static boolean checkExists(String baseUrl, String pid) {
		String url = baseUrl+(baseUrl.endsWith("/") ? "" : "/")+API_POSTFIX+pid;
		Client c = Client.create();
        WebResource r = c.resource(url);
        ClientResponse response = r.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        return response.getStatus() == ClientResponse.Status.OK.getStatusCode();
	}
	
	public static List<String> getSkipList() {
		return KConfiguration.getInstance().getConfiguration().getList("skip.pids");
	}
	
}
