package cz.incad.kramerius.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.xml.xpath.XPathExpressionException;

import org.cas.lib.cdl.CachedAccessToDC;
import org.cas.lib.cdl.CachedAccessToMods;
import org.fedora.api.FedoraAPIA;
import org.fedora.api.FedoraAPIM;
import org.fedora.api.ObjectFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.StreamHeadersObserver;
import cz.incad.kramerius.TreeNodeProcessor;

public class OAIFedoraAccess implements FedoraAccess {

	private CachedAccessToMods chacedToMods;
	private CachedAccessToDC chacedToDC;
	
	
	
	public OAIFedoraAccess(CachedAccessToMods chacedToMods, CachedAccessToDC chacedToDC) {
		super();
		this.chacedToMods = chacedToMods;
		this.chacedToDC = chacedToDC;
	}

	@Override
	public String findFirstViewablePid(String arg0) throws IOException {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public FedoraAPIA getAPIA() {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public FedoraAPIM getAPIM() {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public Document getBiblioMods(String arg0) throws IOException {
		try {
			return this.chacedToMods.get(arg0);
		} catch (ExecutionException e) {
			throw new IOException(e);
		}
	}

	@Override
	public Document getDC(String arg0) throws IOException {
		try {
			return this.chacedToDC.get(arg0);
		} catch (ExecutionException e) {
			throw new IOException(e);
		}
	}
	
	

	@Override
	public boolean isObjectAvailable(String arg0) throws IOException {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public InputStream getDataStream(String arg0, String arg1) throws IOException {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public InputStream getDataStreamXml(String arg0, String arg1) throws IOException {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public Document getDataStreamXmlAsDocument(String arg0, String arg1) throws IOException {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public String getDonator(Document arg0) {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public String getDonator(String arg0) throws IOException {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public InputStream getFedoraDataStreamsList(String arg0) throws IOException {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public Document getFedoraDataStreamsListAsDocument(String arg0) throws IOException {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public String getFedoraVersion() throws IOException {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public boolean getFirstViewablePath(List<String> arg0, List<String> arg1) throws IOException {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public InputStream getFullThumbnail(String arg0) throws IOException {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public String getFullThumbnailMimeType(String arg0) throws IOException, XPathExpressionException {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public InputStream getImageFULL(String arg0) throws IOException {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public String getImageFULLMimeType(String arg0) throws IOException, XPathExpressionException {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public Document getImageFULLProfile(String arg0) throws IOException {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public String getKrameriusModelName(Document arg0) throws IOException {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public String getKrameriusModelName(String arg0) throws IOException {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public String getMimeTypeForStream(String arg0, String arg1) throws IOException {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public List<String> getModelsOfRel(Document arg0) {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public List<String> getModelsOfRel(String arg0) throws IOException {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public ObjectFactory getObjectFactory() {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public Document getObjectProfile(String arg0) throws IOException {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public List<Element> getPages(String arg0, boolean arg1) throws IOException {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public List<Element> getPages(String arg0, Element arg1) throws IOException {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public Set<String> getPids(String arg0) throws IOException {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public Document getRelsExt(String arg0) throws IOException {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public InputStream getSmallThumbnail(String arg0) throws IOException {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public String getSmallThumbnailMimeType(String arg0) throws IOException, XPathExpressionException {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public Document getSmallThumbnailProfile(String arg0) throws IOException {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public Document getStreamProfile(String arg0, String arg1) throws IOException {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public boolean isContentAccessible(String arg0) throws IOException {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public boolean isFullthumbnailAvailable(String arg0) throws IOException {
		throw new UnsupportedOperationException("this is unsupported");
	}

	@Override
	public boolean isImageFULLAvailable(String arg0) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isStreamAvailable(String arg0, String arg1) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void observeStreamHeaders(String arg0, String arg1, StreamHeadersObserver arg2) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processSubtree(String arg0, TreeNodeProcessor arg1) throws ProcessSubtreeException, IOException {
		// TODO Auto-generated method stub
		
	}

	
}
