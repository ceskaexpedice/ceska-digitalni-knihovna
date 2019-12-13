package cz.incad.cdk.cdkharvester.commands;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import cz.incad.cdk.cdkharvester.guice.CDKHarvestModule;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.fedora.impl.CDKRepoModule;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.resourceindex.ProcessingIndexFeeder;
import cz.incad.kramerius.resourceindex.ResourceIndexModule;
import cz.incad.kramerius.service.SortingService;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.incad.kramerius.utils.pid.LexerException;
import org.apache.solr.client.solrj.SolrServerException;
import org.kramerius.Import;
import org.kramerius.ImportModule;

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;
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

    protected void ingest(String pid, InputStream is) throws IOException, RepositoryException, TransformerException, JAXBException, LexerException {
        Injector injector = Guice.createInjector(
                new SolrModule(),
                new ResourceIndexModule(),
                new RepoModule(),
                new NullStatisticsModule(),
                new ImportModule(),
                new ResourceIndexModule(),
                new CDKRepoModule(),
                new CDKHarvestModule());
        FedoraAccess fa = injector.getInstance(Key.get(FedoraAccess.class, Names.named("akubraFedoraAccess")));
        Import.ingest(fa.getInternalAPI(), is, pid, null, null, true);
    }
}
