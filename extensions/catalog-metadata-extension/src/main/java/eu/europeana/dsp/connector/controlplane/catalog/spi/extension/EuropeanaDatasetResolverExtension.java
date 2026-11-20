package eu.europeana.dsp.connector.controlplane.catalog.spi.extension;

import eu.europeana.dsp.connector.controlplane.catalog.spi.service.EuropeanaDatasetResolver;
import eu.europeana.dsp.connector.controlplane.catalog.spi.service.EuropeanaDistributionResolver;
import eu.europeana.dsp.controlplane.extension.EuropeanaSqlAssetIndexExtension;
import eu.europeana.dsp.controlplane.store.sql.assetindex.EuropeanaSqlAssetIndex;
import org.eclipse.edc.connector.controlplane.catalog.ContractDefinitionResolverImpl;
import org.eclipse.edc.connector.controlplane.catalog.spi.DatasetResolver;
import org.eclipse.edc.connector.controlplane.contract.spi.offer.store.ContractDefinitionStore;
import org.eclipse.edc.connector.controlplane.policy.spi.store.PolicyDefinitionStore;
import org.eclipse.edc.policy.engine.spi.PolicyEngine;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.runtime.metamodel.annotation.Provides;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.query.CriterionOperatorRegistry;
import org.eclipse.edc.spi.system.ServiceExtension;

/**
 * Service extension responsible for creating and registering the Europeana dataset resolver.
 *
 * @author Srishti Singh
 * @since 2026-12-19
 */
@Extension(value = EuropeanaDatasetResolverExtension.NAME)
@Provides(DatasetResolver.class)
public class EuropeanaDatasetResolverExtension implements ServiceExtension {

    public static final String NAME = "Europeana Dataset Resolver Extension";

    /**
     * To inject our service we must have a @provider service
     * See {@link EuropeanaSqlAssetIndexExtension}
     */
    @Inject
    private EuropeanaSqlAssetIndex europeanaSqlAssetIndex;

    @Inject
    private PolicyDefinitionStore policyDefinitionStore;

    /**
     * To inject our service we must have a @provider service
     * See {@link EuropeanaDistributionResolverExtension}
     */
    @Inject
    private EuropeanaDistributionResolver europeanaDistributionResolver;

    @Inject
    private CriterionOperatorRegistry criterionOperatorRegistry;

    @Inject
    private ContractDefinitionStore contractDefinitionStore;

    @Inject
    private PolicyEngine policyEngine;

    @Inject
    private Monitor monitor;

    @Override
    public String name() {
        return NAME;
    }

    @Provider
    public EuropeanaDatasetResolver datasetResolver() {
        var contractDefinitionResolver = new ContractDefinitionResolverImpl(contractDefinitionStore, policyEngine, policyDefinitionStore);
        return new EuropeanaDatasetResolver(contractDefinitionResolver, europeanaSqlAssetIndex, policyDefinitionStore,
                europeanaDistributionResolver, criterionOperatorRegistry, monitor);
    }

}
