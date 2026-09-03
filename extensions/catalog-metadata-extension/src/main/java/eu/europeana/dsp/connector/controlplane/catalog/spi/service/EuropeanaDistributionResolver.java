package eu.europeana.dsp.connector.controlplane.catalog.spi.service;

import eu.europeana.dsp.connector.controlplane.catalog.spi.definitions.EuropeanaDcatDistribution;
import org.eclipse.edc.connector.controlplane.asset.spi.domain.Asset;
import org.eclipse.edc.connector.controlplane.catalog.DefaultDistributionResolver;
import org.eclipse.edc.connector.controlplane.catalog.spi.DataService;
import org.eclipse.edc.connector.controlplane.catalog.spi.DataServiceRegistry;
import org.eclipse.edc.connector.controlplane.catalog.spi.Distribution;
import org.eclipse.edc.connector.controlplane.transfer.spi.flow.DataFlowController;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.types.domain.DataAddress;

import java.util.*;

import static eu.europeana.dsp.connector.controlplane.catalog.spi.service.DistributionMetadataExtractor.buildDistribution;
import static eu.europeana.dsp.connector.controlplane.catalog.spi.service.DistributionMetadataExtractor.getDistributionCount;
import static org.eclipse.edc.jsonld.spi.PropertyAndTypeNames.DCT_FORMAT_ATTRIBUTE;

/**
 * Example of an asset with distribution metadata:
 * {
 *   "id": "asset-123",
 *   "properties": {
 *     "dct:title": "Dataset 22",
 *     "dct:description": "A provider dataset distributed as ZIP files."
 *   },
 *   "dataAddress": {
 *     "type": "EuropeanaDataAddress",
 *     "properties": {
 *       "distribution.1.dct:title": "RDF/XML ZIP distribution",
 *       "distribution.1.dct:description": "A ZIP archive containing RDF/XML files.",
 *       "distribution.1.dcat:mediaType": "https://www.iana.org/assignments/media-types/application/rdf+xml",
 *       "distribution.1.dcat:packagingFormat": "https://www.iana.org/assignments/media-types/application/zip",
 *       "distribution.1.dct:format": "HttpData-PULL",
 *
 *       "distribution.2.dct:title": "CSV distribution",
 *       "distribution.2.dct:description": "A CSV representation of the dataset.",
 *       "distribution.2.dcat:mediaType": "https://www.iana.org/assignments/media-types/text/csv",
 *       "distribution.2.dct:format": "HttpData-PULL"
 *     }
 *   }
 * }
 */
public class EuropeanaDistributionResolver extends DefaultDistributionResolver {

    public EuropeanaDistributionResolver(
            DataServiceRegistry dataServiceRegistry,
            DataFlowController dataFlowController,
            Monitor monitor) {
        super(dataServiceRegistry, dataFlowController, monitor);
    }

    // will return Europeana Dcat Distribution
    @Override
    public List<Distribution> getDistributions(String protocol, Asset asset) {
        if (asset.isCatalog()) {
            var format = asset.getPropertyAsString(DCT_FORMAT_ATTRIBUTE);
            if (format == null) {
                format = Optional.ofNullable(asset.getDataAddress()).map(DataAddress::getType).orElse("");
            }

            // todo check if we want to retirn here EuropeanaDcatDistribution
            //  or this will be build out of asset properties
            return List.of(Distribution.Builder.newInstance()
                    .format(format)
                    .dataService(DataService.Builder.newInstance()
                            .id(Base64.getUrlEncoder().encodeToString(asset.getId().getBytes()))
                            .build())
                    .build());
        }

        // TODO check if we want to use private or public properties of asset or properties of data Address
        return buildDistributions(asset.getDataAddress().getProperties(), asset.getId());
    }

    /**
     * Builds a list of {@link Distribution} objects based on the given properties and asset ID.
     *
     * @param properties a map of asset properties containing distribution metadata. The keys should follow
     *                   a specific naming pattern ("distribution.{id}.[property]") to be correctly processed.
     * @param assetId the unique identifier of the asset to associate with each distribution's data service.
     * @return a list of {@link Distribution} objects constructed from the provided properties.
     *         If no distributions are found, an empty list is returned.
     */
    private List<Distribution> buildDistributions(Map<String, Object> properties, String assetId) {
        List<Distribution> distributions = new ArrayList<>();
        int noOfDistribution = getDistributionCount(properties);

        if (noOfDistribution > 0) {
            // TODO check if this will be the same dataservice created everytime based on asset id
            var dataService = DataService.Builder.newInstance()
                    .id(Base64.getUrlEncoder()
                            .encodeToString(assetId.getBytes()))
                    .build();

            for (int i = 1; i <= noOfDistribution; i++) {
                EuropeanaDcatDistribution distribution = buildDistribution(properties, String.valueOf(i));

                distribution.setDataService(dataService);
                var format = distribution.getProperties().get("dct:format");
                distribution.setFormat(String.valueOf(format));
                distributions.add(distribution);

            }
        }
        return distributions;
    }

}
