package cz.incad.cdk.cdkharvester.iterator;

/**
 * One iteration item
 * @author pstastny
 */
public interface CDKHarvestIterationItem {
	
	/**
	 * Pid  of item
	 * @return
	 */
	public String getPid();
	
	/**
	 * Timestamp of item
	 * @return
	 */
	public String getTimestamp();
}
