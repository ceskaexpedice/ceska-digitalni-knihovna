package org.cas.lib.cdl;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.xml.transform.TransformerException;

import org.json.JSONObject;
import org.junit.Assert;
import org.w3c.dom.Document;

import cz.incad.kramerius.utils.XMLUtils;
import junit.framework.TestCase;

public class CachedAccessToDCTest extends TestCase{

	public void testCachedDC() throws ExecutionException {
		CachedAccessToDC cached = new CachedAccessToDC();
		Document dc = cached.get("uuid:6a17aac3-b9e9-11e1-1746-001143e3f55c");
		Assert.assertNotNull(dc);
	}

	public void testCachedDCInPath() throws ExecutionException {
		CachedAccessToJson aware = new CachedAccessToJson();
		CachedAccessToDC cached = new CachedAccessToDC();
		List<Document> path = cached.getForPath("uuid:6a17aac3-b9e9-11e1-1746-001143e3f55c", aware);
		Assert.assertTrue(path.size() == 3);
	}

	public void testCached2DCInPath() throws ExecutionException, TransformerException {
		CachedAccessToJson aware = new CachedAccessToJson();
		String pid = "uuid:6b182ad3-b9e9-11e1-1726-001143e3f55c";
		CachedAccessToDC cached = new CachedAccessToDC();
		List<Document> path = cached.getForPath(pid, aware);
		for (Document doc : path) {
			XMLUtils.print(doc, System.out);
		}
	}
}
