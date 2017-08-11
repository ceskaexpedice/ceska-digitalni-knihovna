package cz.incad.cdk.cdkharvester.commands;

import cz.incad.cdk.cdkharvester.ZipIteration;
import cz.incad.cdk.cdkharvester.utils.FilesUtils;
import cz.incad.kramerius.utils.IOUtils;
import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;

/**
 * Created by pstastny on 7/27/2017.
 */
public abstract class AbstractFailingIngestFedoraCommandTest extends TestCase {
    protected File tempDir = null;

    @Override
    protected void setUp() throws Exception {
        tempDir = FilesUtils.createTempDirectory("test");
        this.foxmlExpections(this.tempDir);
    }

    @Override
    protected void tearDown() throws Exception {
        FilesUtils.deleteFolder(this.tempDir);
    }

    public void foxmlExpections(final File folder) throws IOException {
        ZipIteration iteration = new ZipIteration();
        iteration.iterate_command_ingest_FOXML(new ZipIteration.ZipIterationCall() {
            @Override
            public void onIterate(String name, String pid, ZipInputStream zipStream) throws IOException {
                File f = new File(folder, pid.replace(":","_"));
                IOUtils.saveToFile(zipStream,f);
            }
        });
    }

    public void runCommandIngest() throws IOException, InterruptedException {

        IngestFedoraFolderFailoverCommand folder = EasyMock.createMockBuilder(IngestFedoraFolderFailoverCommand.class)
                .withConstructor()
                .addMockedMethod("doFileCommand")
                .addMockedMethod("doFolderCommand", File.class)
                .addMockedMethod("doFolderCommand", File.class, File.class)
                .createMock();

        folder.doFileCommand(EasyMock.<File>isA(File.class));
        EasyMock.expectLastCall().andDelegateTo(delegator(this.tempDir)).anyTimes();

        folder.doFolderCommand(EasyMock.<File>isA(File.class));
        EasyMock.expectLastCall().andDelegateTo(delegator(this.tempDir)).anyTimes();

        folder.doFolderCommand(EasyMock.<File>isA(File.class),EasyMock.<File>isA(File.class));
        EasyMock.expectLastCall().andDelegateTo(delegator(this.tempDir)).anyTimes();


        EasyMock.replay(folder);

        List<String> params = new ArrayList<String>();
        params.add(this.tempDir.getAbsolutePath());

        if (getThresholdIteration() > 0) {
            params.add(""+getThresholdIteration());
            if (getDelayedThresholdIteration() > 0) {
                params.add(""+getDelayedThresholdIteration());
                if (getDelayedThresholdTime() > 0) {
                    params.add(""+getDelayedThresholdTime());
                }
            }
        }

        folder.doCommand(params.toArray(new String[params.size()]));
    }

    public abstract int getThresholdIteration();

    public abstract int getDelayedThresholdIteration();

    public abstract int getDelayedThresholdTime();


    public abstract IngestFedoraFolderFailoverCommand delegator(final File tempDir);

}
