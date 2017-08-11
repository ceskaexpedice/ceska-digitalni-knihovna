package cz.incad.cdk.cdkharvester.commands;

import java.io.File;
import java.io.IOException;

/**
 * Created by pstastny on 7/27/2017.
 */
public class IngestIOException extends IOException {

    private String pid;
    private File ingestedFile;


    public IngestIOException(String pid, File file) {
        super("error ingesting '"+pid+"' from file '"+file.getAbsolutePath()+"'");
        this.pid = pid;
        this.ingestedFile = file;
    }

    public IngestIOException(Throwable cause) {
        super(cause);
    }

    public IngestIOException(String pid, File file, String message) {
        super(message);
        this.pid = pid;
        this.ingestedFile = file;
    }

    public IngestIOException(String pid, File file, Throwable cause) {
        super(cause);
        this.pid = pid;
        this.ingestedFile = file;
    }

    public String getPid() {
        return pid;
    }

    public File getIngestedFile() {
        return ingestedFile;
    }
}
