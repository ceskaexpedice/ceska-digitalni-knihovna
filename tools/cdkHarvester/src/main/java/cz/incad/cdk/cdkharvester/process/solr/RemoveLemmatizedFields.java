package cz.incad.cdk.cdkharvester.process.solr;

import cz.incad.kramerius.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

public class RemoveLemmatizedFields implements ProcessSOLRXML {

    @Override
    public byte[] process(String url, String pid, InputStream is) throws Exception {
        Document document = XMLUtils.parseDocument(is, false);
        Element doc = XMLUtils.findElement(XMLUtils.findElement(document.getDocumentElement(), "result"), "doc");

        List<Element> elements1 = XMLUtils.getElements(doc, new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                String name = element.getAttribute("name");
                if (name.contains("_lemmatized_")) {
                    return true;
                }
                return false;
            }
        });

        elements1.stream().forEach(toRemove ->{
            toRemove.getParentNode().removeChild(toRemove);
        });

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XMLUtils.print(document, bos);
        return bos.toByteArray();
    }
}
