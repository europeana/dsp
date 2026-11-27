package eu.europeana.dsp.connector.controlplane.catalog.spi.transformer.from;

import eu.europeana.dsp.connector.controlplane.catalog.spi.definitions.EuropeanaDistribution;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import org.eclipse.edc.jsonld.spi.transformer.AbstractJsonLdTransformer;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static eu.europeana.dsp.connector.controlplane.catalog.spi.DspVocabulary.DATA_ADDRESS_ATTRIBUTE;
import static org.eclipse.edc.connector.controlplane.asset.spi.domain.Asset.EDC_ASSET_PROPERTIES;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.ID;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.TYPE;
import static org.eclipse.edc.jsonld.spi.PropertyAndTypeNames.DCAT_DISTRIBUTION_TYPE;

/**
 * Transforms a {@link EuropeanaDistribution} object into a {@link JsonObject} representation,
 * preserving key metadata and properties specific to the Europeana and DCAT contexts.
 * This transformation is performed using a {@link JsonBuilderFactory} instance to construct the JSON.
 * We are overriding the default implementation of the JsonObjectFromDistributionTransformer.
 *
 * @link : https://github.com/eclipse-edc/Connector/blob/f1a5b8202cfdd7ebb28edd482210b13e74e57b9e/data-protocols/dsp/dsp-lib/src/main/java/org/eclipse/edc/protocol/dsp/catalog/transform/from/JsonObjectFromDistributionTransformer.java
 *
 * @author Srishti Singh
 * @since 2026-09-1
 */
public class JsonObjectFromEuropeanaDcatDistributionTransformer
        extends AbstractJsonLdTransformer<EuropeanaDistribution, JsonObject> {

    private final JsonBuilderFactory jsonFactory;
    private final TypeManager typeManager;
    private final String typeContext;

    public JsonObjectFromEuropeanaDcatDistributionTransformer(
            JsonBuilderFactory jsonFactory,
            TypeManager typeManager,
            String typeContext) {

        super(EuropeanaDistribution.class, JsonObject.class);
        this.jsonFactory = jsonFactory;
        this.typeManager = typeManager;
        this.typeContext = typeContext;
    }

    @Override
    public @Nullable JsonObject transform(
            @NotNull EuropeanaDistribution distribution,
            @NotNull TransformerContext context) {

        var builder = jsonFactory.createObjectBuilder()
                //.add("@context", CONTEXT)
                .add(TYPE, DCAT_DISTRIBUTION_TYPE);

        // Distribution identifier.
        if (distribution.getDistributionId() != null) {
            builder.add(ID, distribution.getDistributionId());
        }

        // Distribution properties.
        if (distribution.getProperties() != null && !distribution.getProperties().isEmpty()) {
            var propertiesBuilder = jsonFactory.createObjectBuilder();
            transformProperties(distribution.getProperties(), propertiesBuilder, typeManager.getMapper(typeContext), context);
            builder.add(EDC_ASSET_PROPERTIES, propertiesBuilder);
        }

        // Distribution data address.
        if (distribution.getDataAddress() != null) {
            var dataAddress = context.transform(distribution.getDataAddress(), JsonObject.class);
            if (dataAddress != null) {
                builder.add(DATA_ADDRESS_ATTRIBUTE, dataAddress);
            }
        }
        return builder.build();
    }
}