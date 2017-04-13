package cz.incad.cdk.cdkharvester.manageprocess;

import java.io.IOException;

import javax.ws.rs.core.MediaType;

import org.kramerius.replications.BasicAuthenticationClientFilter;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.utils.conf.KConfiguration;
import net.sf.json.JSONObject;

public abstract class AbstractCheckLiveProcess implements CheckLiveProcess {

	protected String getStatus(String processUuid) throws IOException {
        Client c = Client.create();
        WebResource r = c.resource(KConfiguration.getInstance().getConfiguration().getString("_fedoraTomcatHost") + "/search/api/v4.6/processes/" + processUuid);
        r.addFilter(new BasicAuthenticationClientFilter(KConfiguration.getInstance().getConfiguration().getString("cdk.krameriusUser"),
        		KConfiguration.getInstance().getConfiguration().getString("cdk.krameriusPwd")));
        String t = r.accept(MediaType.APPLICATION_JSON).get(String.class);
        JSONObject j = JSONObject.fromObject(t);
        return j.getString("state");
    }

	
}
