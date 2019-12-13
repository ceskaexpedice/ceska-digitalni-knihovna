package cz.incad.cdk.cdkharvester.process.solr;

import static cz.incad.cdk.cdkharvester.process.utils.CDKPIDsUtils.*;

import cz.incad.kramerius.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ChangeSOLRPIDFields implements ProcessSOLRXML {


    @Override
    public byte[] process(String name, String url, String pid, InputStream is) throws Exception {
        Document document = XMLUtils.parseDocument(is, false);
        Element doc = XMLUtils.findElement(document.getDocumentElement(),  "doc");

        Element pidElm = XMLUtils.findElement(doc, (element)-> {
                String n = element.getAttribute("name");
                if (n.equals("PID")) return true;
                else return false;
        });

        Element rootPid = XMLUtils.findElement(doc, (element)->  {
            String  n = element.getAttribute("name");
            if (n.equals("root_pid")) return true;
            else return false;
        });

        Element pidPath = XMLUtils.findElement(doc, (element)->  {
            String  n = element.getAttribute("name");
            if (n.equals("pid_path")) return true;
            else return false;
        });

        changePIDElemContent(name, pidElm,rootPid);
        changePIDPathElemContent(name, pidPath);

        document.normalize();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XMLUtils.print(document, bos);
        return bos.toByteArray();
    }

}
