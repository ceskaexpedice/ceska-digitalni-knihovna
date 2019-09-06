package cz.incad.cdk.cdkharvester;

import cz.incad.cdk.cdkharvester.iterator.CDKHarvestIteration;
import cz.incad.cdk.cdkharvester.iterator.CDKHarvestIterationException;
import cz.incad.cdk.cdkharvester.iterator.CDKHarvestIterationItem;
import cz.incad.cdk.cdkharvester.iterator.TitleCDKHarvestIterationImpl;
import cz.incad.cdk.cdkharvester.process.foxml.ImageReplaceProcess;
import cz.incad.cdk.cdkharvester.process.foxml.ProcessFOXML;
import cz.incad.cdk.cdkharvester.utils.FilesUtils;
import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.processes.impl.ProcessStarter;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.kramerius.Import;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CDKImportOneTitleProcess extends AbstractCDKSourceHarvestProcess {

    private int processed;

    public static final Logger LOGGER  = Logger.getLogger(CDKImportOneTitleProcess.class.getName());

    // Modified by PS - foxml manip
    private List<ProcessFOXML> processingChain = new ArrayList<ProcessFOXML>();


    /**
     * @throws IOException
     *
     */
    public CDKImportOneTitleProcess() throws IOException {
        super();
        this.processingChain.add(new ImageReplaceProcess());
    }

    public CDKImportOneTitleProcess(List<ProcessFOXML> chains) {
        super();
        this.processingChain.addAll(chains);
    }

    @Process
    public static void cdkImport(@ParameterName("url") String url, @ParameterName("name") String name,
                                 @ParameterName("collectionPid") String collectionPid, @ParameterName("username") String userName,
                                 @ParameterName("pswd") String pswd,
                                 @ParameterName("pid") String pid
                                ) throws Exception {

        ProcessStarter.updateName("Import one title CDK from " + name);
        CDKImportOneTitleProcess p = new CDKImportOneTitleProcess();

        p.start(url, name, collectionPid, userName, pswd, pid);
    }


    //TODO: Rewrite it
    public void start(String url, String name, String collectionPid, String userName, String pswd, String pid) throws Exception {

        this.batchFolders = FilesUtils.batchFolders(name);
        // init variables
        initVariables(url, name, collectionPid, userName, pswd);
        initTransformations();

        Import.initialize(KConfiguration.getInstance().getProperty("ingest.user"),KConfiguration.getInstance().getProperty("ingest.password"));
        processDocs(pid);

        LOGGER.log(Level.INFO, "Finished. ");
    }


    private void processDocs(String pid) throws CDKHarvestIterationException, ParseException {
        try {
            CDKHarvestIteration iteration = iteration(pid);
            while(iteration.hasNext()) {
                CDKHarvestIterationItem next = iteration.next();
                String iteratingPid = next.getPid();
                replicate(iteratingPid, true);
                if (processed % this.getBatchModeSize() == 0) {
                    // import all from batch
                    processBatches();
                }
            }
            processBatches();
            LOGGER.log(Level.INFO, "{0} processed", processed);
        } catch (Exception e) {
            throw new CDKHarvestIterationException(e);
        }
    }


    protected CDKHarvestIteration iteration(String pid) throws ParseException, CDKHarvestIterationException {
        CDKHarvestIteration iteration = new TitleCDKHarvestIterationImpl(this.k4Url,pid);
        return iteration;
    }


}
