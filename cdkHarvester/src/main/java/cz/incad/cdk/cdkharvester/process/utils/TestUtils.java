package cz.incad.cdk.cdkharvester.process.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.kramerius.Import;

public class TestUtils {
	
	public static void main(String[] args) throws IOException {
		File f = new File("foxml-2.xml");
		System.out.println(f.exists());
		System.out.println(f.getAbsolutePath());
		FileInputStream fis = new FileInputStream(f);
		//uuid:9a37d4db-9f59-4bf4-88a1-4692d3f86aaf
		Import.initialize("fedoraAdmin", "fedoraAdmin");
        Import.ingest(fis, "uuid:9a37d4db-9f59-4bf4-88a1-4692d3f86aaf", null, null, false);
	}
}
