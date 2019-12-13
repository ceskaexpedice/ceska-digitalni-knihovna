/*
 * Copyright (C) 2013 Alberto Hernandez
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.cdk.cdkharvester;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import cz.incad.cdk.cdkharvester.process.foxml.ImageReplaceProcess;
import cz.incad.cdk.cdkharvester.process.foxml.ProcessFOXML;
import cz.incad.cdk.cdkharvester.process.solr.ProcessSOLRXML;
import cz.incad.cdk.cdkharvester.process.solr.RemoveLemmatizedAndChangePidFields;
import cz.incad.cdk.cdkharvester.utils.FilesUtils;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.processes.States;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.processes.starter.ProcessStarter;
import cz.incad.kramerius.resourceindex.ResourceIndexModule;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.incad.kramerius.utils.BasicAuthenticationClientFilter;
import cz.incad.kramerius.utils.conf.KConfiguration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import javax.ws.rs.core.MediaType;

import net.sf.json.JSONObject;
import org.kramerius.Import;
import org.kramerius.ImportModule;
import org.kramerius.replications.*;

/**
 * CDK import process
 *
 * @author alberto
 * @TODO !!! REWRITE IT !!! 
 */
public class CDKImportProcess extends AbstractCDKSourceHarvestProcess {

    static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(CDKImportProcess.class.getName());
    public static String API_VERSION = "v4.6";
    public static int ROWS = 500;

    private int total;
    private int processed;
    private String updateTimeFile = "cdkimport.time";
    private String uuidFile = "cdkimport.uuid";
    private String harvestUrl;

    /**
     * @throws IOException 
     * 
     */
    public CDKImportProcess() throws IOException {
        super();
        this.foxmlProcessingChain.add(new ImageReplaceProcess());
        this.solrProcessingChain.add(new RemoveLemmatizedAndChangePidFields());
    }

    public CDKImportProcess(List<ProcessFOXML> foxmlProcessChain, List<ProcessSOLRXML> solrProcessChain) {
        super();
        this.foxmlProcessingChain.addAll(foxmlProcessChain);
        this.solrProcessingChain.addAll(solrProcessChain);
    }

    @Process
    public static void cdkImport(@ParameterName("url") String url, @ParameterName("name") String name,
            @ParameterName("collectionPid") String collectionPid, @ParameterName("username") String userName,
            @ParameterName("pswd") String pswd,@ParameterName("dateLimit") String dateLimit) throws Exception {

        ProcessStarter.updateName("Import CDK from " + name);
        CDKImportProcess p = new CDKImportProcess();
        p.start(url, name, collectionPid, userName, pswd, dateLimit);
    }


    protected String getStatus(String uuid) throws Exception {
        Client c = Client.create();
        WebResource r = c.resource(getConfig().getString("_fedoraTomcatHost") + "/search/api/v4.6/processes/" + uuid);
        r.addFilter(new BasicAuthenticationClientFilter(getConfig().getString("cdk.krameriusUser"),
                getConfig().getString("cdk.krameriusPwd")));
        String t = r.accept(MediaType.APPLICATION_JSON).get(String.class);
        JSONObject j = JSONObject.fromObject(t);
        return j.getString("state");
    }

    protected void writeUuid(String s) throws FileNotFoundException, IOException {
        File dateFile = new File(uuidFile);
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dateFile)));
        out.write(s);
        out.close();
    }

    protected String getLastUuid() throws FileNotFoundException, IOException {
        if ((new File(uuidFile)).exists()) {
            BufferedReader in = new BufferedReader(new FileReader(uuidFile));
            return in.readLine();
        }
        return "";
    }

    protected void writeUpdateTime(String to) throws FileNotFoundException, IOException {
        File dateFile = new File(updateTimeFile);
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dateFile)));
        out.write(to);
        out.close();
    }

    protected String getLastUpdateTime() throws FileNotFoundException, IOException {
        String from;
        File dateFile = new File(updateTimeFile);
        if ((new File(updateTimeFile)).exists()) {
            BufferedReader in = new BufferedReader(new FileReader(updateTimeFile));
            from = in.readLine();
        } else {
            from = "1900-01-01T00:00:00.002Z";
        }
        return from;
    }


    public void setCollectionPid(String collectionPid) {
        this.collectionPid = collectionPid;
    }

    //TODO: Rewrite it
    public void start(String url, String name, String collectionPid, String userName, String pswd, String dateLimit) throws Exception {


        this.uuidFile = FilesUtils.uuidFile(name);
        this.batchFolders = FilesUtils.batchFolders(name);
        String uuid = getLastUuid();
        String actualUUID = System.getProperty(ProcessStarter.UUID_KEY);


        logger.log(Level.INFO, "Trying to get information about previous process '"+uuid+"'");
        if (uuid != null && !uuid.equals("") && !States.notRunningState(States.valueOf(getStatus(uuid)))) {
            logger.log(Level.INFO, "Process yet active. Finish.");
            File f = new File(FilesUtils.xslsFolder().getAbsolutePath() + File.separator + "uuids" + File.separator + actualUUID);
            f.createNewFile();
            return;
        }
        writeUuid(actualUUID);
        this.updateTimeFile = FilesUtils.updateFile(name);
        String from = getLastUpdateTime();
        logger.log(Level.INFO, "Last prepareIndex time: {0}", from);

        // init variables
        initVariables(url, name, collectionPid, userName, pswd);

        initTransformations();

        total = 0;
        Import.initialize(KConfiguration.getInstance().getProperty("ingest.user"),KConfiguration.getInstance().getProperty("ingest.password"));
        getDocs(name, from,dateLimit);

        logger.log(Level.INFO, "Finished. Total documents processed: {0}", total);
        System.exit(0);
    }


    protected void getDocs(String name, String date, String dateLimit) throws Exception {
        PidsRetriever dr = getPidsRetriever(date, dateLimit);
        while (dr.hasNext()) {
            Map.Entry<String, String> entry = dr.next();
            replicate(name, entry.getKey(),false);
            if (entry.getValue() != null) {
                writeUpdateTime(entry.getValue());
                processed++;
            }
            if (processed % this.getBatchModeSize() == 0) {
                // import all from batch
                processBatches();
            }
        }

        processBatches();
        logger.log(Level.INFO, "{0} processed", processed);
    }

    protected PidsRetriever getPidsRetriever(String date, String dateLimit) throws ParseException {
        return new PidsRetriever(date, k4Url, userName, pswd, dateLimit);
    }

}
