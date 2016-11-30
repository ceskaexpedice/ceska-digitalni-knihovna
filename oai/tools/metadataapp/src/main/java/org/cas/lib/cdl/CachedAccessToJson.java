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
	
	private LoadingCache<String, JSONObject> jsonObjectsChache;
	
	public CachedAccessToJson() {
		this.jsonObjectsChache = 
        CacheBuilder.newBuilder()
           .maximumSize(1000) 
           .expireAfterAccess(30, TimeUnit.MINUTES) // cache will expire after 30 minutes of access
           .build(new CacheLoader<String, JSONObject>(){ // build the cacheloader
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
		List<List<String>> rets = new ArrayList<>();
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
		List<JSONObject> retvals = new ArrayList<>();
		List<String> path = contextAware.pathsSelect(k);
		for (String pid : path) {
			retvals.add(get(pid));
		}
		return retvals;
	}


	
//	public static List<JSONObject> items(String pid) {
//		Map<String, JSONObject> maps = new HashMap<>();
//		List<JSONObject> objs = new ArrayList<>();
//	    JSONObject periodicalItem = Utils.item(pid);
//	    maps.put(pid, periodicalItem);
//	
//		JSONArray jArray = periodicalItem.getJSONArray("context").getJSONArray(0);
//	    for (int i = jArray.length()-1; i>=0; i--) {
//	    	String arrayPid = jArray.getJSONObject(i).getString("pid");
//	    	if (!maps.containsKey(arrayPid)) {
//	    		maps.put(arrayPid, Utils.item(arrayPid));
//	    	}
//	    	objs.add(0,maps.get(arrayPid));
//		}
//	    
//	    return objs;
//	}

	private JSONObject loadJSONFromServer(String pid) {
		return item(pid);
	}
}
