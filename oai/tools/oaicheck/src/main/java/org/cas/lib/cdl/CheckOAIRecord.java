package org.cas.lib.cdl;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class CheckOAIRecord implements IterationControl {
	public static final Logger LOGGER = Logger.getLogger(ChecksEnum.cdkoairec.name());

	private int counter = 0;
	private long totalTime = 0;
	private List<String> errList = new ArrayList<>();

	@Override
	public void onPidsIterate(List<String> pids) throws UnsupportedEncodingException {
		for (String pid : pids) {
			counter += 1;
			try {
				long start = System.currentTimeMillis();
				LOGGER.info("Testing transformation for "+pid);
				String transformed = PublicConnectUtils.oaiRec(pid);
				XMLUtils.parseDocument(new StringReader(transformed));
				System.out.println(transformed);
				long stop = System.currentTimeMillis();
				
				long l = stop - start;
				totalTime += l;
				
				LOGGER.info("It took :"+l +" counter :"+counter+" average :"+(totalTime/counter)  );
				
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
