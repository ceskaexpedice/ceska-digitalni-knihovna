package cz.incad.cdk.cdkharvester.commands;

import org.kramerius.Import;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by pstastny on 7/27/2017.
 */
public class IngestFedoraFileCommand implements  Command{

    public static final Logger LOGGER = Logger.getLogger(IngestFedoraFileCommand.class.getName());

    @Override
    public void doCommand(String[] args) throws IngestIOException {
        File f = new File(args[0]);
        String pid = f.getName().replace("_",":");
        try {
            // must merge; last parameter must be false
            InputStream is = new FileInputStream(f);
            LOGGER.info("derived pid :"+pid);
            ingest(pid, is);
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            throw new IngestIOException(pid, f, e);
        }
    }

    protected void ingest(String pid, InputStream is) throws IOException {
        Import.ingest(is, pid, null, null, false);
    }
}
