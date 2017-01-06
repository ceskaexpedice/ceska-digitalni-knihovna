package cz.incad.kramerius.change;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * Meni jmeno zdroje v DC streamu
 * @author pavels
 */
public class CDKSourceUrl {
	
	public static final Logger LOGGER = Logger.getLogger(CDKSourceUrl.class.getName());
	
	public static final String STREAM = "DC";
	
	public static FedoraAccess fa() throws IOException {
		KConfiguration conf = KConfiguration.getInstance();
		FedoraAccess fa = new FedoraAccessImpl(conf, null);
		return fa;
	}
	

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, TransformerException {
		if (args.length >= 2) {
			String pid = args[0];
			String url = args[1];
			InputStream dataStream = fa().getDataStream(pid, STREAM);
			Document parsed = XMLUtils.parseDocument(dataStream,true);
			Element findElement = XMLUtils.findElement(parsed.getDocumentElement(), new XMLUtils.ElementsFilter() {
				
				@Override
				public boolean acceptElement(Element elm) {
					return (elm.getLocalName().equals("source"));
				}
			});
			if (findElement == null) {
				findElement = parsed.createElementNS(FedoraNamespaces.DC_NAMESPACE_URI, "source");
				parsed.getDocumentElement().appendChild(findElement);
			}
			findElement.setTextContent(url);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			XMLUtils.print(parsed, bos);

			fa().getAPIM().modifyDatastreamByValue(pid,  FedoraUtils.DC_STREAM, null, null, null, null, bos.toByteArray(), null, null, null, false);
			LOGGER.info("Zmenena sbirka pid ("+pid+") s hodnotou ("+url+")" );
		} else usage();
	}


	private static void usage() {
		LOGGER.info("change <pid> <url>");
	}
}
