package cz.incad.cdk;

import static cz.incad.cdk.Utils.*;

import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import org.antlr.stringtemplate.StringTemplate;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.LSOutput;
import org.xml.sax.SAXException;

import java.io.*;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Set of parts dedicated for moving data from MZK to CDK
 */
public enum MZKMigrationParts  {


    /** move and rename streams */
    STREAMS {
        @Override
        public void doMigrationPart(Connection db) throws SQLException {
            String datastreamPaths = KConfiguration.getInstance().getProperty("akubrafs.streams.target");
            dbSelect(db, new File(datastreamPaths), "select * from datastreampaths", (f) ->{});
        }
    },

    /** move reaname and change mzk objects */
    OBJECTS {
        @Override
        public void doMigrationPart(Connection db) throws SQLException {
            String objectPaths = KConfiguration.getInstance().getProperty("akubrafs.objects.target");
            dbSelect(db, new File(objectPaths), "select * from objectpaths", (f) ->{
                try {
                    Document parsed = BUILDER.parse(f);
                    Element rootElement = parsed.getDocumentElement();
                    String pid = rootElement.getAttribute("PID");
                    if (pid.startsWith("uuid")) {

                        List<Element> imgFulls = XMLUtils.getElements(rootElement, new XMLUtils.ElementsFilter() {
                            @Override
                            public boolean acceptElement(Element element) {
                                String localName = element.getLocalName();
                                String namespace = element.getNamespaceURI();
                                if ((localName.equals("datastream")) && (namespace.equals("info:fedora/fedora-system:def/foxml#"))) {
                                    String id = element.getAttribute("ID");
                                    return id.equals(FedoraUtils.IMG_FULL_STREAM);
                                }
                                return false;
                            }
                        });

                        List<Element> imgPreviews = XMLUtils.getElements(rootElement, new XMLUtils.ElementsFilter() {
                            @Override
                            public boolean acceptElement(Element element) {
                                String localName = element.getLocalName();
                                String namespace = element.getNamespaceURI();
                                if ((localName.equals("datastream")) && (namespace.equals("info:fedora/fedora-system:def/foxml#"))) {
                                    String id = element.getAttribute("ID");
                                    return id.equals(FedoraUtils.IMG_PREVIEW_STREAM);
                                }
                                return false;
                            }
                        });

                        List<Element> imgThumbs = XMLUtils.getElements(rootElement, new XMLUtils.ElementsFilter() {
                            @Override
                            public boolean acceptElement(Element element) {
                                String localName = element.getLocalName();
                                String namespace = element.getNamespaceURI();
                                if ((localName.equals("datastream")) && (namespace.equals("info:fedora/fedora-system:def/foxml#"))) {
                                    String id = element.getAttribute("ID");
                                    return id.equals(FedoraUtils.IMG_THUMB_STREAM);
                                }
                                return false;
                            }
                        });

                        List<Element> relsExt = XMLUtils.getElements(rootElement, new XMLUtils.ElementsFilter() {
                            @Override
                            public boolean acceptElement(Element element) {
                                String localName = element.getLocalName();
                                String namespace = element.getNamespaceURI();
                                if ((localName.equals("datastream")) && (namespace.equals("info:fedora/fedora-system:def/foxml#"))) {
                                    String id = element.getAttribute("ID");
                                    return id.equals(FedoraUtils.RELS_EXT_STREAM);
                                }
                                return false;
                            }
                        });

                        imgFulls.stream().forEach(element ->  {
                            XMLUtils.getElements(element).stream().forEach((elm)->{
                                changeLinkUrl(elm,"imgfull.replace",pid);
                            });
                        });
                        imgPreviews.forEach(element ->{
                            XMLUtils.getElements(element).stream().forEach((elm)->{
                                changeLinkUrl(elm,"imgpreview.replace",pid);
                            });
                        });

                        imgThumbs.forEach(element -> {
                            XMLUtils.getElements(element).stream().forEach((elm)->{
                                changeLinkUrl( elm,"imgthumb.replace",pid);
                            });
                        });

                        relsExt.forEach(element -> {
                            addCollection(element, pid);
                        });

                        print(parsed,new FileOutputStream(f));

                    }

                } catch (SAXException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                }
            });

        }

        private void addCollection(Element element, String pid) {
            Object value = KConfiguration.getInstance().getConfiguration().getProperty("relsext.collection");

            Element description = XMLUtils.findElement(element, new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    String localName = element.getLocalName();
                    String namespace = element.getNamespaceURI();
                    String about = element.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "about");
                    return ((localName.equals("Description")) && (namespace.equals(FedoraNamespaces.RDF_NAMESPACE_URI)) && (about.equals("info:fedora/"+pid)));
                }
            });

            Element elm = element.getOwnerDocument().createElementNS(FedoraNamespaces.RDF_NAMESPACE_URI,"isMemberOfCollection");
            elm.setAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource", "info:fedora/"+value);
            description.appendChild(elm);
        }


        private void changeLinkUrl(Element elm, String key, String pid) {
            Object value = KConfiguration.getInstance().getConfiguration().getProperty(key);
            elm.setAttribute("CONTROL_GROUP", "E");
            List<Element> contentLocation = XMLUtils.getElements(elm, new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    return element.getLocalName().equals("contentLocation");
                }
            });

            contentLocation.stream().forEach((e)->{
                StringTemplate template = new StringTemplate(value.toString());
                template.setAttribute("pid",pid);
                e.setAttribute("REF", template.toString());
                e.setAttribute("TYPE", "URL");
            });
        }
    };

    private static void print(Document parsed, FileOutputStream fos) {
        LSOutput lsOutput = DOMIMPL.createLSOutput();
        lsOutput.setByteStream(fos);
        SERIALIZER.write(parsed,lsOutput);
    }

    private static void dbSelect(Connection db, File targetDir, String sqlCommand, Consumer<File> consumer) throws SQLException {
        final long start = System.currentTimeMillis();
        Stack<Integer> stack = new Stack<>();
        stack.push(new Integer(0));
        int counter = 0;
        List<Pair<String,String>> ids = new JDBCQueryTemplate<Pair<String, String>>(db, false){

            public boolean handleRow(ResultSet rs, List<Pair<String,String>> returnsList) throws SQLException {

                Integer currentIteration = stack.pop();
                if ((currentIteration % LOG_MESSAGE_ITERATION) == 0) {
                    long stop = System.currentTimeMillis();
                    LOGGER.info("Current iteration "+currentIteration+" and took "+(stop - start)+ " ms ");
                }
                stack.push(new Integer(currentIteration.intValue() + 1));

                String token = rs.getString("token");
                String path = rs.getString("path");

                token = token.replaceAll("\\+", "/");
                String hex = Utils.asHex(MD5.digest(("info:fedora/"+token).getBytes(Charset.forName("UTF-8"))));

                File objectFile = new File(rs.getString("path"));
                try {
                    File directory = Utils.directory(targetDir, hex, 2, 3);
                    FileUtils.moveFileToDirectory(objectFile, directory, true);
                    new File(directory,  objectFile.getName()).renameTo(new File(directory, Utils.encode("info:fedora/" + token)));

                    consumer.accept(new File(directory, Utils.encode("info:fedora/" + token)));

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return true;
            }
        }.executeQuery(sqlCommand);
    }

    abstract void doMigrationPart(Connection connection) throws SQLException;


    static Logger LOGGER = Logger.getLogger(MZKMigrationParts.class.getName());

    // Message after 60 iterations
    static int LOG_MESSAGE_ITERATION = 10000;

}
