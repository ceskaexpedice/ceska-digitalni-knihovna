package cz.incad.cdk.cdkharvester.utils;

import cz.incad.kramerius.Constants;
import cz.incad.kramerius.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * Created by pstastny on 7/25/2017.
 */
public class FilesUtils {

    public static final Logger LOGGER = Logger.getLogger(FilesUtils.class.getName());

    public static final String FOXML_FILES = "foxml";
    public static final String SOLRXML_FILES = "solrxml";


    public static void dumpXMLS(String harestName, String type, InputStream is,String pid) throws IOException {
        File batchFolders = batchFolders(harestName);
        File folder = new File(batchFolders, type);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String replaced = pid.replace(":", "_");
        if (replaced.contains("/@")) {
            replaced = replaced.replaceAll("/@", "-@");
        }

        File f = new File(folder, replaced);
        LOGGER.info("preparing file :"+f.getAbsolutePath());
        if (!f.exists()) {
            f.createNewFile();
        }
        IOUtils.saveToFile(is, f);
    }

    public static File xslsFolder() {
        String dirName = Constants.WORKING_DIR + File.separator + "cdk";
        File dir = new File(dirName);
        if (!dir.exists()) {
            boolean mkdirs = dir.mkdirs();
            if (!mkdirs) {
                throw new RuntimeException("cannot create dir '" + dir.getAbsolutePath() + "'");
            }
        }
        return dir;
    }


    public static File batchFolders(String name) {
        File bfolders = new File(xslsFolder().getAbsolutePath() + File.separator + name + "_batch");
        if (!bfolders.mkdirs()) {
            bfolders.mkdirs();
        }
        return bfolders;
    }

    public static String uuidFile(String name) {
        return xslsFolder().getAbsolutePath() + File.separator + name + ".uuid";
    }

    public static String updateFile(String name) {
        return xslsFolder().getAbsolutePath() + File.separator + name + ".time";
    }

    public static void deleteFolder(File subfolder) {
        File[] files = subfolder.listFiles();
        if (files != null) {
            for (int i=0,ll=files.length;i<ll;i++) {
                files[i].delete();
            }
        }
        subfolder.delete();
    }

    public static File createTempDirectory(String test) {
        File f = new File(System.getProperty("java.io.tmpdir"));
        String name = test+"_"+System.currentTimeMillis();
        File folder = new File(f, name);
        return folder;
    }
}
