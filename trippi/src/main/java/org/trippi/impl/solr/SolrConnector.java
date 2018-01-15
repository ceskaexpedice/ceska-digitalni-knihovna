package org.trippi.impl.solr;

import org.jrdf.graph.GraphElementFactory;
import org.nsdl.mptstore.core.BasicTableManager;
import org.nsdl.mptstore.core.DDLGenerator;
import org.nsdl.mptstore.core.GenericDatabaseAdaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trippi.*;
import org.trippi.config.ConfigUtils;
import org.trippi.impl.base.*;
import org.trippi.io.TripleIteratorFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SolrConnector extends TriplestoreConnector {

    private static Logger logger = LoggerFactory.getLogger(SolrConnector.class.getName());

    private Map<String, String> m_config;

    private GraphElementFactory m_elementFactory = new RDFUtil();
    private TripleIteratorFactory m_iteratorFactory;
    private TriplestoreSession m_updateSession;
    private TriplestoreWriter m_writer;

    private String solrString;
    //private SolrClient solrClient;

    public SolrConnector() { }

    public SolrConnector(Map<String, String> var1) throws TrippiException {
        this.setConfiguration(var1);
    }

    /** @deprecated */
    @Deprecated
    public void init(Map<String, String> var1) throws TrippiException {
        this.setConfiguration(var1);
    }

    public void setConfiguration(Map<String, String> var1) throws TrippiException {
        HashMap var2 = new HashMap(var1);
        var2.put("tripleSolrUrl", ConfigUtils.getRequired(var1, "tripleSolrUrl"));
        this.m_config = var2;
    }

    public Map<String, String> getConfiguration() {
        return this.m_config;
    }

    public void setTripleIteratorFactory(TripleIteratorFactory var1) {
        this.m_iteratorFactory = var1;
    }

    public void open() throws TrippiException {
        if (this.m_config == null) {
            throw new TrippiException("Cannot open " + this.getClass().getName() + " without valid configuration");
        } else {
            if (this.m_iteratorFactory == null) {
                this.m_iteratorFactory = TripleIteratorFactory.defaultInstance();
            }
            this.solrString = (String)this.m_config.get("tripleSolrUrl");
            int var6 = Integer.parseInt((String)this.m_config.get("poolInitialSize"));
            int var7 = Integer.parseInt((String)this.m_config.get("poolMaxSize"));
            int var10 = Integer.parseInt((String)this.m_config.get("autoFlushDormantSeconds"));
            int var11 = Integer.parseInt((String)this.m_config.get("autoFlushBufferSize"));
            int var12 = Integer.parseInt((String)this.m_config.get("bufferSafeCapacity"));
            int var13 = Integer.parseInt((String)this.m_config.get("bufferFlushBatchSize"));

            try {
                SolrSessionFactory sessionFactory = new SolrSessionFactory(this.solrString);
                this.m_updateSession = sessionFactory.newSession();
                ConfigurableSessionPool var19 = new ConfigurableSessionPool(sessionFactory, var6, var7, 0);
                MemUpdateBuffer var20 = new MemUpdateBuffer(var12, var13);
                this.m_writer = new ConcurrentTriplestoreWriter(var19, new DefaultAliasManager(new HashMap()), this.m_updateSession, var20, this.m_iteratorFactory, var11, var10);
            } catch (Exception var21) {
                throw new TrippiException("Error initializing MPTConnector: " + var21.getMessage(), var21);
            }
        }
    }


    public TriplestoreReader getReader() {
        if (this.m_writer == null) {
            try {
                this.open();
            } catch (TrippiException var2) {
                logger.error(var2.toString(), var2);
            }
        }

        return this.m_writer;
    }

    public TriplestoreWriter getWriter() {
        if (this.m_writer == null) {
            try {
                this.open();
            } catch (TrippiException var2) {
                logger.error(var2.toString(), var2);
            }
        }

        return this.m_writer;
    }

    public GraphElementFactory getElementFactory() {
        return this.m_elementFactory;
    }

    public void close() throws TrippiException {
        if (this.m_writer != null) {
            this.m_writer.close();
            this.m_updateSession.close();
            this.m_writer = null;
        }
    }
}
