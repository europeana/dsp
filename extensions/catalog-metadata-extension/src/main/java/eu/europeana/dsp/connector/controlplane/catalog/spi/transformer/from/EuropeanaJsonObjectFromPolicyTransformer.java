package eu.europeana.dsp.connector.controlplane.catalog.spi.transformer.from;

import jakarta.json.*;
import org.eclipse.edc.connector.controlplane.transform.odrl.from.JsonObjectFromPolicyTransformer;
import org.eclipse.edc.participant.spi.ParticipantIdMapper;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import static eu.europeana.dsp.connector.controlplane.catalog.spi.DspVocabulary.EDC_POLICY_DEFINITION_PUBLIC_PROPERTIES;
import static eu.europeana.dsp.connector.controlplane.catalog.spi.DspVocabulary.POLICY_DEFINITION_EUROPEANA_PROPERTIES;

/**
 * Transforms a {@link Policy} object into a {@link JsonObject} representation.
 * The existing transformer already does everything we need for now except for handling
 * extensibleProperties field of Policy. We save the public properties of policy definitions also in
 * the policy extensibleProperties as europeanaProperties.
 *
 * "extensibleProperties": [
 *     {
 *         "europeanaProperties": {...}
 *     }
 * ]
 *
 * Hence the class transforms these properties as well for the catalog gerneration response.
 * We add the extra field properties to the already transformed policy
 *
 * @author Srishti singh
 * @since 2026-09-16
 */
public class EuropeanaJsonObjectFromPolicyTransformer extends JsonObjectFromPolicyTransformer {

    public EuropeanaJsonObjectFromPolicyTransformer(JsonBuilderFactory jsonFactory, ParticipantIdMapper participantIdMapper) {
        super(jsonFactory, participantIdMapper, new TransformerConfig());
    }

    public EuropeanaJsonObjectFromPolicyTransformer(JsonBuilderFactory jsonFactory, ParticipantIdMapper participantIdMapper, TransformerConfig config) {
        super(jsonFactory, participantIdMapper, config);
    }

    @Override
    public @Nullable JsonObject transform(@NotNull Policy policy, @NotNull TransformerContext context) {
        var policyJson = super.transform(policy, context); // already transformed policy
        if (policyJson == null) {
            return null;
        }

        // add now the europeana properties from the extensibleProperties
        var builder = Json.createObjectBuilder(policyJson);
        var publicProperties = policy.getExtensibleProperties();
        if (publicProperties != null && !publicProperties.isEmpty()) {
            Map<String, Object> europeanaProp = (Map<String, Object>) publicProperties.get(POLICY_DEFINITION_EUROPEANA_PROPERTIES);
            europeanaProp.forEach((key, value) -> {
                if (value == null) {
                    builder.addNull(key);
                } else if (value instanceof JsonValue jsonValue) {
                    builder.add(key, jsonValue);
                } else {
                    builder.add(key, value.toString());
                }
            });
        }
        return builder.build();
    }
}
