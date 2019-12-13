package cz.incad.cdk.cdkharvester.process.utils;

import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.stream.Collectors;

public class CDKPIDsUtils {

    private CDKPIDsUtils() {}

    public static String changePid(String name, String pid) {
        return name+"_"+pid;
    }

    public static void changePIDElemContent(String name, Element ... elms) {
        for (Element elm: elms) {
            elm.setTextContent(changePid(name, elm.getTextContent().trim()));
        }
    }

    public static String changePIDPath(String name, String pidpath) {
        String reduce = Arrays.stream(pidpath.split("/")).map((it) -> it.startsWith("uuid:") ? changePid(name, it) : it).reduce("", (id, it) -> id = id + "/" + it);
        return reduce.substring(1);
    }

    public static void changePIDPathElemContent(String name,  Element elm) {
        elm.setTextContent(changePIDPath(name, elm.getTextContent().trim()));
    }
}
