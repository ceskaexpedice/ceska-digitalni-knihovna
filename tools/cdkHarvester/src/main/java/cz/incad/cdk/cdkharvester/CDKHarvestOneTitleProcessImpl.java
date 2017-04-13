package cz.incad.cdk.cdkharvester;

import cz.incad.cdk.cdkharvester.iterator.CDKHarvestIteration;
import cz.incad.cdk.cdkharvester.iterator.TitleCDKHarvestIterationImpl;
import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.processes.impl.ProcessStarter;

public class CDKHarvestOneTitleProcessImpl extends AbstractCDKSourceHarvestProcess {

	private String pid;

	public CDKHarvestOneTitleProcessImpl(String pid) {
		super();
		this.pid = pid;
	}

	@Process
	public static void cdkTitle(@ParameterName("url") String url, @ParameterName("name") String name,
			@ParameterName("collectionPid") String collectionPid, @ParameterName("username") String userName,
			@ParameterName("pswd") String pswd, @ParameterName("pid") String pid) throws Exception {
		ProcessStarter.updateName("Import CDK from " + name);
		CDKHarvestOneTitleProcessImpl p = new CDKHarvestOneTitleProcessImpl(pid);
		p.start(url, name, collectionPid, userName, pswd);
	}

	public void start(String url, String name, String collectionPid, String userName, String pswd) throws Exception {

		initVariables(url, name, collectionPid, userName, pswd);
		initImport();
		initTransformations();

		CDKHarvestIteration iterator = new TitleCDKHarvestIterationImpl(this.k4Url, this.pid);
		super.process(collectionPid, iterator, null);
	}

}
