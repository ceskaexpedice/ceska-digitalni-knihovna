package cz.incad.cdk.cdkharvester.commands;

import java.io.IOException;

public enum SupportedCommands {

    FEDORA(new IngestFedoraFolderCommand()),
    FEDORA_THRESHOLD(new IngestFedoraFolderFailoverCommand()),
    FEDORA_ONE_FILE(new IngestFedoraFileCommand()),

    SOLR(new IngestSolrFolderCommand());


    public void doCommand(String[] args) throws IngestIOException {
        this.command.doCommand(args);
    }


    SupportedCommands(Command command) {
        this.command = command;
    }

    public Command getCommand() {
        return command;
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
