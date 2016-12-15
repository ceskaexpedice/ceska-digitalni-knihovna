package org.cas.lib.cdl;

import static org.cas.lib.cdl.Utils.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class CachedAccessToMods implements CachedAccess<Document, String> {

	public static final int DEFAULT_MAXIMUM_SIZE = 1000000;
	public static final int EXPIRATION_TIME = 30;

	private LoadingCache<String, Document> modsObjectsChache;

	public CachedAccessToMods() {
		this.modsObjectsChache = CacheBuilder.newBuilder().maximumSize(DEFAULT_MAXIMUM_SIZE)
				.expireAfterAccess(EXPIRATION_TIME, TimeUnit.DAYS).build(new CacheLoader<String, Document>() {
					@Override
					public Document load(String empId) throws Exception {
						return loadMODSFromServer(empId);
					}
				});
	}

	@Override
	public Document get(String k) throws ExecutionException {
		return modsObjectsChache.get(k);
	}

	@Override
	public List<Document> getForPath(String k, ContextAware contextAware) throws ExecutionException {
		List<Document> retvals = new ArrayList<Document>();
		List<String> path = contextAware.pathsSelect(k);
		for (String pid : path) {
			retvals.add(get(pid));
		}
		return retvals;
	}

	private Document loadMODSFromServer(String pid)
			throws MalformedURLException, ParserConfigurationException, SAXException, IOException {
		return mods(pid);
	}

}
