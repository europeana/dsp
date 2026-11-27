package eu.europeana.dsp.controlplane.services;

import eu.europeana.dsp.connector.controlplane.catalog.spi.DspVocabulary;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static eu.europeana.dsp.controlplane.services.BaseService.*;

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

    /**
     * Finds an asset by its identifier and enriches its public properties with the associated Europeana distributions.
     *
     * @param assetId the identifier of the asset to look up
     * @return the asset with Europeana distributions added to its properties, or
     *         null if no asset with the specified identifier exists
     * @throws NullPointerException if assetId is null
     */
    @Override
    public Asset findById(String assetId) {
        var europeanaAsset = europeanaSqlAssetIndex.findEuropeanaAssetById(assetId);
        if (europeanaAsset == null) {
            return null;
        }

        var asset = europeanaAsset.getAsset();
        var properties = new HashMap<>(asset.getProperties());
        properties.put(DspVocabulary.EUROPEANA_DISTRIBUTIONS, europeanaAsset.getDistributions());
        return buildAsset(asset, properties);
    }

    @Override
    public ServiceResult<Asset> create(Asset asset) {
        if (asset == null) {
            return ServiceResult.badRequest("Asset must not be null");
        }
        if (asset.hasDuplicatePropertyKeys()) {
            return ServiceResult.badRequest(DUPLICATED_KEYS_MESSAGE);
        }
        // Convert Asset to Europeana Asset for persistence.
        EuropeanaAsset europeanaAsset = toEuropeanaAsset(asset);
        return transactionContext.execute(() ->
                europeanaSqlAssetIndex.create(europeanaAsset)
                        .onSuccess(i -> observable.invokeForEach(l -> l.created(europeanaAsset.getAsset())))
                        .flatMap(ServiceResult::from)
                        .map(i -> europeanaAsset.getAsset())
        );
    }

    /**
     * Updates an existing asset and its Europeana distributions.
     *
     * The asset must not contain duplicate property keys and must already exist in the asset index.
     * Europeana distribution data is extracted from the asset properties before persistence, and
     * distributions that are no longer present are removed as part of the update. Registered
     * observers are notified after a successful update.
     *
     * @param asset the asset containing the updated metadata and Europeana distribution data
     * @return a successful result containing the updated asset, or a bad request result if the asset
     *         contains duplicate property keys or no existing asset is found for the specified identifier
     */
    @Override
    public ServiceResult<Asset> update(Asset asset) {
        if (asset.hasDuplicatePropertyKeys()) {
            return ServiceResult.badRequest(DUPLICATED_KEYS_MESSAGE);
        }

        EuropeanaAsset existingAsset = europeanaSqlAssetIndex.findEuropeanaAssetById(asset.getId());
        if (existingAsset == null) {
            return ServiceResult.badRequest("Asset with id " + asset.getId() + " not found");
        }

        Map<String,Object> properties = new HashMap<>(asset.getProperties());
        List<EuropeanaDistribution> newDistributions = extractDistributionData(properties);
        List<EuropeanaDistribution> distributionsToRemove = getDistributionToRemove(newDistributions, existingAsset);

        Asset assetToUpdate = buildAsset(asset, properties); // properties without distributions
        EuropeanaAsset europeanaAsset = wrapEuropeanaAsset(assetToUpdate, newDistributions); // wrap around europeana asset for persistence

        return transactionContext.execute(() ->
                europeanaSqlAssetIndex.updateAsset(europeanaAsset, distributionsToRemove)
                        .onSuccess(a -> observable.invokeForEach(l -> l.updated(a)))
                        .flatMap(ServiceResult::from)
        );
    }
}
