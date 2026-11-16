package eu.europeana.dsp.connector.controlplane.catalog.spi.transformer.to;

import eu.europeana.dsp.connector.controlplane.catalog.spi.definitions.EuropeanaDistribution;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import org.eclipse.edc.connector.controlplane.asset.spi.domain.Asset;
import org.eclipse.edc.connector.controlplane.transform.edc.to.JsonObjectToAssetTransformer;
import org.eclipse.edc.jsonld.spi.transformer.AbstractJsonLdTransformer;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static eu.europeana.dsp.connector.controlplane.catalog.spi.DspVocabulary.EUROPEANA_DISTRIBUTIONS;

/**
 * Transforms a JSON-LD asset representation into an {@link Asset} enriched with Europeana-specific
 * distribution metadata.
 *
 * The base asset transformation is delegated to the configured {@link JsonObjectToAssetTransformer}.
 * If the base transformation succeeds, this transformer reads the Europeana distributions field from
 * the source JSON object and transforms each valid distribution entry into a {@link EuropeanaDistribution}
 * through the provided {@link TransformerContext} and adds them asset properties
 *
 * @author Srishti Singh
 * @since 2026-11-16
 */
public class JsonObjectToEuropeanaAssetTransformer extends AbstractJsonLdTransformer<JsonObject, Asset> {

    private final JsonObjectToAssetTransformer assetTransformer;

    public JsonObjectToEuropeanaAssetTransformer(JsonObjectToAssetTransformer assetTransformer) {
        super(JsonObject.class, Asset.class);
        this.assetTransformer = assetTransformer;
    }

    @Override
    public @Nullable Asset transform(@NotNull JsonObject jsonObject, @NotNull TransformerContext context) {
        var asset = assetTransformer.transform(jsonObject, context);
        if (asset == null) {
            return null;
        }

        // get the distibutions
        var distributions = extractDistributions(jsonObject, context);
        if (!distributions.isEmpty()) {
            asset.getProperties().put(EUROPEANA_DISTRIBUTIONS, distributions);
        }
        return asset;
    }

    /**
     * Extracts Europeana distributions from the supplied JSON object.
     * If the distributions field is missing or is not a JSON array, an empty list is returned.
     * Each array entry must be a JSON object and is transformed into a EuropeanaDistribution.
     * Invalid non-object entries are reported through the transformer context and skipped.
     *
     * @param jsonObject the JSON object containing the Europeana distributions field
     * @param context the transformer context used to report problems and transform distribution entries
     * @return the list of successfully transformed Europeana distributions, or an empty list if none are present
     */
    private List<EuropeanaDistribution> extractDistributions(JsonObject jsonObject, TransformerContext context) {
        var distributions = new ArrayList<EuropeanaDistribution>();
        var value = jsonObject.get(EUROPEANA_DISTRIBUTIONS);

        if (value == null || value.getValueType() != JsonValue.ValueType.ARRAY) {
            return distributions;
        }

        value.asJsonArray().forEach(item -> {
            if (item.getValueType() != JsonValue.ValueType.OBJECT) {
                context.reportProblem(
                        "Distribution entries must be JSON objects"
                );
                return;
            }

            // JsonObjectToEuropeanaDcatDistributionTransformer will be invoked
            var distribution = context.transform(
                    item.asJsonObject(),
                    EuropeanaDistribution.class
            );

            if (distribution != null) {
                distributions.add(distribution);
            }
        });

        return distributions;
    }
}