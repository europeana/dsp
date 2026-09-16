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

    private final TypeManager typeManager;
    private final String typeContext;
    private final JsonBuilderFactory jsonFactory;

    public EuropeanaJsonObjectFromPolicyTransformer(JsonBuilderFactory jsonFactory, ParticipantIdMapper participantIdMapper,
                                                    TransformerConfig config, TypeManager typeManager, String typeContext) {
        super(jsonFactory, participantIdMapper, config);
        this.typeManager = typeManager;
        this.typeContext = typeContext;
        this.jsonFactory = jsonFactory;
    }

    @Override
    public @Nullable JsonObject transform(@NotNull Policy policy, @NotNull TransformerContext context) {
        var policyJson = super.transform(policy, context); // already transformed policy
        if (policyJson == null) {
            return null;
        }

        // add now the public properties from the extensibleProperties
        var builder = Json.createObjectBuilder(policyJson);
        var publicProperties = policy.getExtensibleProperties();

        if (publicProperties != null && !publicProperties.isEmpty()) {
            var publicPropertiesBuilder = jsonFactory.createObjectBuilder();

            transformProperties(
                    (Map<String, ?>) publicProperties.get(POLICY_DEFINITION_EUROPEANA_PROPERTIES),
                    publicPropertiesBuilder,
                    typeManager.getMapper(typeContext),
                    context);

            builder.add(
                    EDC_POLICY_DEFINITION_PUBLIC_PROPERTIES,
                    publicPropertiesBuilder
            );
        }
        return builder.build();
    }
}
