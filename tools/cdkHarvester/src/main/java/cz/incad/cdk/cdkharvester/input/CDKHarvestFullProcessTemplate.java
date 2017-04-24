package cz.incad.cdk.cdkharvester.input;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.template.ProcessInputTemplate;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.stemplates.ResourceBundleUtils;

public class CDKHarvestFullProcessTemplate implements ProcessInputTemplate {

    @Inject
    KConfiguration configuration;
    
    @Inject
    Provider<Locale> localesProvider;
    
    @Inject
    ResourceBundleService resourceBundleService;

    
    @Override
    public void renderInput(LRProcessDefinition definition, Writer writer, Properties paramsMapping) throws IOException {
        InputStream iStream = this.getClass().getResourceAsStream("res/full.stg");
        StringTemplateGroup processManages = new StringTemplateGroup(new InputStreamReader(iStream,"UTF-8"), DefaultTemplateLexer.class);
        StringTemplate template = processManages.getInstanceOf("form");

        String value = paramsMapping.getProperty("source");
        
        ResourceBundle resbundle = resourceBundleService.getResourceBundle("labels", localesProvider.get());

        template.setAttribute("bundle", ResourceBundleUtils.resourceBundleMap(resbundle));
        template.setAttribute("source", value);
        
        writer.write(template.toString());
    }
}
