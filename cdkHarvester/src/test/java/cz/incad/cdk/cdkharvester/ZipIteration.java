package cz.incad.cdk.cdkharvester;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.easymock.EasyMock;

import cz.incad.kramerius.utils.IOUtils;
import junit.framework.TestCase;

public class ZipIteration  {
	
	public static interface ZipIterationCall {
		public void onIterate(String name, String pid, ZipInputStream stream) throws IOException;
	}
	
	protected static interface PidDisect {
		public String disect(String name);
	}

	protected static class FOXMLPID implements PidDisect {
		@Override
		public String disect(String name) {
			return name.substring("foxml/".length(),name.length()-".foxml".length()).replace('_', ':');
		}
	}
	
	protected static class SOLRXMLPID implements PidDisect {

		@Override
		public String disect(String name) {
			return name.substring("solrxml/".length(),name.length()-".foxml".length()).replace('_', ':');
		}
		
	}
	
	public  void iterateSOLRXML(ZipIterationCall call) throws IOException {
		InputStream resourceAsStream = ZipIteration.class.getResourceAsStream("solrxml.zip");
		iterate(call, resourceAsStream, new SOLRXMLPID());
	}
	
	public void iterateFOXML(ZipIterationCall call) throws IOException {
		InputStream resourceAsStream = ZipIteration.class.getResourceAsStream("foxml.zip");
		iterate(call, resourceAsStream, new FOXMLPID());
	}

	private void iterate(ZipIterationCall call, InputStream resourceAsStream,PidDisect disect ) throws IOException {
		ZipInputStream zipStream = new ZipInputStream(resourceAsStream);
		ZipEntry entry;
		while ((entry = zipStream.getNextEntry()) != null) {
			if (entry.isDirectory()) continue;
			String pid = disect.disect(entry.getName());
			call.onIterate(entry.getName(), pid, zipStream);
		}
	}

	private InputStream getStream(String pid, InputStream resourceAsStream,PidDisect disect ) throws IOException {
		ZipInputStream zipStream = new ZipInputStream(resourceAsStream);
		ZipEntry entry;
		while ((entry = zipStream.getNextEntry()) != null) {
			if (entry.isDirectory()) continue;
			String expectedPid = disect.disect(entry.getName());
			if (expectedPid.equals(pid)) return zipStream;
		}
		return null;
	}

	public InputStream getSOLRXML(String pid) throws IOException {
		InputStream resourceAsStream = ZipIteration.class.getResourceAsStream("solrxml.zip");
		return getStream(pid, resourceAsStream, new SOLRXMLPID());
	}

	public InputStream getFOXML(String pid) throws IOException {
		InputStream resourceAsStream = ZipIteration.class.getResourceAsStream("foxml.zip");
		return getStream(pid, resourceAsStream, new FOXMLPID());
	}

}
