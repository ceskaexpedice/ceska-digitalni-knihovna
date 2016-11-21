package org.cas.lib.cdl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    
    public static void printUsage() {
        StringBuilder builder = new StringBuilder();
        builder.append("Main")
            .append("[solraddr]")
            .append("<pid>")
            .append("<field>")
            .append("<value>");
        System.out.println(builder);
    }
    
    public static void main(String[] args) {
        if (args.length < 3) {
            printUsage();
        } else {
            List<String> argsList = new ArrayList<>(Arrays.asList(args));
            String addr = ChangeField.SOLR_UDATE_ENDPOINT;
            if (args.length > 3) {
                addr = argsList.remove(0);
            }
            String pid = argsList.remove(0);
            String field = argsList.remove(0);
            String value = argsList.remove(0);
            
            ChangeField f = new ChangeField(pid, field, value);
            f.changeField(addr);
        }
    }
}
