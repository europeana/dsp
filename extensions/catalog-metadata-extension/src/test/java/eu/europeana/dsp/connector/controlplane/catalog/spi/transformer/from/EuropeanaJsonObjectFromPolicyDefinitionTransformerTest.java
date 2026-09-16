package eu.europeana.dsp.connector.controlplane.catalog.spi.transformer.from;

import eu.europeana.dsp.connector.controlplane.catalog.spi.TestUtils;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.eclipse.edc.connector.controlplane.policy.spi.PolicyDefinition;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static eu.europeana.dsp.connector.controlplane.catalog.spi.DspVocabulary.*;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.connector.controlplane.policy.spi.PolicyDefinition.*;
import static org.eclipse.edc.connector.controlplane.policy.spi.PolicyDefinition.EDC_POLICY_DEFINITION_PRIVATE_PROPERTIES;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.ID;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.TYPE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

/**
 * Unit test class for verifying the behavior of {@link EuropeanaJsonObjectFromPolicyDefinitionTransformer}.
 * This test ensures the correct transformation of {@link PolicyDefinition} objects into {@link JsonObject}
 * while adhering to the specific requirements for private and public property handling for Europeana policies.
 *
 * @author Srishti Singh
 * @since 2026-09-15
 */
public class EuropeanaJsonObjectFromPolicyDefinitionTransformerTest {

    private final TypeManager typeManager = mock();

    private final EuropeanaJsonObjectFromPolicyDefinitionTransformer transformer =
            new EuropeanaJsonObjectFromPolicyDefinitionTransformer(
                    Json.createBuilderFactory(emptyMap()), typeManager, "test");

    private final TransformerContext context = mock(TransformerContext.class);

    @BeforeEach
    void setup() {
        when(typeManager.getMapper("test")).thenReturn(TestUtils.OBJECT_MAPPER);
    }

    @Test
    void transform_withPrivateProperties_simpleTypes() {
        var policy = Policy.Builder.newInstance().build();
        var input = PolicyDefinition.Builder.newInstance().id("definitionId").policy(policy).
                privateProperty(
                "some-key", "some-value");

        addEuropeanaPublicProperties(input);

        var policyJson = Json.createObjectBuilder().build();
        when(context.transform(any(), eq(JsonObject.class))).thenReturn(policyJson);

        var result = transformer.transform(input.build(), context);

        assertThat(result).isNotNull();
        assertThat(result.getString(ID)).isEqualTo("definitionId");
        assertThat(result.getString(TYPE)).isEqualTo(EDC_POLICY_DEFINITION_TYPE);
        assertThat(result.getJsonObject(EDC_POLICY_DEFINITION_POLICY)).isSameAs(policyJson);

        assertThat(result.getJsonObject(EDC_POLICY_DEFINITION_PRIVATE_PROPERTIES).
                getJsonString("some-key").getString()).isEqualTo("some-value");

        assertThat(result.getJsonObject(EDC_POLICY_DEFINITION_PUBLIC_PROPERTIES).
               getJsonString("dct:title").getString()).isEqualTo("title test value");

        assertThat(result.getJsonObject(EDC_POLICY_DEFINITION_PUBLIC_PROPERTIES).
                getJsonString("dct:description").getString()).isEqualTo("description test value");

        assertThat(result.getJsonObject(EDC_POLICY_DEFINITION_PUBLIC_PROPERTIES).
                getJsonString("test").getString()).isEqualTo("TestValue");

        verify(context).transform(policy, JsonObject.class);
    }


    @Test
    void transform_withPrivateProperties_complexTypes() {
        var policy = Policy.Builder.newInstance().build();
        var input = PolicyDefinition
                .Builder.newInstance()
                .id("definitionId")
                .policy(policy)
                .privateProperty(
                        "root", Map.of(
                                "key1", "value1",
                                "nested1", Map.of("key2", "value2",
                                        "key3", Map.of("theKey", "theValue, this is what we're looking for")))
                );
        addEuropeanaPublicProperties(input);

        var policyJson = Json.createObjectBuilder().build();
        when(context.transform(any(), eq(JsonObject.class))).thenReturn(policyJson);

        var result = transformer.transform(input.build(), context);

        assertThat(result).isNotNull();
        assertThat(result.getString(ID)).isEqualTo("definitionId");
        assertThat(result.getString(TYPE)).isEqualTo(EDC_POLICY_DEFINITION_TYPE);
        assertThat(result.getJsonObject(EDC_POLICY_DEFINITION_POLICY)).isSameAs(policyJson);

        assertThat(result.getJsonObject(EDC_POLICY_DEFINITION_PRIVATE_PROPERTIES)
                .getJsonObject("root")
                .getJsonString("key1")
                .getString())
                .isEqualTo("value1");
        assertThat(result.getJsonObject(EDC_POLICY_DEFINITION_PRIVATE_PROPERTIES)
                .getJsonObject("root")
                .getJsonObject("nested1")
                .getJsonString("key2")
                .getString())
                .isEqualTo("value2");
        assertThat(result.getJsonObject(EDC_POLICY_DEFINITION_PRIVATE_PROPERTIES)
                .getJsonObject("root")
                .getJsonObject("nested1")
                .getJsonObject("key3")
                .getJsonString("theKey")
                .getString())
                .isEqualTo("theValue, this is what we're looking for");

        assertThat(result.getJsonObject(EDC_POLICY_DEFINITION_PUBLIC_PROPERTIES).
                getJsonString("dct:title").getString()).isEqualTo("title test value");

        assertThat(result.getJsonObject(EDC_POLICY_DEFINITION_PUBLIC_PROPERTIES).
                getJsonString("dct:description").getString()).isEqualTo("description test value");

        assertThat(result.getJsonObject(EDC_POLICY_DEFINITION_PUBLIC_PROPERTIES).
                getJsonString("test").getString()).isEqualTo("TestValue");

        verify(context).transform(policy, JsonObject.class);
    }

    private void addEuropeanaPublicProperties(PolicyDefinition.Builder builder) {
        builder.privateProperty(POLICY_DEFINITION_EUROPEANA_PROPERTIES , Map.of(
                "dct:title", "title test value",
                "dct:description", "description test value",
                "test" , "TestValue"));
    }
}
