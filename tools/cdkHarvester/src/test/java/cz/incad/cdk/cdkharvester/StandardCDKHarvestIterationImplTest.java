package cz.incad.cdk.cdkharvester;

import org.easymock.EasyMock;

import cz.incad.cdk.cdkharvester.iterator.CDKHarvestIterationItem;
import cz.incad.cdk.cdkharvester.iterator.StandardCDKHarvestIterationImpl;
import cz.incad.kramerius.utils.XMLUtils;
import junit.framework.Assert;
import junit.framework.TestCase;

public class StandardCDKHarvestIterationImplTest extends TestCase {

	public void testStandardCDKHarvest() throws Exception {
		StandardCDKHarvestIterationImpl iterator = EasyMock.createMockBuilder(StandardCDKHarvestIterationImpl.class)
        .withConstructor("1900-01-01T00:00:00.002Z","http://localhost:8080/search","krameriusAdmin","krameriusAdmin")
        .addMockedMethod("solrResults")
        .createMock();
		
		
        EasyMock.expect(iterator.solrResults("http://localhost:8080/search/api/v4.6/cdk/prepare?rows=500&date=1900-01-01T00:00:00.002Z")).andReturn(XMLUtils.parseDocument(StandardCDKHarvestIterationImplTest.class.getResourceAsStream("pidsretrieve1.xml"))).anyTimes();
        EasyMock.expect(iterator.solrResults("http://localhost:8080/search/api/v4.6/cdk/prepare?rows=500&date=2016-10-13T12:43:03.105Z")).andReturn(XMLUtils.parseDocument(StandardCDKHarvestIterationImplTest.class.getResourceAsStream("pidsretrieve2.xml"))).anyTimes();
        EasyMock.expect(iterator.solrResults("http://localhost:8080/search/api/v4.6/cdk/prepare?rows=500&date=2016-10-23T18:43:51.311Z")).andReturn(XMLUtils.parseDocument(StandardCDKHarvestIterationImplTest.class.getResourceAsStream("pidsretrieve3.xml"))).anyTimes();
        EasyMock.expect(iterator.solrResults("http://localhost:8080/search/api/v4.6/cdk/prepare?rows=500&date=2016-12-15T12:24:03.128Z")).andReturn(XMLUtils.parseDocument(StandardCDKHarvestIterationImplTest.class.getResourceAsStream("pidsretrieve4.xml"))).anyTimes();
        
        EasyMock.replay(iterator);

        iterator.init();
        
        int counter = 0;
        while (iterator.hasNext()) {
        	CDKHarvestIterationItem next = iterator.next();
        	counter +=1;
        }
        Assert.assertTrue(counter == 1184);
	}

}
