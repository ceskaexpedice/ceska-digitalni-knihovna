package cz.incad.cdk.cdkharvester.commands;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by pstastny on 7/20/2017.
 */
public interface Command {

    public void doCommand(String[] args) throws IngestIOException;

}
