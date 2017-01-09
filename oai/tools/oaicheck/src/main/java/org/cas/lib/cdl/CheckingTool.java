package org.cas.lib.cdl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jws.Oneway;

import org.json.JSONObject;

/**
 * Main tool class
 * @author pstastny
 */
public class CheckingTool {
	
	// default is knav in this iteration
	private static final String DEFAULT_COLLECTION = "vc:c4bb27af-3a51-4ac2-95c7-fd393b489e26";
	
	
	
	public static final Logger LOGGER = Logger.getLogger(CheckingTool.class.getName());
	
    public static final int DEFAULT_LIMIT = Integer.MAX_VALUE;
    public static final int DEFAUTL_PAGE = 1000;
    private int limit = DEFAULT_LIMIT;
    
    public CheckingTool(int limit) {
        super();
        this.limit = limit;
    }
    
    public CheckingTool() {}

    private JSONObject searchObjectsInCDK(int start, int page) throws UnsupportedEncodingException {
        String leftPart = "(fedora.model:monograph OR fedora.model:periodicalitem OR fedora.model:manuscript OR fedora.model:graphic OR fedora.model:map OR fedora.model:sheetmusic OR fedora.model:article OR fedora.model:supplement)";
        String rightPart = "(collection:(\""+DEFAULT_COLLECTION+"\") AND dostupnost:public)";
        String query = URLEncoder.encode(leftPart+ " AND "+rightPart,"UTF-8")+"&rows="+page+"&start="+start+"&fl=PID";
        LOGGER.info("query is :"+query);
        String search = PublicConnectUtils.searchInCDK(query);
        JSONObject objects = new JSONObject(search);
        return objects;
    }

    private static void facetModels() throws UnsupportedEncodingException {        
        String rightPart = "collection:(\"vc:c4bb27af-3a51-4ac2-95c7-fd393b489e26\") AND dostupnost:public";
        String search = PublicConnectUtils.searchInCDK(URLEncoder.encode(rightPart,"UTF-8")+"&facet.field=fedora.model&facet=on&rows=0");
        JSONObject objects = new JSONObject(search);
        System.out.println(objects);
    }

    public static final void help() {
        StringBuilder builder = new StringBuilder();
        builder.append("Main")
            .append(" [source collection pid]")
            .append(" [soruce kramerius url]")
            .append(" (<checkid>* )");
        System.out.println(builder);

    }
    
    public static void main(String[] args) throws UnsupportedEncodingException {
    	String collection = DEFAULT_COLLECTION;
    	String sourceKramerius = PublicConnectUtils.DEFAULT_SOURCE_KRAMERIUS;
    	List<IterationControl> icontrols = new ArrayList<IterationControl>();
    	for (String arg : args) {
			if (arg.startsWith("vc:")) {
				collection = arg;
			} else if (arg.startsWith("http")) {
				sourceKramerius = arg;
			} else {
				List<IterationControl> argument = ChecksEnum.argument(arg);
				icontrols.addAll(argument);
			}
		}
    	
    	long start = System.currentTimeMillis();
        CheckingTool forward = new CheckingTool();
        for (IterationControl ic : icontrols) {
			LOGGER.info("\t docking iteraction control:"+ic.getClass().getName());
		}
        forward.cdkIteration(icontrols,collection, sourceKramerius);
        long stop = System.currentTimeMillis();
        for (IterationControl c : icontrols) {
            c.printResult();
        }
        LOGGER.info("Cas straveny iteraci :"+(stop - start));
    }
    
    
    public  void cdkIteration(List<IterationControl> controls, String sourceCollection, String sourceKramerius) throws UnsupportedEncodingException {
        int start = 0;
        int page = DEFAUTL_PAGE;
        int numFound = 1;
        while(start < Math.min(numFound, limit)) {
            JSONObject searchModels = searchObjectsInCDK(start,page);
            JSONObject response = searchModels.getJSONObject("response");
            List<String> pids = SOLRUtils.pidsDocument(response);
            for (IterationControl c : controls) {
                c.onPidsIterate(pids, sourceKramerius);
            }
            numFound = SOLRUtils.numFound(response);
            LOGGER.info("starting from "+start);
            start += page;
        }
    }
}
