package cz.incad.cdk.cdkharvester.iterator;

import cz.incad.kramerius.utils.BasicAuthenticationClientFilter;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;

public abstract class AbstractCDKHarvestIteration implements CDKHarvestIteration {

    protected WebResource client(String urlStr) {
        return client(urlStr, null, null);
    }

    protected WebResource client(String urlStr, String userName, String pswd) {
        Client c = Client.create();
        // follow redirect
        c.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
        c.setConnectTimeout(2000);
        c.setReadTimeout(20000);
        WebResource r = c.resource(urlStr);
        if (userName != null && pswd != null) {
            r.addFilter(new BasicAuthenticationClientFilter(userName, pswd));
        }
        return r;
    }
}