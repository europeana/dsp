package eu.europeana.dsp.connector.controlplane.catalog.spi.service;

import eu.europeana.dsp.connector.controlplane.catalog.spi.definitions.EuropeanaDcatDistribution;
import org.eclipse.edc.connector.controlplane.catalog.spi.DataService;
import org.eclipse.edc.connector.controlplane.catalog.spi.Distribution;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// this class extracts the distribution metadata from the asset properties.
// so distribution.distributionId.dct:title will become dct:title
public class DistributionMetadataExtractor {

    private static final String DISTRIBUTION_PREFIX   = "distribution.";
    private static final Pattern DISTRIBUTION_PATTERN = Pattern.compile("^distribution\\.(\\d+)\\..+$");

    public static int getDistributionCount(Map<String, Object> properties) {
        return properties.keySet().stream()
                .map(DISTRIBUTION_PATTERN::matcher)
                .filter(Matcher::matches)
                .mapToInt(matcher -> Integer.parseInt(matcher.group(1)))
                .max()
                .orElse(0);
    }

    public static EuropeanaDcatDistribution buildDistribution(Map<String, Object> properties, String distributionId) {
        if (distributionId == null || properties == null) {
            return null;
        }

        var prefix = DISTRIBUTION_PREFIX + distributionId + ".";

        Map<String, Object> distributionProperties = new HashMap<>();

        properties.forEach((propertyName, value) -> {
            if (!propertyName.startsWith(prefix)) {
                return;
            }
            var metadataProperty = propertyName.substring(prefix.length());
            distributionProperties.put(metadataProperty, value);
        });

        return new EuropeanaDcatDistribution(distributionId, distributionProperties);
    }

}
