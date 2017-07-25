package cz.incad.cdk.cdkharvester.commands;

import java.io.File;
import java.io.IOException;

public class IngestBoth implements Command {

    private IngestFedoraCommand fedoraCommand;
    private IngestSolrCommand solrCommand;

    @Override
    public void doCommand(String[] args) throws IOException {
        File folder = new File("");
    }

    @Override
    public void startSlaveMode() {
        throw new UnsupportedOperationException("unsupported");
    }

    @Override
    public void doCommandInSlaveMode(String oneArg) throws IOException {
        throw new UnsupportedOperationException("unsupported");
    }

    @Override
    public void endSlaveMode() {
        throw new UnsupportedOperationException("unsupported");
   }
}
