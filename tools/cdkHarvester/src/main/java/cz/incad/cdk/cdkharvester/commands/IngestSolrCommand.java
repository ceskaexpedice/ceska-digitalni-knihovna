package cz.incad.cdk.cdkharvester.commands;

import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.kramerius.Import;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by pstastny on 7/20/2017.
 */
public class IngestSolrCommand implements  Command{

    public static Logger LOGGER = Logger.getLogger(IngestSolrCommand.class.getName());

    @Override
    public void doCommand(String[] args) throws IOException {
        File folder = new File(args[0]);
        File[] files = folder.listFiles();
        for (File f : files) {
            LOGGER.info("ingesting "+f.getAbsolutePath());
            // must merge; last parameter must be false
            InputStream is = new FileInputStream(f);
            String s = IOUtils.readAsString(is, Charset.forName("UTF-8"), true);
            try {
                postData(new StringReader(s.toString()), new StringBuilder());
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

    }

    @Override
    public void startSlaveMode() throws IOException {
        try {
            this.commit();
        } catch (Exception e) {
            throw new IOException(e);
        }

    }

    @Override
    public void doCommandInSlaveMode(String oneArg) throws IOException {
        File f = new File(oneArg);
        InputStream is = new FileInputStream(f);
        String s = IOUtils.readAsString(is, Charset.forName("UTF-8"), true);
        try {
            postData(new StringReader(s.toString()), new StringBuilder());
        } catch (Exception e) {
            throw new IOException(e);
        }

    }

    @Override
    public void endSlaveMode() throws IOException {
        try {
            this.commit();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    protected String getSolrUpdateEndpoint() {
        String solrUrlString = KConfiguration.getInstance().getConfiguration().getString("solrHost") + "/update";
        return solrUrlString;
    }

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
            urlc.setConnectTimeout(KConfiguration.getInstance().getConfiguration().getInt("http.timeout", 10000));
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

    protected void commit() throws java.rmi.RemoteException, Exception {
        String s = "<commit />";
        LOGGER.log(Level.FINE, "commit");

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
