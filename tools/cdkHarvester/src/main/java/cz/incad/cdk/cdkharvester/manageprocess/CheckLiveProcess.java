package cz.incad.cdk.cdkharvester.manageprocess;

import java.io.IOException;

public interface CheckLiveProcess {

	public void informAboutStart(String pid, String processUuid) throws IOException;
	
	public String getLatest(String pid) throws IOException;
	
	public boolean isAlive(String pid) throws IOException;
}
