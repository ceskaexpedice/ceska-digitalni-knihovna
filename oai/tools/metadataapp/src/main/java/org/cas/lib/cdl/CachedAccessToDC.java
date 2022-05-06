package org.cas.lib.cdl;

import static org.cas.lib.cdl.Utils.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class CachedAccessToDC implements CachedAccess<Document, String>{

	public static final int DEFAULT_MAXIMUM_SIZE = 500;
	public static final int EXPIRATION_TIME = 3;

	private LoadingCache<String, Document> dcObjectsChache;
	
	public CachedAccessToDC() {
		this.dcObjectsChache = 
        CacheBuilder.newBuilder()
           .maximumSize(DEFAULT_MAXIMUM_SIZE)
           .expireAfterAccess(EXPIRATION_TIME, TimeUnit.MINUTES) 
           .build(new CacheLoader<String, Document>(){ 
              @Override
              public Document load(String empId) throws Exception {
                 return loadDCFromServer(empId);
              } 
           });
	}

	@Override
	public Document get(String k) throws ExecutionException {
		return dcObjectsChache.get(k);
	}
	
	@Override
	public List<Document> getForPath(String k, ContextAware contextAware) {
            List<Document> retvals = new ArrayList<Document>();
            try {
                List<String> path = contextAware.pathsSelect(k);
                for (String pid : path) {
                    retvals.add(get(pid));
		}
                // ignore and return retvals
            } catch (ExecutionException ex) {
                return retvals;
            }
            return retvals;
	}

	private Document loadDCFromServer(String pid) throws MalformedURLException, ParserConfigurationException, SAXException, IOException {
		return dc(pid);
	}
}
