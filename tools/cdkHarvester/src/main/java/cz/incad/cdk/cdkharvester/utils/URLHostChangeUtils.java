package cz.incad.cdk.cdkharvester.utils;

import cz.incad.kramerius.utils.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;

public class URLHostChangeUtils {
    private URLHostChangeUtils() {}

    public static String changeHostString(String givenURL, String expectedHost) throws MalformedURLException {

        URL urlObject = new URL(givenURL);
        String query = urlObject.getQuery();
        String path = urlObject.getPath();
        if (expectedHost.endsWith("/")) {
            expectedHost = expectedHost.substring(0, expectedHost.length()-1);
        }
        String base = String.format("%s%s", expectedHost, path);
        if (query != null && StringUtils.isAnyString(query)) {
            return  base +"?"+query;
        } else {
            return base;
        }
    }
}
