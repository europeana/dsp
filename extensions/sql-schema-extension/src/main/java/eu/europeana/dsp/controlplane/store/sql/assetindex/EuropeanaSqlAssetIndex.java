package eu.europeana.dsp.controlplane.store.sql.assetindex;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.dsp.connector.controlplane.catalog.spi.definitions.EuropeanaAsset;
import eu.europeana.dsp.controlplane.store.sql.distributionindex.EuropeanaSqlDistributionIndex;
import org.eclipse.edc.connector.controlplane.asset.spi.domain.Asset;
import org.eclipse.edc.connector.controlplane.store.sql.assetindex.SqlAssetIndex;
import org.eclipse.edc.spi.persistence.EdcPersistenceException;
import org.eclipse.edc.spi.result.StoreResult;
import org.eclipse.edc.spi.types.domain.DataAddress;
import org.eclipse.edc.sql.QueryExecutor;
import org.eclipse.edc.transaction.datasource.spi.DataSourceRegistry;
import org.eclipse.edc.transaction.spi.TransactionContext;

import java.sql.Connection;
import java.util.Objects;
import java.util.Optional;

/**
 * SQL-backed asset index implementation for the Europeana asset index schema.
 *
 * Extends the standard SQL asset index behavior with persistence support for a
 * modification timestamp managed by {@link EuropeanaAssetStatements}. Asset
 * creation stores the current modification time together with the asset
 * metadata, private properties, data address properties, participant context,
 * and dataplane metadata. Asset updates refresh the modification timestamp while
 * updating the stored asset content.
 *
 * @see SqlAssetIndex
 * @see EuropeanaAssetStatements
 *
 * @author Srishti Singh
 * @since 2026-12-11
 */
public class EuropeanaSqlAssetIndex extends SqlAssetIndex {

    private final EuropeanaAssetStatements assetStatements;
    private final EuropeanaSqlDistributionIndex distributionIndex;

    public EuropeanaSqlAssetIndex(DataSourceRegistry dataSourceRegistry, String dataSourceName,
                                  TransactionContext transactionContext, ObjectMapper objectMapper,
                                  EuropeanaAssetStatements assetStatements, QueryExecutor queryExecutor,
                                  EuropeanaSqlDistributionIndex distributionIndex) {
        super(dataSourceRegistry, dataSourceName, transactionContext, objectMapper, assetStatements, queryExecutor);
        this.assetStatements = Objects.requireNonNull(assetStatements);
        this.distributionIndex = distributionIndex;
    }

    /**
     * Creates a standard EDC asset.
     */
    @Override
    public StoreResult<Void> create(Asset asset) {
        Objects.requireNonNull(asset);
        return transactionContext.execute(() -> {
            try (var connection = getConnection()) {
                if (existsById(asset.getId(), connection)) {
                    return StoreResult.alreadyExists("Asset with id '" + asset.getId() + "' already exists");
                }
                insertAsset(asset, connection);
                return StoreResult.success();
            } catch (Exception e) {
                throw new EdcPersistenceException(e);
            }
        });
    }

    /**
     * Creates a Europeana asset together with its DCAT distributions.
     */
    public StoreResult<Void> create(EuropeanaAsset europeanaAsset) {
        Objects.requireNonNull(europeanaAsset);
        Objects.requireNonNull(europeanaAsset.getAsset());

        return transactionContext.execute(() -> {
            try (var connection = getConnection()) {

                var asset = europeanaAsset.getAsset();
                var assetId = asset.getId();

                if (existsById(assetId, connection)) {
                    return StoreResult.alreadyExists("Asset with id '" + assetId + "' already exists");
                }

                // Insert the parent asset first.
                insertAsset(asset, connection);
                // Insert its distributions using the same connection.
                distributionIndex.insertDistributions(
                        asset,
                        europeanaAsset.getDistributions(),
                        connection);

                return StoreResult.success();
            } catch (Exception e) {
                throw new EdcPersistenceException(e);
            }
        });
    }

    /**
     * Inserts the asset using the supplied connection.
     */
    private void insertAsset(Asset asset, Connection connection) {
        queryExecutor.execute(
                connection,
                assetStatements.getInsertAssetTemplate(),
                asset.getId(),
                asset.getCreatedAt(),
                toJson(asset.getProperties()),
                toJson(asset.getPrivateProperties()),
                toJson(Optional.ofNullable(asset.getDataAddress())
                        .map(DataAddress::getProperties)
                        .orElse(null)),
                asset.getParticipantContextId(),
                toJson(asset.getDataplaneMetadata()),
                System.currentTimeMillis() // modified_at
        );
    }

    /**
     * Updates an existing asset in the asset index.
     *
     * The asset is persisted with the current modification timestamp, public and private
     * properties, data address properties, and dataplane metadata. If no asset with the
     * same identifier exists, no update is performed.
     *
     * @param asset the asset to update
     * @return a successful result containing the updated asset, or a not-found result if no asset with the
     *         same identifier exists
     * @throws NullPointerException if the asset is null
     * @throws EdcPersistenceException if the asset cannot be updated
     */
    @Override
    public StoreResult<Asset> updateAsset(Asset asset) {
        Objects.requireNonNull(asset);

        return transactionContext.execute(() -> {
            try (var connection = getConnection()) {
                var assetId = asset.getId();

                if (existsById(assetId, connection)) {

                    queryExecutor.execute(
                            connection,
                            assetStatements.getUpdateAssetTemplate(),
                            System.currentTimeMillis(), // modified_at
                            toJson(asset.getProperties()),
                            toJson(asset.getPrivateProperties()),
                            toJson(Optional.ofNullable(asset.getDataAddress())
                                    .map(DataAddress::getProperties)
                                    .orElse(null)),
                            toJson(asset.getDataplaneMetadata()),
                            assetId
                    );

                    return StoreResult.success(asset);
                }

                return StoreResult.notFound(
                        "Asset with id '" + assetId + "' was not found"
                );

            } catch (Exception e) {
                throw new EdcPersistenceException(e);
            }
        });
    }

    /**
     * Determines whether an asset with the specified identifier exists in the asset index.
     *
     * @param assetId the identifier of the asset to look up
     * @param connection the database connection to use for the lookup
     * @return true if an asset with the specified identifier exists, otherwise false
     */
    private boolean existsById(String assetId, Connection connection) {
        var sql = assetStatements.getCountAssetByIdClause();

        try (var stream = queryExecutor.query(
                connection,
                false,
                resultSet -> resultSet.getInt(
                        assetStatements.getCountVariableName()
                ),
                sql,
                assetId
        )) {
            return stream.findFirst().orElse(0) > 0;
        }
    }
}
