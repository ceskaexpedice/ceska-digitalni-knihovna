package org.cas.lib.cdl;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.json.JSONObject;
import org.junit.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import cz.incad.kramerius.utils.XMLUtils;
import junit.framework.TestCase;

public class UtilsTests extends TestCase {

	public void testMatchModel() {
		JSONObject item = Utils.item("uuid:6a17aac3-b9e9-11e1-1746-001143e3f55c");
		boolean matchModel = Utils.matchModel(item, "periodicalitem");
		Assert.assertTrue(matchModel);
		boolean notMatchModel = Utils.matchModel(item, "page");
		Assert.assertFalse(notMatchModel);
		boolean notMatchModel2 = Utils.matchModel(item, "monograph");
		Assert.assertFalse(notMatchModel2);
	}
	
	public void testDocuments2() throws ExecutionException, ParserConfigurationException {
		CachedAccessToDC dcs = new CachedAccessToDC();
		CachedAccessToJson jsons = new CachedAccessToJson();
		List<Document> forPath = dcs.getForPath("uuid:728deddb-4eb0-11e1-1418-001143e3f55c", jsons);
		Map<String, List<String>> collVals = Utils.collectValues(forPath);
		System.out.println(collVals);
		
	}
	public void testDocuments() throws ExecutionException, ParserConfigurationException, TransformerException {
		CachedAccessToDC dcs = new CachedAccessToDC();
		CachedAccessToJson jsons = new CachedAccessToJson();
		List<Document> forPath = dcs.getForPath("uuid:6a17aac3-b9e9-11e1-1746-001143e3f55c", jsons);
		
		Map<String, List<String>> collVals = Utils.collectValues(forPath);
		//{date=[1775 - 1779, 1775], identifier=[uuid:6862d997-b9e9-11e1-baae-005056a60003, issn:1210-0293, uuid:6a17aac2-b9e9-11e1-1746-001143e3f55c, contract:ABA00, uuid:6a17aac3-b9e9-11e1-1746-001143e3f55c], rights=[policy:public], publisher=[Gerlischen Buchhandlung], title=[Abhandlungen einer Privatgesellschaft in Böhmen, zur Aufnahme der Mathematik, der vaterländischen Geschichte, und der Naturgeschichte, ], type=[model:periodical, model:periodicalvolume, model:periodicalitem]}

		System.out.println(collVals);
	}
//	public void testDC() throws ParserConfigurationException, SAXException, IOException, TransformerException {
//		Document dc = Utils.dc("uuid:6a17aac3-b9e9-11e1-1746-001143e3f55c");
//		Element documentElement = dc.getDocumentElement();
//		XMLUtils.print(dc, System.out);
//		Element test = XMLUtils.findElement(dc.getDocumentElement(), "title","http://purl.org/dc/elements/1.1/");
//		System.out.println(test.getTextContent());
//		test.setTextContent("Abhandlungen einer Privatgesellschaft in Böhmen, zur Aufnahme der Mathematik, der vaterländischen Geschichte, und der Naturgeschichte");
//		System.out.println(test.getTextContent());
//		XMLUtils.print(dc, System.out);
//
//			
////		String locName = documentElement.getLocalName();
////		String namespace = documentElement.getNamespaceURI();
////		System.out.println("locname :"+locName);
////		System.out.println("namespcae :"+namespace);
//	}
}
