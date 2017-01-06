package cz.incad.cdk.cdkharvester;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.easymock.EasyMock;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cz.incad.cdk.cdkharvester.process.ImageReplaceProcess;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;
import junit.framework.Assert;
import junit.framework.TestCase;

public class ImageReplaceProcessTest extends TestCase  {

	public void testReplaceError() throws Exception {
		ZipIteration iter = new ZipIteration();
		InputStream foxml = iter.getFOXML("uuid:3450c8a6-1327-46f5-8b31-15f46dc23152");

    	Document document = XMLUtils.parseDocument(foxml, true);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
    	XMLUtils.print(document, bos);


		ImageReplaceProcess imgProcess = EasyMock.createMockBuilder(ImageReplaceProcess.class)
				.withConstructor()
				.addMockedMethod("binaryContentStream")
				.createMock();
		
		
		imgProcess.binaryContentStream(EasyMock.<Document>isA(Document.class),EasyMock.<Element>isA(Element.class),EasyMock.<Element>isA(Element.class),EasyMock.<String>isA(String.class));
		EasyMock.expectLastCall().andThrow(new IOException("java.io.IOException: Server returned HTTP response code:"));
		
		EasyMock.replay(imgProcess);

		byte[] processed = imgProcess.process("http://localhost:8080/search", "uuid:3450c8a6-1327-46f5-8b31-15f46dc23152", new ByteArrayInputStream(bos.toByteArray()));
		// IMG_THUMB must be the same 
		Document processedDoc = XMLUtils.parseDocument(new ByteArrayInputStream(processed), true);
		Element foundElement = XMLUtils.findElement(processedDoc.getDocumentElement(), new XMLUtils.ElementsFilter() {
			
			@Override
			public boolean acceptElement(Element element) {
				String locname = element.getLocalName();
				if (locname.equals("datastream")) {
					String attribute = element.getAttribute("ID");
					return attribute.equals("IMG_THUMB");
				}
				return false;
			}
		});
		
		
		Assert.assertTrue(foundElement != null);
		Element locElement = XMLUtils.findElement(foundElement, new XMLUtils.ElementsFilter() {

			@Override
			public boolean acceptElement(Element element) {
				String locName = element.getLocalName();
				if (locName.equals("contentLocation")) {
					return true;
				}
				return false;
			}
			
		});
		
		Assert.assertTrue(locElement != null);
		String attribute = locElement.getAttribute("REF");
		Assert.assertEquals("http://imageserver.mzk.cz/mzk03/000/649/862/3450c8a6-1327-46f5-8b31-15f46dc23152/preview.jpg", attribute);
	
	}
}	