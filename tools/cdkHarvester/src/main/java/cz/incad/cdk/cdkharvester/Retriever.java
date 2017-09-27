package cz.incad.cdk.cdkharvester;

import java.text.ParseException;
import java.util.Map;

/**
 * Created by pstastny on 8/11/2017.
 */
public interface Retriever {

    public boolean hasNext() throws Exception;

    public Map.Entry<String, String> next() throws Exception;

}
