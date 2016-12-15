package cz.incad.kramerius.utils;

import java.io.IOException;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.*;

/**
 * Context for process
 * @author pavels
 */
public class OAICriteriumContext implements RightCriteriumContext {

    private String pid;
    private OAIFedoraAccess fedoraA;
    private OAISolrAccess solrA;
    
    public OAICriteriumContext(String pid, OAIFedoraAccess fa, OAISolrAccess solrA) {
        super();
        this.pid = pid;
        this.fedoraA = fa;
        this.solrA = solrA;
    }

    @Override
    public String getRequestedPid() {
        return this.pid;
    }

    @Override
    public String getRequestedStream() {
        throw new UnsupportedOperationException("unsupported for this context");
    }

    @Override
    public String getAssociatedPid() {
        return this.pid;
    }

    @Override
    public void setAssociatedPid(String uuid) {
        throw new UnsupportedOperationException("unsupported for this context");
    }

    @Override
    public ObjectPidsPath[] getPathsToRoot() {
    	try {
			return this.solrA.getPath(pid);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
    }

    @Override
    public User getUser() {
        throw new UnsupportedOperationException("unsupported for this context");
    }

    @Override
    public FedoraAccess getFedoraAccess() {
    	return this.fedoraA;
    }

    @Override
    public SolrAccess getSolrAccess() {
        throw new UnsupportedOperationException("unsupported for this context");
    }

    @Override
    public UserManager getUserManager() {
        throw new UnsupportedOperationException("unsupported for this context");
    }

    @Override
    public String getRemoteHost() {
        throw new UnsupportedOperationException("unsupported for this context");
    }

    @Override
    public String getRemoteAddr() {
        throw new UnsupportedOperationException("unsupported for this context");
    }
}
