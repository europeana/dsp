package org.eclipse.edc.europeana.catalog;

import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonValue;
import java.util.Map;
import java.util.Set;

import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.ID;

/**
 * Example of an asset with distribution metadata:
 * {
 *   "dct:title": "Dataset 22",
 *   "dct:description": "A provider dataset distributed as ZIP files.",
 *
 *   "distribution.HttpData-PULL.dct:title": "RDF/XML ZIP distribution",
 *   "distribution.HttpData-PULL.dct:description": "A ZIP archive containing RDF/XML files.",
 *   "distribution.HttpData-PULL.dcat:mediaType": "https://www.iana.org/assignments/media-types/application/rdf+xml",
 *   "distribution.HttpData-PULL.dcat:packagingFormat": "https://www.iana.org/assignments/media-types/application/zip"
 *
 *   "distribution.HttpData-PUSH.dct:title":"CSV distribution",
 *   "distribution.HttpData-PUSH.dct:description": "A CSV representation of the dataset.",
 *   "distribution.HttpData-PUSH.dcat:mediaType":"https://www.iana.org/assignments/media-types/text/csv"
 * }
 * RULE: distribution.<format>.<property>
 * This is important because an Asset can have multiple distributions.
 *
 * this class extracts the distribution metadata from the asset properties.
 * so distribution.HttpData-PULL.dct:title will become dct:title
 *
 * @author Srishti Singh
 * @since 15 August 2026
 */
public class DistributionMetadataExtractor {

    //TODO make it configurable later
    private static final String DISTRIBUTION_PREFIX = "distribution.";

    private static final Set<String> DISTRIBUTION_PROPERTIES = Set.of(
            "dct:title",
            "dct:description",
            "dcat:mediaType",
            "dcat:packagingFormat"
    );

    /**
     * Adds distribution-specific metadata to a JSON object being constructed.
     * The metadata is extracted from the provided properties map, filtered,
     * and added to the {@code JsonObjectBuilder} if it matches the predefined
     * distribution format and is included in the allow-list of distribution properties.
     *
     * Only metadata keys that start with a specific prefix derived from the
     * distribution format are considered. The remaining part of the key is
     * validated against an explicit allow-list, and only those properties
     * are added to the JSON object.
     *
     * @param builder the {@code JsonObjectBuilder} to which the distribution metadata is added
     * @param jsonFactory the {@code JsonBuilderFactory} used for creating nested JSON structures
     * @param properties the map of all available properties from which distribution metadata is extracted
     * @param distributionFormat the distribution format used to derive the key prefix for filtering properties
     */
    public static void addDistributionMetadata(jakarta.json.JsonObjectBuilder builder, JsonBuilderFactory jsonFactory,
                                               Map<String, Object> properties, String distributionFormat) {
        if (distributionFormat == null) {
            return;
        }
        var prefix = DISTRIBUTION_PREFIX + distributionFormat + ".";

        properties.forEach((propertyName, value) -> {
            if (!propertyName.startsWith(prefix)) {
                return;
            }
            var metadataProperty = propertyName.substring(prefix.length());

            // Only allow properties from the explicit allow-list.
            if (!DISTRIBUTION_PROPERTIES.contains(metadataProperty)) {
                return;
            }
            if (value == null) {
                return;
            }
            addJsonLdProperty(builder, jsonFactory, metadataProperty, value);
        });
    }

    /**
     * Adds a JSON-LD property to the provided {@code JsonObjectBuilder} instance.
     * The property value is processed based on its type and the property name.
     *
     * Certain properties, like "dcat:mediaType" and "dcat:packagingFormat", are treated as URI-valued
     * DCAT properties and represented using the {@code @id} JSON-LD notation. Other properties
     * are handled as strings or as pre-parsed JSON-LD values depending on their type. If none of
     * these conditions are satisfied, the value is converted to a string as a fallback.
     *
     * @param builder the {@code JsonObjectBuilder} to which the property is added
     * @param jsonFactory the {@code JsonBuilderFactory} used to create JSON-LD objects
     * @param propertyName the name of the property to add
     * @param value the value of the property, which may be a string, a {@code JsonValue}, or another object
     */
    private static void addJsonLdProperty(
            jakarta.json.JsonObjectBuilder builder,
            JsonBuilderFactory jsonFactory,
            String propertyName,
            Object value) {

        // mediaType and packagingFormat are URI-valued DCAT properties, so represent them using @id.
        if (propertyName.equals("dcat:mediaType") || propertyName.equals("dcat:packagingFormat")) {
            if (value instanceof String stringValue) {
                builder.add(propertyName, jsonFactory.createObjectBuilder()
                        .add(ID, stringValue)
                        .build());
            }
            return;
        }

        // Human-readable properties.
        if (value instanceof String stringValue) {
            builder.add(propertyName, stringValue);
            return;
        }

        // Already parsed JSON-LD value.
        if (value instanceof JsonValue jsonValue) {
            builder.add(propertyName, jsonValue);
            return;
        }

        // fallback
        builder.add(propertyName, String.valueOf(value)
        );
    }
}