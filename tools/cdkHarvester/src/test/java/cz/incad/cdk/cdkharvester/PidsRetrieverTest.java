package cz.incad.cdk.cdkharvester;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.easymock.EasyMock;

import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.XMLUtils;
import junit.framework.Assert;
import junit.framework.TestCase;

public class PidsRetrieverTest extends TestCase {

	public void testRetriever() throws Exception {
		PidsRetriever retriever = EasyMock.createMockBuilder(PidsRetriever.class)
        .withConstructor("1900-01-01T00:00:00.002Z","http://localhost:8080/search","krameriusAdmin","krameriusAdmin","")
        .addMockedMethod("solrResults")
        .createMock();

		
        EasyMock.expect(retriever.solrResults("http://localhost:8080/search/api/v4.6/cdk/prepare?rows=500&date=1900-01-01T00:00:00.002Z")).andReturn(XMLUtils.parseDocument(PidsRetrieverTest.class.getResourceAsStream("pidsretrieve1.xml"))).anyTimes();
        EasyMock.expect(retriever.solrResults("http://localhost:8080/search/api/v4.6/cdk/prepare?rows=500&date=2016-10-13T12:43:03.105Z")).andReturn(XMLUtils.parseDocument(PidsRetrieverTest.class.getResourceAsStream("pidsretrieve2.xml"))).anyTimes();
        EasyMock.expect(retriever.solrResults("http://localhost:8080/search/api/v4.6/cdk/prepare?rows=500&date=2016-10-23T18:43:51.311Z")).andReturn(XMLUtils.parseDocument(PidsRetrieverTest.class.getResourceAsStream("pidsretrieve3.xml"))).anyTimes();
        EasyMock.expect(retriever.solrResults("http://localhost:8080/search/api/v4.6/cdk/prepare?rows=500&date=2016-12-15T12:24:03.128Z")).andReturn(XMLUtils.parseDocument(PidsRetrieverTest.class.getResourceAsStream("pidsretrieve4.xml"))).anyTimes();
        
        EasyMock.replay(retriever);
        int counter = 0;
        while (retriever.hasNext()) {
            Map.Entry<String, String> entry = retriever.next();
            if (entry.getValue() != null) {
            	Assert.assertNotNull(entry.getKey());
            	Assert.assertNotNull(entry.getValue());
            	counter +=1;
            }
        }
        Assert.assertTrue(counter == 1184);
	}

    public void testRetriever2() throws Exception {
        PidsRetriever retriever = EasyMock.createMockBuilder(PidsRetriever.class)
                .withConstructor("1900-01-01T00:00:00.002Z","http://localhost:8080/search","krameriusAdmin","krameriusAdmin","2016-10-13T11:23:01.511Z")
                .addMockedMethod("solrResults")
                .createMock();


        EasyMock.expect(retriever.solrResults("http://localhost:8080/search/api/v4.6/cdk/prepare?rows=500&date=1900-01-01T00:00:00.002Z")).andReturn(XMLUtils.parseDocument(PidsRetrieverTest.class.getResourceAsStream("pidsretrieve1.xml"))).anyTimes();
        EasyMock.expect(retriever.solrResults("http://localhost:8080/search/api/v4.6/cdk/prepare?rows=500&date=2016-10-13T12:43:03.105Z")).andReturn(XMLUtils.parseDocument(PidsRetrieverTest.class.getResourceAsStream("pidsretrieve2.xml"))).anyTimes();
        EasyMock.expect(retriever.solrResults("http://localhost:8080/search/api/v4.6/cdk/prepare?rows=500&date=2016-10-23T18:43:51.311Z")).andReturn(XMLUtils.parseDocument(PidsRetrieverTest.class.getResourceAsStream("pidsretrieve3.xml"))).anyTimes();
        EasyMock.expect(retriever.solrResults("http://localhost:8080/search/api/v4.6/cdk/prepare?rows=500&date=2016-12-15T12:24:03.128Z")).andReturn(XMLUtils.parseDocument(PidsRetrieverTest.class.getResourceAsStream("pidsretrieve4.xml"))).anyTimes();

        EasyMock.replay(retriever);
        int counter = 0;
        while (retriever.hasNext()) {
            Map.Entry<String, String> entry = retriever.next();
            if (entry.getValue() != null) {
                Assert.assertNotNull(entry.getKey());
                Assert.assertNotNull(entry.getValue());
                counter +=1;
            }
        }
        Assert.assertTrue(counter == 7);
    }
}
