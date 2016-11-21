package org.cas.lib.cdl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

/**
 * Check if the title exists in in target SOLR
 * @author pstastny
 */
public class CheckExists implements IterationControl {

    private int counter = 0;
    private int missing = 0;
    
    @Override
    public void onPidsIterate(List<String> pids) throws UnsupportedEncodingException {
        this.counter += pids.size();
        List<String> sourcePids = new ArrayList<String>(pids);
        String result = PublicConnectUtils.searchInSourceKramerius(URLEncoder.encode(existsQuery(pids), "UTF-8")+"&fl=PID");
        JSONObject resultJSON = new JSONObject(result);
        JSONObject response = resultJSON.getJSONObject("response");
        List<String> pidsFromKNAV = SOLRUtils.pidsDocument(response);
        while(!sourcePids.isEmpty()) {
            String pid = sourcePids.remove(0);
            if (pidsFromKNAV.contains(pid)) {
                pidsFromKNAV.remove(pid);
            } else {
                this.missing += 1;
                System.out.println("non existent pid "+pid);
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
        System.out.println("Pocet dokumentu: "+this.getCounter());
        System.out.println("Pocet chybejicich: "+this.getMissing());
    }
}
