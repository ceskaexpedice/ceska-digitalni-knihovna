package cz.incad.cdk.cdkharvester.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.impl.SolrAccessImpl;
import cz.incad.kramerius.virtualcollections.CollectionsManager;
import cz.incad.kramerius.virtualcollections.impl.fedora.FedoraCollectionsManagerImpl;

public class CDKHarvestModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(SolrAccess.class).to(SolrAccessImpl.class).in(Scopes.SINGLETON);
        bind(CollectionsManager.class).annotatedWith(Names.named("fedora")).to(FedoraCollectionsManagerImpl.class);
    }
}
