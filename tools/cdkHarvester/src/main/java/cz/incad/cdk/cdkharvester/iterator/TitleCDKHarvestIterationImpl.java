package cz.incad.cdk.cdkharvester.iterator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.utils.IOUtils;

/**
 * Iterator for one title
 * @author pstastny
 */
public class TitleCDKHarvestIterationImpl extends AbstractCDKHarvestIteration {

	public static final Logger LOGGER = Logger.getLogger(StandardCDKHarvestIterationImpl.class.getName());
	private String baseUrl;
	private String topPid;
	private List<CDKHarvestIterationItem> processingList;
	
	public TitleCDKHarvestIterationImpl(String k4Url, String pid)
			throws CDKHarvestIterationException {
		this.baseUrl = k4Url;
		this.topPid = pid;
		this.processingList = new LinkedList<>();
		this.processingList.add(new CDKHarvestIterationItemImpl(pid, null));
	}



	private String childrenURL(String k4Url, String pid) {
		return k4Url + "/api/v5.0/item/"+pid+"/children";
	}

	
	@Override
	public void init() throws CDKHarvestIterationException {
		// no init is needed
		try {
			Stack<String> stack = new Stack<>();
			stack.push(this.topPid);
			while(!stack.isEmpty()) {
				String p = stack.pop();
				JSONArray childrenResults = childrenResults(childrenURL(this.baseUrl,p));
				for (int i = 0,ll=childrenResults.length(); i < ll; i++) {
					JSONObject jsonObject = childrenResults.getJSONObject(i);
					String childP = jsonObject.getString("pid");
					stack.push(childP);
					this.processingList.add(new CDKHarvestIterationItemImpl(childP, null));
				}	
			}
		} catch (IOException e) {
			throw new CDKHarvestIterationException(e);
		}
	}

	@Override
	public boolean hasNext() throws CDKHarvestIterationException {
		return !this.processingList.isEmpty();
	}

	@Override
	public CDKHarvestIterationItem next() throws CDKHarvestIterationException {
		while(!this.processingList.isEmpty()) {
			CDKHarvestIterationItem removed = this.processingList.remove(0);
			return removed;
		}
		return null;
	}
	
	public JSONArray childrenResults(String urlStr) throws IOException {
		WebResource r = client(urlStr);
		try (InputStream is = r.accept(MediaType.APPLICATION_JSON).get(InputStream.class)){
			return chilrenJSONArray(is);
		} catch (Exception ex) {
			LOGGER.log(Level.WARNING, "Retrying...", ex);
			try (InputStream is = r.accept(MediaType.APPLICATION_JSON).get(InputStream.class)){
				return chilrenJSONArray(is);
			}
		}
	}

	private JSONArray chilrenJSONArray(InputStream is) throws IOException {
		String str = IOUtils.readAsString(is, Charset.forName("UTF-8"), true);
		JSONArray jArray = new JSONArray(str);
		return jArray;
	}
	
}
