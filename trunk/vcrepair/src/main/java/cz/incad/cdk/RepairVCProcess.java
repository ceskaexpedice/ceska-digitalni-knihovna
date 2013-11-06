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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.pdf.Break;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.resourceindex.IResourceIndex;
import cz.incad.kramerius.resourceindex.MPTStoreService;
import cz.incad.kramerius.resourceindex.ResourceIndexService;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.PIDParser;

public class RepairVCProcess {

	public static final Logger LOGGER = Logger.getLogger(RepairVCProcess.class.getName());
	
	@Process
	public static void process() {
		try {
			FedoraAccessImpl fa = new FedoraAccessImpl(KConfiguration.getInstance(), null);
			String query = "*%20<fedora-model:hasModel>%20*";
			String ri = KConfiguration.getInstance().getProperty("FedoraResourceIndex");
			String fuser = KConfiguration.getInstance().getProperty("fedoraUser");
			String fpass = KConfiguration.getInstance().getProperty("fedoraPass");
			
			LOGGER.info("requesting resource index");
			InputStream istream = RESTHelper.inputStream(ri+"?type=triples&lang=spo&format=N-Triples&distinct=on&query="+query, fuser, fpass);
			LOGGER.info("processing results");
			
			BufferedReader bReader = new BufferedReader(new InputStreamReader(istream));
			String line = null;
			while((line = bReader.readLine()) != null) {
				StringTokenizer tokenizer = new StringTokenizer(line," ,\t");
				if (tokenizer.hasMoreTokens()) {
					String nextToken = tokenizer.nextToken();
					LOGGER.info("token '"+nextToken+"'");
					nextToken = nextToken.trim().substring(1);
					nextToken = nextToken.substring(0, nextToken.lastIndexOf('>'));
					try {
						PIDParser parser = new PIDParser(nextToken);
						parser.disseminationURI();
						String pid = parser.getObjectPid();
						if (pid.startsWith("uuid")) {
							Document relsExt = fa.getRelsExt(pid);
							checkRelsExt(pid, relsExt);
						}
					} catch (Exception e) {
						e.printStackTrace();
						System.err.println("skipping "+nextToken);
					}
				}
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(),e);
		}
	}
	
	public static void checkRelsExt(String pid, Document relsExt) {
		//<rdf:isMemberOfCollection
		Element collectionElm = XMLUtils.findElement(relsExt.getDocumentElement(), "isMemberOfCollection", FedoraNamespaces.RDF_NAMESPACE_URI);
		if (collectionElm!= null) {
			if (collectionElm.hasAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "isMemberOfCollection")) {
				String attr = collectionElm.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "isMemberOfCollection");
				LOGGER.info("pid contains collection "+attr);
			}
		}
	}
}
