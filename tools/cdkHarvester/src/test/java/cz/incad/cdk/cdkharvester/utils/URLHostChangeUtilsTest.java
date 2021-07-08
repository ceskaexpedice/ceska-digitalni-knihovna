package cz.incad.cdk.cdkharvester.utils;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.net.MalformedURLException;

public class URLHostChangeUtilsTest extends TestCase {


    public void testURLChange() throws MalformedURLException {
        String replaced = URLHostChangeUtils.changeHostString("http://kramerius.nfa.cz:443/search/handle/uuid:9f7f8ec3-443e-11eb-836c-00505684fda5", "https://kramerius.nfa.cz");
        Assert.assertTrue(replaced.equals("https://kramerius.nfa.cz/search/handle/uuid:9f7f8ec3-443e-11eb-836c-00505684fda5"));

        replaced = URLHostChangeUtils.changeHostString("http://kramerius.nfa.cz:443/search/handle/uuid:9f7f8ec3-443e-11eb-836c-00505684fda5", "https://kramerius.nfa.cz/");
        Assert.assertTrue(replaced.equals("https://kramerius.nfa.cz/search/handle/uuid:9f7f8ec3-443e-11eb-836c-00505684fda5"));

        replaced = URLHostChangeUtils.changeHostString("http://kramerius.nfa.cz:443/search/handle/uuid:9f7f8ec3-443e-11eb-836c-00505684fda5?param1=1&param2=2&param3=3", "https://kramerius.nfa.cz/");
        Assert.assertTrue(replaced.equals("https://kramerius.nfa.cz/search/handle/uuid:9f7f8ec3-443e-11eb-836c-00505684fda5?param1=1&param2=2&param3=3"));

    }
}
