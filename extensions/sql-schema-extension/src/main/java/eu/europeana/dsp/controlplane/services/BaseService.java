package eu.europeana.dsp.controlplane.services;

import eu.europeana.dsp.connector.controlplane.catalog.spi.definitions.EuropeanaAsset;
import eu.europeana.dsp.connector.controlplane.catalog.spi.definitions.EuropeanaDistribution;
import org.eclipse.edc.connector.controlplane.asset.spi.domain.Asset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static eu.europeana.dsp.connector.controlplane.catalog.spi.DspVocabulary.EUROPEANA_DISTRIBUTIONS;

public class BaseService {

    /**
     * Builds a new {@link Asset} by copying the identifying and internal metadata from the specified asset
     * and replacing its public properties with the provided property map.
     *
     * @param asset the source asset whose id, private properties, data address, creation timestamp,
     *              dataplane metadata, and participant context id are copied
     * @param properties the public properties to assign to the newly built asset
     * @return a new asset containing the copied values from the source asset and the provided public properties
     */
    public static Asset buildAsset(Asset asset, Map<String, Object> properties) {
        return Asset.Builder.newInstance()
                .id(asset.getId())
                .properties(properties)
                .privateProperties(asset.getPrivateProperties())
                .dataAddress(asset.getDataAddress())
                .createdAt(asset.getCreatedAt())
                .dataplaneMetadata(asset.getDataplaneMetadata())
                .participantContextId(asset.getParticipantContextId())
                .build();
    }

    /**
     * Converts the specified {@link Asset} into a {@link EuropeanaAsset} by extracting any
     * Europeana distribution data from its public properties, rebuilding the asset without
     * the internal distributions property, and wrapping the cleaned asset together with the
     * extracted distributions.
     *
     * @param asset the source asset to convert
     * @return a Europeana asset containing the cleaned asset and its extracted Europeana distributions
     */
    public static EuropeanaAsset toEuropeanaAsset(Asset asset) {
        Map<String,Object> properties = new HashMap<>(asset.getProperties());
        // this method will also update the properties to remove the internal distributions' property.
        List<EuropeanaDistribution> distributions = extractDistributionData(properties);
        // Build a clean EDC Asset without the internal distributions property.
        var cleanAsset = buildAsset(asset, properties);
        // Wrap the asset and distributions for persistence.
        return wrapEuropeanaAsset(cleanAsset, distributions);
    }

    /**
     * Wraps the specified asset and Europeana distributions into a new {@link EuropeanaAsset}.
     *
     * @param assetWoDistributions the asset without embedded Europeana distribution data
     * @param distributions the Europeana distributions to associate with the asset
     * @return a new Europeana asset containing the specified asset and distributions
     */
    public static EuropeanaAsset wrapEuropeanaAsset(Asset assetWoDistributions, List<EuropeanaDistribution> distributions) {
        EuropeanaAsset europeanaAsset = new EuropeanaAsset();
        europeanaAsset.setAsset(assetWoDistributions);
        europeanaAsset.setDistributions(distributions);
        return europeanaAsset;
    }

    /**
     * Removes the Europeana distributions entry from the specified property map and extracts it as a list of
     * Europeana distribution instances.
     *
     * NOTE : properties object is modified as well
     *
     * @param properties the property map from which the Europeana distributions value is removed and extracted
     * @return an empty list when no distributions entry is present, a list containing the extracted Europeana distributions
     *         when the entry is valid, or null when the removed value is not a valid Europeana distributions list
     */
    public static List<EuropeanaDistribution> extractDistributionData(Map<String, Object> properties) {
        var distributionsValue = properties.remove(EUROPEANA_DISTRIBUTIONS);
        return extractDistributions(distributionsValue);
    }

    /**
     * Determines which Europeana distributions from an existing asset are no longer present in the incoming asset.
     *
     * @param newDistributions new distributions to be added/updated
     * @param existingAsset the existing Europeana asset whose distributions are compared against the incoming asset
     * @return the list of existing distributions whose distribution identifiers are not present in the incoming asset
     */
    public static List<EuropeanaDistribution> getDistributionToRemove(List<EuropeanaDistribution> newDistributions, EuropeanaAsset existingAsset) {
        Map<String, EuropeanaDistribution> incomingById = newDistributions.stream()
                .collect(Collectors.toMap(
                        EuropeanaDistribution::getDistributionId,
                        Function.identity()
                ));

        return existingAsset.getDistributions().stream()
                .filter(d -> !incomingById.containsKey(d.getDistributionId()))
                .toList();
    }

    /**
     * Extracts Europeana distributions from the supplied property value.
     *
     * @param value the value to extract distributions from; may be null or a list of EuropeanaDistribution instances
     * @return an empty list when the value is null, a list containing the extracted
     * distributions when the value is valid, or null when the value is not a list or contains non-EuropeanaDistribution entries
     */
    private static List<EuropeanaDistribution> extractDistributions(Object value) {
        if (value == null) {
            return new ArrayList<>();
        }
        if (!(value instanceof List<?> distributionList)) {
            return null;
        }
        var distributions = new ArrayList<EuropeanaDistribution>();

        for (var item : distributionList) {
            if (!(item instanceof EuropeanaDistribution distribution)) {
                return null;
            }
            distributions.add(distribution);
        }
        return distributions;
    }
}
