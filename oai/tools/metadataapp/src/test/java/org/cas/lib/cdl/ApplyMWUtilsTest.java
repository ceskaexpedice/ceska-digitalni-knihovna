package org.cas.lib.cdl;

import java.io.IOException;

import org.apache.commons.configuration.Configuration;

import cz.cas.lib.knav.ApplyMWUtils;
import cz.incad.kramerius.utils.OAIFedoraAccess;
import cz.incad.kramerius.utils.OAISolrAccess;
import cz.incad.kramerius.utils.conf.KConfiguration;
import junit.framework.TestCase;

public class ApplyMWUtilsTest extends TestCase {

	public void testSettings() throws IOException {
		Configuration conf = KConfiguration.getInstance().getConfiguration();

		CachedAccessToMods modsCache = new CachedAccessToMods();
		CachedAccessToDC dcCache = new CachedAccessToDC();
		CachedAccessToJson jsonCache = new CachedAccessToJson();
		OAIFedoraAccess fa = new OAIFedoraAccess(modsCache, dcCache);
		OAISolrAccess sa = new OAISolrAccess(jsonCache);

		//OAIFedoraAccess fa = new OAIFedoraAccess(modsCache, dcCache);
		
		int configuredWall = ApplyMWUtils.configuredWall(sa, "uuid:6a17aac3-b9e9-11e1-1746-001143e3f55c", conf);
		System.out.println(configuredWall);
		
	}
}
