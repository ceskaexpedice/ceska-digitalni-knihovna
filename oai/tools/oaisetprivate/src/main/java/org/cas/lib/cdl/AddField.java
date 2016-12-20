package org.cas.lib.cdl;

import org.json.JSONObject;

public class AddField {

	public static final String SOLR_UDATE_ENDPOINT = "http://localhost:8983/solr/kramerius/update?commit=true";

    private String pid;
    private String name;
    private Object value;
    
    
    public AddField(String pid, String name, Object value) {
        super();
        this.pid = pid;
        this.name = name;
        this.value = value;
    }
    
    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        obj.put("PID", this.pid);
        
        JSONObject setval = new JSONObject();
        setval.put("add", this.value);
        
        obj.put(this.name, setval);
        
        JSONObject doc = new JSONObject();
        doc.put("doc", obj);
        return doc;
    }

    public JSONObject addValueToArray(String addr) {
        return PrivateConnectUtils.indexDocument(addr, this.pid, this.toJSONObject());
    }

    public JSONObject addValueToArray() {
        return PrivateConnectUtils.indexDocument(SOLR_UDATE_ENDPOINT, this.pid, this.toJSONObject());
    }


    public static void main(String[] args) {
    	
    	AddField chField = new AddField("uuid:9a37d4db-9f59-4bf4-88a1-4692d3f86aaf", "collection", "dalsi");
        System.out.println(chField.toJSONObject());
        chField.addValueToArray();
    }

}
