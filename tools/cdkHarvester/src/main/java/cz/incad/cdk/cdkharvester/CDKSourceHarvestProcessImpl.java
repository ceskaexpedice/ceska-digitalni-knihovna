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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import cz.incad.cdk.cdkharvester.foxmlprocess.ImageReplaceProcess;
import cz.incad.cdk.cdkharvester.foxmlprocess.ProcessFOXML;
import cz.incad.cdk.cdkharvester.iterator.CDKHarvestIteration;
import cz.incad.cdk.cdkharvester.iterator.CDKHarvestIterationException;
import cz.incad.cdk.cdkharvester.iterator.CDKHarvestIterationItem;
import cz.incad.cdk.cdkharvester.iterator.StandardCDKHarvestIterationImpl;
import cz.incad.cdk.cdkharvester.postponed.PostponedItemsList;
import cz.incad.cdk.cdkharvester.postponed.PostponedItemsListImpl;
import cz.incad.cdk.cdkharvester.timestamp.ProcessingTimestamps;
import cz.incad.kramerius.Constants;
import cz.incad.kramerius.processes.States;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.processes.impl.ProcessStarter;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.virtualcollections.CDKProcessingIndex;
import cz.incad.kramerius.virtualcollections.impl.CDKProcessingIndexImpl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;

import net.sf.json.JSONObject;
import org.apache.commons.configuration.Configuration;
import org.codehaus.jackson.map.ser.ArraySerializers;
import org.kramerius.Import;
import org.kramerius.replications.*;

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
		this.processingChain.addAll(chains);
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

		// initalization
		initVariables(url, name, collectionPid, userName, pswd);
		initImport();
		initTransformations();

		// processing
		if (!this.checkLiveProcesses.isAlive(System.getProperty(ProcessStarter.UUID_KEY))) {
			this.checkLiveProcesses.informAboutStart(collectionPid, System.getProperty(ProcessStarter.UUID_KEY));
			LocalDateTime timestamp = this.processingTimestamp.getTimestamp(collectionPid);
			CDKHarvestIteration iterator = new StandardCDKHarvestIterationImpl(processingTimestamp.format(timestamp),
					url, userName, pswd);
			super.process(collectionPid, iterator, this.processingTimestamp);
		}
	}

}
