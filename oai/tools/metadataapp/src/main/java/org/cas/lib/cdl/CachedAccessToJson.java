package org.cas.lib.cdl;

import static org.cas.lib.cdl.Utils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class CachedAccessToJson implements ContextAware, CachedAccess<JSONObject, String> {
	
	public static final int DEFAULT_MAXIMUM_SIZE = 1000000;
	public static final int EXPIRATION_TIME = 30;
	
	private LoadingCache<String, JSONObject> jsonObjectsChache;
	
	public CachedAccessToJson() {
		this.jsonObjectsChache = 
        CacheBuilder.newBuilder()
           .maximumSize(DEFAULT_MAXIMUM_SIZE) 
           .expireAfterAccess(EXPIRATION_TIME, TimeUnit.DAYS) 
           .build(new CacheLoader<String, JSONObject>(){ 
              @Override
              public JSONObject load(String empId) throws Exception {
                 return loadJSONFromServer(empId);
              } 
       });
	}
	
	public JSONObject get(String pid) throws ExecutionException {
		return this.jsonObjectsChache.get(pid);
	}
	
	
	@Override
	public List<List<String>> paths(String pid) throws ExecutionException {
		List<List<String>> rets = new ArrayList<List<String>>();
		JSONObject item = get(pid);
		JSONArray jArray = item.getJSONArray("context");
		for (int i = 0; i < jArray.length(); i++) {
			rets.add(Utils.onePath(jArray.getJSONArray(i)));
		}
		return rets;
	}

	
	@Override
	public List<String> pathsSelect(String pid) throws ExecutionException {
		JSONObject item = get(pid);
		JSONArray jArray = item.getJSONArray("context");
		return Utils.onePath(jArray.getJSONArray(0));
	}

	@Override
	public List<JSONObject> getForPath(String k, ContextAware contextAware) throws ExecutionException {
		List<JSONObject> retvals = new ArrayList<JSONObject>();
		List<String> path = contextAware.pathsSelect(k);
		for (String pid : path) {
			retvals.add(get(pid));
		}
		return retvals;
	}


	

	private JSONObject loadJSONFromServer(String pid) {
		return item(pid);
	}
}
