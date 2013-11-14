/*
 * Copyright (C) 2013 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.cdk;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.TransformerException;

import org.apache.commons.codec.binary.Hex;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

public class RepairVCProcess {

	public static final SimpleDateFormat SDATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss");
	
	private static final String HASH = "SHA-1";
	public static final Logger LOGGER = Logger.getLogger(RepairVCProcess.class
			.getName());

	@Process
	public static void process() {
		try {
			FedoraAccessImpl fa = new FedoraAccessImpl(
					KConfiguration.getInstance(), null);
			String query = "*%20<fedora-model:hasModel>%20*";
			String ri = KConfiguration.getInstance().getProperty(
					"FedoraResourceIndex");
			String fuser = KConfiguration.getInstance().getProperty(
					"fedoraUser");
			String fpass = KConfiguration.getInstance().getProperty(
					"fedoraPass");

			LOGGER.info("requesting resource index");
			InputStream istream = RESTHelper
					.inputStream(
							ri
									+ "?type=triples&lang=spo&format=N-Triples&distinct=on&query="
									+ query, fuser, fpass);
			LOGGER.info("processing results");

			BufferedReader bReader = new BufferedReader(new InputStreamReader(
					istream));
			String line = null;
			while ((line = bReader.readLine()) != null) {
				StringTokenizer tokenizer = new StringTokenizer(line, " ,\t");
				if (tokenizer.hasMoreTokens()) {
					String nextToken = tokenizer.nextToken();
					nextToken = nextToken.trim().substring(1);
					nextToken = nextToken.substring(0,
							nextToken.lastIndexOf('>'));
					try {
						PIDParser parser = new PIDParser(nextToken);
						parser.disseminationURI();
						String pid = parser.getObjectPid();
						if (pid.startsWith("uuid")) {
							LOGGER.info("processing '" + pid + "'");
							Document relsExt = fa.getRelsExt(pid);
							checkRelsExt(pid, relsExt, fa);
						}
					} catch (Exception e) {
						e.printStackTrace();
						System.err.println("skipping " + nextToken);
					}
				}
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	public static void checkRelsExt(String pid, Document relsExt,
			FedoraAccess fa) {
		Element descElement = XMLUtils.findElement(
				relsExt.getDocumentElement(), "Description",
				FedoraNamespaces.RDF_NAMESPACE_URI);
		List<Element> delems = XMLUtils.getElements(descElement);
		for (Element rel : delems) {
			if (rel.getNamespaceURI() != null) {
				if (rel.getNamespaceURI().equals(
						FedoraNamespaces.RDF_NAMESPACE_URI)
						&& rel.getLocalName().equals("isMemberOfCollection")) {
					Attr resource = rel.getAttributeNodeNS(
							FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
					if (resource != null) {
						String value = resource.getValue();
						if (value.startsWith(PIDParser.INFO_FEDORA_PREFIX)) {
							try {
								PIDParser pars = new PIDParser(value);
								pars.disseminationURI();
							} catch (LexerException e) {
								LOGGER.log(Level.SEVERE, e.getMessage(), e);
								//repair(pid, relsExt, fa, resource, value);
							}
						} else {
							repair(pid, relsExt, fa, resource, value);
						}
					}
				}
			}
		}
	}

	private static void repair(String pid, Document relsExt, FedoraAccess fa,
			Attr resource, String value) {
		File backupFile = null;
		try {
			backupFile = backup(pid, relsExt);
			PIDParser parse = new PIDParser(value);
			parse.objectPid();
			resource.setValue(PIDParser.INFO_FEDORA_PREFIX
					+ parse.getObjectPid());
			uploadRELSEXT(pid, relsExt, fa);
			boolean deleted = backupFile.delete();
			LOGGER.info("backup file '"
					+ backupFile.getAbsolutePath() + "' "
					+ (deleted ? "deleted" : "not deleted"));
		} catch (LexerException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		} catch (TransformerException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	private static File backup(String pid, Document relsExt)
			throws TransformerException, IOException {
		LOGGER.info("backup file for pid '" + pid + "'");
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		XMLUtils.print(relsExt, bos);
		File backupFile = new File(pid);
		backupFile.createNewFile();
		IOUtils.copyStreams(new ByteArrayInputStream(bos.toByteArray()),
				new FileOutputStream(backupFile));
		return backupFile;
	}

	private static void uploadRELSEXT(String pid, Document relsExt,
			FedoraAccess fa) throws TransformerException,
			NoSuchAlgorithmException {
		LOGGER.info("changing RELS-EXT stream");
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		XMLUtils.print(relsExt, bos);
		byte[] message = bos.toByteArray();
//		doesnt work for XML -> custom serializer on fedora side
//		MessageDigest md5inst = MessageDigest.getInstance(HASH);
//		byte[] digest = md5inst.digest(message);
//		String encodeHexString = new String(Hex.encodeHex(digest));
		
		fa.getAPIM().modifyDatastreamByValue(pid, "RELS-EXT", null, "RELS-EXT",
				"application/rdf+xml", null, message, "DISABLED", null,
				"RepairVCProcess "+SDATE_FORMAT.format(new Date()), false);
	}
}
