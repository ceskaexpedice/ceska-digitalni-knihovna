package cz.incad.cdk.cdkharvester;

/**
 *
 * @author Alberto Hernandez
 */
public class ProgramArguments {

    public String dir = null;
    public String dest = null;
    public String xsl = null;
    public String orig = null;
    public String from = null;
    public String q = null;
    public int start = 0; 
    public boolean fullIndex = false;
    //public boolean withBatchs = false;
    
    public int startNum = 1;
    public int endNum = 1;

    public ProgramArguments() {
    }

    public Boolean parse(String[] args) {
        try {
            int total = args.length;
            int i = 0;
            while (i < total) {
                if (args[i].equalsIgnoreCase("-dir")) {
                    i++;
                    dir = args[i];
                } else if (args[i].equalsIgnoreCase("-dest")) {
                    i++;
                    dest = args[i];
                } else if (args[i].equalsIgnoreCase("-xsl")) {
                    i++;
                    xsl = args[i];
                } else if (args[i].equalsIgnoreCase("-orig")) {
                    i++;
                    orig = args[i];
                } else if (args[i].equalsIgnoreCase("-from")) {
                    i++;
                    from = args[i];
                } else if (args[i].equalsIgnoreCase("-fullIndex")) {
                    fullIndex = true;
//                } else if (args[i].equalsIgnoreCase("-withBatchs")) {
//                    withBatchs = true;
                } else if (args[i].equalsIgnoreCase("-start")) {
                    i++;
                    start = Integer.parseInt(args[i]);
                } else if (args[i].equalsIgnoreCase("-startNum")) {
                    i++;
                    startNum = Integer.parseInt(args[i]);
                } else if (args[i].equalsIgnoreCase("-endNum")) {
                    i++;
                    endNum = Integer.parseInt(args[i]);
                }else if (args[i].equalsIgnoreCase("-q")) {
                    i++;
                    q = args[i];
                }

                i++;
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
