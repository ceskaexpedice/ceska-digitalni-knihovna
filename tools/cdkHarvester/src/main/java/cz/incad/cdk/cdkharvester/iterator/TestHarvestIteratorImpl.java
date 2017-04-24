package cz.incad.cdk.cdkharvester.iterator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;

import com.hp.hpl.jena.iri.impl.Main;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.virtualcollections.CDKSourcesAware;

public class TestHarvestIteratorImpl extends AbstractCDKHarvestIteration {

	public static final Logger LOGGER = Logger.getLogger(TestHarvestIteratorImpl.class.getName());
	
	//https://cdk.lib.cas.cz/search/api/v5.0/search?q=*:*&fl=PID,modified_date&sort=modified_date%20asc&rows=1
		
	private String baseUrl;
	private String topPid;

	private List<CDKHarvestIterationItem> processingList;
	
	public TestHarvestIteratorImpl(String k4Url)
			throws CDKHarvestIterationException {
		this.baseUrl = k4Url;
		this.processingList = new LinkedList<>();
	}

	@Override
	public void init() throws CDKHarvestIterationException {
		try {
			String date = dateResult(dateURL(this.baseUrl));
			System.out.println(date);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		
	}

	@Override
	public boolean hasNext() throws CDKHarvestIterationException {
		return false;
	}

	@Override
	public CDKHarvestIterationItem next() throws CDKHarvestIterationException {
		return null;
	}
	
	private String dateURL(String k4Url) {
		return k4Url + "?q=*:*&fl=modified_date&sort=modified_date%20asc&rows=1";
	}

	public String dateResult(String urlStr) throws IOException {
		WebResource r = client(urlStr);
		try (InputStream is = r.accept(MediaType.APPLICATION_JSON).get(InputStream.class)){
			String string = IOUtils.readAsString(is, Charset.forName("UTF-8"), true);
			//{"response":{"docs":[{"modified_date":"2010-09-26T08:49:59.353Z"}],"numFound":33222340,"start":0},"responseHeader":{"QTime":1,"params":{"q":"*:*","fl":"modified_date","sort":"modified_date asc","rows":"1","wt":"json"},"status":0}}
			
			JSONObject jsonObject = new JSONObject(string);
			JSONArray jsonArray = jsonObject.getJSONObject("response").getJSONArray("docs");
			if (jsonArray.length() > 0) {
				JSONObject arrayObject = jsonArray.getJSONObject(0);
				String modifiedDate = arrayObject.getString("modified_date");
				return modifiedDate;
			} else return null;
			
		} catch (Exception ex) {
			LOGGER.log(Level.WARNING, "Retrying...", ex);
			return  null;
//			try (InputStream is = r.accept(MediaType.APPLICATION_JSON).get(InputStream.class)){
//				return chilrenJSONArray(is);
//			}
		}
	}
	
	public static void main(String[] args) throws CDKHarvestIterationException {
		TestHarvestIteratorImpl imp = new TestHarvestIteratorImpl("https://cdk.lib.cas.cz/search/api/v5.0/search");
		imp.init();
	}

}
