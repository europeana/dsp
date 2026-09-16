package eu.europeana.dsp.connector.controlplane.catalog.spi.service;

import eu.europeana.dsp.connector.controlplane.catalog.spi.definitions.EuropeanaDcatDistribution;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.edc.connector.controlplane.asset.spi.domain.Asset;
import org.eclipse.edc.connector.controlplane.catalog.spi.DataService;
import org.eclipse.edc.connector.controlplane.catalog.spi.Distribution;
import org.eclipse.edc.spi.types.domain.DataAddress;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
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
        return new EuropeanaDcatDistribution(distributionId, distributionProperties);
    }

    public static String getDistributionFormat(DataAddress dataAddress) {
        var format = dataAddress != null
                ? dataAddress.getType() + "-PULL"
                : null;
        if (format == null) {
            format = dataAddress.getProperties().get(DCT_FORMAT_ATTRIBUTE).toString();
        }
        return format;
    }

}