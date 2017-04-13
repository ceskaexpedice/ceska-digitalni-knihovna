package cz.incad.cdk.cdkharvester.timestamp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;

public class ProcessingTimestampsFilesStoreImpl extends AbstractProcessingTimestamps {

	public static final String DEFAULT_UPDATE_TIME_FILE = "cdkimport.time";

	@Override
	public LocalDateTime getTimestamp(String pid) throws IOException {
		if ((new File(DEFAULT_UPDATE_TIME_FILE)).exists()) {
        	try(BufferedReader in = new BufferedReader(new FileReader(DEFAULT_UPDATE_TIME_FILE))) {
        		return super.parse(in.readLine());
        	}
        } else {
            return nullvalue();
        }
	}


	@Override
	public void setTimestamp(String pid, LocalDateTime date) throws IOException {
        if (!new File(DEFAULT_UPDATE_TIME_FILE).exists()) {
        	new File(DEFAULT_UPDATE_TIME_FILE).createNewFile();
        }
    	try(BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DEFAULT_UPDATE_TIME_FILE)))) {
            out.write(super.format(date));
    	}        
	}
}
