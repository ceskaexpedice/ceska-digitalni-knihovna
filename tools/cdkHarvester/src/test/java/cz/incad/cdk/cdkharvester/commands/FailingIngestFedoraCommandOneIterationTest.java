package cz.incad.cdk.cdkharvester.commands;

import cz.incad.cdk.cdkharvester.ZipIteration;
import cz.incad.cdk.cdkharvester.utils.FilesUtils;
import cz.incad.kramerius.utils.IOUtils;
import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.zip.ZipInputStream;

/**
 * Created by pstastny on 7/27/2017.
 */
public class FailingIngestFedoraCommandOneIterationTest extends AbstractFailingIngestFedoraCommandTest {



    public void testOneIteration() throws IOException, InterruptedException {
        super.runCommandIngest();
    }


    public int getThresholdIteration() {
        return 1;
    }

    public int getDelayedThresholdIteration() {
        return 0;
    }

    public int getDelayedThresholdTime() {
        return 0;
    }


    public IngestFedoraFolderFailoverCommand delegator(final File tempDir) {
        IngestFedoraFolderFailoverCommand command = new IngestFedoraFolderFailoverCommand() {
            @Override
            protected void doFileCommand(File file) throws IngestIOException {
                Assert.assertTrue(file.getName().equals("uuid_f19ec730-0681-11e4-a798-001b63bd97ba"));
            }

            @Override
            protected void doFolderCommand(File subFodler) throws IngestIOException {
                RuntimeException ex = new RuntimeException("something is bad");
                File f = new File(subFodler,"uuid_f19ec730-0681-11e4-a798-001b63bd97ba");
                throw new IngestIOException("uuid:f19ec730-0681-11e4-a798-001b63bd97ba", f, ex);
            }

            @Override
            protected void doFolderCommand(File subFodler, File file) throws IngestIOException {
                Assert.assertTrue(file.getName().equals("uuid_f19ec730-0681-11e4-a798-001b63bd97ba"));
            }
        };
        return command;
    }


}
