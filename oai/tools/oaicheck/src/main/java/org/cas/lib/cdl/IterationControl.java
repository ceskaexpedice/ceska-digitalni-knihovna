package org.cas.lib.cdl;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Listening during the iteration process 
 * @author pstastny
 */
public interface IterationControl {
    
    /**
     * Information about batch of pids
     * @param pids
     * @throws UnsupportedEncodingException
     */
    public void onPidsIterate(List<String> pids) throws UnsupportedEncodingException;

    /**
     * Print results after iteration
     */
    public void printResult();

}
