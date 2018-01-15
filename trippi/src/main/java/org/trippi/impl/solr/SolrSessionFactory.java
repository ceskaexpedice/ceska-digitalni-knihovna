package org.trippi.impl.solr;

import org.trippi.TrippiException;
import org.trippi.impl.base.TriplestoreSession;
import org.trippi.impl.base.TriplestoreSessionFactory;

public class SolrSessionFactory implements TriplestoreSessionFactory {

    private String  solrEndpoint;

    public SolrSessionFactory(String solrEndpoint) {
        this.solrEndpoint = solrEndpoint;
    }

    public TriplestoreSession newSession() throws TrippiException {
        return new SolrSession(this.solrEndpoint);
    }

    public String[] listTripleLanguages() {
        return SolrSession.TRIPLE_LANGUAGES;
    }

    public String[] listTupleLanguages() {
        return SolrSession.TUPLE_LANGUAGES;
    }

    public void close() throws TrippiException {

    }

}
