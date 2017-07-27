package cz.incad.cdk.cdkharvester.commands;

import org.kramerius.Import;

import java.io.*;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IngestFedoraFolderCommand implements  Command{

    public static final Logger LOGGER = Logger.getLogger(IngestFedoraFolderCommand.class.getName());

    @Override
    public void doCommand(String[] args) throws IngestIOException {
        File folder = new File(args[0]);
        File skipToFile = null;
        boolean skippingMode = false;
        if (args.length > 1) {
            skipToFile = new File(args[1]);
            skippingMode = true;
        }
        File[] files = folder.listFiles();
        if (files != null) {
            Arrays.sort(files);
            for (File f : files) {
               if (skippingMode) {
                    if (skipToFile != null) {
                        if (f.getAbsolutePath().equals(skipToFile.getAbsolutePath())) {
                            skippingMode = false;
                        }
                    }
                   LOGGER.info("skipping file :"+f.getAbsolutePath());
                    continue;
                } else {
                    String pid = f.getName().replace("_",":");
                    try {
                        LOGGER.info("ingesting "+f.getAbsolutePath());
                        // must merge; last parameter must be false
                        InputStream is = new FileInputStream(f);
                        LOGGER.info("derived pid :"+pid);
                        ingest(pid, is);
                    } catch (Throwable e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                        throw new IngestIOException(pid, f, e);
                    }
                }
            }
        } else {
            LOGGER.info("no files in fodler; skipping" );
        }
    }

    protected void ingest(String pid, InputStream is) throws IOException {
        Import.ingest(is, pid, null, null, false);
    }
}
