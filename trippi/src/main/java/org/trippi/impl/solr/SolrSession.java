package org.trippi.impl.solr;

import static org.trippi.impl.solr.SolrTrippiUtils.*;

import org.apache.http.impl.client.DefaultHttpClient;
import org.jrdf.graph.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.nsdl.mptstore.util.NTriplesUtil;
import org.trippi.TripleIterator;
import org.trippi.TrippiException;
import org.trippi.TupleIterator;
import org.trippi.impl.base.TriplestoreSession;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.*;


public class SolrSession implements TriplestoreSession {

    private static final String _SPO = "spo";
    private static final String _SPONGE = "sponge";
    private static final String _UNSUPPORTED = "unsupported";
    public static final String[] TRIPLE_LANGUAGES = new String[]{"spo", "sponge"};
    public static final String[] TUPLE_LANGUAGES = new String[]{"unsupported"};
    private static final Object URIREFERENCE_TYPE = "uri_reference";

    //private SolrClient solrClient;
    private String solrPoint;
    private DefaultHttpClient httpclient;

    public SolrSession(String solrPoint) {
        this.solrPoint = solrPoint;
        this.httpclient =  new DefaultHttpClient();
    }

    public String[] listTripleLanguages() {
        return TRIPLE_LANGUAGES;
    }

    public String[] listTupleLanguages() {
        return TUPLE_LANGUAGES;
    }

    public TripleIterator findTriples(String var1, String var2) throws TrippiException {
        if (var1.equals("sponge")) {
            return this.findTriples(var2);
        } else {
            throw new TrippiException("Unsupported triple query language: " + var1);
        }
    }

    private String toString(Node var1) {
        return var1 == null ? "*" : jrdfToMPT(var1).toString();
    }

    public TripleIterator findTriples(SubjectNode var1, PredicateNode var2, ObjectNode var3) throws TrippiException {
        String var4 = this.toString(var1) + " " + this.toString(var2) + " " + this.toString(var3);
        return this.findTriples(var4);
    }

    private TripleIterator findTriples(String string) throws TrippiException {
        try {
            StringTokenizer tokenizer = new StringTokenizer(string, " ");
            if (!tokenizer.hasMoreTokens()) {
                throw new TrippiException("Error querying triples");
            }
            String subject = tokenizer.nextToken();
            if (!tokenizer.hasMoreTokens()) {
                throw new TrippiException("Error querying triples");
            }
            String predicate = tokenizer.nextToken();
            if (!tokenizer.hasMoreTokens()) {
                throw new TrippiException("Error querying triples");
            }
            String object = tokenizer.nextToken();

            StringBuilder builder = new StringBuilder();
            builder.append(SolrTrippiUtils.SUBJECT).append(':');

            if (subject.equals("*")) {
                builder.append(subject);
            } else {
                builder.append("\"").append(subject).append("\"");
            }

            builder.append(" AND ");
            builder.append(SolrTrippiUtils.PREDICATE).append(':');

            if (predicate.equals("*")) {
                builder.append(predicate);
            } else {
                builder.append("\"").append(predicate).append("\"");
            }

            builder.append(" AND ");
            builder.append(SolrTrippiUtils.OBJECT).append(':');
            if (object.equals("*")) {
                builder.append(object);
            } else {
                builder.append("\"").append(object).append("\"");
            }


            String encoded = URLEncoder.encode(builder.toString(), "UTF-8");
            List<JSONObject> results = iterate(encoded);
            SOLRQueryResults solrQueryResults = new SOLRQueryResults(results);
            return new SolrIterator(solrQueryResults);
        } catch (IOException e) {
            throw new TrippiException(e.getMessage());
        }
    }

    private List<JSONObject> iterate(String query) throws IOException {
        List<JSONObject> retlist = new ArrayList<>();
        int offset = 0;
        int rows = 100;
        JSONObject jsonObject = SolrTrippiUtils.clientGET(this.httpclient, solrQueryingPoint(this.solrPoint, query, offset, rows));
        JSONObject response = jsonObject.getJSONObject("response");
        long numFound = response.getLong("numFound");
        while(offset < numFound) {
            JSONArray docs = response.getJSONArray("docs");
            for(int i=0,ll=docs.length();i<ll;i++) {
                retlist.add(docs.getJSONObject(i));
            }
            offset += rows;
            jsonObject = SolrTrippiUtils.clientGET(this.httpclient, solrQueryingPoint(this.solrPoint, query, offset, rows));
            response = jsonObject.getJSONObject("response");
        }
        return retlist;
    }


    public TupleIterator query(String var1, String var2) throws TrippiException {
        throw new TrippiException("Unsupported tuple query language: " + var2);
    }

    public void add(Set<Triple> set) throws TrippiException {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            set.stream().forEach((Triple t)->{
                try {
                    JSONObject jsonObject = new JSONObject();

                    SubjectNode subject = t.getSubject();
                    PredicateNode predicate = t.getPredicate();
                    ObjectNode object = t.getObject();

                    jsonObject.put(SolrTrippiUtils.SUBJECT, "<"+subject.toString()+">");
                    jsonObject.put(SolrTrippiUtils.PREDICATE, "<"+predicate.toString()+">");

                    if (object instanceof URIReference) {
                        jsonObject.put(SolrTrippiUtils.OBJECT_TYPE, URIREFERENCE_TYPE);
                        jsonObject.put(SolrTrippiUtils.OBJECT, "<"+object.toString()+">");
                    } else if (object instanceof  Literal) {
                        jsonObject.put(SolrTrippiUtils.OBJECT_TYPE, SolrTrippiUtils.LITERAL_TYPE);
                        String obj = object.toString();
                        if (obj.startsWith("\"") && obj.endsWith("\"")) {
                            String substring = obj.substring(1, obj.length() - 1);
                            jsonObject.put(SolrTrippiUtils.OBJECT,  "\""+NTriplesUtil.escapeLiteralValue(substring)+"\"");

                        } else {
                            jsonObject.put(SolrTrippiUtils.OBJECT,  obj);
                        }

                    }

                    byte[] digest = md5.digest((subject.toString() + " " + predicate.toString() + " " + object.toString()).getBytes("UTF-8"));
                    jsonObject.put("id",bytesToHex(digest));

                    clientPOST(this.httpclient,solrUpdatingPoint(this.solrPoint), jsonObject);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (NoSuchAlgorithmException e) {
            throw new TrippiException(e.getMessage(),e);
        }
    }

    public void delete(Set<Triple> set) throws TrippiException {
        set.stream().forEach((Triple t)->{
            try {

                JSONObject queryObject =new JSONObject();
                StringBuilder builder = new StringBuilder();
                builder.append(SolrTrippiUtils.SUBJECT).append(':').append(t.getSubject().toString());
                builder.append(" AND ");
                builder.append(SolrTrippiUtils.OBJECT).append(':').append(t.getObject().toString());
                builder.append(" AND ");
                builder.append(SolrTrippiUtils.PREDICATE).append(':').append(t.getPredicate().toString());
                queryObject.put("query", queryObject);

                JSONObject deleteObject = new JSONObject();
                deleteObject.put("delete", queryObject);

                clientPOST(this.httpclient,solrUpdatingPoint(this.solrPoint), deleteObject);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }


    protected static Set<org.nsdl.mptstore.rdf.Triple> jrdfToMPT(Set<Triple> var0) {
        HashSet var1 = new HashSet(var0.size());
        Iterator var2 = var0.iterator();

        while(var2.hasNext()) {
            Triple var3 = (Triple)var2.next();
            org.nsdl.mptstore.rdf.SubjectNode var4 = (org.nsdl.mptstore.rdf.SubjectNode)jrdfToMPT((Node)var3.getSubject());
            org.nsdl.mptstore.rdf.PredicateNode var5 = (org.nsdl.mptstore.rdf.PredicateNode)jrdfToMPT((Node)var3.getPredicate());
            org.nsdl.mptstore.rdf.ObjectNode var6 = (org.nsdl.mptstore.rdf.ObjectNode)jrdfToMPT((Node)var3.getObject());
            var1.add(new org.nsdl.mptstore.rdf.Triple(var4, var5, var6));
        }

        return var1;
    }

    protected static org.nsdl.mptstore.rdf.Node jrdfToMPT(Node var0) {
        try {
            if (var0 instanceof URIReference) {
                URIReference var6 = (URIReference)var0;
                return new org.nsdl.mptstore.rdf.URIReference(var6.getURI());
            } else if (var0 instanceof Literal) {
                Literal var1 = (Literal)var0;
                String var2 = var1.getLanguage();
                if (var2 != null && var2.length() > 0) {
                    return new org.nsdl.mptstore.rdf.Literal(var1.getLexicalForm(), var2);
                } else if (var1.getDatatypeURI() != null) {
                    org.nsdl.mptstore.rdf.URIReference var3 = new org.nsdl.mptstore.rdf.URIReference(var1.getDatatypeURI());
                    return new org.nsdl.mptstore.rdf.Literal(var1.getLexicalForm(), var3);
                } else {
                    return new org.nsdl.mptstore.rdf.Literal(var1.getLexicalForm());
                }
            } else {
                throw new RuntimeException("Unrecognized node type; cannot convert to MPT Node: " + var0.getClass().getName());
            }
        } catch (URISyntaxException var4) {
            throw new RuntimeException("Bad URI syntax, cannot convert to MPT Node", var4);
        } catch (ParseException var5) {
            throw new RuntimeException("Bad language syntax, cannot convert to MPT Node", var5);
        }
    }

    public void close() throws TrippiException {
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

}
