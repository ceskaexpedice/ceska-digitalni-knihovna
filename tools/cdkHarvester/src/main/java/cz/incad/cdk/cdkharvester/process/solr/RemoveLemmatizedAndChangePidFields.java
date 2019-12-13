package cz.incad.cdk.cdkharvester.process.solr;

import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class RemoveLemmatizedAndChangePidFields implements ProcessSOLRXML {

    @Override
    public byte[] process(String name, String url, String pid, InputStream is) throws Exception {
        Document document = XMLUtils.parseDocument(is, false);
        Element doc = XMLUtils.findElement(document.getDocumentElement(),  "doc");
        List<Element> elements1 = XMLUtils.getElements(doc, new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                String name = element.getAttribute("name");
                List list = KConfiguration.getInstance().getConfiguration().getList("cdk.prepareSOLRXML.skipfields", Arrays.asList(
                            "root_title_lemmatized",
                            "root_title_lemmatized_ascii",
                            "root_title_lemmatized_nostopwords",

                            "title_lemmatized",
                            "title_lemmatized_ascii",
                            "title_lemmatized_nostopwords",

                            "text_ocr_lemmatized",
                            "text_ocr_lemmatized_ascii",
                            "text_ocr_lemmatized_nostopwords",

                            "search_title",
                            "facet_autor",
                            "search_autor"
                        ));

                if (list.contains(name)) {
                    return true;
                } else return false;
            }
        });

        for (Element toRemove :  elements1) {  toRemove.getParentNode().removeChild(toRemove); }



        document.normalize();

        byte[] barr2 = renderXML(document);
        return barr2;
    }

    private byte[] renderXML(Document document) throws TransformerException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XMLUtils.print(document, bos);
        return bos.toByteArray();
    }
}
