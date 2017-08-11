package cz.incad.cdk.cdkharvester.commands;

import cz.incad.cdk.cdkharvester.CDKImportProcess;
import cz.incad.cdk.cdkharvester.ZipIteration;
import cz.incad.cdk.cdkharvester.process.ImageReplaceProcess;
import cz.incad.cdk.cdkharvester.utils.FilesUtils;
import cz.incad.kramerius.utils.IOUtils;
import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipInputStream;

public class IngestFedoraFolderCommandTest extends TestCase {

    private File tempDir = null;

    @Override
    protected void setUp() throws Exception {
        tempDir = FilesUtils.createTempDirectory("test");
        this.foxmlExpections(this.tempDir);
    }


    @Override
    protected void tearDown() throws Exception {
        FilesUtils.deleteFolder(this.tempDir);
    }

    public void testIngestFolderCommandTest() throws IOException {
        List<String> processedPids = new ArrayList<String>();
        IngestFedoraFolderCommand p = EasyMock.createMockBuilder(IngestFedoraFolderCommand.class)
                .withConstructor()
                .addMockedMethod("ingest")
                .createMock();

        p.ingest(EasyMock.<String>isA(String.class), EasyMock.<InputStream>isA(InputStream.class));
        EasyMock.expectLastCall().andDelegateTo(ingestFedorFolderDelegator(processedPids)).anyTimes();

        EasyMock.replay(p);

        p.doCommand(new String[] {this.tempDir.getAbsolutePath()});
        Assert.assertTrue(processedPids.size() == 71);
    }

    public void testSkipFolderCommandTest() throws IOException {
        List<String> processedPids = new ArrayList<String>();
        IngestFedoraFolderCommand p = EasyMock.createMockBuilder(IngestFedoraFolderCommand.class)
                .withConstructor()
                .addMockedMethod("ingest")
                .createMock();

        p.ingest(EasyMock.<String>isA(String.class), EasyMock.<InputStream>isA(InputStream.class));
        EasyMock.expectLastCall().andDelegateTo(ingestFedorFolderDelegator(processedPids)).anyTimes();

        EasyMock.replay(p);

        p.doCommand(new String[] {this.tempDir.getAbsolutePath(),this.tempDir.getAbsolutePath()+File.separator+"uuid_fc303a33-98eb-4d27-9d07-71fc1845b13d"});
        Assert.assertTrue(processedPids.size() == 1);
    }

    private IngestFedoraFolderCommand ingestFedorFolderDelegator(final List<String> list) {



        IngestFedoraFolderCommand delegator = new IngestFedoraFolderCommand() {
            @Override
            protected void ingest(String pid, InputStream is) throws IOException {
                list.add(pid);
            }

        };

        return delegator;
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

}