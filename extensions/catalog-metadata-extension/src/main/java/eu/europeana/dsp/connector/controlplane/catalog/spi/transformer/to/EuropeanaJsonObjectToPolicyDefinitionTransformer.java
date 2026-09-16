package eu.europeana.dsp.connector.controlplane.catalog.spi.transformer.to;

import jakarta.json.JsonObject;
import org.eclipse.edc.connector.controlplane.policy.spi.PolicyDefinition;
import org.eclipse.edc.jsonld.spi.transformer.AbstractJsonLdTransformer;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Optional;

import static eu.europeana.dsp.connector.controlplane.catalog.spi.DspVocabulary.*;
import static org.eclipse.edc.connector.controlplane.policy.spi.PolicyDefinition.EDC_POLICY_DEFINITION_POLICY;
import static org.eclipse.edc.connector.controlplane.policy.spi.PolicyDefinition.EDC_POLICY_DEFINITION_PRIVATE_PROPERTIES;

/**
 * A transformer class that converts a {@link JsonObject} into a {@link PolicyDefinition}.
 * This implementation extends the {@code AbstractJsonLdTransformer} to provide specific transformation
 * behavior for Europeana policy definitions.
 *
 * This class is designed to facilitate the transformation of policy-related data into a structured,
 * JSON-LD-compatible format, specifically tailored for use within Europeana's ecosystem.
 *
 * The publicProperties are stored inside the existing private-properties structure.
 * See:
 * "privateProperties": [
 *     {
 *         "europeanaProperties": {...}
 *     }
 * ]
 *
 * the class is responsible for transforming publicProperties (sent in the request) into the privateProperties
 * of Policy Definitions.
 *
 * @author Srishti Singh
 * @since 2026-09-15
 */
public class EuropeanaJsonObjectToPolicyDefinitionTransformer extends AbstractJsonLdTransformer<JsonObject, PolicyDefinition> {

    public EuropeanaJsonObjectToPolicyDefinitionTransformer() {
        super(JsonObject.class, PolicyDefinition.class);
    }

    @Override
    public @Nullable PolicyDefinition transform(@NotNull JsonObject input, @NotNull TransformerContext context) {
        var builder = PolicyDefinition.Builder.newInstance();
        builder.id(nodeId(input));

        var policy = Optional.of(EDC_POLICY_DEFINITION_POLICY)
                .map(input::get)
                .map(it -> transformObject(it, Policy.class, context))
                .orElse(null);

        if (policy == null) {
            return null;
        }
//        else {
//            builder.policy(policy);
//        }

        var privateProperties = input.get(EDC_POLICY_DEFINITION_PRIVATE_PROPERTIES);
        if (privateProperties != null) {
            var props = privateProperties.asJsonArray().getJsonObject(0);
            visitProperties(props, (key, value) -> builder.privateProperty(key, transformGenericProperty(value, context)));
        }

        // ADDING publicProperties (from the request) in the private properties
        // of policy definition as europeana.publicproperties
        var publicProperties = input.get(EDC_POLICY_DEFINITION_PUBLIC_PROPERTIES);

        if (publicProperties != null) {
            var publicProps = publicProperties.asJsonArray().getJsonObject(0);

            var publicPropertiesMap = new HashMap<String, Object>();

            visitProperties(publicProps, (key, value) ->
                    publicPropertiesMap.put(
                            key,
                            transformGenericProperty(value, context)
                    )
            );

            builder.privateProperty(
                    POLICY_DEFINITION_EUROPEANA_PROPERTIES,
                    publicPropertiesMap
            );

            // Also put it on Policy for the DSP catalog / hasPolicy
            policy = policy.toBuilder()
                    .extensibleProperty(
                            POLICY_DEFINITION_EUROPEANA_PROPERTIES,
                            publicPropertiesMap
                    )
                    .build();
        }
        builder.policy(policy);

        var response = builder.build();
        System.out.println("Processed privateProperties ===> "+ response.getPrivateProperties());
        System.out.println(
                "Policy extensibleProperties ===> "
                        + response.getPolicy().getExtensibleProperties()
        );
        return response;
    }
}