package cz.incad.cdk.cdkharvester.iterator;

/**
 * The error has been during cdk harvesting 
 * @author pstastny
 */
public class CDKHarvestIterationException extends Exception {

	public CDKHarvestIterationException() {
		super();
	}

	public CDKHarvestIterationException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public CDKHarvestIterationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public CDKHarvestIterationException(String arg0) {
		super(arg0);
	}

	public CDKHarvestIterationException(Throwable arg0) {
		super(arg0);
	}

	
}