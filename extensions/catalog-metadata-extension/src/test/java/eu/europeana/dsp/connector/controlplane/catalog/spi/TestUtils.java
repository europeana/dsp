package eu.europeana.dsp.connector.controlplane.catalog.spi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.JsonObject;
import org.eclipse.edc.connector.controlplane.asset.spi.domain.Asset;

import java.io.IOException;
import java.io.InputStream;

public class TestUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static InputStream getInputStream(String resourceName) {
        InputStream inputStream = Asset.class.getClassLoader().getResourceAsStream(resourceName);

        if (inputStream == null) {
            throw new IllegalArgumentException("Resource not found:"+resourceName);
        }
        return inputStream;
    }

    public static Asset loadAsset(String resourceName) throws IOException {
        return objectMapper.readValue(getInputStream(resourceName), Asset.class);
    }

    public static JsonNode loadJson(String resourceName) throws IOException {
        return objectMapper.readTree(getInputStream(resourceName));
    }

    public static JsonNode loadJson(JsonObject jsonObject) throws IOException {
        return objectMapper.readTree(jsonObject.toString());
    }
}