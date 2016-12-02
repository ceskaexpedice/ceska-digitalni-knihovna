package org.cas.lib.cdl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

public class LanguageDetectImpl implements LanguageDetect{
	private static final String DEFAULT_LANG = "cze";
	private static final Map<String, String> MAP = new HashMap<String, String>();
	static {
		MAP.put("gerlischenbuchhandlung","ger");
	}
	
	
	@Override
	public String detectLanguage(List<Document> dcs) {
		for (Document document : dcs) {
			String lang = Utils.findLanguageFromDC(document);
			if (lang != null && (!"".equals(lang.trim()))) return lang;
		}
		
		for (Document document : dcs) {
			String publisher = Utils.findPublisherFromDC(document);
			if (publisher != null) {
				String chpublisher = normalizepublishername(publisher);
				if (MAP.containsKey(chpublisher)) {
					return MAP.get(chpublisher);
				}
			}
		}
		return DEFAULT_LANG;
	}

	
	static String normalizepublishername(String publisher) {
		StringBuilder builder = new StringBuilder();
		publisher = publisher.toLowerCase();
		for (int i = 0,ll=publisher.length(); i < ll; i++) {
			char ch = publisher.charAt(i);
			if (!Character.isWhitespace(ch)) {
				builder.append(ch);
			}
		}
		return builder.toString();
	}
}
