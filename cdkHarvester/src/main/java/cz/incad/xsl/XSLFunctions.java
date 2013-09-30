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

/**
 *
 * @author alberto
 */
public class XSLFunctions {

    UTFSort utf_sort;
    List<String> dict;

    public XSLFunctions() throws IOException {
        utf_sort = new UTFSort();
        utf_sort.init();


        BufferedReader reader = new BufferedReader(new FileReader("dict.txt"));
        dict = new ArrayList<String>();

        String line = reader.readLine();

        while (line != null) {
            dict.add(line);
            line = reader.readLine();
        }


    }

    public String prepareCzech(String s) throws Exception {
        return utf_sort.translate(s);
    }

    public String getDimensions(String pid) throws Exception {
        return FileDataStore.getDimensions(pid);
    }

    public String fillOCR() {

        StringBuilder sb = new StringBuilder();
        Random r = new Random();
        for (int i = 0; i < 100; i++) {
            String randomString = dict.get(r.nextInt(dict.size() - 1));
            sb.append(randomString).append(" ");
        }
        return sb.toString();
    }
    
    public int randomDate(){
        Random r = new Random();
        
            return r.nextInt(700) + 1512;
        
        
    }
}
