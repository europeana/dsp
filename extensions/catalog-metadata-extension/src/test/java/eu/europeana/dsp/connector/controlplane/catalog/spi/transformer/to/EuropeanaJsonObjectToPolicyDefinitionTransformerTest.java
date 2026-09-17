package eu.europeana.dsp.connector.controlplane.catalog.spi.transformer.to;

import jakarta.json.JsonObjectBuilder;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static eu.europeana.dsp.connector.controlplane.catalog.spi.DspVocabulary.*;
import static jakarta.json.Json.createArrayBuilder;
import static jakarta.json.Json.createObjectBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.connector.controlplane.policy.spi.PolicyDefinition.*;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.CONTEXT;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.ID;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.TYPE;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.VOCAB;
import static org.eclipse.edc.spi.constants.CoreConstants.EDC_NAMESPACE;
import static org.eclipse.edc.spi.constants.CoreConstants.EDC_PREFIX;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for {@code EuropeanaJsonObjectToPolicyDefinitionTransformer}.
 *
 * This class is responsible for unit testing the transformation of a JSON-LD object
 * into a {@code PolicyDefinition} object using the {@code EuropeanaJsonObjectToPolicyDefinitionTransformer}.
 * It ensures that the transformation process works as intended, including handling of
 * public and private properties and proper assignment of transformed fields.
 *
 * @author Srishti Singh
 * @since 2026-09-15
 */
public class EuropeanaJsonObjectToPolicyDefinitionTransformerTest {

    private final EuropeanaJsonObjectToPolicyDefinitionTransformer transformer = new EuropeanaJsonObjectToPolicyDefinitionTransformer();
    private final TransformerContext context = mock(TransformerContext.class);

    @Test
    void transform_withPublicProperties() {
        when(context.transform(any(), eq(Object.class))).thenReturn("test-val");

        var policyJson = createObjectBuilder().build();
        var policy = Policy.Builder.newInstance().build();

        when(context.transform(any(), eq(Policy.class))).thenReturn(policy);

        var publicProperties = createObjectBuilder()
                .add("dct:title", "Europeana Test Policy")
                .add("dct:description", "Test policy description")
                .add("test", "Test value")
                .build();

        var json = createObjectBuilder()
                .add(CONTEXT, createContextBuilder().addNull(EDC_PREFIX).build())
                .add(ID, "definitionId")
                .add(TYPE, EDC_POLICY_DEFINITION_TYPE)
                .add(EDC_POLICY_DEFINITION_POLICY, policyJson)
                .add(
                        EDC_POLICY_DEFINITION_PRIVATE_PROPERTIES,
                        createArrayBuilder()
                                .add(
                                        createObjectBuilder()
                                                .add("somekey", "somevalue")
                                                .build()
                                )
                                .build()
                )
                .add(EDC_POLICY_DEFINITION_PUBLIC_PROPERTIES,  createArrayBuilder().add(publicProperties).build())
                .build();

        var result = transformer.transform((json), context);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("definitionId");


        assertThat(result.getPrivateProperties())
                .containsKey(POLICY_DEFINITION_EUROPEANA_PROPERTIES);
    }

    private JsonObjectBuilder createContextBuilder() {
        return createObjectBuilder()
                .add(VOCAB, EDC_NAMESPACE)
                .add(EDC_PREFIX, EDC_NAMESPACE);
    }
}
