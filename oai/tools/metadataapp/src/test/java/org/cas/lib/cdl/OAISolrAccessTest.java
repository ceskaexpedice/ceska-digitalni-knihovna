package org.cas.lib.cdl;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.impl.SolrAccessImpl;
import cz.incad.kramerius.utils.OAISolrAccess;
import junit.framework.TestCase;

public class OAISolrAccessTest extends TestCase {

	public void testOAISolrAccess() throws ExecutionException, IOException {
		CachedAccessToJson cached = new CachedAccessToJson();
		OAISolrAccess solrAccess = new OAISolrAccess(cached);
		ObjectPidsPath[] path = solrAccess.getPath("uuid:6a17aac3-b9e9-11e1-1746-001143e3f55c");
		Assert.assertTrue(path.length == 1);

		Assert.assertEquals(path[0].getPathFromRootToLeaf()[0],"uuid:6862d997-b9e9-11e1-baae-005056a60003");
		Assert.assertEquals(path[0].getPathFromRootToLeaf()[1],"uuid:6a17aac2-b9e9-11e1-1746-001143e3f55c");
		Assert.assertEquals(path[0].getPathFromRootToLeaf()[2],"uuid:6a17aac3-b9e9-11e1-1746-001143e3f55c");
		
	}
}
