package cz.incad.kramerius.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.cas.lib.cdl.CachedAccessToDC;
import org.cas.lib.cdl.CachedAccessToJson;
import org.cas.lib.cdl.CachedAccessToMods;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;

import cz.incad.kramerius.AbstractObjectPath;
import cz.incad.kramerius.ObjectModelsPath;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;

public class OAISolrAccess implements SolrAccess {

	public static final Logger LOGGER = Logger.getLogger(OAISolrAccess.class.getName());
	
	private CachedAccessToJson cachedToJSON;

	public OAISolrAccess(CachedAccessToJson cachedToJSON) {
		super();
		this.cachedToJSON = cachedToJSON;
	}

	@Override
	public ObjectPidsPath[] getPath(String arg0, Document arg1) throws IOException {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public ObjectPidsPath[] getPath(String pid) throws IOException {
		try {
			List<ObjectPidsPath> paths = new ArrayList<ObjectPidsPath>();
			JSONObject jsonObject = cachedToJSON.get(pid);
			JSONArray jsonArray = jsonObject.getJSONArray("context");
			for (int i = 0,ll=jsonArray.length(); i < ll; i++) {
				JSONArray pathArray = jsonArray.getJSONArray(i);
				List<String> pids = new ArrayList<String>();
				for (int j = 0,lj=pathArray.length(); j < lj; j++) {
					JSONObject oneItem = pathArray.getJSONObject(j);
					pids.add(pid(oneItem));
				}
				paths.add(new ObjectPidsPath(pids));
			}
			return paths.toArray(new ObjectPidsPath[paths.size()]);
		} catch (ExecutionException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(),e);
			throw new IOException(e);
		}
	}

	private String pid(JSONObject oneItem) {
		return oneItem.getString("pid");
	}

	private String model(JSONObject oneItem) {
		return oneItem.getString("model");
	}

	@Override
	public ObjectModelsPath[] getPathOfModels(String pid) throws IOException {
		try {
			List<ObjectModelsPath> paths = new ArrayList<ObjectModelsPath>();
			JSONObject jsonObject = cachedToJSON.get(pid);
			JSONArray jsonArray = jsonObject.getJSONArray("context");
			for (int i = 0,ll=jsonArray.length(); i < ll; i++) {
				JSONArray pathArray = jsonArray.getJSONArray(i);
				List<String> pids = new ArrayList<String>();
				for (int j = 0,lj=pathArray.length(); j < lj; j++) {
					JSONObject oneItem = pathArray.getJSONObject(j);
					pids.add(model(oneItem));
				}
				paths.add(new ObjectModelsPath(pids.toArray(new String[pids.size()])));
			}
			return paths.toArray(new ObjectModelsPath[paths.size()]);
		} catch (ExecutionException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(),e);
			throw new IOException(e);
		}
	}

	@Override
	public Map<String, AbstractObjectPath[]> getPaths(String arg0) throws IOException {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public Document getSolrDataDocmentsByParentPid(String arg0, String arg1) throws IOException {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public Document getSolrDataDocument(String arg0) throws IOException {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public Document getSolrDataDocumentByHandle(String arg0) throws IOException {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public InputStream request(String arg0, String arg1) throws IOException {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public Document request(String arg0) throws IOException {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public InputStream terms(String arg0, String arg1) throws IOException {
		throw new UnsupportedOperationException("this is unsupported");
	}

}
