package cz.incad.cdk.cdkharvester;

import java.net.MalformedURLException;
import java.net.URL;

import com.google.inject.Injector;

import cz.incad.cdk.cdkharvester.iterator.CDKHarvestIteration;
import cz.incad.cdk.cdkharvester.iterator.TitleCDKHarvestIterationImpl;
import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.utils.handle.DisectHandle;

public class CDKHarvestOneTitleProcessImpl extends AbstractCDKSourceHarvestProcess {


	public CDKHarvestOneTitleProcessImpl() {
		super();
	}

	@Process
	public static void cdkTitle(@ParameterName("pid") String title, @ParameterName("source") String source, @ParameterName("username") String userName, @ParameterName("pswd") String pswd) throws Exception {
		CDKHarvestOneTitleProcessImpl p = new CDKHarvestOneTitleProcessImpl();
		p.start(disectPid(title), source, userName, pswd);
	}

	private static String disectPid(String title) {
		try {
			// check if given title is url
			new URL(title);
			return DisectHandle.disectHandle(title);
		} catch (MalformedURLException e) {
			// no url, simple pid
			return title;
		}
	}

	public void start(String pid, String source, String userName, String pswd) throws Exception {
		Injector inj = injector();
		initFromGivenSource(pid, source, userName, pswd, inj);
		initImport();
		initTransformations();

		CDKHarvestIteration iterator = new TitleCDKHarvestIterationImpl(this.k4Url, pid);
		iterator.init();

		super.process(collectionPid, iterator, null, null);
	}


}
