package org.cas.lib.cdl;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;


public class CheckContainsLangElement implements IterationControl {

	public static final Logger LOGGER = Logger.getLogger(ChecksEnum.langelm.name());
	private List<String> errList = new ArrayList<>();
	private int counter = 0;

	@Override
	public void onPidsIterate(List<String> pids) throws UnsupportedEncodingException {
		for (String pid : pids) {
			this.counter += 1;
			try {
				String transformed = PublicConnectUtils.dcTransformation(pid);
				Document parseDocument = XMLUtils.parseDocument(new StringReader(transformed), true);
				Element langElement = XMLUtils.findElement(parseDocument.getDocumentElement(), "language",
						"http://purl.org/dc/elements/1.1/");
				if (langElement == null || langElement.getTextContent().trim().equals("")) {
					LOGGER.severe("No lang element for pid '"+pid+"'");
					errList.add(pid);
				}
			} catch (ParserConfigurationException e) {
				errList.add(pid);
				LOGGER.log(Level.SEVERE, e.getMessage(),e);
			} catch (SAXException e) {
				errList.add(pid);
				LOGGER.log(Level.SEVERE, e.getMessage(),e);
			} catch (IOException e) {
				errList.add(pid);
				LOGGER.log(Level.SEVERE, e.getMessage(),e);
			}
		}

	}

	@Override
	public void printResult() {
		LOGGER.info("Iterated pids "+this.counter);
		LOGGER.info("Errors :"+this.errList.size());
		if (!this.errList.isEmpty()) {
			LOGGER.info("Error pids :"+this.errList);
		}
	}
}
