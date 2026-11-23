package eu.europeana.dsp.controlplane.extension;

import eu.europeana.dsp.controlplane.services.EuropeanaAssetServiceImpl;
import eu.europeana.dsp.controlplane.store.sql.assetindex.EuropeanaSqlAssetIndex;
import org.eclipse.edc.connector.controlplane.asset.spi.observe.AssetObservableImpl;
import org.eclipse.edc.connector.controlplane.contract.spi.negotiation.store.ContractNegotiationStore;
import org.eclipse.edc.connector.controlplane.services.asset.AssetEventListener;
import org.eclipse.edc.connector.controlplane.services.asset.AssetQueryValidator;
import org.eclipse.edc.connector.controlplane.services.spi.asset.AssetService;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.runtime.metamodel.annotation.Provides;
import org.eclipse.edc.spi.event.EventRouter;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.transaction.spi.TransactionContext;

@Extension(EuropeanaAssetServiceExtension.NAME)
@Provides(AssetService.class)
public class EuropeanaAssetServiceExtension implements ServiceExtension {
    public static final String NAME = "Europeana Asset Service Extension";

    @Inject
    private ContractNegotiationStore contractNegotiationStore;

    @Inject
    private TransactionContext transactionContext;

    @Inject
    private EventRouter eventRouter;

    @Inject
    EuropeanaSqlAssetIndex sqlAssetIndex;

    @Inject
    Monitor monitor;

    @Provider
    public AssetService assetService() {
        var assetObservable = new AssetObservableImpl();
        assetObservable.registerListener(new AssetEventListener(eventRouter));

        return new EuropeanaAssetServiceImpl(sqlAssetIndex,
                contractNegotiationStore,
                transactionContext,
                assetObservable,
                new AssetQueryValidator(),
                monitor);
    }
}
