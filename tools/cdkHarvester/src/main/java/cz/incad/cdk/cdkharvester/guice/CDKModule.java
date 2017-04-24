package cz.incad.cdk.cdkharvester.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;

import cz.incad.cdk.cdkharvester.manageprocess.CheckLiveProcess;
import cz.incad.cdk.cdkharvester.manageprocess.CheckLiveProcessesFileStoreImpl;
import cz.incad.cdk.cdkharvester.timestamp.ProcessingTimestamps;
import cz.incad.cdk.cdkharvester.timestamp.ProcessingTimestampsSolrStoreImpl;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.virtualcollections.CDKProcessingIndex;
import cz.incad.kramerius.virtualcollections.CDKSourcesAware;
import cz.incad.kramerius.virtualcollections.CollectionsManager;
import cz.incad.kramerius.virtualcollections.impl.CDKProcessingIndexImpl;
import cz.incad.kramerius.virtualcollections.impl.cdk.CDKProcessingCollectionManagerImpl;
import cz.incad.kramerius.virtualcollections.impl.solr.SolrCollectionManagerImpl;

public class CDKModule extends AbstractModule {

	
	@Override
	protected void configure() {
        bind(FedoraAccess.class).annotatedWith(Names.named("rawFedoraAccess")).to(FedoraAccessImpl.class).in(Scopes.SINGLETON);
        bind(StatisticsAccessLog.class).toProvider(Providers.of(null));

		bind(CDKProcessingIndex.class).to(CDKProcessingIndexImpl.class);
        bind(CollectionsManager.class).annotatedWith(Names.named("solr")).to(SolrCollectionManagerImpl.class);
        bind(CollectionsManager.class).annotatedWith(Names.named("cdk")).to(CDKProcessingCollectionManagerImpl.class);
        bind(CDKSourcesAware.class).to(CDKProcessingCollectionManagerImpl.class);
        
        bind(ProcessingTimestamps.class).to(ProcessingTimestampsSolrStoreImpl.class);
        bind(CheckLiveProcess.class).to(CheckLiveProcessesFileStoreImpl.class);
        
	}

}
