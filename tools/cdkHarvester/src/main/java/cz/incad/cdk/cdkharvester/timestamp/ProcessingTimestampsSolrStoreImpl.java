package cz.incad.cdk.cdkharvester.timestamp;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.virtualcollections.impl.support.CDKCollectionsIndexImpl;
import cz.incad.kramerius.virtualcollections.support.CDKCollectionsIndexException;


public class ProcessingTimestampsSolrStoreImpl extends AbstractProcessingTimestamps {
	
	private static final String DEFAULT_TIMESTAMP_KEY = "harvesting_timestamp";

	
	
	@Override
	public LocalDateTime getTimestamp(String pid) throws IOException {
		try {
			CDKCollectionsIndexImpl impl = new CDKCollectionsIndexImpl();
			JSONObject json = impl.getDataByPid(pid);
			if (json != null) {
				if (json.has(DEFAULT_TIMESTAMP_KEY)) {
					String harvestingFile = json.getString(DEFAULT_TIMESTAMP_KEY);
					if (harvestingFile != null) {
						return super.parse(harvestingFile);
					}
				}
			} 
			return nullvalue();
		} catch (CDKCollectionsIndexException e) {
			throw new IOException(e);
		}
	}

    public static String getSolrAddress() {
        return  KConfiguration.getInstance().getConfiguration().getString("cdk.solr.resources", "http://localhost:8983/solr/resources");
    }


	@Override
	public void setTimestamp(String pid, LocalDateTime date) throws IOException {
		try {
			CDKCollectionsIndexImpl impl = new CDKCollectionsIndexImpl();
			impl.updateField(pid, DEFAULT_TIMESTAMP_KEY, super.format(date));
		} catch (CDKCollectionsIndexException e) {
			throw new IOException(e);
		}
	}

	public static void main(String[] args) throws IOException {
		ZonedDateTime departure = ZonedDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));
		ProcessingTimestampsSolrStoreImpl pImpl = new ProcessingTimestampsSolrStoreImpl();
		System.out.println(departure.toLocalDateTime());
		pImpl.setTimestamp("vc:44679769-b5bb-4ac7-ad27-a0c44698c2ea", departure.toLocalDateTime());
		
		//date = LocalDate.parse(in, DateTimeFormatter.BASIC_ISO_DATE);
//		System.out.println(date);
//		ZoneId leavingZone = ZoneId.systemDefault();
//		ZonedDateTime departure = ZonedDateTime.now();
//		try {
//		    //DateTimeFormatter format = DateTimeFormatter.ofPattern("MMM d yyyy  hh:mm a");
//		    String out = departure.format(FORMATTER);
//		    System.out.printf("LEAVING:  %s (%s)%n", out, leavingZone);
//		}
//		catch (DateTimeException exc) {
//		    System.out.printf("%s can't be formatted!%n", departure);
//		    throw exc;
//		}
	}
}
