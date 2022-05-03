package org.cas.lib.cdl;

import static org.cas.lib.cdl.Utils.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.cas.lib.cdl.PeriodicalProvideServlet.Actions;
import org.json.JSONObject;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

import cz.cas.lib.knav.ApplyMWUtils;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.utils.OAIFedoraAccess;
import cz.incad.kramerius.utils.OAIMWUtils;
import cz.incad.kramerius.utils.OAISolrAccess;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.json.JSONArray;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import java.util.Map;
import java.util.HashMap;

import javax.imageio.ImageIO;
import java.net.URL;
import java.awt.Image;
import org.json.JSONException;
import com.neovisionaries.i18n.*;
import java.io.UnsupportedEncodingException;
import javax.swing.JOptionPane; 

public class PeriodicalProvideServlet extends HttpServlet {

        
	public static final Logger LOGGER = Logger.getLogger(PeriodicalProvideServlet.class.getName());

	public static final String DC_STREAM_LOCATION = "https://cdk.lib.cas.cz/search/api/v5.0/item/%s/streams/DC";
	public static final String BIBLIO_MODS_STREAM_LOCATION = "https://cdk.lib.cas.cz/search/api/v5.0/item/%s/streams/BIBLIO_MODS";
	public static final String ITEM_LOCATION = "https://cdk.lib.cas.cz/search/api/v5.0/item/%s";
	public static final String CDK_LOCATION = "https://cdk.lib.cas.cz/search/%s";
        // 12.10.2021 novinka
        public static final String SOLR_LOCATION = "https://cdk.lib.cas.cz/search/api/v5.0/search?wt=json&q=PID:%s&fl=%s";
        
        /**  client location */
	public static final String CDK_CLIENT_LOCATION = "https://cdk.lib.cas.cz/client/%s";
	
	/** handle for detecting uuid */
	public static final String HANDLE_REPLACEMENT = "https://cdk.lib.cas.cz/client/handle/";
 
	
	private static String getPid(HttpServletRequest req) {
		return req.getParameter("pid");
	}

	public static void changeIdentifier(final Document dc) {
		List<Element> elements = XMLUtils.getElements(dc.getDocumentElement(), new XMLUtils.ElementsFilter() {
			@Override
			public boolean acceptElement(Element element) {
				if (element.getNamespaceURI().equals("http://purl.org/dc/elements/1.1/")) {
					if (element.getLocalName().equals("identifier")) {
						String cnt = element.getTextContent();
						if (cnt.startsWith("uuid:")) {
							return true;
						}
					}
				}
				return false;
			}
		});
		
		for (Element uuidElem : elements) {
			String cnt = uuidElem.getTextContent();
                        
                        NodeList nodeList = dc.getElementsByTagName("dc:identifier");
                        Boolean duplicate = false; 
                        for (int i = 0; i < nodeList.getLength(); i++) { 
                            Element uuidOri = (Element)nodeList.item(i);
                            String uuidOriText = uuidOri.getTextContent();
                            
                            if (uuidOriText.equals(HANDLE_REPLACEMENT+cnt)) {
                                duplicate = true;
                                break;
                            }
                        }
                        
                        if (duplicate == false) {
                            Element nuuid = dc.createElementNS("http://purl.org/dc/elements/1.1/","dc:identifier");
                            nuuid.setTextContent(HANDLE_REPLACEMENT+cnt);
                            dc.getDocumentElement().insertBefore(nuuid, uuidElem); 
                        }
		}
	}

	private CachedAccessToJson jsonCache;
	private CachedAccessToDC dcCache;
	private CachedAccessToMods modsCache;
	
	private FedoraAccess fa;
	private SolrAccess sa;
	public PeriodicalProvideServlet() {
		this.jsonCache = new CachedAccessToJson();
		this.dcCache = new CachedAccessToDC();
		this.modsCache = new CachedAccessToMods();
		
		this.sa = new OAISolrAccess(this.jsonCache);
		this.fa = new OAIFedoraAccess(this.modsCache, this.dcCache);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                String action = req.getParameter("action");
		Actions act = Actions.valueOf(action);
		act.perform(this.fa, this.sa, this.jsonCache, this.dcCache, req, resp);
	}
        
        private static boolean isInCollection(String collection, String pid) throws JSONException, UnsupportedEncodingException {
            JSONArray collections = Utils.getCollections(pid);

            if (collections != null) {
                int length = collections.length();
                for (int i = 0; i < length; i++) {
                    if (collections.get(i).toString().equals(collection)) {
                        return true;
                    }
                }
            }
            return false;
        }
        
	public enum Actions {
                exists {
			
			@Override
			public void perform(FedoraAccess fa, SolrAccess sa, CachedAccessToJson jsonCache,CachedAccessToDC dcCache, HttpServletRequest req, HttpServletResponse resp) throws IOException {
                            try {
                                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                                factory.setNamespaceAware(true);
                                DocumentBuilder builder = factory.newDocumentBuilder();
                                Document exists = builder.newDocument();
                                Element root = exists.createElement("exists");
                                
                                try {
                                    String pid = getPid(req);
                                    if (pid != null && (!pid.trim().equals(""))) {                                              
                                        Document mods = fa.getBiblioMods(pid);  
                                        Document dc = dcCache.get(pid);
                                        JSONObject itemJSON = jsonCache.get(pid);
                                        root.setTextContent("YES");
                                        exists.appendChild(root);
                                        resp.setContentType("text/xml; charset=utf-8");
                                        XMLUtils.print(exists, resp.getWriter());
                                    }
                                    else {
                                        root.setTextContent("NO");
                                        exists.appendChild(root);
                                        resp.setContentType("text/xml; charset=utf-8");
                                        XMLUtils.print(exists, resp.getWriter());
                                    }
                                } catch (DOMException e) {
                                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                } catch (ExecutionException e) {
                                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                } catch (IOException e) {
                                    root.setTextContent("NO");
                                    exists.appendChild(root);
                                    resp.setContentType("text/xml; charset=utf-8");
                                    try { 
                                        XMLUtils.print(exists, resp.getWriter());
                                    } catch (TransformerException ex) {
                                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
					resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                    }
                                } catch (TransformerException e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
					resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                }
                            } catch (ParserConfigurationException ex) {
					Logger.getLogger(PeriodicalProvideServlet.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		},
                
		europeana {

			@Override
			public void perform(FedoraAccess fa, SolrAccess sa, CachedAccessToJson jsonCache, CachedAccessToDC dcCache, HttpServletRequest req, HttpServletResponse resp) throws IOException {
				try {
					String pid = getPid(req);
					if (pid != null && (!pid.trim().equals(""))) {
						DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
						factory.setNamespaceAware(true);
						DocumentBuilder builder = factory.newDocumentBuilder();
						Document europeana = builder.newDocument();
						Element root = europeana.createElementNS("http://www.europeana.eu/schemas/edm/", "edm:record");
						root.setAttribute("xmlns:rdf",FedoraNamespaces.RDF_NAMESPACE_URI);
						europeana.appendChild(root);

                                                String formattedURL = String.format(PeriodicalProvideServlet.CDK_LOCATION, "handle/"+pid);

						Element elementShownAt = europeana.createElementNS("http://www.europeana.eu/schemas/edm/","edm:isShownAt");
						elementShownAt.setAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "rdf:resource", formattedURL);
						root.appendChild(elementShownAt);
                                                
						JSONObject itemJSON = jsonCache.get(pid);

						if (itemJSON.has("pdf") && matchPolicy(itemJSON, "public")) {
							JSONObject pdfObject = itemJSON.getJSONObject("pdf");
							if (pdfObject.has("url")) {
								String url = pdfObject.getString("url");
								Element elementShownBy = europeana.createElementNS("http://www.europeana.eu/schemas/edm/","edm:isShownBy");
								elementShownBy.setAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "rdf:resource", url);
								root.appendChild(elementShownBy);
							}
						}
                                                
                                                // 14.10.2020 - for map and graphic they want url for image in isShownBy
                                                if ((matchModel(itemJSON, "map") || matchModel(itemJSON, "graphic")) && matchPolicy(itemJSON, "public")) {
                                                    String objectUrl = String.format(PeriodicalProvideServlet.CDK_LOCATION, "img?pid=" + pid + "&stream=IMG_FULL");
                                                    Element elementShownBy = europeana.createElementNS("http://www.europeana.eu/schemas/edm/","edm:isShownBy");
                                                    elementShownBy.setAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "rdf:resource", objectUrl);
                                                    root.appendChild(elementShownBy);
						}

                                                // vc:c4bb27af-3a51-4ac2-95c7-fd393b489e26 - KNAV
                                                boolean isKNAV = isInCollection("vc:c4bb27af-3a51-4ac2-95c7-fd393b489e26", pid);
                                                
                                                // For KNAV only use moving wall otherwise decide after policy
						if (isKNAV) {
                                                    boolean allowed = OAIMWUtils.process(fa, sa, pid, null); 
                                                    if (allowed) {
							Element right = europeana.createElementNS("http://www.europeana.eu/schemas/edm/","edm:rights");
							right.setAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "rdf:resource", "http://creativecommons.org/publicdomain/mark/1.0/");
							root.appendChild(right); 
                                                    }
                                                    else {
                                                        if (matchPolicy(itemJSON, "public")) {
                                                            Element right = europeana.createElementNS("http://www.europeana.eu/schemas/edm/","edm:rights");
                                                            right.setAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "rdf:resource", "http://creativecommons.org/licenses/by-nc-sa/4.0/");
                                                            root.appendChild(right);  
                                                        }
                                                        else {
                                                            Element right = europeana.createElementNS("http://www.europeana.eu/schemas/edm/","edm:rights");
                                                            right.setAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "rdf:resource", "http://rightsstatements.org/vocab/InC/1.0/");
                                                            root.appendChild(right);
                                                        }  
                                                    }	
						} 
                                                else {
                                                    if (matchPolicy(itemJSON, "public")) {
                                                        Element right = europeana.createElementNS("http://www.europeana.eu/schemas/edm/","edm:rights");
                                                        // 12.10.2021 - non KNAV articles different licence
                                                        if (matchModel(itemJSON, "article")) {
                                                            right.setAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "rdf:resource", "http://creativecommons.org/publicdomain/zero/1.0/");
                                                        }
                                                        else {
                                                          right.setAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "rdf:resource", "http://creativecommons.org/publicdomain/mark/1.0/");  
                                                        }
                                                        
                                                        root.appendChild(right);  
                                                    }
                                                    else {
                                                        Element right = europeana.createElementNS("http://www.europeana.eu/schemas/edm/","edm:rights");
                                                        right.setAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "rdf:resource", "http://rightsstatements.org/vocab/InC/1.0/");
                                                        root.appendChild(right);
                                                    } 
                                                }
                                                
                                                String objectUrl = "";
                                                Element elementObject = europeana.createElementNS("http://www.europeana.eu/schemas/edm/","edm:object");
                                                
                                                if (matchPolicy(itemJSON, "public")) {
                                                    objectUrl = String.format(PeriodicalProvideServlet.CDK_LOCATION, "img?pid=" + pid + "&stream=IMG_FULL");
                                                }
                                                else {
                                                    objectUrl = String.format(PeriodicalProvideServlet.CDK_LOCATION, "img?pid=" + pid + "&stream=IMG_THUMB");
                                                }
						elementObject.setAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "rdf:resource", objectUrl);
						root.appendChild(elementObject);

						resp.setContentType("text/xml; charset=utf-8");
						XMLUtils.print(europeana, resp.getWriter());
						
					}
	
				} catch (ExecutionException e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
					resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				} catch (ParserConfigurationException e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
					resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				} catch (TransformerException e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
					resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                } catch (XPathExpressionException e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
					resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				} catch (RightCriteriumException e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
					resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				} catch (JSONException ex) {				
                                Logger.getLogger(PeriodicalProvideServlet.class.getName()).log(Level.SEVERE, null, ex);
                            }				
			}
		},
		
		dc {
			
			@Override
			public void perform(FedoraAccess fa, SolrAccess sa, CachedAccessToJson jsonCache,CachedAccessToDC dcCache, HttpServletRequest req, HttpServletResponse resp) throws IOException {
				try {
					String pid = getPid(req);
					LanguageDetect langDetect = new LanguageDetectImpl();

					if (pid != null && (!pid.trim().equals(""))) {
                                            
                                            Document mods = fa.getBiblioMods(pid);
                                            Document dc = dcCache.get(pid);
                                            JSONObject itemJSON = jsonCache.get(pid);
                                            // build new title
                                            
                                             getLangForDescription(dc, mods);
                                             getLangForTitle(dc, mods);
                                             getDCType(dc, itemJSON);
                                             
                                            if (matchModel(itemJSON, "periodical")) {
                                                String ntitle = TitleParts.buildTitle(jsonCache.getForPath(pid, jsonCache));

                                                // title
                                                Element titleElement = XMLUtils.findElement(dc.getDocumentElement(), "title",
                                                            "http://purl.org/dc/elements/1.1/");
                                                if (titleElement == null) {
                                                    titleElement = dc.createElementNS("http://purl.org/dc/elements/1.1/", "dc:title");
                                                    dc.getDocumentElement().appendChild(titleElement);
                                                    titleElement.setTextContent(ntitle);
                                                }					
                                                     
                                                Element sourceElement = XMLUtils.findElement(dc.getDocumentElement(), "source",
                                                            "http://purl.org/dc/elements/1.1/");
                                                     
                                                if (sourceElement == null) {
                                                    sourceElement = dc.createElementNS("http://purl.org/dc/elements/1.1/", "dc:source");
                                                    dc.getDocumentElement().appendChild(sourceElement);
                                                }
                                                     sourceElement.setTextContent(ntitle);
                                            }
                                            
                                            // dc:language for SOUND and IMAGE is not mandatory
                                            if (!matchModel(itemJSON, "soundrecording") && !matchModel(itemJSON, "graphic") && !matchModel(itemJSON, "map")) {
                                                Element langElement = XMLUtils.findElement(dc.getDocumentElement(), "language",
                                                            "http://purl.org/dc/elements/1.1/");
                                                if (langElement == null) {
                                                    langElement = dc.createElementNS("http://purl.org/dc/elements/1.1/", "dc:language");
                                                    dc.getDocumentElement().appendChild(langElement);
                                                }
                                                langElement.setTextContent(langDetect.detectLanguage(dcCache.getForPath(pid, jsonCache)));
                                            }
                                            
                                            changeIdentifier(dc);
                                            resp.setContentType("text/xml; charset=utf-8");
                                            XMLUtils.print(dc, resp.getWriter()); 
					}
				} catch (DOMException e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
					resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				} catch (ExecutionException e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
					resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				} catch (TransformerException e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
					resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}
			}
		},
                
                agent {
			
			@Override
			public void perform(FedoraAccess fa, SolrAccess sa, CachedAccessToJson jsonCache, CachedAccessToDC dcCache, HttpServletRequest req, HttpServletResponse resp) throws IOException {
				try {
					String pid = getPid(req);                                    

					if (pid != null && (!pid.trim().equals(""))) {
                                            
                                            Document mods = fa.getBiblioMods(pid);
                                            // build new title
                                            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                                            factory.setNamespaceAware(true);
                                            DocumentBuilder builder = factory.newDocumentBuilder();
                                            Document agent = builder.newDocument();
                                            Element root = agent.createElementNS("http://www.europeana.eu/schemas/edm/","edm:record");
					    root.setAttribute("xmlns:rdf",FedoraNamespaces.RDF_NAMESPACE_URI);
					    agent.appendChild(root);
                                            
                                            Element agents = agent.createElementNS("http://www.europeana.eu/schemas/edm/","edm:agents");
                                            root.appendChild(agents);
                                            
                                            Element creators = agent.createElementNS(FedoraNamespaces.DC_NAMESPACE_URI,"dc:creators");
                                            root.appendChild(creators);
                                            
                                            getCreator(agent, mods);
                                            
                                            resp.setContentType("text/xml; charset=utf-8");
                                            XMLUtils.print(agent, resp.getWriter()); 
					}
				} catch (DOMException e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
					resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				} catch (TransformerException e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
					resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				} catch (ParserConfigurationException e) {
                                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
					resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                }
			}
		};

		public abstract void perform(FedoraAccess fa,SolrAccess sa,  CachedAccessToJson jsonCache,CachedAccessToDC dcCache, HttpServletRequest req, HttpServletResponse resp) throws IOException ;
	}
        
        private static void getLangForTitle(Document dc, Document mods) {
            List<Element> titleInfoElements = XMLUtils.findElements(mods.getDocumentElement(), "titleInfo", "lang", "http://www.loc.gov/mods/v3");

            for (Element titleInfoElement : titleInfoElements) {
                String titleInfoParent = titleInfoElement.getParentNode().getLocalName();
                String lang = titleInfoElement.getAttribute("lang");

                if (!titleInfoParent.equals("mods")) {
                    continue;
                }
                NodeList childNodes = titleInfoElement.getElementsByTagNameNS("http://www.loc.gov/mods/v3", "title");
                for (int i = 0, ll = childNodes.getLength(); i < ll; i++) {
                    Element titleElement = (Element)childNodes.item(i);
                    if (titleElement != null) {
                        String title = titleElement.getTextContent();
                        Element titleDCElement = XMLUtils.findElement(dc.getDocumentElement(), "title", title,
                                    "http://purl.org/dc/elements/1.1/");
                        if (titleDCElement != null) {
                            
                            LanguageAlpha3Code alpha3B = LanguageAlpha3Code.getByCode(lang);
                            if (alpha3B != null) {
                                JOptionPane.showMessageDialog(null, "Lang je " + lang);
                                LanguageCode alpha2 = alpha3B.getAlpha2();
                                if (alpha2 != null) {
                                    lang = alpha2.toString();
                                }
                            }
                            
                            if (lang != null && !lang.equals("")) {
                                titleDCElement.setAttribute("xml:lang", lang);
                            }
                        }
                    }
               }
            }
        }
        
        private static void getLangForDescription(Document dc, Document mods) {
            List<Element> abstractElements = XMLUtils.findElements(mods.getDocumentElement(), "abstract", "http://www.loc.gov/mods/v3");
                                     
            if (abstractElements != null) {     
                for (Element abstractElement : abstractElements) {
                    if (abstractElement.hasAttribute("lang")) {
                        String lang = abstractElement.getAttribute("lang");
                        String abstractText = abstractElement.getTextContent();
                        Element descriptionElement = XMLUtils.findElement(dc.getDocumentElement(), "description", abstractText,
									"http://purl.org/dc/elements/1.1/");
                        if (descriptionElement != null) {
                            LanguageAlpha3Code alpha3B = LanguageAlpha3Code.getByCode(lang);
                            if (alpha3B != null) {
                                LanguageCode alpha2 = alpha3B.getAlpha2();
                                if (alpha2 != null) {
                                    lang = alpha2.toString();
                                }
                            }
                            
                            if (lang != null && !lang.equals("")) {
                                descriptionElement.setAttribute("xml:lang", lang);
                            }
                        }
                    }
                }
            }
        }
        
        private static void getDCType(Document dc, JSONObject itemJSON) {
            List<Element> typeDCElement = XMLUtils.findElements(dc.getDocumentElement(), "type", "http://purl.org/dc/elements/1.1/");
            int size = typeDCElement.size();
            
            if (size == 0) {
                String model = getModel(itemJSON);
                Element typeElement = dc.createElementNS("http://purl.org/dc/elements/1.1/", "dc:type");
                if (model != null) {
                    typeElement.setTextContent("model:" + model);
                    dc.getDocumentElement().appendChild(typeElement);
                }
            }
        }
        
        private static void getCreator(Document agent, Document mods) throws ParserConfigurationException, IOException, TransformerException {
            List<Element> nameElements = XMLUtils.findElements(mods.getDocumentElement(), "name", "http://www.loc.gov/mods/v3");
            
            if (nameElements != null) {
                for (Element nameElement : nameElements) {
                    if (nameElement.getParentNode().getLocalName().equals("mods")) {
                    if (nameElement.hasAttribute("authorityURI") && nameElement.hasAttribute("valueURI")) {
                        String uri = nameElement.getAttribute("valueURI");;
                        
                        Element agentElement = agent.createElementNS("http://www.europeana.eu/schemas/edm/","edm:Agent");
                        agentElement.setAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "rdf:about", uri);
                        agent.getDocumentElement().getFirstChild().appendChild(agentElement);
                        
                        Map <String, String> record = getNameAndDate(nameElement, true);
                        String prefLabel = record.get("name");
                        String date = record.get("date");
                        
                        if (!prefLabel.equals("")) {
                            Element prefLabelElement = agent.createElementNS("http://www.w3.org/2004/02/skos/core#", "skos:prefLabel");
                            prefLabelElement.setTextContent(prefLabel);
                            agentElement.appendChild(prefLabelElement);
                            
                            Element creator = agent.createElementNS(FedoraNamespaces.DC_NAMESPACE_URI, "dc:creator");
                            creator.setAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "rdf:resource", uri);
                            agent.getDocumentElement().getLastChild().appendChild(creator);
                        }
                        
                        if (!date.equals("")) {
                            Map <String, String> dates  = getDate(date);
                            String birth = dates.get("birth");
                            String death = dates.get("death");
                            
                            if (!birth.equals("")) {
                                Element birthElement = agent.createElementNS("http://rdvocab.info/ElementsGr2/", "rdaGr2:dateOfBirth");
                                birthElement.setTextContent(birth);
                                agentElement.appendChild(birthElement);
                            }
                            if (!death.equals("")) {
                                Element deathElement = agent.createElementNS("http://rdvocab.info/ElementsGr2/", "rdaGr2:dateOfDeath");
                                deathElement.setTextContent(death);
                                agentElement.appendChild(deathElement);
                            }
                        }
                        
                    }
                    else {
                        Map <String, String> record = getNameAndDate(nameElement, false);
                        String prefLabel = record.get("name");
                        if (!prefLabel.equals("")) {
                            Element creator = agent.createElementNS(FedoraNamespaces.DC_NAMESPACE_URI, "dc:creator");
                            creator.setTextContent(prefLabel);
                            agent.getDocumentElement().getLastChild().appendChild(creator);
                        }
                    }
                    }
                }
            }
        }
        
        private static Map <String, String> getNameAndDate(Element nameElement, Boolean isForAgent) {
            String givenName = "";
            String familyName = "";
            String date = "";
            String prefLabel = "";
            String finalName = "";
            Map <String, String> record = new HashMap<String, String>();
            Boolean isCorporate = false;
            
            NodeList namePartList = nameElement.getChildNodes();

            if (nameElement.hasAttribute("type")) {
               String type = nameElement.getAttribute("type");
               if (type.equals("corporate")) {
                   isCorporate = true;
               }
            }
            
            for (int i = 0, ll = namePartList.getLength(); i < ll; i++) {
                Node namePartNode = namePartList.item(i);

                if (namePartNode.getLocalName() != null  && namePartNode.getLocalName().equals("namePart")) {
                    Element namePartElement = (Element)namePartNode;
                    
                    if (namePartElement.hasAttribute("type")) {
                        String type = namePartElement.getAttribute("type");
                        if (type.equals("family")) {
                            familyName = namePartElement.getTextContent();
                        }
                        if (type.equals("given")) {
                            givenName = namePartElement.getTextContent();
                        }
                        if (type.equals("date")) {
                            date = namePartElement.getTextContent();
                        }
                    }
                    else {
                        if (prefLabel.equals("")) {
                            prefLabel = namePartElement.getTextContent();
                        }
                        else {
                            // for <mods:name> which has more elements <mods:namePart> without type
                            prefLabel = prefLabel + ". " + namePartElement.getTextContent();
                        }
                    }
                }
            }
            
            if (!familyName.equals("") && !givenName.equals("")) {
                if (isForAgent == true) {
                    finalName = givenName + " " + familyName;
                }
                else {
                    finalName = familyName + ", " + givenName;
                } 
            }
            else {
                if (!familyName.equals("")) {
                    finalName = familyName;
                }
                if (!givenName.equals("")) {
                    finalName = givenName;
                }
                if (familyName.equals("") && givenName.equals("")) {
                    // České vysoké učení technické v Praze || Novak, Jan
                    if (isCorporate == true || isForAgent == false) {
                        finalName = prefLabel;
                    }
                    // Jan Novak
                    if (isForAgent == true) {
                        finalName = parseName(prefLabel);
                    }
                }
            } 
            record.put("name", finalName);
            record.put("date", date);
            
            return record;
        }
        
        private static Map <String, String> getDate(String date) {
            int length = date.length();
            int i = 0;
            String birth = "";
            String death = "";
            Map <String, String> dates = new HashMap<String, String>();

            while (i < length && date.charAt(i) != '-') {
                birth += date.charAt(i);
                i++;
            }
            i++;
            while (i < length) {
                death += date.charAt(i);
                i++;
            }
            
            dates.put("birth", birth);
            dates.put("death", death);
            return dates;
        }
        
        public static String parseName(String name) {
            String family = "";
            String given = "";
            int i = 0;
            int length = name.length();
                       
            while (i < length && name.charAt(i) != ',') {
                family += name.charAt(i);
                i++;
            }
            i++;
            while (i < length) {
                if (name.charAt(i) == ' ' && given.equals("")) {
                    i++;
                    continue;
                }
                given += name.charAt(i);
                i++;
            }
            
            if (!given.equals("") && !family.equals("")) {
                return given + " " + family;
            }
            return family;
        }
        
}
