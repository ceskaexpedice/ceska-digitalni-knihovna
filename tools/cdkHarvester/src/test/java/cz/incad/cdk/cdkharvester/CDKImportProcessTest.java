package cz.incad.cdk.cdkharvester;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.easymock.EasyMock;
import org.json.JSONObject;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import cz.incad.cdk.cdkharvester.foxmlprocess.ImageReplaceProcess;
import cz.incad.cdk.cdkharvester.iterator.CDKHarvestIteration;
import cz.incad.cdk.cdkharvester.iterator.CDKHarvestIterationItem;
import cz.incad.cdk.cdkharvester.iterator.StandardCDKHarvestIterationImpl;
import cz.incad.cdk.cdkharvester.timestamp.ProcessingTimestamps;
import cz.incad.cdk.cdkharvester.timestamp.ProcessingTimestampsSolrStoreImpl;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.impl.fedora.FedoraStreamUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Testing whole process; consider about the integration tests.. if they are possible
 * @author pstastny
 */
public class CDKImportProcessTest extends TestCase {

	private File tmpFolder;
	
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.tmpFolder = _tmpFolder();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		IOUtils.cleanDirectory(this.tmpFolder);
		tmpFolder.delete();
	}

	/**
	 * Testik whole import process
	 * 
	 * @throws Exception
	 */
	public void testWholeCDKProcess() throws Exception {
		CDKHarvestIteration iterator = iterator();

		ImageReplaceProcess imgProcess = EasyMock.createMockBuilder(ImageReplaceProcess.class)
				.withConstructor()
				.addMockedMethod("binaryContentStream")
				.createMock();
		// mocking replacing binary content stream
		imgProcess.binaryContentStream(EasyMock.<Document>isA(Document.class),EasyMock.<Element>isA(Element.class),EasyMock.<Element>isA(Element.class),EasyMock.<String>isA(String.class));
		EasyMock.expectLastCall().andDelegateTo(imageReplaceDelagator()).anyTimes();

		
		CDKSourceHarvestProcessImpl p = EasyMock.createMockBuilder(CDKSourceHarvestProcessImpl.class)
		        .withConstructor(Arrays.asList(imgProcess))
				.addMockedMethod("foxml")
				.addMockedMethod("solrxml")
				.addMockedMethod("rawIngest")
				.addMockedMethod("postToIndex")
				.addMockedMethod("findDocFromCurrentIndex")
				.addMockedMethod("getCollectionPid")
				.addMockedMethod("getSolrSelectEndpoint")
				
				.createMock();

		
		ProcessingTimestampsSolrStoreImpl processingTimestamps = EasyMock.createMockBuilder(ProcessingTimestampsSolrStoreImpl.class)
				.withConstructor()
				.addMockedMethod("getTimestamp")
				.addMockedMethod("setTimestamp")
				.createMock();

		
		
		p.rawIngest(EasyMock.<String>isA(String.class),EasyMock.<InputStream>isA(InputStream.class));
		EasyMock.expectLastCall().andDelegateTo(cdkProcessDelegator()).anyTimes();

		//EasyMock.expect(processingTimestamps.getTimestamp("vc:test_collection")).andReturn(processingTimestamps.parse("1900-01-01T00:00:00.002Z")).anyTimes();

		processingTimestamps.getTimestamp(EasyMock.anyObject(String.class));
		ProcessingTimestampsSolrStoreImpl timestampDelegator = processingTimestampsDelegator();
		EasyMock.expectLastCall().andDelegateTo(timestampDelegator).anyTimes();

		processingTimestamps.setTimestamp(EasyMock.anyObject(String.class), EasyMock.anyObject(LocalDateTime.class));
		EasyMock.expectLastCall().andDelegateTo(timestampDelegator).anyTimes();

		p.postToIndex(EasyMock.<String>isA(String.class));
		EasyMock.expectLastCall().andDelegateTo(cdkProcessDelegator()).anyTimes();

		emptySolrResult(p);
		solrxmlExpections(p,"http://localhost:8080/search","vc:test_collection");
		foxmlExpections(p, "http://localhost:8080/search","vc:test_collection");
		
		
		// return collection
		EasyMock.expect(p.getCollectionPid()).andReturn("vc:test_collection").anyTimes();
		// return select endpoint
		EasyMock.expect(p.getSolrSelectEndpoint()).andReturn(this.tmpFolder.toURI().toURL().toString()).anyTimes();
		
		
		EasyMock.replay(iterator, p, imgProcess, processingTimestamps);

		p.initVariables("http://localhost:8080/search", "name", "vc:test_collection", "krameriusAdmin",
				"krameriusAdmin");
		p.initTransformations();
		// set for test variable
		p.getTransformer().setParameter("_for_tests", true);
		
		p.process("vc:test_collection", iterator, processingTimestamps);
		//p.processDocs("1900-01-01T00:00:00.002Z","vc:test_collection",processingTimestamps);
	}

	private ProcessingTimestampsSolrStoreImpl processingTimestampsDelegator() {
		ProcessingTimestampsSolrStoreImpl delegator = new ProcessingTimestampsSolrStoreImpl() {
			Map<String, LocalDateTime> map = new HashMap<>();
			@Override
			public void setTimestamp(String pid, LocalDateTime date) throws IOException {
				map.put(pid, date);
			}
			
			@Override
			public LocalDateTime getTimestamp(String pid) throws IOException {
				return map.get(pid);
			}
		};
		return delegator;
	}
	
	// delegated and processed 
	private AbstractCDKSourceHarvestProcess cdkProcessDelegator() throws IOException {
		AbstractCDKSourceHarvestProcess delegator = new CDKSourceHarvestProcessImpl() {
			@Override
			protected void rawIngest(String pid, InputStream processingStream) throws IOException {
				try {
					Document parseDocument = XMLUtils.parseDocument(processingStream,true);
					List<Element> elements = XMLUtils.getElements(parseDocument.getDocumentElement(),new XMLUtils.ElementsFilter() {
						@Override
						public boolean acceptElement(Element element) {
							String localName = element.getLocalName();
							String streamName = element.getAttribute("ID");
							if (localName.equals("datastream") && streamName.equals("RELS-EXT")) {
								Element foundElement = XMLUtils.findElement(element, new XMLUtils.ElementsFilter() {
									@Override
									public boolean acceptElement(Element element) {
										String localName = element.getLocalName();
										return localName.equals("hasModel");
									}
								});
								if (foundElement != null) {
									Attr attrNs = foundElement.getAttributeNodeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
									if (attrNs !=  null) {
										String val = attrNs.getValue();
										return val != null && val.trim().equals("info:fedora/model:page");
									}
								}
							}
							return false;
						}
					});
					if (!elements.isEmpty()) {
						final List<String> streamNames = new ArrayList<String>();
						XMLUtils.getElements(parseDocument.getDocumentElement(),new XMLUtils.ElementsFilter() {
							@Override
							public boolean acceptElement(Element element) {
								String localName = element.getLocalName();
								String streamName = element.getAttribute("ID");
								if (localName.equals("datastream")) {
									streamNames.add(streamName);
								}
								return false;
							}
						});

						Assert.assertTrue(streamNames.contains("IMG_FULL"));
						Assert.assertTrue(streamNames.contains("IMG_THUMB"));
					}
					
				} catch (ParserConfigurationException e) {
					Assert.fail(e.getMessage());
				} catch (SAXException e) {
					Assert.fail(e.getMessage());
				}
			}

			
			public void postToIndex(String xmlcont) throws CDKReplicationException {
				System.out.println("Posting to index");
			}
			
		};
		return delegator;
	}
	
	private ImageReplaceProcess imageReplaceDelagator() {
		ImageReplaceProcess delegator = new ImageReplaceProcess() {
			
			@Override
			public byte[] process(String url, String pid, InputStream is) throws Exception {
				throw new UnsupportedOperationException("this is unsupported");
			}

			@Override
			public void binaryContentStream(Document document, Element datStreamElm, Element version, String imgUrl)
					throws IOException, MalformedURLException {
				URL url = new URL(imgUrl);
				String query = url.getQuery();
				StringTokenizer tokenizer = new StringTokenizer(query, "&");
				while(tokenizer.hasMoreTokens()) {
					String token = tokenizer.nextToken();
					if (token.startsWith("action")) {
						Assert.assertEquals(token, "action=GETRAW");
					}
					if (token.startsWith("stream")) {
						Assert.assertEquals(token, "stream=IMG_THUMB");
					}
				}
				
				Assert.assertTrue(url.getHost().equals("localhost"));
				Assert.assertTrue(url.getProtocol().equals("http"));
				Assert.assertTrue(url.getPort() == 8080);
				
			}
		};
		return delegator;
	}
	
	

	public CDKHarvestIteration iterator() throws Exception {
		StandardCDKHarvestIterationImpl iterator = EasyMock.createMockBuilder(StandardCDKHarvestIterationImpl.class)
			.withConstructor("1900-01-01T00:00:00.002Z","http://localhost:8080/search","krameriusAdmin","krameriusAdmin")
        	.addMockedMethod("solrResults")
        	.createMock();
		
        EasyMock.expect(iterator.solrResults("http://localhost:8080/search/api/v4.6/cdk/prepare?rows=500&date=1900-01-01T00:00:00.002Z")).andReturn(XMLUtils.parseDocument(StandardCDKHarvestIterationImplTest.class.getResourceAsStream("pidsretrieve1.xml"))).anyTimes();
        EasyMock.expect(iterator.solrResults("http://localhost:8080/search/api/v4.6/cdk/prepare?rows=500&date=2016-10-13T12:43:03.105Z")).andReturn(XMLUtils.parseDocument(StandardCDKHarvestIterationImplTest.class.getResourceAsStream("pidsretrieve2.xml"))).anyTimes();
        EasyMock.expect(iterator.solrResults("http://localhost:8080/search/api/v4.6/cdk/prepare?rows=500&date=2016-10-23T18:43:51.311Z")).andReturn(XMLUtils.parseDocument(StandardCDKHarvestIterationImplTest.class.getResourceAsStream("pidsretrieve3.xml"))).anyTimes();
        EasyMock.expect(iterator.solrResults("http://localhost:8080/search/api/v4.6/cdk/prepare?rows=500&date=2016-12-15T12:24:03.128Z")).andReturn(XMLUtils.parseDocument(StandardCDKHarvestIterationImplTest.class.getResourceAsStream("pidsretrieve4.xml"))).anyTimes();
        
        return iterator;
	}



	
	public File _tmpFolder() throws IOException {
		final File baseDir = new File(System.getProperty("java.io.tmpdir"));
		final File tmpDir = new File(baseDir,"_solr");
		tmpDir.mkdirs();
		
		
		ZipIteration iteration = new ZipIteration();
		iteration.iterateSOLRXML(new ZipIteration.ZipIterationCall() {
			@Override
			public void onIterate(String name, String pid, ZipInputStream zipStream) throws IOException {
				String changedPid = pid.replace(':','_');
				File f = new File(tmpDir, changedPid);
				f.createNewFile();
		    	
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				IOUtils.copyStreams(zipStream, bos);

				IOUtils.saveToFile(bos.toByteArray(), f);
			}
		});
		
		return tmpDir;
		
	}

	
	public void emptySolrResult(final AbstractCDKSourceHarvestProcess p) throws IOException, URISyntaxException {
		InputStream jsonStream = ZipIteration.class.getResourceAsStream("emptysolr.json");
		final JSONObject obj = new JSONObject(IOUtils.readAsString(jsonStream, Charset.forName("UTF-8"), true));
		ZipIteration iteration = new ZipIteration();
		iteration.iterateSOLRXML(new ZipIteration.ZipIterationCall() {
			@Override
			public void onIterate(String name, String pid, ZipInputStream zipStream) throws IOException {
				try {
					EasyMock.expect(p.findDocFromCurrentIndex(pid)).andReturn(obj).anyTimes();
				} catch (URISyntaxException e) {
					throw new IOException(e);
				}
			}
		});
	}
	
	public void solrxmlExpections(final AbstractCDKSourceHarvestProcess p, final String url, final String collectionPid) throws IOException {
		ZipIteration iteration = new ZipIteration();
		iteration.iterateSOLRXML(new ZipIteration.ZipIterationCall() {
			@Override
			public void onIterate(String name, String pid, ZipInputStream zipStream) throws IOException {
		    	String collected = url + "/api/v4.6/cdk/" + pid + "/solrxml";
		    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
				IOUtils.copyStreams(zipStream, bos);
				EasyMock.expect(p.solrxml(collected)).andReturn(new ByteArrayInputStream(bos.toByteArray())).anyTimes();
			}
		});
		
	}
	public void foxmlExpections(final AbstractCDKSourceHarvestProcess p, final String url, final String collectionPid) throws IOException {
		ZipIteration iteration = new ZipIteration();
		iteration.iterateFOXML(new ZipIteration.ZipIterationCall() {

			@Override
			public void onIterate(String name, String pid, ZipInputStream zipStream) throws IOException {
		    	String collected = url + "/api/v4.6/cdk/" + pid + "/foxml?collection=" + collectionPid;
		    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
				IOUtils.copyStreams(zipStream, bos);
				EasyMock.expect(p.foxml(pid, collected)).andReturn(new ByteArrayInputStream(bos.toByteArray())).anyTimes();
			}
		});
	}

}
