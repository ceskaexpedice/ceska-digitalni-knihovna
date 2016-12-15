package org.cas.lib.cdl;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.w3c.dom.Document;

import cz.incad.kramerius.utils.OAIFedoraAccess;
import junit.framework.TestCase;

public class OAIFedoraAccessTest extends TestCase {

	public void testOAIFEdoraAccess() throws ExecutionException, IOException {
		CachedAccessToMods modsCache = new CachedAccessToMods();
		CachedAccessToDC dcCache = new CachedAccessToDC();
		OAIFedoraAccess fa = new OAIFedoraAccess(modsCache, dcCache);
		Document mods = fa.getBiblioMods("uuid:6a17aac3-b9e9-11e1-1746-001143e3f55c");
		Assert.assertNotNull(mods);
	}

}
