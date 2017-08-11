package cz.incad.cdk.cdkharvester.commands;

import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pstastny on 7/27/2017.
 */
public class FailingIngestFedoraCommandFourDelayIterationsTest2 extends  AbstractFailingIngestFedoraCommandTest {

    public void testFourIterations() throws IOException, InterruptedException {
        super.runCommandIngest();
    }

    @Override
    public int getThresholdIteration() {
        return 2;
    }

    @Override
    public int getDelayedThresholdIteration() {
        return 4;
    }

    @Override
    public int getDelayedThresholdTime() {
        return 1000;
    }

    private IngestFedoraFolderFailoverCommand _delegatorInstance = null;
    @Override
    public IngestFedoraFolderFailoverCommand delegator(File tempDir) {
        if (_delegatorInstance ==  null) {
            _delegatorInstance = new IngestFedoraFolderFailoverCommand() {

                Map<String, Integer> standard = new HashMap<String, Integer>();
                Map<String, Integer> delayed = new HashMap<String, Integer>();
                int counter = 0;

                @Override
                protected void doFileCommand(File file) throws IngestIOException {
                    Assert.assertTrue(file.getName().equals("uuid_f19ec730-0681-11e4-a798-001b63bd97ba"));
                    counter++;
                    if (counter < 10) {
                        exception(file.getParentFile());
                    }
                }

                @Override
                protected void doFolderCommand(File subFodler) throws IngestIOException {
                    exception(subFodler);
                    return;
                }



                @Override
                protected void doFolderCommand(File subFodler, File file) throws IngestIOException {
                    Assert.assertTrue(file.getName().equals("uuid_f19ec730-0681-11e4-a798-001b63bd97ba"));
                    Assert.assertTrue(counter == 5);
                }

                private void increment(Map<String, Integer> map, String key) {
                    if (!map.containsKey(key)) {
                        map.put(key,0);
                    }
                    Integer integer = map.get(key);
                    map.put(key, integer+1);
                }

                private int get(Map<String, Integer> map, String key) {
                    if (map.containsKey(key)) {
                        return map.get(key);
                    } else return 0;
                }


                private void exception(File subFodler) throws IngestIOException {
                    RuntimeException ex = new RuntimeException("something is bad");
                    File f = new File(subFodler,"uuid_f19ec730-0681-11e4-a798-001b63bd97ba");
                    throw new IngestIOException("uuid:f19ec730-0681-11e4-a798-001b63bd97ba", f, ex);
                }
            };

        }
        return _delegatorInstance;
    }
}
