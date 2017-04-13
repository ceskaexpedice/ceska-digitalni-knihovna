package cz.incad.cdk.cdkharvester.iterator;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.util.URIUtil;
import org.kramerius.replications.BasicAuthenticationClientFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;

import cz.incad.kramerius.utils.IOUtils;

/**
 * Standard list of pids based on prepare endpoint
 * @author pstastny
 */
public class StandardCDKHarvestIterationImpl extends  AbstractCDKHarvestIteration {

	public static final Logger LOGGER = Logger.getLogger(StandardCDKHarvestIterationImpl.class.getName());
	public static final String APIURL_PREFIX = "/api/v4.6/cdk/prepare?rows=500&date=";

	private String harvestUrl;
	private String userName;
	private String pswd;
	private String actualDate;

	private List<CDKHarvestIterationItem> processingList;

	public StandardCDKHarvestIterationImpl(String date, String k4Url, String userName, String pswd)
			throws CDKHarvestIterationException {
		this.actualDate = date;
		this.harvestUrl = k4Url + APIURL_PREFIX;
		this.userName = userName;
		this.pswd = pswd;
		this.processingList = new LinkedList<>();

	}

	public void init() throws CDKHarvestIterationException {
		try {
			this.loadNext();
		} catch (XPathExpressionException e) {
			throw new CDKHarvestIterationException(e);
		} catch (SAXException e) {
			throw new CDKHarvestIterationException(e);
		} catch (IOException e) {
			throw new CDKHarvestIterationException(e);
		} catch (ParserConfigurationException e) {
			throw new CDKHarvestIterationException(e);
		}

	}

	public boolean hasNext() throws CDKHarvestIterationException {
		try {
			if (this.processingList.isEmpty()) {
				this.loadNext();
			}
			return !this.processingList.isEmpty();
		} catch (XPathExpressionException e) {
			throw new CDKHarvestIterationException(e);
		} catch (SAXException e) {
			throw new CDKHarvestIterationException(e);
		} catch (IOException e) {
			throw new CDKHarvestIterationException(e);
		} catch (ParserConfigurationException e) {
			throw new CDKHarvestIterationException(e);
		}
	}

	@Override
	public CDKHarvestIterationItem next() throws CDKHarvestIterationException {
		if (!this.processingList.isEmpty()) {
			CDKHarvestIterationItem removed = this.processingList.remove(0);
	        this.actualDate = removed.getTimestamp();
	        return removed;
		}
		return null;
	}

	private void loadNext() throws XPathExpressionException, SAXException, IOException, ParserConfigurationException {
		String urlStr = harvestUrl + URIUtil.encodeQuery(actualDate);
		LOGGER.log(Level.INFO, "urlStr: {0}", urlStr);
		Document solrDom = solrResults(urlStr);

		XPathFactory pathFactory = XPathFactory.newInstance();
		XPath xpath = pathFactory.newXPath();

		XPathExpression numFoundExpr = xpath.compile("/response/result/@numFound");
		int numDocs = Integer.parseInt((String) numFoundExpr.evaluate(solrDom, XPathConstants.STRING));

		XPathExpression docExpr = xpath.compile("/response/result/doc/str[@name='PID']");

		LOGGER.log(Level.INFO, "numDocs: {0}", numDocs);
		if (numDocs > 0) {
			NodeList nodes = (NodeList) docExpr.evaluate(solrDom, XPathConstants.NODESET);
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				String pid = node.getFirstChild().getNodeValue();
				String to = node.getNextSibling().getFirstChild().getNodeValue();
				this.processingList.add(new CDKHarvestIterationItemImpl(pid, to));
			}
		}
	}

	public Document solrResults(String urlStr) throws SAXException, IOException, ParserConfigurationException {
		WebResource r = client(urlStr, this.userName, this.pswd);
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		InputStream is = null;
		try {
			is = r.accept(MediaType.APPLICATION_XML).get(InputStream.class);
			return builder.parse(is);
		} catch (Exception ex) {
			LOGGER.log(Level.WARNING, "Retrying...", ex);
			is = r.accept(MediaType.APPLICATION_XML).get(InputStream.class);
			return builder.parse(is);
		} finally {
			IOUtils.tryClose(is);
		}
	}


}
