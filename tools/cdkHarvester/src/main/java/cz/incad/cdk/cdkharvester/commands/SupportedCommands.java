package cz.incad.cdk.cdkharvester.commands;

import java.io.FileNotFoundException;
import java.io.IOException;

public enum SupportedCommands {

    FEDORA(new IngestFedoraCommand()),
    SOLR(new IngestSolrCommand());

    public void doCommand(String[] args) throws IOException {
        this.command.doCommand(args);
    }

    SupportedCommands(Command command) {
        this.command = command;
    }

    public static SupportedCommands find(String name) {
        SupportedCommands[] vals = values();
        for (SupportedCommands cmd: vals) {
            if (cmd.name().equals(name)) {
                return cmd;
            }
        }
        throw new IllegalArgumentException("cannot find action :"+name);
    }

    private Command command;
}
