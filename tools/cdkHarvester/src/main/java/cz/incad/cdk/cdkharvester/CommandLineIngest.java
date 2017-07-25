package cz.incad.cdk.cdkharvester;

import cz.incad.cdk.cdkharvester.commands.SupportedCommands;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.kramerius.Import;

import java.io.IOException;

public class CommandLineIngest {

    public static void help() {
        System.out.println("cmdline <FEDORA|SOLR> <folder>");
    }
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            help();
            System.exit(-1);
        }
        Import.initialize(KConfiguration.getInstance().getProperty("ingest.user"),KConfiguration.getInstance().getProperty("ingest.password"));

        String[] nargs = new String[args.length-1];
        for (int i=1,ll=args.length;i<ll;i++) { nargs[i-1] = args[i]; }
        SupportedCommands.find(args[0]).doCommand(nargs);
    }
}
