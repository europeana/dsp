package eu.europeana.dsp.connector.controlplane.catalog.spi;

import eu.europeana.dsp.connector.controlplane.catalog.spi.definitions.EuropeanaDcatDistribution;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

import org.eclipse.edc.jsonld.spi.transformer.AbstractJsonLdTransformer;
import org.eclipse.edc.transform.spi.TransformerContext;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.ID;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.TYPE;
import static org.eclipse.edc.jsonld.spi.PropertyAndTypeNames.DCAT_ACCESS_SERVICE_ATTRIBUTE;
import static org.eclipse.edc.jsonld.spi.PropertyAndTypeNames.DCAT_DISTRIBUTION_TYPE;
import static org.eclipse.edc.jsonld.spi.PropertyAndTypeNames.DCT_FORMAT_ATTRIBUTE;

/**
 * Transforms a {@link EuropeanaDcatDistribution} object into a {@link JsonObject} representation,
 * preserving key metadata and properties specific to the Europeana and DCAT contexts.
 * This transformation is performed using a {@link JsonBuilderFactory} instance to construct the JSON.
 * We are overriding the default implementation of the JsonObjectFromDistributionTransformer.
 * @link : https://github.com/eclipse-edc/Connector/blob/f1a5b8202cfdd7ebb28edd482210b13e74e57b9e/data-protocols/dsp/dsp-lib/src/main/java/org/eclipse/edc/protocol/dsp/catalog/transform/from/JsonObjectFromDistributionTransformer.java
 *
 * @author Srishti Singh
 * @since 2026-09-1
 */
public class JsonObjectFromEuropeanaDcatDistributionTransformer
        extends AbstractJsonLdTransformer<EuropeanaDcatDistribution, JsonObject> {

    private final JsonBuilderFactory jsonFactory;

    public JsonObjectFromEuropeanaDcatDistributionTransformer(JsonBuilderFactory jsonFactory) {
        super(EuropeanaDcatDistribution.class, JsonObject.class);
        this.jsonFactory = jsonFactory;
    }

    @Override
    public @Nullable JsonObject transform(
            @NotNull EuropeanaDcatDistribution distribution,
            @NotNull TransformerContext context) {

        var builder = jsonFactory.createObjectBuilder()
                .add(TYPE, DCAT_DISTRIBUTION_TYPE);

       // EDC Distribution.format. EDC's format is serialized as dct:format.
        if (distribution.getFormat() != null) {
            builder.add(
                    DCT_FORMAT_ATTRIBUTE,
                    distribution.getFormat()
            );
        }

        // EDC Distribution.dataService. Let EDC serialize the DataService using its existing transformer.
        if (distribution.getDataService() != null) {
            builder.add(
                    DCAT_ACCESS_SERVICE_ATTRIBUTE,
                    context.transform(
                            distribution.getDataService(),
                            JsonObject.class
                    )
            );
        }

        // Europeana/DCAT-specific properties.
        if (distribution.getProperties() != null) {
            distribution.getProperties().forEach(
                    (property, value) ->
                            addProperty(builder, property, value)
            );
        }

        return builder.build();
    }

    private void addProperty(JsonObjectBuilder builder, String property, Object value) {
        if (value == null) {
            return;
        }
        // already serialized
        if ("dct:format".equals(property)) {
            return;
        }

        if (isUri(value)) {
            builder.add(property, jsonFactory.createObjectBuilder()
                    .add(ID, value.toString())
                    .build());
            return;
        }

        if (value instanceof String stringValue) {
            builder.add(property, stringValue);
        } else if (value instanceof Integer integerValue) {
            builder.add(property, integerValue);
        } else if (value instanceof Long longValue) {
            builder.add(property, longValue);
        } else if (value instanceof Double doubleValue) {
            builder.add(property, doubleValue);
        } else if (value instanceof Boolean booleanValue) {
            builder.add(property, booleanValue);
        } else if (value instanceof JsonObject jsonObject) {
            builder.add(property, jsonObject);
        } else {
            builder.add(property, value.toString()); // fallback
        }
    }

    private boolean isUri(Object value) {
        if (!(value instanceof String s)) {
            return false;
        }

        try {
            return URI.create(s).isAbsolute();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}