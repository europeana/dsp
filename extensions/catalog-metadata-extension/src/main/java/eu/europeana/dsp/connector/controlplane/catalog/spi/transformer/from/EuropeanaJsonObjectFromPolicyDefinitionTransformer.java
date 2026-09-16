package eu.europeana.dsp.connector.controlplane.catalog.spi.transformer.from;

import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import org.eclipse.edc.connector.controlplane.policy.spi.PolicyDefinition;
import org.eclipse.edc.jsonld.spi.transformer.AbstractJsonLdTransformer;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static eu.europeana.dsp.connector.controlplane.catalog.spi.DspVocabulary.*;
import static org.eclipse.edc.connector.controlplane.policy.spi.PolicyDefinition.*;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.ID;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.TYPE;
import static org.eclipse.edc.jsonld.spi.PropertyAndTypeNames.EDC_CREATED_AT;

/**
 * Transforms a {@link PolicyDefinition} object into a {@link JsonObject} representation.
 * The transformation includes core attributes like ID, type, and creation timestamp,
 * along with handling nested policies and private/public properties.
 *
 * This class is designed to facilitate the transformation of policy-related data into a structured,
 * JSON-LD-compatible format, specifically tailored for use within Europeana's ecosystem.
 *
 * The privateProperties of Policy Definitions stores also publicProperties.
 * we're deliberately storing public properties inside the existing private-properties structure.
 * See:
 * "privateProperties": [
 *     {
 *         "europeanaProperties": {...}
 *     }
 * ]
 *
 * the class is responsible for transforming the publicProperties while
 * preserving the existing privateProperties structure.
 *
 * @author Srishti Singh
 * @since 2026-09-15
 */
public class EuropeanaJsonObjectFromPolicyDefinitionTransformer extends AbstractJsonLdTransformer<PolicyDefinition, JsonObject> {

    private final TypeManager typeManager;
    private final String typeContext;
    private final JsonBuilderFactory jsonFactory;

    public EuropeanaJsonObjectFromPolicyDefinitionTransformer(JsonBuilderFactory jsonFactory, TypeManager typeManager, String typeContext) {
        super(PolicyDefinition.class, JsonObject.class);
        this.jsonFactory = jsonFactory;
        this.typeManager = typeManager;
        this.typeContext = typeContext;
    }

    @Override
    public @Nullable JsonObject transform(
            @NotNull PolicyDefinition input,
            @NotNull TransformerContext context) {

        var objectBuilder = jsonFactory.createObjectBuilder();

        objectBuilder.add(ID, input.getId());
        objectBuilder.add(TYPE, EDC_POLICY_DEFINITION_TYPE);
        objectBuilder.add(EDC_CREATED_AT, input.getCreatedAt());

        var policy = context.transform(input.getPolicy(), JsonObject.class);
        objectBuilder.add(EDC_POLICY_DEFINITION_POLICY, policy);

        var privateProperties = input.getPrivateProperties();

        if (!privateProperties.isEmpty()) {
            var publicProperties = privateProperties.get(POLICY_DEFINITION_EUROPEANA_PROPERTIES);

            // get the europeana.publicProperties ans transform to publicProperties
            if (publicProperties instanceof Map<?, ?> ) {
                var publicPropertiesBuilder = jsonFactory.createObjectBuilder();

                transformProperties((Map<String, ?>) publicProperties, publicPropertiesBuilder, typeManager.getMapper(typeContext), context);

                objectBuilder.add(
                        EDC_POLICY_DEFINITION_PUBLIC_PROPERTIES,
                        publicPropertiesBuilder
                );
            }

            // preserve the existing privateProperties structure
            var actualPrivateProperties = new HashMap<>(privateProperties);
            actualPrivateProperties.remove(POLICY_DEFINITION_EUROPEANA_PROPERTIES);

            if (!actualPrivateProperties.isEmpty()) {
                var privatePropBuilder = jsonFactory.createObjectBuilder();

                transformProperties(
                        actualPrivateProperties,
                        privatePropBuilder,
                        typeManager.getMapper(typeContext),
                        context
                );

                objectBuilder.add(
                        EDC_POLICY_DEFINITION_PRIVATE_PROPERTIES,
                        privatePropBuilder
                );
            }
        }

        return objectBuilder.build();
    }
}
