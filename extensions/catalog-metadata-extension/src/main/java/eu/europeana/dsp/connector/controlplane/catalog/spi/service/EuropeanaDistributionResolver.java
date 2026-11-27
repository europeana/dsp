package eu.europeana.dsp.connector.controlplane.catalog.spi.service;

import eu.europeana.dsp.connector.controlplane.catalog.spi.definitions.EuropeanaAsset;
import org.eclipse.edc.connector.controlplane.catalog.DefaultDistributionResolver;
import org.eclipse.edc.connector.controlplane.catalog.spi.DataServiceRegistry;
import org.eclipse.edc.connector.controlplane.catalog.spi.Distribution;
import org.eclipse.edc.connector.controlplane.transfer.spi.flow.DataFlowController;
import org.eclipse.edc.spi.monitor.Monitor;

import java.util.*;

/**
 * EuropeanaDistributionResolver extends DefaultDistributionResolver.
 *
 * @author Srishti Singh
 * @since 2026-12-18
 */
public class EuropeanaDistributionResolver extends DefaultDistributionResolver {

    public EuropeanaDistributionResolver(
            DataServiceRegistry dataServiceRegistry,
            DataFlowController dataFlowController,
            Monitor monitor) {
        super(dataServiceRegistry, dataFlowController, monitor);
    }

    public List<? extends Distribution> getEuropeanaDistributions(String protocol, EuropeanaAsset europeanaAsset) {
        return europeanaAsset.getDistributions();
    }

}