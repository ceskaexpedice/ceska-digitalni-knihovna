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
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.util.URIUtil;
import org.kramerius.replications.BasicAuthenticationClientFilter;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author alberto
 */
public class PidsRetriever {

    static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(PidsRetriever.class.getName());
    String harvestUrl;
    String userName;
    String pswd;
    String initial_date;
    String actual_date;
    XPathFactory factory = XPathFactory.newInstance();
    XPath xpath;
    XPathExpression expr;
    final String APIURL_PREFIX = "/api/v4.6/cdk/prepare?rows=500&date=";
    Queue<Map.Entry<String, String>> qe;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public PidsRetriever(String date, String k4Url, String userName, String pswd) throws ParseException {
        this.initial_date = date;
        this.actual_date = date;
        this.harvestUrl = k4Url + APIURL_PREFIX;
        this.userName = userName;
        this.pswd = pswd;
        xpath = factory.newXPath();
        qe = new LinkedList<Map.Entry<String, String>>();
    }

    public boolean hasNext() throws Exception {
        if (!qe.iterator().hasNext()) {
            getDocs();
        }
        return qe.iterator().hasNext();
    }

    public Map.Entry<String, String> next() throws ParseException {
        Map.Entry<String, String> entry = qe.poll();
        
        actual_date = entry.getValue();
        return entry;
    }

    private void getDocs() throws Exception {
        String urlStr = harvestUrl + URIUtil.encodeQuery(actual_date);
        logger.log(Level.INFO, "urlStr: {0}", urlStr);
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document solrDom;
        Client c = Client.create();
        // follow redirect
        c.getProperties().put(
                ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
        c.setConnectTimeout(2000);
        c.setReadTimeout(20000);
        WebResource r = c.resource(urlStr);
        r.addFilter(new BasicAuthenticationClientFilter(userName, pswd));
        InputStream is;
        try {
            is = r.accept(MediaType.APPLICATION_XML).get(InputStream.class);
            solrDom = builder.parse(is);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Retrying...", ex);
            is = r.accept(MediaType.APPLICATION_XML).get(InputStream.class);
            solrDom = builder.parse(is);
        }
        String xPathStr = "/response/result/@numFound";
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
                String to = node.getNextSibling().getFirstChild().getNodeValue();
                qe.add(new DocEntry(pid, to));
            }
        }
        is.close();
    }

    final class DocEntry<K, V> implements Map.Entry<K, V> {

        private final K key;
        private V value;

        public DocEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V old = this.value;
            this.value = value;
            return old;
        }
    }
}
