package eu.europeana.dsp.connector.controlplane.catalog.spi.service;

import eu.europeana.dsp.connector.controlplane.catalog.spi.definitions.EuropeanaDcatDistribution;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.edc.connector.controlplane.asset.spi.domain.Asset;
import org.eclipse.edc.connector.controlplane.catalog.DefaultDistributionResolver;
import org.eclipse.edc.connector.controlplane.catalog.spi.DataService;
import org.eclipse.edc.connector.controlplane.catalog.spi.DataServiceRegistry;
import org.eclipse.edc.connector.controlplane.catalog.spi.Distribution;
import org.eclipse.edc.connector.controlplane.transfer.spi.flow.DataFlowController;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.types.domain.DataAddress;

import java.util.*;

import static eu.europeana.dsp.connector.controlplane.catalog.spi.service.DistributionMetadataExtractor.*;
import static org.eclipse.edc.jsonld.spi.PropertyAndTypeNames.DCT_FORMAT_ATTRIBUTE;

/**
 * Example of an asset with distribution metadata:
 * {
 *   "id": "asset-123",
 *   "properties": {
 *     "title": "Dataset 22",
 *     "description": "A provider dataset distributed as ZIP files."
 *   },
 *   "dataAddress": {
 *     "type": "EuropeanaDataAddress",
 *     "properties": {
 *       "distribution.1.title": "RDF/XML ZIP distribution",
 *       "distribution.1.description": "A ZIP archive containing RDF/XML files.",
 *       "distribution.1.mediaType": "https://www.iana.org/assignments/media-types/application/rdf+xml",
 *       "distribution.1.packagingFormat": "https://www.iana.org/assignments/media-types/application/zip",
 *       "distribution.1.format": "HttpData-PULL",
 *
 *       "distribution.2.title": "CSV distribution",
 *       "distribution.2.description": "A CSV representation of the dataset.",
 *       "distribution.2.mediaType": "https://www.iana.org/assignments/media-types/text/csv",
 *       "distribution.2.format": "HttpData-PULL"
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
            if (asset.getDataAddress() == null) {
                return List.of(Distribution.Builder.newInstance()
                        .format(getFormat(asset))
                        .dataService(DataService.Builder.newInstance()
                                .id(Base64.getUrlEncoder().encodeToString(asset.getId().getBytes()))
                                .build())
                        .build());
            }
            return buildDistributions(asset.getDataAddress(), asset.getId());
        }
        if (asset.getDataAddress() != null) {
            return buildDistributions(asset.getDataAddress(), asset.getId());
        }
        return Collections.emptyList();
    }

    /**
     * Builds a list of {@link Distribution} objects based on the given properties and asset ID.
     *
     * @param dataAddress dataaddress object with a map of properties containing distribution metadata. The keys should follow
     *                   a specific naming pattern ("distribution.{id}.[property]") to be correctly processed.
     * @param assetId the unique identifier of the asset to associate with each distribution's data service.
     * @return a list of {@link Distribution} objects constructed from the provided properties.
     *         If no distributions are found, an empty list is returned.
     */
    public static List<Distribution> buildDistributions(DataAddress dataAddress, String assetId) {
        List<Distribution> distributions = new ArrayList<>();
        Map<String, Object> properties = dataAddress.getProperties();
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
                distribution.setFormat(getDistributionFormat(dataAddress));
                distributions.add(distribution);
            }
        }
        return distributions;
    }


    /**
     * Retrieves the format of the given asset.
     * It first attempts to fetch the format using the {@code DCT_FORMAT_ATTRIBUTE}
     * property of the asset. If the format is not defined, it attempts to derive
     * the format from the type of the asset's data address. If no format can be
     * determined, an empty string is returned.
     *
     * @param asset the asset from which to extract the format. This asset may contain
     *              metadata properties and a data address from which the format can
     *              be inferred.
     * @return the format of the asset as a {@code String}. If the format cannot be
     *         determined, an empty string is returned.
     */
    private String getFormat(Asset asset) {
        var format = asset.getPropertyAsString(DCT_FORMAT_ATTRIBUTE);
        if (format == null) { // will fetch from https://w3id.org/edc/v0.0.1/ns/type (edc:type)
            format = Optional.ofNullable(asset.getDataAddress()).map(DataAddress::getType).orElse("");
        }
        return format;
    }


}