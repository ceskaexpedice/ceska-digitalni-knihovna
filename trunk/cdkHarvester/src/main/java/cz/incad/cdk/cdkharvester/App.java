package cz.incad.cdk.cdkharvester;


public class App 
{
    public static void main( String[] args ) throws Exception
    {
        CDKImportProcess p = new CDKImportProcess();
        p.start("kk", "vmkramerius", "vc:534b8b98-82d8-49c7-a751-33e88aaeeea9", "krameriusAdmin", "krameriusAdmin");
        //p.start("http://vmkramerius.incad.cz:8080/search", "vmkramerius", "krameriusAdmin", "krameriusAdmin");
        //p.start("http://localhost:8080/search", "alberto", "krameriusAdmin", "krameriusAdmin");
    }
}