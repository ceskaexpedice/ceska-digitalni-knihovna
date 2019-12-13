package cz.incad.cdk.cdkharvester.postponed;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import cz.incad.cdk.cdkharvester.postponed.PostponedItemsListImpl;
import junit.framework.Assert;
import junit.framework.TestCase;

public class PostponedItemsListImplTest extends TestCase {

    public void testList2() throws IOException {
        PostponedItemsListImpl impl = new PostponedItemsListImpl();
        File postponeFile = impl.getPostponeFile();
        Assert.assertTrue(postponeFile != null);
    }

    public void testList() throws IOException {
        File tempFile = File.createTempFile("test", "list", new File(System.getProperty("user.dir")));
        PostponedItemsListImpl impl = new PostponedItemsListImpl(tempFile);
        Assert.assertTrue(impl.getCount() == 0);
        impl.postpone("uuid:first");
        Assert.assertTrue(impl.getCount() == 1);
        impl.postpone("uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22");
        Assert.assertTrue(impl.getCount() == 2);
        impl.postpone("uuid:second");
        Assert.assertTrue(impl.getCount() == 3);

        FileReader freader = new FileReader(tempFile);
        BufferedReader bufReader = new BufferedReader(freader);
        Assert.assertEquals("uuid:first", bufReader.readLine());
        Assert.assertEquals("uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22", bufReader.readLine());
        Assert.assertEquals("uuid:second", bufReader.readLine());

        bufReader.close();
    }
}
