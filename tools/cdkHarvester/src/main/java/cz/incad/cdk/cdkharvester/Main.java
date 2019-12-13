package cz.incad.cdk.cdkharvester;

import cz.incad.kramerius.processes.starter.ProcessStarter;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws Exception {
        System.setProperty(ProcessStarter.UUID_KEY,"");

        CDKImportProcess importProcess = new CDKImportProcess();
        importProcess.start("http://localhost:18080/search", "mzk", "vc:44679769-b5bb-4ac7-ad27-a0c44698c2ea","krameriusAdmin","krameriusAdmin", null );
    }
}
