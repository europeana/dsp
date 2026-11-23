package eu.europeana.dsp.connector.controlplane.catalog.spi.transformer.to;

import eu.europeana.dsp.connector.controlplane.catalog.spi.definitions.EuropeanaDistribution;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

import org.eclipse.edc.jsonld.spi.transformer.AbstractJsonLdTransformer;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.eclipse.edc.spi.types.domain.DataAddress;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.ID;
import static org.eclipse.edc.spi.constants.CoreConstants.EDC_NAMESPACE;
import static org.eclipse.edc.connector.controlplane.asset.spi.domain.Asset.EDC_ASSET_PROPERTIES;

/**
 * Transforms an expanded JSON-LD object into a Europeana DCAT distribution model.
 *
 * @author Srishti Singh
 * @since 2026-11-16
 */
public class JsonObjectToEuropeanaDcatDistributionTransformer
        extends AbstractJsonLdTransformer<JsonObject, EuropeanaDistribution> {

    private static final String EDC_PROPERTIES = EDC_NAMESPACE + "properties";
    private static final String EDC_DATA_ADDRESS = EDC_NAMESPACE + "dataAddress";

    public JsonObjectToEuropeanaDcatDistributionTransformer() {
        super(JsonObject.class, EuropeanaDistribution.class);
    }

    @Override
    public @Nullable EuropeanaDistribution transform(@NotNull JsonObject object, @NotNull TransformerContext context) {
        var distribution = new EuropeanaDistribution(nodeId(object));
        visitProperties(object, (key, value) ->
                transformProperties(key, value, distribution, context));

        return distribution;
    }

    private void transformProperties(String key, JsonValue value, EuropeanaDistribution distribution, TransformerContext context) {
        if (ID.equals(key)) {
            transformString(value, distribution::setDistributionId, context);
        } else if (EDC_ASSET_PROPERTIES.equals(key)) {
            var propertiesObject = getFirstObject(value, context);
            if (propertiesObject != null) {
                distribution.setProperties(transformProperties(propertiesObject, context));
            }
        } else if (EDC_DATA_ADDRESS.equals(key)) {
            var dataAddressObject = getFirstObject(value, context);

            if (dataAddressObject != null) {
                var dataAddress = context.transform(
                        dataAddressObject,
                        DataAddress.class
                );

                distribution.setDataAddress(dataAddress);
            }
        }
    }

    /**
     * Transforms a JSON value containing distribution properties into a map.
     * The supplied value must be a JSON object; otherwise, a problem is reported through the
     * transformer context and an empty map is returned.
     * Each object entry is added to the resulting map using its JSON object key and a converted
     * Java value.
     *
     * @param value the JSON value containing the distribution properties to transform
     * @param context the transformer context used to report invalid property structures
     * @return a map containing the transformed distribution properties, or an empty map if
     *         the supplied value is not a JSON object
     */
    private Map<String, Object> transformProperties(JsonValue value, TransformerContext context) {
        if (value.getValueType() != JsonValue.ValueType.OBJECT) {
            context.reportProblem("Distribution properties must be a JSON object");
            return Collections.emptyMap();
        }

        var properties = new HashMap<String, Object>();
        value.asJsonObject().forEach((key, propertyValue) ->
                properties.put(key, transformPropertyValue(propertyValue))
        );

        return properties;
    }

    /**
     * Converts a JSON value to the corresponding Java representation.
     * Strings, booleans, and null values are converted to their Java equivalents.
     * Integral numbers are converted to Long values, non-integral numbers are converted to BigDecimal values,
     * and JSON objects and arrays are returned as JsonObject and JsonArray instances.
     *
     * @param value the JSON value to transform
     * @return the transformed Java value, or null if the supplied JSON value represents null
     */
    private Object transformPropertyValue(JsonValue value) {
        return switch (value.getValueType()) {
            case STRING -> ((JsonString) value).getString();

            case NUMBER -> {
                var number = (JsonNumber) value;
                yield number.isIntegral()
                        ? number.longValue()
                        : number.bigDecimalValue();
            }

            case TRUE -> true;
            case FALSE -> false;
            case NULL -> null;

            case ARRAY -> {
                var array = value.asJsonArray();

                // Expanded JSON-LD commonly represents even a single value
                // as an array. Unwrap singleton arrays.
                if (array.size() == 1) {
                    yield transformPropertyValue(array.get(0));
                }

                yield array.stream()
                        .map(this::transformPropertyValue)
                        .toList();
            }

            case OBJECT -> {
                var object = value.asJsonObject();

                // JSON-LD literal value.
                if (object.containsKey("@value")) {
                    yield transformPropertyValue(object.get("@value"));
                }

                // JSON-LD identifier reference.
                if (object.containsKey("@id")) {
                    yield object.getString("@id");
                }

                // Handle nested objects recursively.
                var map = new HashMap<String, Object>();
                object.forEach((key, nestedValue) ->
                        map.put(key, transformPropertyValue(nestedValue))
                );
                yield map;
            }
        };
    }

    /**
     * Returns the supplied JSON value as a JSON object, or the first element when the value is an array.
     *
     * @param value the JSON value to read as an object or as an array containing an object
     * @param context the transformer context used to report invalid JSON structures
     * @return the JSON object represented by the supplied value, the first object in the supplied array,
     *         or null if the value is not a valid object source
     */
    private JsonObject getFirstObject(
            JsonValue value,
            TransformerContext context) {

        if (value.getValueType() == JsonValue.ValueType.OBJECT) {
            return value.asJsonObject();
        }

        if (value.getValueType() == JsonValue.ValueType.ARRAY) {
            var array = value.asJsonArray();

            if (array.isEmpty()
                    || array.get(0).getValueType() != JsonValue.ValueType.OBJECT) {
                context.reportProblem("Expected an object in expanded JSON-LD array");
                return null;
            }

            return array.getJsonObject(0);
        }

        context.reportProblem("Expected a JSON object or an array containing an object");
        return null;
    }
}