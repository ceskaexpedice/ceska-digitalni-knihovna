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
import cz.incad.kramerius.Constants;
import cz.incad.kramerius.processes.States;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;


import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.processes.impl.ProcessStarter;
import cz.incad.kramerius.utils.conf.KConfiguration;
import java.io.BufferedReader;
import java.io.BufferedWriter;
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
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import net.sf.json.JSONObject;
import org.apache.commons.configuration.Configuration;
import org.kramerius.Import;
import org.kramerius.replications.*;

/**
 * CDK import process
 *
 * @author alberto
 */
public class CDKImportProcess {

    static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(CDKImportProcess.class.getName());
    String API_VERSION = "v4.6";
    int ROWS = 500;
    int total;
    int processed;
    String updateTimeFile = "cdkimport.time";
    String uuidFile = "cdkimport.uuid";
    String harvestUrl;
    String k4Url;
    String sourceName;
    String collectionPid;
    String userName;
    String pswd;
    Transformer transformer;
    protected Configuration config;

    @Process
    public static void cdkImport(@ParameterName("url") String url, @ParameterName("name") String name, @ParameterName("collectionPid") String collectionPid, @ParameterName("username") String userName, @ParameterName("pswd") String pswd) throws Exception {

        ProcessStarter.updateName("Import CDK from " + name);

        CDKImportProcess p = new CDKImportProcess();
        p.start(url, name, collectionPid, userName, pswd);

    }

    private String getStatus(String uuid) throws Exception {
        //System.out.println(config.getString("_fedoraTomcatHost") + "/search/api/v4.6/processes/" + uuid);
        Client c = Client.create();
        WebResource r = c.resource(config.getString("_fedoraTomcatHost") + "/search/api/v4.6/processes/" + uuid);
        r.addFilter(new BasicAuthenticationClientFilter(
                config.getString("cdk.krameriusUser"),
                config.getString("cdk.krameriusPwd")));
        String t = r.accept(MediaType.APPLICATION_JSON).get(String.class);
        //System.out.println(t);
        JSONObject j = JSONObject.fromObject(t);
        return j.getString("state");
    }

    private void writeUuid(String s) throws FileNotFoundException, IOException {
        File dateFile = new File(uuidFile);
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dateFile)));
        out.write(s);
        out.close();
    }

    private String getLastUuid() throws FileNotFoundException, IOException {
        if ((new File(uuidFile)).exists()) {
            BufferedReader in = new BufferedReader(new FileReader(uuidFile));
            return in.readLine();
        }
        return "";
    }

    private void writeUpdateTime(String to) throws FileNotFoundException, IOException {
        File dateFile = new File(updateTimeFile);
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dateFile)));
        out.write(to);
        out.close();
    }

    private String getLastUpdateTime() throws FileNotFoundException, IOException {
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

    private String updateFile(String name) {
        return xslsFolder().getAbsolutePath() + File.separator + name + ".time";
    }

    private String uuidFile(String name) {
        return xslsFolder().getAbsolutePath() + File.separator + name + ".uuid";
    }

    private File xslsFolder() {
        String dirName = Constants.WORKING_DIR + File.separator + "cdk";
        File dir = new File(dirName);
        if (!dir.exists()) {
            boolean mkdirs = dir.mkdirs();
            if (!mkdirs) {
                throw new RuntimeException("cannot create dir '" + dir.getAbsolutePath() + "'");
            }
        }
        return dir;
    }
    
    public void start(String url, String name, String collectionPid, String userName, String pswd) throws Exception {


        config = KConfiguration.getInstance().getConfiguration();

        this.uuidFile = uuidFile(name);
        String uuid = getLastUuid();
        String actualUUID = System.getProperty(ProcessStarter.UUID_KEY);

        if (uuid != null && !uuid.equals("") && !States.notRunningState(States.valueOf(getStatus(uuid)))) {
            logger.log(Level.INFO, "Process yet active. Finish.");
            File f = new File(xslsFolder().getAbsolutePath() + File.separator + "uuids" + File.separator + actualUUID);
            f.createNewFile();
            return;
        }
        writeUuid(actualUUID);
        this.updateTimeFile = updateFile(name);
        String from = getLastUpdateTime();
        logger.log(Level.INFO, "Last index time: {0}", from);
        this.k4Url = url;
        this.sourceName = name;
        this.collectionPid = collectionPid;
//        setVirtualCollection();
        this.userName = userName;
        this.pswd = pswd;

        TransformerFactory tfactory = TransformerFactory.newInstance();
        InputStream stylesheet = this.getClass().getResourceAsStream("/cz/incad/cdk/cdkharvester/tr.xsl");
        StreamSource xslt = new StreamSource(stylesheet);
        transformer = tfactory.newTransformer(xslt);
//        factory = XPathFactory.newInstance();
//        xpath = factory.newXPath();

        total = 0;
        Import.initialize(KConfiguration.getInstance().getProperty("ingest.user"), KConfiguration.getInstance().getProperty("ingest.password"));
        //getDocs(url, from);
        getDocs(from);

        logger.log(Level.INFO, "Finished. Total documents processed: {0}", total);
    }

    private void getDocs(String date) throws Exception {
        PidsRetriever dr = new PidsRetriever(date, k4Url, userName, pswd);
        while (dr.hasNext()) {
            Map.Entry<String, String> entry = dr.next();
            replicate(entry.getKey());
            if (entry.getValue() != null) {
                writeUpdateTime(entry.getValue());
                processed++;
            }
            commit();
        }
        commit();
        logger.log(Level.INFO, "{0} processed", processed);
    }
    
    private void replicate(String pid) throws Exception {
        //get foxml from origin
        // https://xxx.xxx.xxx.xxx/search/api/v4.6/cdk/uuid:1a43499e-c953-11df-84b1-001b63bd97ba/foxml?collection=vc:111-222-xxx
        String url = k4Url + "/api/" + API_VERSION + "/cdk/" + pid + "/foxml?collection=" + collectionPid;
        logger.log(Level.FINE, "get foxml from origin {0}...", url);
        Client c = Client.create();
        c.setConnectTimeout(2000);
        c.setReadTimeout(60000);
        WebResource r = c.resource(url);
        r.addFilter(new BasicAuthenticationClientFilter(userName, pswd));
        InputStream t;
        try {
            t = r.accept(MediaType.APPLICATION_XML).get(InputStream.class);
        } catch (UniformInterfaceException ex2){
                if(ex2.getResponse().getStatus()==404){
                    logger.log(Level.WARNING, "Call to {0} failed with message {1}. Skyping document.", 
                            new Object[]{url, ex2.getResponse().toString()});
                    return;
                }else{
                    logger.log(Level.WARNING, "Call to {0} failed. Retrying...", url);
                    t = r.accept(MediaType.APPLICATION_XML).get(InputStream.class);
                }
        }catch (Exception ex) {
            logger.log(Level.WARNING, "Call to {0} failed. Retrying...", url);
            t = r.accept(MediaType.APPLICATION_XML).get(InputStream.class);
        }
        //import foxml to dest
        logger.log(Level.FINE, "ingesting {0}...", pid);
        ingest(t, pid);


        logger.log(Level.INFO, "indexing {0}...", pid);
        index(pid);
    }

    private void ingest(InputStream foxml, String pid) throws Exception {
        Import.ingest(foxml, pid, null, null,true);
    }

    private void index(String pid) throws Exception {
        String url = k4Url + "/api/" + API_VERSION + "/cdk/" + pid + "/solrxml";
        Client c = Client.create();
        c.setConnectTimeout(2000);
        c.setReadTimeout(60000);
        WebResource r = c.resource(url);
        r.addFilter(new BasicAuthenticationClientFilter(userName, pswd));
        InputStream t = r.accept(MediaType.APPLICATION_XML).get(InputStream.class);

        StreamResult destStream = new StreamResult(new StringWriter());
        transformer.setParameter("collectionPid", collectionPid);
        transformer.setParameter("solr_url", config.getString("solrHost")+"/select");
        transformer.transform(new StreamSource(t), destStream);

        StringWriter sw = (StringWriter) destStream.getWriter();
        //logger.info(sw.toString());
        postData(new StringReader(sw.toString()), new StringBuilder());

    }

    /**
     * Reads data from the data reader and posts it to solr, writes the response
     * to output
     */
    private void postData(Reader data, StringBuilder output)
            throws Exception {
        URL solrUrl = null;
        String solrUrlString = config.getString("solrHost") + "/update";
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
                    errorStream.append("postData URL=").append(solrUrlString).append(" HTTP response code=").append(status).append(" ");
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

    private void commit() throws java.rmi.RemoteException, Exception {
        String s = "<commit />";
        logger.log(Level.FINE, "commit");

        postData(new StringReader(s), new StringBuilder());

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
