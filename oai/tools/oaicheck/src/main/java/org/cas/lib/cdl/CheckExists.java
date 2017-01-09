package org.cas.lib.cdl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONObject;

/**
 * Requesting target solr; 
 * Check if the title exists in in target SOLR
 * @author pstastny
 */
public class CheckExists implements IterationControl {

	public static final Logger LOGGER = Logger.getLogger(ChecksEnum.exists.name());
	
	
    private int counter = 0;
    private int missing = 0;
    
    @Override
    public void onPidsIterate(List<String> pids, String sourceKramerius) throws UnsupportedEncodingException {
        this.counter += pids.size();
        List<String> sourcePids = new ArrayList<String>(pids);
        String result = sourceKramerius != null ? PublicConnectUtils.searchInSourceKramerius(sourceKramerius, URLEncoder.encode(existsQuery(pids), "UTF-8")+"&fl=PID"): PublicConnectUtils.searchInSourceKramerius(URLEncoder.encode(existsQuery(pids), "UTF-8")+"&fl=PID");
        JSONObject resultJSON = new JSONObject(result);
        JSONObject response = resultJSON.getJSONObject("response");
        List<String> pidsFromKNAV = SOLRUtils.pidsDocument(response);
        while(!sourcePids.isEmpty()) {
            String pid = sourcePids.remove(0);
            if (pidsFromKNAV.contains(pid)) {
                pidsFromKNAV.remove(pid);
            } else {
                this.missing += 1;
                LOGGER.info("non existent pid "+pid);
            }
        }
    }
    
    
    public int getCounter() {
        return counter;
    }
    
    public int getMissing() {
        return missing;
    }
    
    private String existsQuery(List<String> pids) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0,ll=pids.size(); i < ll; i++) {
            if(i > 0) {
                buffer.append(" OR ");
            }
            buffer.append("PID:").append('"').append(pids.get(i)).append('"');
        }
        return buffer.toString();
    }


    @Override
    public void printResult() {
        LOGGER.info("Pocet dokumentu: "+this.getCounter());
        LOGGER.info("Pocet chybejicich: "+this.getMissing());
    }
}
