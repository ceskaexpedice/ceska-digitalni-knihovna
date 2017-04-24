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

import cz.incad.kramerius.Constants;
import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.processes.impl.ProcessStarter;
import cz.incad.kramerius.utils.StringUtils;

public class CheckLiveProcessesFileStoreImpl extends AbstractCheckLiveProcess {

    public static final  String DEFAULT_UUID_NAME = "cdkimport.uuid";
	
    
    protected File _folder() {
        String dirName = Constants.WORKING_DIR + File.separator + "cdk";
        File dir = new File(dirName);
        if (!dir.exists()) {
            boolean mkdirs = dir.mkdirs();
            if (!mkdirs) {
                throw new RuntimeException("cannot create dir '" + dir.getAbsolutePath() + "'");
            }
        }
        return dir;
    }
    
	@Override
	public void informAboutStart(String sourcePid, String sourceName, String processUuid) throws IOException {
		File dateFile = new File(_folder(), StringUtils.isAnyString(sourceName) ? sourceName : DEFAULT_UUID_NAME);
		try(BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dateFile)))) { 
			if (!dateFile.exists()) {
				dateFile.createNewFile();
			}
			out.write(processUuid);
		}
	}

	@Override
	public String getLatest(String sourcePid, String sourceName) throws IOException {
		File dateFile = new File(_folder(), StringUtils.isAnyString(sourceName) ? sourceName : DEFAULT_UUID_NAME);
		if (dateFile.exists()) {
			try(BufferedReader in = new BufferedReader(new FileReader(dateFile))) {
				return in.readLine();
			}
        }
        return "";
	}

	@Override
	public boolean isAlive(String sourcePid, String sourceName) throws IOException {
		String latest = getLatest(sourcePid, sourceName);
        if (latest != null && !latest.equals("") && !States.notRunningState(States.valueOf(getStatus(latest)))) {
        	return true;
        } else return false;
	}
}
