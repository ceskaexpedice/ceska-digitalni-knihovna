package org.cas.lib.cdl;

import java.util.List;

import org.w3c.dom.Document;

public interface LanguageDetect {
		
	public String detectLanguage(List<Document> dcs);
}
