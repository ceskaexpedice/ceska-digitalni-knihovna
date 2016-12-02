package org.cas.lib.cdl;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

public class LanguageDetectImplTest extends TestCase {
	
	public void testLanguageElement() throws ParserConfigurationException, SAXException, IOException {
		Document dc = Utils.dc("uuid:719be951-4eb0-11e1-9043-005056a60003");
		LanguageDetectImpl impl = new LanguageDetectImpl();
		String languageFromDC = Utils.findLanguageFromDC(dc);
		Assert.assertEquals("cze", languageFromDC);
	}

	public void testLanguagePublisherElement() throws ParserConfigurationException, SAXException, IOException {
		Document dc = Utils.dc("uuid:719be951-4eb0-11e1-9043-005056a60003");
		LanguageDetectImpl impl = new LanguageDetectImpl();
		String publisherFromDC = Utils.findPublisherFromDC(dc);
		Assert.assertEquals("Academia", publisherFromDC);
	}
	
	
	public void testPublisherNormalization() {
		String name = LanguageDetectImpl.normalizepublishername("Gerlischen Buchhandlung");
		Assert.assertEquals("gerlischenbuchhandlung", name);
	}

	public void testLanguage() throws ExecutionException {
		CachedAccessToDC dcs = new CachedAccessToDC();
		CachedAccessToJson jsons = new CachedAccessToJson();
		LanguageDetectImpl impl = new LanguageDetectImpl();
		String detectLanguage = impl.detectLanguage(dcs.getForPath("uuid:6a17aac3-b9e9-11e1-1746-001143e3f55c", jsons));
		Assert.assertEquals("ger", detectLanguage);
	}

	public void testLanguage2() throws ExecutionException {
		CachedAccessToDC dcs = new CachedAccessToDC();
		CachedAccessToJson jsons = new CachedAccessToJson();
		LanguageDetectImpl impl = new LanguageDetectImpl();
		String detectLanguage = impl.detectLanguage(dcs.getForPath("uuid:9e413e2f-abbd-11e1-7639-001143e3f55c", jsons));
		Assert.assertEquals("cze", detectLanguage);
	}
}
