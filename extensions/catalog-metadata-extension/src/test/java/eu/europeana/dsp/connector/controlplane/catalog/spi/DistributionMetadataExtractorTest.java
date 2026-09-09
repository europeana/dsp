package eu.europeana.dsp.connector.controlplane.catalog.spi;

import eu.europeana.dsp.connector.controlplane.catalog.spi.definitions.EuropeanaDcatDistribution;
import eu.europeana.dsp.connector.controlplane.catalog.spi.service.DistributionMetadataExtractor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit test class for validating the functionality of the {@code DistributionMetadataExtractor} class
 * and its ability to extract and construct metadata properties into {@code EuropeanaDcatDistribution} objects.
 * @author Srishti singh
 * @since 2026-09-2
 */
public class DistributionMetadataExtractorTest {

    private static Map<String, Object> properties = new HashMap<>();
    private static final String DISTRIBUTION_1_TITLE = "RDF/XML ZIP distribution.";
    private static final String DISTRIBUTION_1_DESCRIPTION = "A ZIP archive containing RDF/XML files.";
    private static final String DISTRIBUTION_FORMAT = "HttpData-PULL";
    private static final String DISTRIBUTION_2_TITLE = "CSV distribution";
    private static final String DISTRIBUTION_2_DESCRIPTION = "A CSV representation of the dataset.";


    @BeforeAll
    static void init() {
        properties.put("distribution.1.dct:title", DISTRIBUTION_1_TITLE);
        properties.put("distribution.1.dct:description", DISTRIBUTION_1_DESCRIPTION);
        properties.put("distribution.1.dcat:mediaType", "https,//www.iana.org/assignments/media-types/application/rdf+xml");
        properties.put("distribution.1.dcat:packagingFormat", "https,//www.iana.org/assignments/media-types/application/zip");
        properties.put("distribution.1.dct:format", DISTRIBUTION_FORMAT);

        properties.put("distribution.2.dct:title",DISTRIBUTION_2_TITLE);
        properties.put("distribution.2.dct:description", DISTRIBUTION_2_DESCRIPTION);
        properties.put("distribution.2.dcat:mediaType","https,//www.iana.org/assignments/media-types/text/csv");
        properties.put("distribution.2.dct:format", DISTRIBUTION_FORMAT)  ;
    }

    @Test
    public void testDistribution1() {
        EuropeanaDcatDistribution distribution = DistributionMetadataExtractor.buildDistribution(
                properties, "1");
        Assertions.assertNotNull(distribution);
        Map<String, Object> distributionProperties = distribution.getProperties();
        Assertions.assertEquals(DISTRIBUTION_1_TITLE, distributionProperties.get("dct:title"));
        Assertions.assertEquals(DISTRIBUTION_1_DESCRIPTION, distributionProperties.get("dct:description"));
        Assertions.assertNotNull(distributionProperties.get("dcat:mediaType"));
        Assertions.assertNotNull(distributionProperties.get("dcat:packagingFormat"));
        Assertions.assertEquals(DISTRIBUTION_FORMAT, distributionProperties.get("dct:format"));
    }

    @Test
    public void testDistribution2() {
        EuropeanaDcatDistribution distribution = DistributionMetadataExtractor.buildDistribution(
                properties, "2");
        Assertions.assertNotNull(distribution);
        Map<String, Object> distributionProperties = distribution.getProperties();
        Assertions.assertEquals(DISTRIBUTION_2_TITLE, distributionProperties.get("dct:title"));
        Assertions.assertEquals(DISTRIBUTION_2_DESCRIPTION, distributionProperties.get("dct:description"));
        Assertions.assertNotNull(distributionProperties.get("dcat:mediaType"));
        Assertions.assertEquals(DISTRIBUTION_FORMAT, distributionProperties.get("dct:format"));
    }
}
