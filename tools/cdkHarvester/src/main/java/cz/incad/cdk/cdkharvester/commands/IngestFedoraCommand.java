package cz.incad.cdk.cdkharvester.commands;

import org.kramerius.Import;

import java.io.*;
import java.util.logging.Logger;

public class IngestFedoraCommand implements  Command{

    public static final Logger LOGGER = Logger.getLogger(IngestFedoraCommand.class.getName());

    @Override
    public void doCommand(String[] args) throws IOException {
        File folder = new File(args[0]);
        File[] files = folder.listFiles();
        for (File f : files) {
            LOGGER.info("ingesting "+f.getAbsolutePath());
            // must merge; last parameter must be false
            InputStream is = new FileInputStream(f);
            String pid = f.getName().replace("_",":");
            LOGGER.info("derived pid :"+pid);
            Import.ingest(is, pid, null, null, false);
        }
    }

    @Override
    public void startSlaveMode() {

    }

    @Override
   public void doCommandInSlaveMode(String oneArg) throws IOException {
        File f =new File(oneArg);
        // must merge; last parameter must be false
        InputStream is = new FileInputStream(f);
        String pid = f.getName().replace("_",":");
        LOGGER.info("derived pid :"+pid);
        Import.ingest(is, pid, null, null, false);
    }

    @Override
    public void endSlaveMode() {

    }
}
