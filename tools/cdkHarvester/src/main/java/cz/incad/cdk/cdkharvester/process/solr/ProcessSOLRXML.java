package cz.incad.cdk.cdkharvester.process.solr;

import java.io.InputStream;

public interface ProcessSOLRXML {

    public byte[] process(String name, String url, String pid, InputStream is) throws Exception;
}
