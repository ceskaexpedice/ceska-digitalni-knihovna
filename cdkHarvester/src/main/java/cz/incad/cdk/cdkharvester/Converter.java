package cz.incad.cdk.cdkharvester;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.httpclient.util.URIUtil;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author alberto
 */
public class Converter {

    private Logger logger = Logger.getLogger(this.getClass().getName());
    ProgramArguments arguments;
    int total;
    Transformer transformer;
    XPathFactory factory = XPathFactory.newInstance();
    XPath xpath;
    XPathExpression expr;
    String updateTimeFile = "last.time";

    public Converter(ProgramArguments arguments) throws Exception {
        this.arguments = arguments;
        TransformerFactory tfactory = TransformerFactory.newInstance();
        //StreamSource xslt = new StreamSource(new File(arguments.xsl));
        //transformer = tfactory.newTransformer(xslt);
    }

    public void convert() throws Exception {
        int rows = 100;
        total = 0;
        Date date = new Date();
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); //2013-09-25T06:30:50.172Z
        String to = formatter.format(date);
        if (arguments.q != null) {
            getDocs(arguments.q, rows);
        } else if (arguments.fullIndex) {
            getDocs("*:*", rows);
        } else {
            logger.log(Level.INFO, "Current index time: {0}", to);
            String from = getLastUpdateTime();
            getDocs("timestamp:[" + from + " TO *]", rows);
        }
        writeUpdateTime(to);

        logger.log(Level.INFO, "Finished. Total documents processed: {0}", total);
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
        if (arguments.from == null) {
            if ((new File(updateTimeFile)).exists()) {
                BufferedReader in = new BufferedReader(new FileReader(updateTimeFile));
                from = in.readLine();
            } else {
                from = "1900-01-01T00:00:00.002Z";
            }
        } else {
            from = arguments.from;
        }
        return from;
    }

    private void getDocs(String q, int rows) throws Exception {
        int start = arguments.start;
        String urlStr = arguments.orig + "/searchXSL.jsp?asis=true&collapsed=false&facet=false&hl=false&fl=PID&q=" + URIUtil.encodeQuery(q)
                + "&rows=" + rows + "&offset=" + start;
        logger.log(Level.INFO, "urlStr: {0}", urlStr);
        java.net.URL url = new java.net.URL(urlStr);
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document solrDom;
        InputStream is;
        try {
            is = url.openStream();
            solrDom = builder.parse(is);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "", ex);
            is = url.openStream();
            solrDom = builder.parse(is);
        }
        String xPathStr = "/response/result/@numFound";
        factory = XPathFactory.newInstance();
        xpath = factory.newXPath();
        expr = xpath.compile(xPathStr);
        int numDocs = Integer.parseInt((String) expr.evaluate(solrDom, XPathConstants.STRING));
        logger.log(Level.INFO, "numDocs: {0}", numDocs);
        if (numDocs > 0) {

            xPathStr = "/response/result/doc/str[@name='PID']";
            expr = xpath.compile(xPathStr);
            NodeList nodes = (NodeList) expr.evaluate(solrDom, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                String pid = node.getFirstChild().getNodeValue();
                replicate(pid);
                proccesed++;
            }
            logger.log(Level.INFO, "{0} processed", proccesed);
            start = start + rows;
            while (start < numDocs) {
                getDocs(q, rows, start);
                start = start + rows;
            }
            total += numDocs;
            logger.log(Level.INFO, "total: {0}", total);
        }
    }
    int proccesed = 0;

    private void getDocs(String q, int rows, int start) throws Exception {
        String urlStr = arguments.orig + "/searchXSL.jsp?asis=true&collapsed=false&facet=false&hl=false&fl=PID&q=" + URIUtil.encodeQuery(q)
                + "&rows=" + rows + "&offset=" + start;
        logger.log(Level.INFO, "urlStr: {0}", urlStr);
        java.net.URL url = new java.net.URL(urlStr);
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document solrDom;
        InputStream is;
        try {
            is = url.openStream();
            solrDom = builder.parse(is);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "", ex);
            is = url.openStream();
            solrDom = builder.parse(is);
        }
        String xPathStr = "/response/result/@numFound";
        factory = XPathFactory.newInstance();
        xpath = factory.newXPath();
        expr = xpath.compile(xPathStr);
        int numDocs = Integer.parseInt((String) expr.evaluate(solrDom, XPathConstants.STRING));
        if (numDocs > 0) {

            xPathStr = "/response/result/doc/str[@name='PID']";
            expr = xpath.compile(xPathStr);
            NodeList nodes = (NodeList) expr.evaluate(solrDom, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                String pid = node.getFirstChild().getNodeValue();
                replicate(pid);
                proccesed++;
            }
            logger.log(Level.INFO, "{0} processed", proccesed);

        }
    }

    private void replicate(String pid) {
        //get foxml from origin
        // https://xxx.xxx.xxx.xxx/search/api/v4.6/replication/uuid:1a43499e-c953-11df-84b1-001b63bd97ba/foxml
        String url = arguments.orig + "/api/v4.6/cdk/" + pid + "/foxml";
        logger.log(Level.INFO, "get foxml from origin {0}...", url);
        
        //import foxml to dest
    }


}
