package org.trippi.impl.solr;

import org.jrdf.graph.*;
import org.nsdl.mptstore.query.QueryResults;
import org.nsdl.mptstore.rdf.Literal;
import org.nsdl.mptstore.rdf.Node;
import org.nsdl.mptstore.rdf.URIReference;
import org.trippi.RDFUtil;
import org.trippi.TripleIterator;
import org.trippi.TrippiException;

import java.util.List;

public class SolrIterator extends TripleIterator {

    private QueryResults _results;
    private RDFUtil _util;

    public SolrIterator(QueryResults var1) {
        this._results = var1;
        this._util = new RDFUtil();
    }

    public boolean hasNext() {
        return this._results.hasNext();
    }

    public Triple next() throws TrippiException {
        try {
            List var1 = this._results.next();
            Node var2 = (Node)var1.get(0);
            SubjectNode var3 = (SubjectNode)this.mptToJRDF(var2);
            var2 = (Node)var1.get(1);
            PredicateNode var4 = (PredicateNode)this.mptToJRDF(var2);
            var2 = (Node)var1.get(2);
            ObjectNode var5 = (ObjectNode)this.mptToJRDF(var2);
            return this._util.createTriple(var3, var4, var5);
        } catch (Exception var6) {
            throw new TrippiException("Error getting next triple", var6);
        }
    }

    public void close() {
        this._results.close();
    }

    private org.jrdf.graph.Node mptToJRDF(Node var1) {
        try {
            if (var1 instanceof URIReference) {
                URIReference var4 = (URIReference)var1;
                return this._util.createResource(var4.getURI());
            } else if (var1 instanceof Literal) {
                org.nsdl.mptstore.rdf.Literal var2 = ( org.nsdl.mptstore.rdf.Literal)var1;
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
