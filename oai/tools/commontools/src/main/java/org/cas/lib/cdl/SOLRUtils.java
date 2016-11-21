package org.cas.lib.cdl;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * SOLR utilities 
 */
public class SOLRUtils {
    
    /**
     * Number of documents
     * @param obj
     * @return
     */
    public static int numFound(JSONObject obj) {
        return obj.getInt("numFound");
    }
    
    /**
     * Returns list of pids disected from given JSON response
     * @param response
     * @return
     */
    public static  List<String> pidsDocument(JSONObject response) {
        List<String> pids = new ArrayList<String>();
        JSONArray jsonArray = response.getJSONArray("docs");
        for (int i = 0,ll=jsonArray.length(); i < ll; i++) {
            JSONObject doc = jsonArray.getJSONObject(i);
            pids.add(doc.getString("PID"));
        }
        return pids;
    }
}
