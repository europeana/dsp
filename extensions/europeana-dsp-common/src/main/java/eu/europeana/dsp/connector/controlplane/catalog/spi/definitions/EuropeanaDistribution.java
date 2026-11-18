package eu.europeana.dsp.connector.controlplane.catalog.spi.definitions;

import eu.europeana.dsp.connector.controlplane.catalog.spi.Resource;
import eu.europeana.dsp.connector.controlplane.catalog.spi.ResourceDescriptionProvider;
import org.eclipse.edc.connector.controlplane.catalog.spi.Distribution;
import org.eclipse.edc.spi.types.domain.DataAddress;

import java.util.Map;

/**
 * Represents a Europeana-specific DCAT distribution that can provide a resource
 * description derived from its metadata map.
 *
 * The resource description is resolved dynamically from the distribution metadata
 * when requested. This allows the distribution to expose DCAT-compatible resource
 * information while keeping the underlying metadata representation flexible.
 *
 * assetId and participantContextId will come from the parent asset when we persist the distribution.
 * @author Srishti Singh
 * @since 2026-11-15
 */
public class EuropeanaDistribution extends Distribution implements ResourceDescriptionProvider {

    private String distributionId;
    private Map<String, Object> properties;
    private DataAddress dataAddress;

    public EuropeanaDistribution(String distributionId) {
        this.distributionId = distributionId;
    }

    public String getDistributionId() {
        return distributionId;
    }

    public void setDistributionId(String distributionId) {
        this.distributionId = distributionId;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public DataAddress getDataAddress() {
        return dataAddress;
    }

    public void setDataAddress(DataAddress dataAddress) {
        this.dataAddress = dataAddress;
    }

    @Override
    public Resource getResourceInfo() {
        return buildResourceInfo(properties);
    }

    @Override
    public Resource buildResourceInfo(Map<String, Object> properties) {
        return new ResourceResolver(properties);
    }

    @Override
    public void setResourceInfo(Resource resourceInfo) {
        // left empty as we don't need ResourceInfo for DCAT distributions
    }
}
