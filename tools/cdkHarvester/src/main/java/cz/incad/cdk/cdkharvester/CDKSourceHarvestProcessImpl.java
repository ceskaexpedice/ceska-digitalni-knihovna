/*
 * Copyright (C) 2013 Alberto Hernandez
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.cdk.cdkharvester;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import com.google.inject.Injector;

import cz.incad.cdk.cdkharvester.foxmlprocess.ImageReplaceProcess;
import cz.incad.cdk.cdkharvester.foxmlprocess.ProcessFOXML;
import cz.incad.cdk.cdkharvester.iterator.CDKHarvestIteration;
import cz.incad.cdk.cdkharvester.iterator.StandardCDKHarvestIterationImpl;
import cz.incad.cdk.cdkharvester.manageprocess.CheckLiveProcess;
import cz.incad.cdk.cdkharvester.timestamp.ProcessingTimestamps;
import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.processes.impl.ProcessStarter;
import cz.incad.kramerius.virtualcollections.CDKStateSupport.CDKState;

/**
 * CDK import process
 *
 * @author alberto
 * @TODO !!! REWRITE IT !!!
 */
public class CDKSourceHarvestProcessImpl extends AbstractCDKSourceHarvestProcess {

	static java.util.logging.Logger logger = java.util.logging.Logger
			.getLogger(CDKSourceHarvestProcessImpl.class.getName());

	public static String API_VERSION = "v4.6";
	public static int ROWS = 500;

	@Override
	public List<ProcessFOXML> getProcessingChain() {
		return super.getProcessingChain();
	}

	/**
	 * @throws IOException
	 * 
	 */
	public CDKSourceHarvestProcessImpl() throws IOException {
		super();
		this.processingChain.add(new ImageReplaceProcess());
	}

	public CDKSourceHarvestProcessImpl(List<ProcessFOXML> chains) {
		super();
	}

	@Process
	public static void cdkImport(@ParameterName("url") String url, @ParameterName("name") String name,
			@ParameterName("collectionPid") String collectionPid, @ParameterName("username") String userName,
			@ParameterName("pswd") String pswd) throws Exception {
		ProcessStarter.updateName("Import CDK from " + name);
		CDKSourceHarvestProcessImpl p = new CDKSourceHarvestProcessImpl();
		p.start(url, name, collectionPid, userName, pswd);
	}

	// whole cdk process
	public void start(String url, String name, String collectionPid, String userName, String pswd) throws Exception {
		
		Injector injector = injector();
		
		// initalization
		initFromGivenSource(collectionPid, url, userName, pswd, injector);
		initImport();
		initTransformations();
		
		CheckLiveProcess checkLiveProcess = injector.getInstance(CheckLiveProcess.class);
		ProcessingTimestamps processingTimestamps = injector.getInstance(ProcessingTimestamps.class);
		
		// processing
		if (!checkLiveProcess.isAlive( this.collectionPid, this.sourceName)) {
			checkLiveProcess.informAboutStart(this.collectionPid, this.sourceName, System.getProperty(ProcessStarter.UUID_KEY));
			LocalDateTime timestamp = processingTimestamps.getTimestamp(this.collectionPid);
			CDKHarvestIteration iterator = new StandardCDKHarvestIterationImpl(processingTimestamps.format(timestamp),
					url, userName, pswd);
			super.process(this.collectionPid, iterator, processingTimestamps, CDKState.HARVESTED);
		} else {
			LOGGER.info("previous harvesting is still active");
		}
	}

}
