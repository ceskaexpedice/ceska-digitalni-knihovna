package cz.incad.cdk.cdkharvester.postponed;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;

public class PostponedItemsListImpl implements PostponedItemsList {

    public static final String DEFAULT_POSPONED_FILE = "postponedlist_";
    private File postponeFile = null;
    private int counter = 0;

    public PostponedItemsListImpl() throws IOException  {
        UUID randomUUID = UUID.randomUUID();
        this.postponeFile = new File(System.getProperty("user.dir")+File.separator+DEFAULT_POSPONED_FILE+randomUUID.toString()+".txt");
        this.counter  = 0 ;
    }

    public PostponedItemsListImpl(File f) throws IOException  {
        this.postponeFile = f;
        this.counter  = 0 ;
    }

    public File getPostponeFile() {
        return postponeFile;
    }

    @Override
    public int getCount() {
        return this.counter;
    }

    @Override
    public synchronized void postpone(String pid) throws IOException {
        if (!this.postponeFile.exists()) { this.postponeFile.createNewFile(); }
        RandomAccessFile randomAccessFile = new RandomAccessFile(this.postponeFile, "rw");
        randomAccessFile.seek(randomAccessFile.length());
        randomAccessFile.write((pid+"\n").getBytes("UTF-8"));
        randomAccessFile.close();
        this.counter += 1;
    }
}
