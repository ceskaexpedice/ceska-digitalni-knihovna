/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.xsl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;

/**
 *
 * @author alberto
 */
public class XSLFunctions {

    UTFSort utf_sort;

    public XSLFunctions() throws IOException {
        utf_sort = new UTFSort();
        utf_sort.init();

    }

    public String prepareCzech(String s) throws Exception {
        return utf_sort.translate(s);
    }

    public String getDimensions(String pid) throws Exception {
        return FileDataStore.getDimensions(pid);
    }
    
    public int randomDate(){
        Random r = new Random();
        
            return r.nextInt(700) + 1512;
    }
    public String encode(String url) throws URIException {
        return URIUtil.encodeQuery(url);
    }
}
