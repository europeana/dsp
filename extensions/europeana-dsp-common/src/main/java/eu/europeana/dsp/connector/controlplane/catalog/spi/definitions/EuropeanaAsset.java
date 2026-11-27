package eu.europeana.dsp.connector.controlplane.catalog.spi.definitions;

import org.eclipse.edc.connector.controlplane.asset.spi.domain.Asset;

import java.util.List;

/**
 * Represents an asset together with its Europeana-specific distributions.
 *
 * The asset contains the core EDC asset metadata, while the distributions describe
 * the available Europeana representations or access options associated with that
 * asset. This type is intended to group the parent asset and its related
 * distributions for catalog, persistence, or transfer workflows.
 *
 * @author Srishti Singh
 * @since 2026-11-15
 */
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
