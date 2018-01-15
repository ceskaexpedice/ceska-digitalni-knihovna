package org.trippi.impl.solr;

import org.jrdf.graph.GraphElementFactoryException;
import org.json.JSONObject;
import org.nsdl.mptstore.query.QueryException;
import org.nsdl.mptstore.query.QueryResults;
import org.nsdl.mptstore.query.RuntimeQueryException;
import org.nsdl.mptstore.rdf.Literal;
import org.nsdl.mptstore.rdf.Node;
import org.nsdl.mptstore.rdf.URIReference;
import org.nsdl.mptstore.util.NTriplesUtil;
import org.trippi.RDFUtil;

import java.util.ArrayList;
import java.util.List;

public class SOLRQueryResults implements QueryResults {

    private RDFUtil _util;
    private List<JSONObject> response;
    private int index = 0;

    public SOLRQueryResults(List<JSONObject> response) {
        this._util = new RDFUtil();
        this.response = response;
    }

    @Override
    public List<String> getTargets() {
        return null;
    }

    @Override
    public List<Node> next() throws RuntimeQueryException {
        try {
            JSONObject doc = this.response.get(this.index++);

            Node subject = NTriplesUtil.parseNode((String)doc.getString(SolrTrippiUtils.SUBJECT));
            Node predicate = NTriplesUtil.parseNode((String) doc.getString(SolrTrippiUtils.PREDICATE));
            Node object = NTriplesUtil.parseNode((String) doc.getString(SolrTrippiUtils.OBJECT));

            List<Node> list = new ArrayList<>();
            list.add(subject);
            list.add(predicate);
            list.add(object);

            return list;
        } catch (Exception var6) {
            throw new RuntimeQueryException(new QueryException(var6.getMessage()));
        }
    }

    @Override
    public boolean hasNext() {
        return this.index < this.response.size();
    }

    @Override
    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {

    }

    private org.jrdf.graph.Node mptToJRDF(Node var1) {
        try {
            if (var1 instanceof URIReference) {
                URIReference var4 = (URIReference)var1;
                return this._util.createResource(var4.getURI());
            } else if (var1 instanceof Literal) {
                Literal var2 = (Literal)var1;
                if (var2.getLanguage() != null) {
                    return this._util.createLiteral(var2.getValue(), var2.getLanguage());
                } else {
                    return var2.getDatatype() != null ? this._util.createLiteral(var2.getValue(), var2.getDatatype().getURI()) : this._util.createLiteral(var2.getValue());
                }
            } else {
                throw new RuntimeException("Unrecognized node type: " + var1.getClass().getName());
            }
        } catch (GraphElementFactoryException var3) {
            throw new RuntimeException("Unable to create JRDF node", var3);
        }
    }

}
