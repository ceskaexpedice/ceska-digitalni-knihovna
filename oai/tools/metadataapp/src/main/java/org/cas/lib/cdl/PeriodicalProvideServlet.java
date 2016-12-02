package org.cas.lib.cdl;

import static org.cas.lib.cdl.Utils.*;

import java.io.ByteArrayOutputStream;
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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.cas.lib.cdl.PeriodicalProvideServlet.Actions;
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

public class PeriodicalProvideServlet extends HttpServlet {

	public static final Logger LOGGER = Logger.getLogger(PeriodicalProvideServlet.class.getName());

	public static final String DC_STREAM_LOCATION = "http://cdk.lib.cas.cz/search/api/v5.0/item/%s/streams/DC";
	public static final String ITEM_LOCATION = "http://cdk.lib.cas.cz/search/api/v5.0/item/%s";
	public static final String CDK_LOCATION = "http://cdk.lib.cas.cz/search/%s";

	
	private static String getPid(HttpServletRequest req) {
		return req.getParameter("pid");
	}

	private CachedAccessToJson jsonCache;
	private CachedAccessToDC dcCache;

	public PeriodicalProvideServlet() {
		this.jsonCache = new CachedAccessToJson();
		this.dcCache = new CachedAccessToDC();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String action = req.getParameter("action");
		Actions act = Actions.valueOf(action);
		act.perform(this.jsonCache, this.dcCache, req, resp);
	}

	public enum Actions {
		europeana {

			@Override
			public void perform(CachedAccessToJson jsonCache, CachedAccessToDC dcCache, HttpServletRequest req,HttpServletResponse resp) throws IOException {
				try {
					String pid = getPid(req);
					if (pid != null && (!pid.trim().equals(""))) {
						DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
						factory.setNamespaceAware(true);
						DocumentBuilder builder = factory.newDocumentBuilder();
						Document europeana = builder.newDocument();
						
						Element root = europeana.createElementNS("http://www.europeana.eu/schemas/ese/","europeana:record");
						europeana.appendChild(root);

						JSONObject itemJSON = jsonCache.get(pid);
						if (itemJSON.has("pdf")) {
							JSONObject pdfObject = itemJSON.getJSONObject("pdf");
							if (pdfObject.has("url")) {
								String url = pdfObject.getString("url");
								Element elementShownBy = europeana.createElementNS("http://www.europeana.eu/schemas/ese/","europeana:isShownBy");
								elementShownBy.setTextContent(url);
								root.appendChild(elementShownBy);
							}
						}

						String formattedURL = String.format(PeriodicalProvideServlet.CDK_LOCATION, "handle/"+pid);

						Element elementShownAt = europeana.createElementNS("http://www.europeana.eu/schemas/ese/","europeana:isShownAt");
						elementShownAt.setTextContent(formattedURL);
						root.appendChild(elementShownAt);
						
						resp.setContentType("text/xml; charset=utf-8");
						XMLUtils.print(europeana, resp.getWriter());
					}
	
				} catch (ExecutionException e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
					resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				} catch (ParserConfigurationException e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
					resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				} catch (TransformerException e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
					resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}				
			}
		},
		
		dc {
			
			@Override
			public void perform(CachedAccessToJson jsonCache, CachedAccessToDC dcCache, HttpServletRequest req,HttpServletResponse resp) throws IOException {
				try {
					String pid = getPid(req);
					LanguageDetect langDetect = new LanguageDetectImpl();

					if (pid != null && (!pid.trim().equals(""))) {
						Document dc = dcCache.get(pid);
						JSONObject itemJSON = jsonCache.get(pid);
						// build new title
						if (matchModel(itemJSON, "periodical")) {
							String ntitle = TitleParts.buildTitle(jsonCache.getForPath(pid, jsonCache));

							// title
							Element titleElement = XMLUtils.findElement(dc.getDocumentElement(), "title",
									"http://purl.org/dc/elements/1.1/");
							if (titleElement == null) {
								titleElement = dc.createElementNS("http://purl.org/dc/elements/1.1/", "dc:title");
								dc.getDocumentElement().appendChild(titleElement);
							}					
							titleElement.setTextContent(ntitle);

							Element langElement = XMLUtils.findElement(dc.getDocumentElement(), "language",
									"http://purl.org/dc/elements/1.1/");
							if (langElement == null) {
								langElement = dc.createElementNS("http://purl.org/dc/elements/1.1/", "dc:language");
								dc.getDocumentElement().appendChild(langElement);
							}
							langElement.setTextContent(
									langDetect.detectLanguage(dcCache.getForPath(pid, jsonCache)));

							resp.setContentType("text/xml; charset=utf-8");
							XMLUtils.print(dc, resp.getWriter());
						} else {
							// forward dc
							resp.setContentType("text/xml; charset=utf-8");
							XMLUtils.print(dc, resp.getWriter());
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
		};

		public abstract void perform(CachedAccessToJson jsonCache,CachedAccessToDC dcCache,  HttpServletRequest req,HttpServletResponse resp) throws IOException ;
	}
}
