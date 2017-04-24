package cz.incad.cdk.cdkharvester.manageprocess;

import java.io.IOException;

public interface CheckLiveProcess {

	public void informAboutStart(String sourcePid, String sourceName, String processUuid) throws IOException;
	
	public String getLatest(String sourcePid, String sourceName) throws IOException;
	
	public boolean isAlive(String sourcePid, String sourceName) throws IOException;
}
