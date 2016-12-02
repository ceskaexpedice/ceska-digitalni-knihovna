package org.cas.lib.cdl;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONObject;
import org.junit.Assert;

import junit.framework.TestCase;

public class TitlePartsTests extends TestCase{

	public void testTitle() throws ExecutionException {
		CachedAccessToJson cached = new CachedAccessToJson();
		List<JSONObject> path = cached.getForPath("uuid:6a17aac3-b9e9-11e1-1746-001143e3f55c", cached);
		String buildTitle = TitleParts.buildTitle(path);
		Assert.assertTrue("Abhandlungen einer Privatgesellschaft in Böhmen, zur Aufnahme der Mathematik, der vaterländischen Geschichte, und der Naturgeschichte | 1775".equals(buildTitle));
	}

	public void testTitle2() throws ExecutionException {
		CachedAccessToJson cached = new CachedAccessToJson();
		List<JSONObject> path = cached.getForPath("uuid:728deddb-4eb0-11e1-1418-001143e3f55c", cached);
		String buildTitle = TitleParts.buildTitle(path);
		Assert.assertTrue("Právník | 1861 Volume:1 | Number:5".equals(buildTitle));
	}

}
