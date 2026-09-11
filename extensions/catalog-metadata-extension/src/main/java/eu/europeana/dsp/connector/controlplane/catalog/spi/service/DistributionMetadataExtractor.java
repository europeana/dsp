package eu.europeana.dsp.connector.controlplane.catalog.spi.service;

import eu.europeana.dsp.connector.controlplane.catalog.spi.definitions.EuropeanaDcatDistribution;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.edc.connector.controlplane.catalog.spi.DataService;
import org.eclipse.edc.connector.controlplane.catalog.spi.Distribution;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.eclipse.edc.jsonld.spi.PropertyAndTypeNames.DCT_FORMAT_ATTRIBUTE;
import static org.eclipse.edc.spi.constants.CoreConstants.EDC_NAMESPACE;

// this class extracts the distribution metadata from the asset properties.
// so distribution.distributionId:title will become title
public class DistributionMetadataExtractor {

    private static final String DISTRIBUTION_PREFIX   = "distribution.";
    private static final Pattern DISTRIBUTION_PATTERN =
            Pattern.compile("distribution\\.(\\d+)\\.");

    public static int getDistributionCount(Map<String, Object> properties) {
        return properties.keySet().stream()
                .map(DISTRIBUTION_PATTERN::matcher)
                .filter(Matcher::find)
                .mapToInt(matcher -> Integer.parseInt(matcher.group(1)))
                .max()
                .orElse(0);
    }

    /**
     * Builds a list of {@link Distribution} objects based on the given properties and asset ID.
     *
     * @param properties a map of asset properties containing distribution metadata. The keys should follow
     *                   a specific naming pattern ("distribution.{id}.[property]") to be correctly processed.
     * @param assetId the unique identifier of the asset to associate with each distribution's data service.
     * @return a list of {@link Distribution} objects constructed from the provided properties.
     *         If no distributions are found, an empty list is returned.
     */
    public static List<Distribution> buildDistributions(Map<String, Object> properties, String assetId) {
        List<Distribution> distributions = new ArrayList<>();
        int noOfDistribution = getDistributionCount(properties);

        if (noOfDistribution > 0) {
            // TODO check if this will be the same dataservice created everytime based on asset id
            var dataService = DataService.Builder.newInstance()
                    .id(Base64.getUrlEncoder()
                            .encodeToString(assetId.getBytes()))
                    .build();

            for (int i = 1; i <= noOfDistribution; i++) {
                EuropeanaDcatDistribution distribution = buildDistribution(properties, String.valueOf(i));
                distribution.setDataService(dataService);
                distribution.setFormat(getFormat(properties));
                distributions.add(distribution);
            }
        }
        return distributions;
    }

    public static EuropeanaDcatDistribution buildDistribution(Map<String, Object> properties, String distributionId) {
        if (distributionId == null || properties == null) {
            return null;
        }
        var prefix = DISTRIBUTION_PREFIX + distributionId + ".";
        Map<String, Object> distributionProperties = new HashMap<>();

        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String propertyName = entry.getKey();
            Object value = entry.getValue();
            if (!propertyName.contains(prefix)) {
                continue;
            }
            var metadataProperty = StringUtils.substringAfter(propertyName, prefix);
            distributionProperties.put(metadataProperty, value);
        }
        return new EuropeanaDcatDistribution(distributionId,distributionProperties);
    }

    /**
     * Retrieves the format value from the provided properties map.
     * The format is fetched using the key defined by {@code DCT_FORMAT_ATTRIBUTE}.
     * If not found, it attempts to retrieve the format from a namespaced key
     * using the {@code EDC_NAMESPACE} with the suffix "format".
     * If neither is found, an empty string is returned.
     *
     * @param properties a map containing asset properties, where keys represent metadata attributes
     *                   and their corresponding values.
     * @return the format value as a {@code String}. If the format is not defined in the properties,
     *         an empty string is returned.
     */
    private static String getFormat(Map<String, Object> properties) {
        var format = properties.get(DCT_FORMAT_ATTRIBUTE);
        if (format == null) { // will fetch from https://w3id.org/edc/v0.0.1/ns/format (format)
            format = Optional.ofNullable(properties.get(EDC_NAMESPACE + "format")).orElse("");
        }
        return format.toString();
    }


}