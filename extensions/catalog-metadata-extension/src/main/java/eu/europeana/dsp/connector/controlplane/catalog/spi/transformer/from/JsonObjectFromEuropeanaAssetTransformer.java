package eu.europeana.dsp.connector.controlplane.catalog.spi.transformer.from;

import eu.europeana.dsp.connector.controlplane.catalog.spi.definitions.EuropeanaDistribution;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import org.eclipse.edc.connector.controlplane.asset.spi.domain.Asset;
import org.eclipse.edc.connector.controlplane.transform.edc.from.JsonObjectFromAssetTransformer;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

import static eu.europeana.dsp.connector.controlplane.catalog.spi.DspVocabulary.EUROPEANA_DISTRIBUTIONS;

public class JsonObjectFromEuropeanaAssetTransformer extends JsonObjectFromAssetTransformer {

    private final JsonBuilderFactory jsonFactory;

    public JsonObjectFromEuropeanaAssetTransformer(JsonBuilderFactory jsonFactory, TypeManager typeManager,
                                                   String typeContext) {
        super(jsonFactory, typeManager, typeContext);
        this.jsonFactory = jsonFactory;
    }

    @Override
    public @Nullable JsonObject transform(@NotNull Asset asset, @NotNull TransformerContext context) {
        var properties = new HashMap<>(asset.getProperties());

        // Extract Europeana distributions.
        var distributionsValue = properties.remove(EUROPEANA_DISTRIBUTIONS);

        if (!(distributionsValue instanceof List<?> distributions) || distributions.isEmpty()) {
            // No Europeana distributions: use standard EDC transformation.
            return super.transform(asset, context);
        }

        // Build a clean asset without the internal distributions property.
        Asset cleanAsset = Asset.Builder.newInstance()
                .id(asset.getId())
                .properties(properties) // without distributions
                .privateProperties(asset.getPrivateProperties())
                .dataAddress(asset.getDataAddress())
                .createdAt(asset.getCreatedAt())
                .dataplaneMetadata(asset.getDataplaneMetadata())
                .participantContextId(asset.getParticipantContextId())
                .build();

        // Let EDC handle the standard asset transformation.
        var assetJson = super.transform(cleanAsset, context);

        if (assetJson == null) {
            return null;
        }

        // Transform Europeana distributions.
        var distributionArray = jsonFactory.createArrayBuilder();

        for (var item : distributions) {
            if (item instanceof EuropeanaDistribution distribution) {
                // will invoke JsonObjectFromEuropeanaDistributionTransformer
                var distributionJson = context.transform(
                        distribution,
                        JsonObject.class
                );

                if (distributionJson != null) {
                    distributionArray.add(distributionJson);
                }
            } else {
                context.reportProblem(
                        "Invalid Europeana distribution: expected EuropeanaDistribution"
                );
            }
        }

        // Add distribution at the ROOT level, alongside properties.
        return jsonFactory.createObjectBuilder(assetJson)
                .add(EUROPEANA_DISTRIBUTIONS, distributionArray)
                .build();
    }
}
