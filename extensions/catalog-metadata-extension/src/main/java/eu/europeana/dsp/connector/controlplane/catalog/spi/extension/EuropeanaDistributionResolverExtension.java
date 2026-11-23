package eu.europeana.dsp.connector.controlplane.catalog.spi.extension;

import eu.europeana.dsp.connector.controlplane.catalog.spi.service.EuropeanaDistributionResolver;
import org.eclipse.edc.connector.controlplane.catalog.DataServiceRegistryImpl;
import org.eclipse.edc.connector.controlplane.catalog.spi.DataServiceRegistry;
import org.eclipse.edc.connector.controlplane.catalog.spi.DistributionResolver;
import org.eclipse.edc.connector.controlplane.transfer.spi.flow.DataFlowController;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

/**
 * This class is an implementation of the {@link ServiceExtension} interface that provides
 * extensions for handling Europeana catalog services. It is annotated as an {@link Extension}
 * with the name "Europeana Catalog Services".
 */
@Extension(value = EuropeanaDistributionResolverExtension.NAME)
public class EuropeanaDistributionResolverExtension implements ServiceExtension {

    public static final String NAME = "Europeana Catalog Services";

    @Inject
    private DataFlowController dataFlowController;

    @Inject
    private Monitor monitor;

    private DataServiceRegistry dataServiceRegistry;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        dataServiceRegistry = new DataServiceRegistryImpl();
    }

    @Provider
    public DataServiceRegistry dataServiceRegistry() {
        return dataServiceRegistry;
    }

    @Provider
    public DistributionResolver distributionResolver() {
        return new EuropeanaDistributionResolver(
                dataServiceRegistry,
                dataFlowController,
                monitor
        );
    }

}
