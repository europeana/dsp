package eu.europeana.dsp.connector.controlplane.catalog.spi.definitions;

import org.eclipse.edc.connector.controlplane.catalog.spi.Distribution;
import org.eclipse.edc.spi.types.domain.DataAddress;

import java.util.Map;

// assetId and participantContextId will come from the parent asset when we persist the distribution.
public class EuropeanaDistribution extends Distribution {

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
}
