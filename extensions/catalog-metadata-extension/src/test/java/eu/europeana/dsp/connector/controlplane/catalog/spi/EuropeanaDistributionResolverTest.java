package eu.europeana.dsp.connector.controlplane.catalog.spi;

import eu.europeana.dsp.connector.controlplane.catalog.spi.definitions.EuropeanaDcatDistribution;
import eu.europeana.dsp.connector.controlplane.catalog.spi.service.EuropeanaDistributionResolver;
import org.eclipse.edc.connector.controlplane.catalog.spi.Distribution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class EuropeanaDistributionResolverTest {

    @Test
    void test1() throws IOException {

        EuropeanaDistributionResolver resolver = new EuropeanaDistributionResolver(
                null, null, null);
        List<Distribution> distributionList = resolver.getDistributions(
                "http", TestUtils.loadAsset("asset_1.json"));
        Assertions.assertNotNull(distributionList);
        Assertions.assertEquals(2, distributionList.size());
        distributionList.forEach(distribution -> {
            Assertions.assertNotNull(distribution.getDataService());
            Assertions.assertNotNull(distribution.getFormat());
            System.out.println(((EuropeanaDcatDistribution)distribution).getProperties());
        });
    }
}
