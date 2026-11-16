package eu.europeana.dsp.connector.controlplane.catalog.spi.definitions;

import org.eclipse.edc.connector.controlplane.asset.spi.domain.Asset;

import java.util.List;

public class EuropeanaAsset {

    private Asset asset;
    private List<EuropeanaDistribution> distributions;

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public List<EuropeanaDistribution> getDistributions() {
        return distributions;
    }

    public void setDistributions(List<EuropeanaDistribution> distributions) {
        this.distributions = distributions;
    }
}
