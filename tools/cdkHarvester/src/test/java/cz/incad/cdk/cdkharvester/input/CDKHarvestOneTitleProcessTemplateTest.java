package cz.incad.cdk.cdkharvester.input;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;

import cz.incad.kramerius.utils.stemplates.ResourceBundleUtils;
import junit.framework.TestCase;

public class CDKHarvestOneTitleProcessTemplateTest extends TestCase {
//    <legend>$bundle.("parametrizedcdkonetitle.title")$</legend>
//    <table style="width:100%">
//
//    <tr><td colspan="2">
//    <label>$bundle.("parametrizedcdkonetitle.pid")$</label>
//    </td></tr>
//
//    <tr><td colspan="2">
//	<input id="parametrizedcdkonetitle_pid" type="text" ></input> 
//    </td></tr>
//
//    <tr><td colspan="2">
//    <label>$bundle.("parametrizedcdkonetitle.source")$</label>
//    </td></tr>
//
//    <tr><td colspan="2">
//	<input id="parametrizedcdkonetitle_pid" type="text" >$source$</input> 
//    </td></tr>
//
//    <tr><td colspan="2">
//    <label>$bundle.("parametrizedcdkonetitle.user")$</label>
//    </td></tr>
//
//    <tr><td colspan="2">
//	<input id="parametrizedcdkonetitle_uname" type="text" ></input> 
//    </td></tr>
//
//    <tr><td colspan="2">
//    <label>$bundle.("parametrizedcdkonetitle.pswd")$</label>
//    </td></tr>
//
//    <tr><td colspan="2">
//	<input id="parametrizedcdkonetitle_pswd" type="password"></input> 
//    </td></tr>
        

	public void testHarvest() throws UnsupportedEncodingException {

		InputStream iStream = this.getClass().getResourceAsStream("res/title.stg");
        StringTemplateGroup processManages = new StringTemplateGroup(new InputStreamReader(iStream,"UTF-8"), DefaultTemplateLexer.class);
        StringTemplate template = processManages.getInstanceOf("form");

        String source = "testsource";
        Map<String, String> bundle = new HashMap<String, String>(); 
        {
        	bundle.put("parametrizedcdkonetitle.title", "title");
        	bundle.put("parametrizedcdkonetitle.pid", "pid");
        	bundle.put("parametrizedcdkonetitle.source", "source");
        	bundle.put("parametrizedcdkonetitle.user", "user");
        	bundle.put("parametrizedcdkonetitle.pswd", "pswd");
        }

        template.setAttribute("bundle", bundle);
        template.setAttribute("source", source);

        String str = template.toString();
        System.out.println(str);
	}
	
}
