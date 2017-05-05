package cz.incad.cdk.cdkharvester;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cz.incad.cdk.cdkharvester.foxmlprocess.ProcessFOXML;
import cz.incad.kramerius.virtualcollections.CDKStateSupport.CDKState;

/**
 * Base interface for cdk harvest. 
 * 
 * Note: derived from previous implemenation
 */
public interface CDKSourceHarvestProcess {
	
	/**
	 * Get FOXML from source kramerius
	 * @param pid PID of the document
	 * @param url Base url
	 * @return
	 * @throws CDKReplicationException
	 */
    public InputStream foxml(String pid, String url) throws CDKReplicationException;

    /**
     * Returns solr document from source kramerius
     * @param url
     * @return
     * @throws CDKReplicationException
     */
	public InputStream solrxml(String url) throws CDKReplicationException;
    
	/**
	 * Indexing document
	 * @param pid PID of document
	 * @throws CDKReplicationException
	 */
	public void index(String pid) throws CDKReplicationException;

	/**
	 * Start the replication for one pid
	 * @param pid PID of document
	 * @throws CDKReplicationException
	 */
	public void replicate(String pid, String timeStamp, CDKState state) throws CDKReplicationException;

	/**
	 * Ingesting to fedora
	 * @param foxml FOXML 
	 * @param pid
	 * @throws CDKReplicationException
	 */
    public void ingest(InputStream foxml, String pid) throws CDKReplicationException;

    /**
     * Add preprocessing foxml into the chain
     * @param pxml
     * @throws CDKReplicationException
     */
    public void addIntoChain(ProcessFOXML pxml) throws CDKReplicationException;
    
    /**
     * Remove preprocessing foxml from the chain
     * @param pxml
     * @throws CDKReplicationException
     */
    public void removeFromChain(ProcessFOXML pxml) throws CDKReplicationException;

    /**
     * Get the whole preprocessing chain
     * @return
     * @throws CDKReplicationException
     */
    public List<ProcessFOXML> getChain() throws CDKReplicationException;


    public void postToIndex(String xmlcont) throws CDKReplicationException;

}
