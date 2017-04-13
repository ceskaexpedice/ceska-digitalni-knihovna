/*
 * Copyright (C) 2016 Pavel Stastny
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

package cz.incad.cdk.cdkharvester.foxmlprocess;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.XMLUtils;

/**
 * @author pavels
 *
 */
public class ImageReplaceProcess implements ProcessFOXML {

	public static Logger LOGGER = Logger.getLogger(ImageReplaceProcess.class.getName());

	/** Binary replaced datastram */
	public static final String[] REPLACE_DATASTREAM = { "IMG_THUMB" };

	public static final String[] REDIRECTED_DATASTREAM = { FedoraUtils.IMG_FULL_STREAM, FedoraUtils.IMG_PREVIEW_STREAM,
			// media files
			FedoraUtils.OGG_STREAM, FedoraUtils.MP3_STREAM, FedoraUtils.WAV_STREAM };

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cz.incad.cdk.cdkharvester.process.ProcessFOXML#process(java.lang.String,
	 * java.lang.String, java.io.InputStream)
	 */
	@Override
	public byte[] process(final String url, final String pid, InputStream is) throws Exception {
		if (is == null)
			return null;
		Document document = XMLUtils.parseDocument(is, true);
		Element docElement = document.getDocumentElement();
		if (docElement.getLocalName().equals("digitalObject")) {
			// not managed streams and should be binary
			List<Element> binaryDatastreams = XMLUtils.getElements(docElement, new XMLUtils.ElementsFilter() {

				@Override
				public boolean acceptElement(Element elm) {
					String elmName = elm.getLocalName();
					String idName = elm.getAttribute("ID");
					boolean idContains = Arrays.asList(REPLACE_DATASTREAM).contains(idName);
					return elmName.equals("datastream") && idContains && elm.hasAttribute("CONTROL_GROUP")
							&& (!elm.getAttribute("CONTROL_GROUP").equals("M"));
				}
			});
			// replacing binary content, download from server and replace it
			for (final Element datStreamElm : binaryDatastreams) {

				List<Element> versions = XMLUtils.getElements(datStreamElm, new XMLUtils.ElementsFilter() {

					@Override
					public boolean acceptElement(Element element) {
						String locName = element.getLocalName();
						return locName.endsWith("datastreamVersion");
					}
				});

				for (Element version : versions) {
					Element found = XMLUtils.findElement(version, "contentLocation", version.getNamespaceURI());
					if (found != null) {
						String idAttr = datStreamElm.getAttribute("ID");
						String imgUrl = url + "/img?uuid=" + pid + "&action=GETRAW&stream=" + idAttr;
						try {
							binaryContentStream(document, datStreamElm, version, imgUrl);
						} catch (Exception e) {
							// something happend; must continue
							LOGGER.log(Level.SEVERE, e.getMessage(), e);
						}
					}
				}

			}

			// not
			List<Element> referencedDatastreams = XMLUtils.getElements(docElement, new XMLUtils.ElementsFilter() {

				@Override
				public boolean acceptElement(Element elm) {
					String elmName = elm.getLocalName();
					String idName = elm.getAttribute("ID");
					boolean idContains = Arrays.asList(REDIRECTED_DATASTREAM).contains(idName);
					return elmName.equals("datastream") && idContains;
				}
			});

			for (final Element datStreamElm : referencedDatastreams) {

				List<Element> versions = XMLUtils.getElements(datStreamElm, new XMLUtils.ElementsFilter() {

					@Override
					public boolean acceptElement(Element element) {
						String locName = element.getLocalName();
						return locName.endsWith("datastreamVersion");
					}
				});

				for (Element version : versions) {
					Element found = XMLUtils.findElement(version, "contentLocation", version.getNamespaceURI());
					if (found != null) {
						String idAttr = datStreamElm.getAttribute("ID");
						String refAttr = found.getAttribute("REF");

						String expectingImg = url + "/img?uuid=" + pid + "&action=GETRAW&stream=" + idAttr;

						URL expectingImgURL = new URL(expectingImg);
						URL foxmlURL = new URL(refAttr);
						if (!expectingImgURL.equals(foxmlURL)) {
							// must change
							try {
								referenceStream(document, datStreamElm, version, expectingImgURL);
							} catch (Exception e) {
								// something happend; must continue
								LOGGER.log(Level.SEVERE, e.getMessage(), e);
							}
						}
					}
				}

			}
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		XMLUtils.print(document, bos);
		return bos.toByteArray();
	}

	public void referenceStream(Document document, final Element datStreamElm, Element version, URL expectingImgURL)
			throws DOMException, IOException, URISyntaxException {
		ReplicationUtils.referenceForStream(document, datStreamElm, version, expectingImgURL);
	}

	public void binaryContentStream(Document document, final Element datStreamElm, Element version, String imgUrl)
			throws IOException, MalformedURLException {
		ReplicationUtils.binaryContentForStream(document, datStreamElm, version, new URL(imgUrl));
	}
}
