package org.cas.lib.cdl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

public class CheckingTool {
    
    public static final int DEFAULT_LIMIT = Integer.MAX_VALUE;
    
    private int limit = DEFAULT_LIMIT;
    
    public CheckingTool(int limit) {
        super();
        this.limit = limit;
    }
    
    public CheckingTool() {}

    private JSONObject searchObjectsInCDK(int start, int page) throws UnsupportedEncodingException {
        String leftPart = "(fedora.model:periodical OR fedora.model:periodicalvolume OR fedora.model:periodicalitem OR fedora.model:article OR fedora.model:monograph OR fedora.model:supplement)";
        String rightPart = "(collection:(\"vc:c4bb27af-3a51-4ac2-95c7-fd393b489e26\") AND dostupnost:public)";
        String search = PublicConnectUtils.searchInCDK(URLEncoder.encode(leftPart+ " AND "+rightPart,"UTF-8")+"&rows="+page+"&start="+start+"&fl=PID");
        JSONObject objects = new JSONObject(search);
        return objects;
    }

    private static void facetModels() throws UnsupportedEncodingException {        
        String rightPart = "collection:(\"vc:c4bb27af-3a51-4ac2-95c7-fd393b489e26\") AND dostupnost:public";
        String search = PublicConnectUtils.searchInCDK(URLEncoder.encode(rightPart,"UTF-8")+"&facet.field=fedora.model&facet=on&rows=0");
        JSONObject objects = new JSONObject(search);
        System.out.println(objects);
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        long start = System.currentTimeMillis();
        CheckingTool forward = new CheckingTool();
        List<IterationControl> icontrols = new ArrayList<IterationControl>();
        icontrols.add(new CheckExists());
        icontrols.add(new CheckAccessible());
        forward.cdkIteration(icontrols);
        long stop = System.currentTimeMillis();

        for (IterationControl c : icontrols) {
            c.printResult();
        }
        System.out.println("Cas straveny iteraci :"+(stop - start));
    }
    
    
    public  void cdkIteration(List<IterationControl> controls) throws UnsupportedEncodingException {
        int start = 0;
        int page = 10;
        int numFound = 1;
        while(start < Math.min(numFound, limit)) {
            JSONObject searchModels = searchObjectsInCDK(start,page);
            JSONObject response = searchModels.getJSONObject("response");
            List<String> pids = SOLRUtils.pidsDocument(response);
            for (IterationControl c : controls) {
                c.onPidsIterate(pids);
            }
            numFound = SOLRUtils.numFound(response);
            start += page;
        }
        
    }

}
