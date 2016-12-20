package cz.incad.cdk.cdkharvester.postponed;

import java.io.File;
import java.io.IOException;

public interface PostponedItemsList {
	
	public void postpone(String pid) throws IOException;

	public File getPostponeFile();
	
	public int getCount();
}
