package cz.incad.cdk.cdkharvester.commands;


import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by pstastny on 7/27/2017.
 */
public class IngestFedoraFolderFailoverCommand implements Command {

    public static final Logger LOGGER = Logger.getLogger(IngestFedoraFolderFailoverCommand.class.getName());



    private static  class Configuration {
        public static final int DEFAULT_NUMBER_OF_ITERATIONS = 2;

        private int iteration = DEFAULT_NUMBER_OF_ITERATIONS;
        private boolean delayEnabled = false;
        private int delayIteration = 0;
        private int delayTime = 0;

        public Configuration() {
            super();
        }

        public int getIteration() {
            return iteration;
        }

        public void setIteration(int iteration) {
            this.iteration = iteration;
        }

        public int getDelayIteration() {
            return delayIteration;
        }

        public void setDelayEnabled(boolean delayEnabled) {
            this.delayEnabled = delayEnabled;
        }

        public boolean getDelayEnabled() { return this.delayEnabled; }

        public int getDelayTime() {
            return delayTime;
        }

        public void setDelayIteration(int delayIteration) {
            this.delayIteration = delayIteration;
        }

        public void setDelayTime(int delayTime) {
            this.delayTime = delayTime;
        }
    }

    @Override
    public void doCommand(String[] args) throws IngestIOException {
        File folder = new File(args[0]);
        Configuration conf = new Configuration();
        if (args.length > 1) {
            LOGGER.info("threshold iteration "+args[1]);
            conf.setIteration(Integer.parseInt(args[1]));
        }
        if (args.length > 2) {
            LOGGER.info("delay is enabled: delay iteration  "+args[2]);
            conf.setDelayEnabled(true);
            conf.setDelayIteration(Integer.parseInt(args[2]));
        }

        if (args.length > 3) {
            LOGGER.info("delay is enabled: delay iteration  "+args[3]);
            conf.setDelayTime(Integer.parseInt(args[3]));
        }
        try {
            processFoxmlBatch(folder, null, conf);
        } catch (InterruptedException e) {
            throw new IngestIOException(e);
        } catch (IOException e) {
            throw new IngestIOException(e);
        }

    }

    protected void processFoxmlThreshold(String pid, File f, boolean delay, Configuration conf) throws InterruptedException, IngestIOException {
        if (pid != null && f != null) {
            int iterations = delay ? conf.getDelayIteration() : conf.getIteration();
            for (int i=0;i<iterations;i++) {
                try {
                    LOGGER.info("iteration number :"+i);
                    if (delay) {
                        LOGGER.info("Threshold; Waiting.... ");
                        int thresholdDelay = conf.getDelayTime();
                        Random r = new Random();
                        int randomInt = r.nextInt(thresholdDelay) + 1;
                        LOGGER.info("Waiting for "+randomInt+" ms");
                        Thread.sleep(randomInt);
                    }
                    doFileCommand(f);
                    LOGGER.info("SUCCESS");
                    return;
                } catch (IngestIOException e1) {
                    LOGGER.log(Level.SEVERE,e1.getMessage(),e1);
                }
            }
            if (!delay) {
                if (conf.getDelayEnabled()) {
                    processFoxmlThreshold(pid, f, true, conf);
                } else throw new IngestIOException(pid, f );
            } else {
                throw new IngestIOException(pid, f );
            }
        } else {
            LOGGER.log(Level.SEVERE, "no pid, no file");
        }
    }

    protected void processFoxmlBatch(File subFolder, File fromFile, Configuration conf) throws IOException, InterruptedException {
        try {
            if (fromFile != null) {
                doFolderCommand(subFolder,fromFile.getAbsoluteFile());
            } else {
                doFolderCommand(subFolder);
            }
        } catch (IngestIOException e) {
            // ingesting threshold
            String pid = e.getPid();
            File f = e.getIngestedFile();
            processFoxmlThreshold(pid, f, false, conf);
            processFoxmlBatch(subFolder, f, conf);
        }
    }

    protected void doFileCommand(File file) throws IngestIOException {
        SupportedCommands.FEDORA_ONE_FILE.doCommand(new String[] {file.getAbsolutePath()});
    }

    protected void doFolderCommand(File subFodler) throws IngestIOException {
        SupportedCommands.FEDORA.doCommand(new String[] {subFodler.getAbsolutePath()});
    }

    protected void doFolderCommand(File subFodler, File file) throws IngestIOException {
        SupportedCommands.FEDORA.doCommand(new String[] {subFodler.getAbsolutePath(), file.getAbsolutePath()});

    }
}
