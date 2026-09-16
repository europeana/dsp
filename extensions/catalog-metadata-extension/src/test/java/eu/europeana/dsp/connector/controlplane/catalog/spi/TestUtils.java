package eu.europeana.dsp.connector.controlplane.catalog.spi;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsonp.JSONPModule;
import jakarta.json.JsonObject;
import org.eclipse.edc.connector.controlplane.asset.spi.domain.Asset;

import java.io.IOException;
import java.io.InputStream;

public class TestUtils {

    public static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    private static InputStream getInputStream(String resourceName) {
        InputStream inputStream = Asset.class.getClassLoader().getResourceAsStream(resourceName);

        if (inputStream == null) {
            throw new IllegalArgumentException("Resource not found:" + resourceName);
        }
        return inputStream;
    }

    public static Asset loadAsset(String resourceName) throws IOException {
        return OBJECT_MAPPER.readValue(getInputStream(resourceName), Asset.class);
    }

    public static JsonNode loadJson(String resourceName) throws IOException {
        return OBJECT_MAPPER.readTree(getInputStream(resourceName));
    }

    public static JsonNode loadJson(JsonObject jsonObject) throws IOException {
        return OBJECT_MAPPER.readTree(jsonObject.toString());
    }

    private static ObjectMapper createObjectMapper() {
        var mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new JSONPModule());
        var module = new SimpleModule() {
            @Override
            public void setupModule(SetupContext context) {
                super.setupModule(context);
            }
        };
        mapper.registerModule(module);
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        return mapper;
    }
}