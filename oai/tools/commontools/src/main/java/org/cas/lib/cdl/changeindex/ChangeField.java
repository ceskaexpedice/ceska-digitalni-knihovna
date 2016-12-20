package org.cas.lib.cdl.changeindex;

import org.cas.lib.cdl.PrivateConnectUtils;
import org.json.JSONObject;

public class ChangeField {

    public static final String SOLR_UDATE_ENDPOINT = "http://localhost:8983/solr/kramerius/update?commit=true";

    private String pid;
    
    private String name;
    private Object value;
    
    
    public ChangeField(String pid, String name, Object value) {
        super();
        this.pid = pid;
        this.name = name;
        this.value = value;
    }


    public JSONObject toJSONObject() {
        JSONObject obj = new JSONObject();
        obj.put("PID", this.pid);
        
        JSONObject setval = new JSONObject();
        setval.put("set", this.value);
        
        obj.put(this.name, setval);
        
        JSONObject doc = new JSONObject();
        doc.put("doc", obj);
        return doc;
    }

    public JSONObject changeField(String addr) {
        return PrivateConnectUtils.indexDocument(addr, this.pid, this.toJSONObject());
    }

    public JSONObject changeField() {
        return PrivateConnectUtils.indexDocument(SOLR_UDATE_ENDPOINT, this.pid, this.toJSONObject());
    }

    public static void main(String[] args) {
        if (args.length >1) {
            
        }
        
        ChangeField chField = new ChangeField("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6", "collection", "pridat");
        System.out.println(chField.toJSONObject());
        //chField.changeField();
    }
}
