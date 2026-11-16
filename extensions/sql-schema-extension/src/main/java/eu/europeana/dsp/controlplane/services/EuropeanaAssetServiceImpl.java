package eu.europeana.dsp.controlplane.services;

import eu.europeana.dsp.connector.controlplane.catalog.spi.definitions.EuropeanaAsset;
import eu.europeana.dsp.connector.controlplane.catalog.spi.definitions.EuropeanaDistribution;
import eu.europeana.dsp.controlplane.store.sql.assetindex.EuropeanaSqlAssetIndex;
import org.eclipse.edc.connector.controlplane.asset.spi.domain.Asset;
import org.eclipse.edc.connector.controlplane.asset.spi.observe.AssetObservable;
import org.eclipse.edc.connector.controlplane.contract.spi.negotiation.store.ContractNegotiationStore;
import org.eclipse.edc.connector.controlplane.services.asset.AssetServiceImpl;
import org.eclipse.edc.connector.controlplane.services.query.QueryValidator;
import org.eclipse.edc.connector.controlplane.services.spi.asset.AssetService;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.ServiceResult;
import org.eclipse.edc.transaction.spi.TransactionContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static eu.europeana.dsp.connector.controlplane.catalog.spi.DspVocabulary.EUROPEANA_DISTRIBUTIONS;

/**
 * Asset service implementation that persists standard EDC assets together with Europeana-specific distribution
 * metadata.
 *
 * @author Srishti Singh
 * @since 2026-11-15
 */
public class EuropeanaAssetServiceImpl extends AssetServiceImpl implements AssetService {

    private final EuropeanaSqlAssetIndex europeanaSqlAssetIndex;
    private final TransactionContext transactionContext;
    private final AssetObservable observable;

    protected static final String DUPLICATED_KEYS_MESSAGE = "Duplicate keys in properties and private " +
            "properties are not allowed";

    public EuropeanaAssetServiceImpl(EuropeanaSqlAssetIndex europeanaSqlAssetIndex,
                                     ContractNegotiationStore contractNegotiationStore,
                                     TransactionContext transactionContext, AssetObservable observable,
                                     QueryValidator queryValidator, Monitor monitor) {
        super(europeanaSqlAssetIndex, contractNegotiationStore, transactionContext, observable, queryValidator, monitor);
        this.europeanaSqlAssetIndex = europeanaSqlAssetIndex;
        this.transactionContext = transactionContext;
        this.observable = observable;
    }

    @Override
    public ServiceResult<Asset> create(Asset asset) {
        if (asset == null) {
            return ServiceResult.badRequest("Asset must not be null");
        }
        if (asset.hasDuplicatePropertyKeys()) {
            return ServiceResult.badRequest(DUPLICATED_KEYS_MESSAGE);
        }
        Map<String,Object> properties = new HashMap<>(asset.getProperties());
        var distributionsValue = properties.remove(EUROPEANA_DISTRIBUTIONS);
        List<EuropeanaDistribution> distributions = extractDistributions(distributionsValue);

        if (distributions == null) {
            return ServiceResult.badRequest("Invalid Europeana distributions property");
        }

        // Build a clean EDC Asset without the internal distributions property.
        var cleanAsset = Asset.Builder.newInstance()
                .id(asset.getId())
                .properties(properties)
                .privateProperties(asset.getPrivateProperties())
                .dataAddress(asset.getDataAddress())
                .createdAt(asset.getCreatedAt())
                .dataplaneMetadata(asset.getDataplaneMetadata())
                .participantContextId(asset.getParticipantContextId())
                .build();

        // Wrap the asset and distributions for persistence.
        EuropeanaAsset europeanaAsset = new EuropeanaAsset();
        europeanaAsset.setAsset(cleanAsset);
        europeanaAsset.setDistributions(distributions);

        return transactionContext.execute(() ->
                europeanaSqlAssetIndex.create(europeanaAsset)
                        .onSuccess(i -> observable.invokeForEach(l -> l.created(cleanAsset)))
                        .flatMap(ServiceResult::from)
                        .map(i -> cleanAsset)
        );
    }

    /**
     * Extracts Europeana distributions from the supplied property value.
     *
     * @param value the value to extract distributions from; may be null or a list of EuropeanaDistribution instances
     * @return an empty list when the value is null, a list containing the extracted
     * distributions when the value is valid, or null when the value is not a list or contains non-EuropeanaDistribution entries
     */
    private List<EuropeanaDistribution> extractDistributions(Object value) {
        if (value == null) {
            return new ArrayList<>();
        }
        if (!(value instanceof List<?> distributionList)) {
            return null;
        }
        var distributions = new ArrayList<EuropeanaDistribution>();

        for (var item : distributionList) {
            if (!(item instanceof EuropeanaDistribution distribution)) {
                return null;
            }
            distributions.add(distribution);
        }
        return distributions;
    }
}
