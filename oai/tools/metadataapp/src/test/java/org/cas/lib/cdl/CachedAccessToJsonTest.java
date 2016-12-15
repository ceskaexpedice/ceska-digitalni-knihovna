package org.cas.lib.cdl;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONObject;
import org.junit.Assert;
import org.w3c.dom.Document;

import junit.framework.TestCase;

public class CachedAccessToJsonTest extends TestCase {

	public void testCachedJSON() throws ExecutionException {
		CachedAccessToJson cached = new CachedAccessToJson();
		JSONObject jsonObject = cached.get("uuid:6a17aac3-b9e9-11e1-1746-001143e3f55c");
		Assert.assertNotNull(jsonObject);
	}

	public void testCachedJSONSInPath() throws ExecutionException {
		CachedAccessToJson cached = new CachedAccessToJson();
		List<JSONObject> path = cached.getForPath("uuid:6a17aac3-b9e9-11e1-1746-001143e3f55c", cached);
		Assert.assertTrue(path.size() == 3);
	}
	
}
