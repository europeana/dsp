package eu.europeana.dsp.connector.controlplane.catalog.spi;

import com.fasterxml.jackson.databind.JsonNode;
import eu.europeana.dsp.connector.controlplane.catalog.spi.definitions.EuropeanaDcatDistribution;
import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import org.eclipse.edc.connector.controlplane.catalog.spi.DataService;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Map;

import static org.mockito.Mockito.when;

public class JsonObjectFromEuropeanaDcatDistributionTransformerTest {

    private static JsonObjectFromEuropeanaDcatDistributionTransformer transformer;
    private static EuropeanaDcatDistribution distribution;
    private static TransformerContext context;

    @BeforeAll
    static void init() throws IOException {
        JsonBuilderFactory jsonFactory = Json.createBuilderFactory(Map.of());
        transformer = new JsonObjectFromEuropeanaDcatDistributionTransformer(jsonFactory);

        Map<String, Object> properties = Map.of(
                "dct:title", "RDF/XML ZIP distribution",
                "dct:description", "A ZIP archive containing RDF/XML files.",
                "dcat:mediaType",
                "https://www.iana.org/assignments/media-types/application/rdf+xml",
                "dcat:packagingFormat",
                "https://www.iana.org/assignments/media-types/application/zip",
                "dct:format", "HttpData-PULL",
                "dct:identifier", "https://www.europeana.eu/portal/record/223456789/data.zip"
        );

        distribution = new EuropeanaDcatDistribution("1", properties);
        distribution.setFormat("HttpData-PULL");
        DataService dataService = DataService.Builder.newInstance()
                .id("test-service")
                .build();

        context = Mockito.mock(TransformerContext.class);
        var dataServiceJson = Json.createObjectBuilder()
                .add("@id", "test-service")
                .build();

        when(context.transform(dataService, JsonObject.class))
                .thenReturn(dataServiceJson);

    }

    @Test
    void test_json_object_from_distribution() throws IOException {
        JsonObject actual = transformer.transform(distribution, context);
        Assertions.assertNotNull(actual);
        JsonNode expected = TestUtils.loadJson("distributionJsonResponse.json");
        Assertions.assertEquals(expected, TestUtils.loadJson(actual));
        System.out.println(actual);
    }
}
