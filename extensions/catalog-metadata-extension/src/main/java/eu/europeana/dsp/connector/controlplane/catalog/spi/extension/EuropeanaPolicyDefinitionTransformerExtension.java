package eu.europeana.dsp.connector.controlplane.catalog.spi.extension;

import eu.europeana.dsp.connector.controlplane.catalog.spi.transformer.from.EuropeanaJsonObjectFromPolicyDefinitionTransformer;
import eu.europeana.dsp.connector.controlplane.catalog.spi.transformer.from.EuropeanaJsonObjectFromPolicyTransformer;
import eu.europeana.dsp.connector.controlplane.catalog.spi.transformer.to.EuropeanaJsonObjectToPolicyDefinitionTransformer;
import jakarta.json.Json;
import org.eclipse.edc.connector.controlplane.transform.odrl.from.JsonObjectFromPolicyTransformer;
import org.eclipse.edc.jsonld.spi.JsonLd;
import org.eclipse.edc.participant.spi.ParticipantIdMapper;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.edc.transform.spi.TypeTransformerRegistry;

import java.util.Map;

import static eu.europeana.dsp.connector.controlplane.catalog.spi.DspVocabulary.DSP_TRANSFORMER_CONTEXT_V_2025_1;
import static org.eclipse.edc.spi.constants.CoreConstants.JSON_LD;

@Extension(EuropeanaPolicyDefinitionTransformerExtension.NAME)
public class EuropeanaPolicyDefinitionTransformerExtension implements ServiceExtension {

    public static final String NAME = "Europeana Policy definition Transformer Extension";

    @Inject
    ParticipantIdMapper participantIdMapper;

    @Inject
    private TypeTransformerRegistry transformerRegistry;

    @Inject
    TypeManager typeManager;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        context.getMonitor().info("Initializing " + NAME);
        var jsonFactory = Json.createBuilderFactory(Map.of());
        /**
         * See: The context of the policy definition transformer is registered for
         * <a href="https://github.com/eclipse-edc/Connector/blob/9f850fb1bec86cd1485a3a28b2d52cd5c8a6dcdd/extensions/control-plane/api/management-api/policy-definition-api/src/main/java/org/eclipse/edc/connector/controlplane/api/management/policy/PolicyDefinitionApiExtension.java#L82">
         * PolicyDefinitionApiExtension
         * </a>
         */
        transformerRegistry
                .forContext("management-api")
                .register(
                        new EuropeanaJsonObjectFromPolicyDefinitionTransformer(jsonFactory, typeManager, JSON_LD)
                );

        transformerRegistry
                .forContext("management-api")
                .register(
                        new EuropeanaJsonObjectToPolicyDefinitionTransformer()
                );

        /**
         * See: The context of the JsonObjectFromPolicyTransformer is registered for
         * <a href="https://github.com/eclipse-edc/Connector/blob/c1c5a54a7d9febcc6d0d0778575defc1c204aba7/data-protocols/dsp/dsp-2025/dsp-catalog-2025/dsp-catalog-transform-2025/src/main/java/org/eclipse/edc/protocol/dsp/catalog/transform/v2025/DspCatalogTransformV2025Extension.java#L76">
         * DspCatalogTransformV2025Extension
         * </a>
         */
        transformerRegistry.forContext(DSP_TRANSFORMER_CONTEXT_V_2025_1)
                .register(
                        new EuropeanaJsonObjectFromPolicyTransformer(jsonFactory, participantIdMapper,
                                new JsonObjectFromPolicyTransformer.TransformerConfig(), typeManager, JSON_LD));
    }
}
