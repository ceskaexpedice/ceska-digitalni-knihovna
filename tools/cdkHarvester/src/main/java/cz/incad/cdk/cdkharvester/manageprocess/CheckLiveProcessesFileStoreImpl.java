package cz.incad.cdk.cdkharvester.manageprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.logging.Level;

import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.processes.impl.ProcessStarter;

public class CheckLiveProcessesFileStoreImpl extends AbstractCheckLiveProcess {

    public static final  String DEFAULT_UUID_NAME = "cdkimport.uuid";
	
	@Override
	public void informAboutStart(String pid, String processUuid) throws IOException {
		File dateFile = new File(DEFAULT_UUID_NAME);
		try(BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dateFile)))) { 
			if (dateFile.exists()) {
				dateFile.createNewFile();
			}
			out.write(processUuid);
		}
	}

	@Override
	public String getLatest(String pid) throws IOException {
		if ((new File(DEFAULT_UUID_NAME)).exists()) {
			try(BufferedReader in = new BufferedReader(new FileReader(DEFAULT_UUID_NAME))) {
				return in.readLine();
			}
        }
        return "";
	}

	@Override
	public boolean isAlive(String pid) throws IOException {
		String latest = getLatest(pid);
        if (latest != null && !latest.equals("") && !States.notRunningState(States.valueOf(getStatus(latest)))) {
        	return true;
        } else return false;
	}
}
