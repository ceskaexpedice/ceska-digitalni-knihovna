package org.cas.lib.cdl;

import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.utils.XMLUtils;

public class Utils {

	static Map<String, List<String>> collectValues(List<Document> dcs) throws ParserConfigurationException {
		Map<String, List<String>> map = new HashMap<>();
		for (Document pathsDoc : dcs) {
			NodeList childNodes = pathsDoc.getDocumentElement().getChildNodes();
			for (int i = 0,ll=childNodes.getLength(); i < ll; i++) {
				Node n = childNodes.item(i);
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					Element subElm = (Element) n;
					if (subElm.getNamespaceURI().equals("http://purl.org/dc/elements/1.1/")) {
						String localName = subElm.getLocalName();
						if (!map.containsKey(localName)) {
							map.put(localName, new ArrayList<>());
						}
						List<String> list = map.get(localName);
						String textContent = subElm.getTextContent();
						if (textContent != null) {
							textContent = textContent.trim();
							if (!list.contains(textContent)) {
								list.add(textContent);
							}
						}
					}
				}
			}
		}
		
		return map;
	}

	
	static void dcFowrard(String pid, Writer writer) throws IOException {
		String formatted = String.format(PeriodicalProvideServlet.DC_STREAM_LOCATION, pid);
		Client c = Client.create();
	    WebResource r = c.resource(formatted);
	    String response = r.accept(MediaType.APPLICATION_XML).get(String.class);
	    writer.write(response);
	}
	
	static Document dc(String pid) throws ParserConfigurationException, SAXException, IOException {
		String formatted = String.format(PeriodicalProvideServlet.DC_STREAM_LOCATION, pid);
		Document parseDocument = XMLUtils.parseDocument(new URL(formatted).openStream(), true);
		return parseDocument;
	}

	public static JSONObject item(String pid) {
		String formatted = String.format(PeriodicalProvideServlet.ITEM_LOCATION, pid);
		Client c = Client.create();
	    WebResource r = c.resource(formatted);
	    String response = r.accept(MediaType.APPLICATION_JSON).get(String.class);
		return  new JSONObject(response);
	}
	
	public static boolean matchModel(JSONObject itemJSON, String expectingModel) {
		JSONArray context =  selectContext(itemJSON);
		for (int i = 0,ll=context.length(); i < ll; i++) {
			JSONArray one = context.getJSONArray(i);
			for (int j = 0,lz=one.length(); j < lz; j++) {
				String model = one.getJSONObject(j).getString("model");
				if (model.equals(expectingModel)) return true;
			}
		}
		return false;
	}

	
	public static JSONArray selectContext(JSONObject obj) {
		return obj.getJSONArray("context");
	}
	public static List<String> onePath(JSONArray jsonArray) {
		List<String> rets = new ArrayList<>();
		for (int i = 0; i < jsonArray.length(); i++) {
			rets.add(jsonArray.getJSONObject(i).getString("pid"));
		}
		return rets;
	}

}
