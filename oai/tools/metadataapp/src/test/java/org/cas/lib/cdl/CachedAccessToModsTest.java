package org.cas.lib.cdl;

import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.w3c.dom.Document;

import junit.framework.TestCase;

public class CachedAccessToModsTest extends TestCase {

	public void testCachedMODS() throws ExecutionException {
		CachedAccessToMods cached = new CachedAccessToMods();
		Document mods = cached.get("uuid:6a17aac3-b9e9-11e1-1746-001143e3f55c");
		Assert.assertNotNull(mods);
	}

}
