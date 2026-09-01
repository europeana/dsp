package org.eclipse.edc.europeana.catalog;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import org.eclipse.edc.connector.controlplane.catalog.spi.Dataset;
import org.eclipse.edc.connector.controlplane.catalog.spi.Distribution;
import org.eclipse.edc.jsonld.spi.transformer.AbstractJsonLdTransformer;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.ID;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.TYPE;
import static org.eclipse.edc.jsonld.spi.PropertyAndTypeNames.DCAT_DATASET_TYPE;
import static org.eclipse.edc.jsonld.spi.PropertyAndTypeNames.DCAT_DISTRIBUTION_ATTRIBUTE;
import static org.eclipse.edc.jsonld.spi.PropertyAndTypeNames.ODRL_POLICY_ATTRIBUTE;

public class JsonObjectFromDatasetWithDistributionMetadataTransformer
        extends AbstractJsonLdTransformer<Dataset, JsonObject> {

    private final JsonBuilderFactory jsonFactory;
    private final TypeManager typeManager;
    private final String typeContext;

    public JsonObjectFromDatasetWithDistributionMetadataTransformer(
            JsonBuilderFactory jsonFactory,
            TypeManager typeManager,
            String typeContext) {

        super(Dataset.class, JsonObject.class);

        this.jsonFactory = jsonFactory;
        this.typeManager = typeManager;
        this.typeContext = typeContext;
    }

    @Override
    public @Nullable JsonObject transform(
            @NotNull Dataset dataset,
            @NotNull TransformerContext context) {

        var objectBuilder = jsonFactory.createObjectBuilder()
                .add(ID, dataset.getId())
                .add(TYPE, DCAT_DATASET_TYPE);

        //Keep the existing EDC policy transformation.
        objectBuilder.add(ODRL_POLICY_ATTRIBUTE, transformOffers(dataset, context));

        //Transform every Distribution independently.This is the important part for multiple distributions.
        var distributions = dataset.getDistributions()
                .stream()
                .map(distribution ->
                        transformDistribution(
                                dataset,
                                distribution,
                                context
                        )
                )
                .collect(
                        jsonFactory::createArrayBuilder,
                        JsonArrayBuilder::add,
                        JsonArrayBuilder::add
                )
                .build();

        objectBuilder.add(DCAT_DISTRIBUTION_ATTRIBUTE, distributions);

        /*
         * Keep ALL original Dataset properties.
         *
         * Therefore distribution-specific properties also remain
         * on the Dataset, as required.
         */
        transformProperties(dataset.getProperties(), objectBuilder, typeManager.getMapper(typeContext), context);
        return objectBuilder.build();
    }

    private JsonObject transformDistribution(Dataset dataset, Distribution distribution, TransformerContext context) {
        // First let EDC create the normal Distribution. This preserves format, dataService, accessService, etc.
        var transformedDistribution = context.transform(distribution, JsonObject.class);

        if (transformedDistribution == null) {
            context.problem();
            return JsonValue.EMPTY_JSON_OBJECT;
        }

        var builder = Json.createObjectBuilder(transformedDistribution);

         //Add metadata belonging specifically to this Distribution.
        DistributionMetadataExtractor.addDistributionMetadata(builder, jsonFactory, dataset.getProperties(), distribution.getFormat());
        return builder.build();
    }

    private JsonValue transformOffers(Dataset dataset, TransformerContext context) {
        var builder = jsonFactory.createArrayBuilder();
        for (var entry : dataset.getOffers().entrySet()) {
            var policy = context.transform(entry.getValue(), JsonObject.class);
            var policyBuilder = jsonFactory.createObjectBuilder(policy);
            policyBuilder.add(ID, Json.createValue(entry.getKey()));
            builder.add(policyBuilder.build());
        }

        return builder.build();
    }
}
