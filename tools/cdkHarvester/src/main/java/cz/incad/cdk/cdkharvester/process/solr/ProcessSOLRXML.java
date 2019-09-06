package cz.incad.cdk.cdkharvester.process.solr;

import java.io.InputStream;

public interface ProcessSOLRXML {

    public byte[] process(String url, String pid, InputStream is) throws Exception;
}
