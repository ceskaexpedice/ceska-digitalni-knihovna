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
            LOGGER.log(Level.SEVERE,e.getMessage());
            throw new IngestIOException(pid, f, e);
        }
    }

    protected void ingest(String pid, InputStream is) throws IOException, RepositoryException, TransformerException, JAXBException, LexerException {
        Injector injector = Guice.createInjector(new SolrModule(), new ResourceIndexModule(), new RepoModule(), new NullStatisticsModule(),new ImportModule(),
                new ResourceIndexModule(),
                new CDKRepoModule(),
                new CDKHarvestModule());
        FedoraAccess fa = injector.getInstance(Key.get(FedoraAccess.class, Names.named("akubraFedoraAccess")));
        Import.ingest(fa.getInternalAPI(), is,pid, null, null, true);
    }
}
