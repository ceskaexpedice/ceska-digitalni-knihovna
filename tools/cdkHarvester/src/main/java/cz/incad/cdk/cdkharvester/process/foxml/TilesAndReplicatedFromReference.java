package cz.incad.cdk.cdkharvester.process.foxml;

import cz.incad.cdk.cdkharvester.utils.URLHostChangeUtils;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

public class TilesAndReplicatedFromReference implements ProcessFOXML {


    private List<Pair<String,String>> replacingHosts = new ArrayList<>();


    public TilesAndReplicatedFromReference() {
        String replacingPairs = KConfiguration.getInstance().getConfiguration().getString("cdk.prepareFOXML.relsext.forceReplaceHosts", "");
        StringTokenizer tokenizer = new StringTokenizer(replacingPairs, ";");
        while(tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.contains("-")) {
                StringTokenizer onePairTokenizer = new StringTokenizer(token, "-");
                String left = onePairTokenizer.hasMoreTokens() ? onePairTokenizer.nextToken() :  null;
                String right = onePairTokenizer.hasMoreTokens() ? onePairTokenizer.nextToken() : null;
                if (left != null && right != null) {
                    replacingHosts.add(Pair.of(left,right));
                }
            }
        }
    }

    @Override
    public byte[] process(String url, String pid, InputStream is) throws Exception {

        if (is == null) return null;

        if (this.replacingHosts.isEmpty()) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copyStreams(is, bos);
            return bos.toByteArray();
        } else {
            Document document = XMLUtils.parseDocument(is, true);
            Element docElement = document.getDocumentElement();
            if (docElement.getLocalName().equals("digitalObject")) {

                List<Element> relsExts = XMLUtils.getElements(docElement, new XMLUtils.ElementsFilter() {
                    @Override
                    public boolean acceptElement(Element elm) {
                        String idName = elm.getAttribute("ID");
                        return idName.equals(FedoraUtils.RELS_EXT_STREAM);
                    }
                });
                for (Element rExt : relsExts) {
                    Element replicatedFrom = XMLUtils.findElement(rExt, new XMLUtils.ElementsFilter() {
                        @Override
                        public boolean acceptElement(Element elm) {
                            return elm.getNodeName().equals("replicatedFrom");
                        }
                    });

                    if (replicatedFrom != null) {
                        changeUrl(replicatedFrom);
                    }
                    Element tilesUrl = XMLUtils.findElement(rExt, new XMLUtils.ElementsFilter() {
                        @Override
                        public boolean acceptElement(Element elm) {
                            return elm.getNodeName().equals("tiles-url");
                        }
                    });

                    if (tilesUrl != null) {
                        changeUrl(tilesUrl);
                    }
                }
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            XMLUtils.print(document, bos);
            return bos.toByteArray();
        }
    }

    private void changeUrl(Element replicatedFrom) throws MalformedURLException {
        String textContent = replicatedFrom.getTextContent();
        for (Pair<String, String> p :  this.replacingHosts) {
            String left = p.getLeft();
            if (textContent.contains(left)) {
                replicatedFrom.setTextContent(URLHostChangeUtils.changeHostString(textContent, p.getRight()));
            }
        }
    }
}
