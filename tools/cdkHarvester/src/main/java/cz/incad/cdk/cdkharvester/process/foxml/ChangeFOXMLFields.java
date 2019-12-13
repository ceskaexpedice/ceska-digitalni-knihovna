package cz.incad.cdk.cdkharvester.process.foxml;

import java.io.InputStream;

public class ChangeFOXMLFields implements  ProcessFOXML {


    @Override
    public byte[] process(String name, String url, String pid, InputStream is) throws Exception {
        return new byte[0];
    }
}
