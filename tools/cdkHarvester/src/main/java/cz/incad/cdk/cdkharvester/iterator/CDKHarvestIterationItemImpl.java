package cz.incad.cdk.cdkharvester.iterator;

public class CDKHarvestIterationItemImpl implements CDKHarvestIterationItem {

	private String pid;
	private String timestamp;
	public CDKHarvestIterationItemImpl(String pid, String timestamp) {
		super();
		this.pid = pid;
		this.timestamp = timestamp;
	}
	@Override
	public String getPid() {
		return this.pid;
	}
	
	@Override
	public String getTimestamp() {
		return this.timestamp;
	}
}