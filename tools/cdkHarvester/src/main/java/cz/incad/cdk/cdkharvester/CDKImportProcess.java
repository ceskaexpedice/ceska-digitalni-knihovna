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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import cz.incad.cdk.cdkharvester.changeindex.AddField;
import cz.incad.cdk.cdkharvester.changeindex.ChangeField;
import cz.incad.cdk.cdkharvester.changeindex.PrivateConnectUtils;
import cz.incad.cdk.cdkharvester.changeindex.ResultsUtils;
import cz.incad.cdk.cdkharvester.commands.Command;
import cz.incad.cdk.cdkharvester.commands.IngestIOException;
import cz.incad.cdk.cdkharvester.commands.SupportedCommands;
import cz.incad.cdk.cdkharvester.process.ImageReplaceProcess;
import cz.incad.cdk.cdkharvester.process.ProcessFOXML;
import cz.incad.cdk.cdkharvester.utils.FilesUtils;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.processes.States;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;

import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.processes.impl.ProcessStarter;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.PIDParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import javax.ws.rs.core.MediaType;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import net.sf.json.JSONObject;
import org.apache.commons.configuration.Configuration;
import org.fedora.api.Ingest;
import org.kramerius.Import;
import org.kramerius.replications.*;
import org.w3c.dom.Document;

/**
 * CDK import process
 *
 * @author alberto
 * @TODO !!! REWRITE IT !!! 
 */
public class CDKImportProcess {

    static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(CDKImportProcess.class.getName());
    public static String API_VERSION = "v4.6";
    public static int ROWS = 500;

    private int total;
    private int processed;
    private String updateTimeFile = "cdkimport.time";
    private String uuidFile = "cdkimport.uuid";
    private String harvestUrl;
    private String k4Url;
    private String sourceName;
    private String collectionPid;
    private String userName;
    private String pswd;
    private Transformer transformer;
    protected Configuration config;

    protected File batchFolders;
    
    // Modified by PS - foxml manip
    private List<ProcessFOXML> processingChain = new ArrayList<ProcessFOXML>();
    
    	
    /**
     * @throws IOException 
     * 
     */
    public CDKImportProcess() throws IOException {
        super();
        this.processingChain.add(new ImageReplaceProcess());
    }

    public CDKImportProcess(List<ProcessFOXML> chains) {
        super();
        this.processingChain.addAll(chains);
    }
    
    @Process
    public static void cdkImport(@ParameterName("url") String url, @ParameterName("name") String name,
            @ParameterName("collectionPid") String collectionPid, @ParameterName("username") String userName,
            @ParameterName("pswd") String pswd) throws Exception {

        ProcessStarter.updateName("Import CDK from " + name);

        CDKImportProcess p = new CDKImportProcess();
        p.start(url, name, collectionPid, userName, pswd);
    }


    /** Batch mode properties */
    protected int getBatchModeSize() {
        return KConfiguration.getInstance().getConfiguration().getInteger("cdk.prepareFOXML.batch.size", 400);
    }


    /** threshold properties */
    protected boolean getThresholdMode() {
        return KConfiguration.getInstance().getConfiguration().getBoolean("cdk.prepareFOXML.batch.threshold.enabled", true);
    }

    protected int getThresholdIteration() {
        return KConfiguration.getInstance().getConfiguration().getInt("cdk.prepareFOXML.batch.threshold.iterations", 5);
    }

    protected boolean getThresholdWithDelay() {
        return KConfiguration.getInstance().getConfiguration().getBoolean("cdk.prepareFOXML.batch.threshold_with_delay.enabled", false);
    }
    protected int getThresholdWithDelayIteration() {
        return KConfiguration.getInstance().getConfiguration().getInt("cdk.prepareFOXML.batch.threshold_with_delay.iterations", 5);
    }

    protected int getThresholdWithDelayNumber() {
        return KConfiguration.getInstance().getConfiguration().getInt("cdk.prepareFOXML.batch.threshold_with_delay.delaytime", 100);
    }


    protected String getStatus(String uuid) throws Exception {
        Client c = Client.create();
        WebResource r = c.resource(config.getString("_fedoraTomcatHost") + "/search/api/v4.6/processes/" + uuid);
        r.addFilter(new BasicAuthenticationClientFilter(config.getString("cdk.krameriusUser"),
                config.getString("cdk.krameriusPwd")));
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


    public String getCollectionPid() {
		return collectionPid;
	}

	public void setCollectionPid(String collectionPid) {
		this.collectionPid = collectionPid;
	}

	//TODO: Rewrite it
    public void start(String url, String name, String collectionPid, String userName, String pswd) throws Exception {

        config = KConfiguration.getInstance().getConfiguration();

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
        getDocs(from);

        logger.log(Level.INFO, "Finished. Total documents processed: {0}", total);
    }

	protected void initVariables(String url, String name, String collectionPid, String userName, String pswd) {
		this.k4Url = url;
        this.sourceName = name;
        this.collectionPid = collectionPid;
        // setVirtualCollection();
        this.userName = userName;
        this.pswd = pswd;
        this.config = KConfiguration.getInstance().getConfiguration();
	}

	protected void initTransformations() throws TransformerFactoryConfigurationError, TransformerConfigurationException {
		TransformerFactory tfactory = TransformerFactory.newInstance();
        InputStream stylesheet = this.getClass().getResourceAsStream("/cz/incad/cdk/cdkharvester/tr.xsl");
        StreamSource xslt = new StreamSource(stylesheet);
        //transformer = tfactory.newTransformer(xslt);
        this.setTransformer(tfactory.newTransformer(xslt));
	}

	
    public Transformer getTransformer() {
		return transformer;
	}

	public void setTransformer(Transformer transformer) {
		this.transformer = transformer;
	}

	protected void getDocs(String date) throws Exception {
        PidsRetriever dr = getPidsRetriever(date);
        while (dr.hasNext()) {
            Map.Entry<String, String> entry = dr.next();
            replicate(entry.getKey());
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

    private void processBatches() throws Exception {
        processFoxmlBatch();
        processSolrXmlBatch();
        commit();
        FilesUtils.deleteFolder(new File(batchFolders, FilesUtils.FOXML_FILES));
        FilesUtils.deleteFolder(new File(batchFolders, FilesUtils.SOLRXML_FILES));
    }

    protected PidsRetriever getPidsRetriever(String date) throws ParseException {
		return new PidsRetriever(date, k4Url, userName, pswd);
	}


    protected void replicate(String pid) throws Exception {
    	String url = k4Url + "/api/" + API_VERSION + "/cdk/" + pid + "/foxml?collection=" + collectionPid;
        logger.log(Level.FINE, "get foxml from origin {0}...", url);
        
        PIDParser parser = new PIDParser(pid);
        if (!parser.isPagePid()) {
        	if (!Utils.getSkipList().contains(pid)) {
            	InputStream t = foxml(pid, url);
                prepareFOXML(t, pid);
                prepareIndex(pid);
        	} else {
            	logger.log(Level.INFO,"skipping pid {0} because of configuration",pid); 
        	}
        } else {
        	logger.info("page pid; github #16"); 
        }
    }

	protected InputStream foxml(String pid, String url) {
		WebResource r = client(url, this.userName, this.pswd);
        try {
            return r.accept(MediaType.APPLICATION_XML).get(InputStream.class);
        } catch (UniformInterfaceException ex2) {
            if (ex2.getResponse().getStatus() == 404) {
                logger.log(Level.WARNING, "Call to {0} failed with message {1}. Skyping document.",
                        new Object[] { url, ex2.getResponse().toString() });
                return null;
            } else {
                logger.log(Level.WARNING, "Call to {0} failed. Retrying...", url);
                return r.accept(MediaType.APPLICATION_XML).get(InputStream.class);
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Call to {0} failed. Retrying...", url);
            return r.accept(MediaType.APPLICATION_XML).get(InputStream.class);
        }
	}

	public static WebResource client(String url, String userName, String pswd) {
		Client c = Client.create();
		int connectionTimeout = KConfiguration.getInstance().getConfiguration().getInt("cdk.client.connection.timeout", 2000);
		int readTimeout = KConfiguration.getInstance().getConfiguration().getInt("cdk.client.read.timeout", 600000);
		logger.info("connection timeout: "+connectionTimeout);
		logger.info("read timeout: "+readTimeout);
		c.setConnectTimeout(connectionTimeout);
        c.setReadTimeout(readTimeout);
        WebResource r = c.resource(url);
        r.addFilter(new BasicAuthenticationClientFilter(userName, pswd));
		return r;
	}

    private void prepareFOXML(InputStream foxml, String pid) throws Exception {
    	if(foxml == null) {
    		logger.info("No inputstream for foxml");
			return;
    	}

    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
    	IOUtils.copyStreams(foxml, bos);
    	
		InputStream processingStream = new ByteArrayInputStream(bos.toByteArray());
        for (int i = 0,ll= this.processingChain.size(); i < ll; i++) {
            ProcessFOXML unit = processingChain.get(i);
            processingStream = new ByteArrayInputStream(unit.process(this.k4Url, pid, processingStream));
        }
        storeFOXMLToDisk(pid, processingStream);
    }



    protected void storeFOXMLToDisk(String pid, InputStream is) throws IOException {
        logger.info("storing foxml to disk; preparing the batch");

        FilesUtils.dumpXMLS(this.sourceName,FilesUtils.FOXML_FILES, is, pid);
    }

    protected void storeSOLRXMLToDisk(String pid, InputStream is) throws IOException {
        logger.info("storing solrxml to disk; preparing the batch");
        FilesUtils.dumpXMLS(this.sourceName, FilesUtils.SOLRXML_FILES, is, pid);
    }


    protected void processFoxmlBatch() throws IOException, InterruptedException {
        File batchFolders = FilesUtils.batchFolders(this.sourceName);
        File subFolder = new File(batchFolders, FilesUtils.FOXML_FILES);

        if (getThresholdMode()) {
            List<String> params = new ArrayList<String>();
            params.add(subFolder.getAbsolutePath());
            params.add(""+getThresholdIteration());
            if (getThresholdWithDelay()) {
                params.add(""+getThresholdWithDelayIteration());
            }
            SupportedCommands.FEDORA_THRESHOLD.doCommand(params.toArray(new String[params.size()]));
        } else {
            SupportedCommands.FEDORA.doCommand(new String[] {subFolder.getAbsolutePath()});
        }
    }




    protected void processSolrXmlBatch() throws IOException {
        File batchFolders = FilesUtils.batchFolders(this.sourceName);
        File subFolder = new File(batchFolders, FilesUtils.SOLRXML_FILES);
        SupportedCommands.SOLR.doCommand(new String[] {subFolder.getAbsolutePath()});
    }

    protected boolean pidExists( String pid ) throws IOException {
        try {
            FedoraAccessImpl fa = new FedoraAccessImpl(KConfiguration.getInstance(), null);
            Document relsExt = fa.getRelsExt(pid);
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE,e.getMessage(),e);
            return false;
        }
    }



    private void prepareIndex(String pid) throws Exception {
    	if (pid.contains("@")) {
			logger.info("Page pid; cannot prepareIndex");
			return;
    	}
    	org.json.JSONObject results = findDocFromCurrentIndex(pid);
    	if (ResultsUtils.docsExists(results)) {
    		if (ResultsUtils.collectionExists(results)) {
    			List<String> collections = ResultsUtils.disectCollections(results);
    			if (!collections.contains(this.collectionPid)) {
    				AddField addField = new AddField(pid, "collection", this.collectionPid);
    				addField.addValueToArray(getSolrUpdateEndpoint());
    			}
    		} else {
    	        ChangeField chField = new ChangeField(pid, "collection", this.collectionPid);
    	        chField.changeField(getSolrUpdateEndpoint());
    		}
    	} else {
    		
        	try {
				String url = k4Url + "/api/" + API_VERSION + "/cdk/" + pid + "/solrxml";
				InputStream t = solrxml(url);

				
				StreamResult destStream = new StreamResult(new StringWriter());
				changeTranformationVariables();
				transformer.transform(new StreamSource(t), destStream);
				
				StringWriter sw = (StringWriter) destStream.getWriter();

				//logger.info(sw.toString());
                storeSOLRXMLToDisk(pid, new ByteArrayInputStream(sw.toString().getBytes(Charset.forName("UTF-8"))));

			} catch (UniformInterfaceException e) {
				logger.info("cannot prepareIndex document");
			}
    	}
    }

	protected void changeTranformationVariables() {
		transformer.setParameter("collectionPid", getCollectionPid());
		transformer.setParameter("solr_url", getSolrSelectEndpoint());
	}

    
	public static String reducePid(String pid) {
		// page pid 
    	if (pid.contains("/@")) {
    		pid = pid.replace("/@", "@");
    	}
		return pid;
	}

	protected InputStream solrxml(String url) {
		WebResource r = client(url, this.userName, this.pswd);
		InputStream t = r.accept(MediaType.APPLICATION_XML).get(InputStream.class);
		return t;
	}

	public org.json.JSONObject findDocFromCurrentIndex(String pid)
			throws UnsupportedEncodingException, URISyntaxException {
		org.json.JSONObject results = PrivateConnectUtils.findDoc(getSolrSelectEndpoint(), pid);
		return results;
	}

    // change collection
    
    /**
     * Reads data from the data reader and posts it to solr, writes the response
     * to output
     */
    protected void postData(Reader data, StringBuilder output) throws Exception {
        URL solrUrl = null;
        String solrUrlString = getSolrUpdateEndpoint();
        try {
            solrUrl = new URL(solrUrlString);
        } catch (MalformedURLException e) {
            throw new Exception("solrUrl=" + solrUrlString + ": ", e);
        }
        HttpURLConnection urlc = null;
        String POST_ENCODING = "UTF-8";
        try {
            urlc = (HttpURLConnection) solrUrl.openConnection();
            urlc.setConnectTimeout(config.getInt("http.timeout", 10000));
            try {
                urlc.setRequestMethod("POST");
            } catch (ProtocolException e) {
                throw new Exception("Shouldn't happen: HttpURLConnection doesn't support POST??", e);
            }
            urlc.setDoOutput(true);
            urlc.setDoInput(true);
            urlc.setUseCaches(false);
            urlc.setAllowUserInteraction(false);
            urlc.setRequestProperty("Content-type", "text/xml; charset=" + POST_ENCODING);

            OutputStream out = urlc.getOutputStream();

            try {
                Writer writer = new OutputStreamWriter(out, POST_ENCODING);
                pipe(data, writer);
                writer.close();
            } catch (IOException e) {
                throw new Exception("IOException while posting data", e);
            } finally {
                if (out != null) {
                    out.close();
                }
            }

            InputStream in = urlc.getInputStream();
            int status = urlc.getResponseCode();
            StringBuilder errorStream = new StringBuilder();
            try {
                if (status != HttpURLConnection.HTTP_OK) {
                    errorStream.append("postData URL=").append(solrUrlString).append(" HTTP response code=")
                            .append(status).append(" ");
                    throw new Exception("URL=" + solrUrlString + " HTTP response code=" + status);
                }
                Reader reader = new InputStreamReader(in);
                pipeString(reader, output);
                reader.close();
            } catch (IOException e) {
                throw new Exception("IOException while reading response", e);
            } finally {
                if (in != null) {
                    in.close();
                }
            }

            InputStream es = urlc.getErrorStream();
            if (es != null) {
                try {
                    Reader reader = new InputStreamReader(es);
                    pipeString(reader, errorStream);
                    reader.close();
                } catch (IOException e) {
                    throw new Exception("IOException while reading response", e);
                } finally {
                    es.close();
                }
            }
            if (errorStream.length() > 0) {
                throw new Exception("postData error: " + errorStream.toString());
            }

        } catch (IOException e) {
            throw new Exception("Solr has throw an error. Check tomcat log. " + e);
        } finally {
            if (urlc != null) {
                urlc.disconnect();
            }
        }
    }

	protected String getSolrUpdateEndpoint() {
		String solrUrlString = config.getString("solrHost") + "/update";
		return solrUrlString;
	}
	protected String getSolrSelectEndpoint() {
		String solrUrlString = config.getString("solrHost") + "/select";
		return solrUrlString;
	}

    protected void commit() throws java.rmi.RemoteException, Exception {
        String s = "<commit />";
        logger.log(Level.FINE, "commit");

        postData(new StringReader(s), new StringBuilder());
    }

    protected void closeBatch() {

    }

    /**
     * Pipes everything from the reader to the writer via a buffer
     */
    private static void pipe(Reader reader, Writer writer) throws IOException {
        char[] buf = new char[1024];
        int read = 0;
        while ((read = reader.read(buf)) >= 0) {
            writer.write(buf, 0, read);
        }
        writer.flush();
    }

    /**
     * Pipes everything from the reader to the writer via a buffer except lines
     * starting with '<?'
     */
    private static void pipeString(Reader reader, StringBuilder writer) throws IOException {
        char[] buf = new char[1024];
        int read = 0;
        while ((read = reader.read(buf)) >= 0) {
            if (!(buf[0] == '<' && buf[1] == '?')) {
                writer.append(buf, 0, read);
            }
        }
    }
}