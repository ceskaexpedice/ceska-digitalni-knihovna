package org.cas.lib.cdl;

import static org.cas.lib.cdl.Utils.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.json.JSONObject;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

import cz.incad.kramerius.utils.XMLUtils;

@WebServlet("/metadata")
public class PeriodicalProvideServlet extends HttpServlet {

	public static final Logger LOGGER = Logger.getLogger(PeriodicalProvideServlet.class.getName());
	
	public static final String DC_STREAM_LOCATION = "http://cdk.lib.cas.cz/search/api/v5.0/item/%s/streams/DC";
	public static final String ITEM_LOCATION = "http://cdk.lib.cas.cz/search/api/v5.0/item/%s";
	
	private CachedAccessToJson jsonCache;
	private CachedAccessToDC dcCache;
	private LanguageDetect langDetect;
	
	public PeriodicalProvideServlet() {
		this.jsonCache = new CachedAccessToJson();
		this.dcCache = new CachedAccessToDC();
		this.langDetect = new LanguageDetectImpl();
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			try {
				String pid = req.getParameter("pid");
				if (pid != null && (!pid.trim().equals(""))) {
					JSONObject itemJSON = this.jsonCache.get(pid);
					// build new title
					if (matchModel(itemJSON, "periodical")) {
						String ntitle = TitleParts.buildTitle(this.jsonCache.getForPath(pid, this.jsonCache));
						
						// title
						Document periodicalItem = this.dcCache.get(pid);
						Element titleElement = XMLUtils.findElement(periodicalItem.getDocumentElement(), "title", "http://purl.org/dc/elements/1.1/");
						titleElement.setTextContent(ntitle);
						
						Element langElement = XMLUtils.findElement(periodicalItem.getDocumentElement(), "language", "http://purl.org/dc/elements/1.1/");
						if (langElement == null) {
							langElement = periodicalItem.createElementNS("http://purl.org/dc/elements/1.1/", "language");
							periodicalItem.getDocumentElement().appendChild(langElement);
						}
						langElement.setTextContent(this.langDetect.detectLanguage(this.dcCache.getForPath(pid, this.jsonCache)));

						
						
						resp.setContentType("text/xml");
						XMLUtils.print(periodicalItem, resp.getWriter());
					} else {
						// forward dc
						resp.setContentType("text/xml");
						Utils.dcFowrard(pid, resp.getWriter());
					}
				}
			} catch (DOMException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			} catch (ExecutionException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			} catch (TransformerException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
	}

		
	


	public static void main(String[] args) throws MalformedURLException, ParserConfigurationException, SAXException, IOException, TransformerException {
		//dc("uuid:6a17aac3-b9e9-11e1-1746-001143e3f55c");
//		List<JSONObject> items = Utils.items("uuid:6a17aac3-b9e9-11e1-1746-001143e3f55c");
//		Document dc = Utils.dc("uuid:6a17aac3-b9e9-11e1-1746-001143e3f55c");
//		List<String> titles = new ArrayList<>();
//		for (JSONObject jsonObject : items) {
//			String model = jsonObject.getString("model");
//			TitleParts part = TitleParts.valueOf(model);
//			titles.add(part.part(jsonObject));
//		}
//		
//		Element findElement = XMLUtils.findElement(dc.getDocumentElement(), "dc:title");
//		String title = titles.toString();
//		System.out.println(title);
//		
//		findElement.appendChild(dc.createTextNode(title));
//		
//		XMLUtils.print(dc, System.out);
//		System.out.println(findElement);
	}
}
