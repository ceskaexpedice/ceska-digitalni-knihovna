package cz.incad.cdk.cdkharvester.iterator;

/**
 * Iteration interface
 * @author pstastny
 */
public interface CDKHarvestIteration {
	/**
	 * Initialization
	 * @throws CDKHarvestIterationException
	 */
	public void init() throws CDKHarvestIterationException;
	
	/**
	 * Returns true if there is the next item
	 * @return
	 * @throws CDKHarvestIterationException
	 */
	public boolean hasNext() throws CDKHarvestIterationException;
	
	/**
	 * Returns the next item
	 * @return
	 * @throws CDKHarvestIterationException
	 */
	public CDKHarvestIterationItem next() throws CDKHarvestIterationException;

	
}