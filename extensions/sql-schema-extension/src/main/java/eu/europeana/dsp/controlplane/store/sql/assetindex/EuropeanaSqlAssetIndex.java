package eu.europeana.dsp.controlplane.store.sql.assetindex;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.dsp.connector.controlplane.catalog.spi.definitions.EuropeanaAsset;
import eu.europeana.dsp.connector.controlplane.catalog.spi.definitions.EuropeanaDistribution;
import eu.europeana.dsp.controlplane.store.sql.distributionindex.EuropeanaDistributionStatements;
import eu.europeana.dsp.controlplane.store.sql.distributionindex.EuropeanaSqlDistributionIndex;
import org.eclipse.edc.connector.controlplane.asset.spi.domain.Asset;
import org.eclipse.edc.connector.controlplane.asset.spi.domain.DataplaneMetadata;
import org.eclipse.edc.connector.controlplane.store.sql.assetindex.SqlAssetIndex;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.persistence.EdcPersistenceException;
import org.eclipse.edc.spi.query.QuerySpec;
import org.eclipse.edc.spi.result.StoreResult;
import org.eclipse.edc.spi.types.domain.DataAddress;
import org.eclipse.edc.sql.QueryExecutor;
import org.eclipse.edc.transaction.datasource.spi.DataSourceRegistry;
import org.eclipse.edc.transaction.spi.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static eu.europeana.dsp.controlplane.services.BaseService.wrapEuropeanaAsset;
import static eu.europeana.dsp.controlplane.store.sql.assetindex.EuropeanaAssetStatements.DISTRIBUTION_FETCHED_IN_JOIN;

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
    private final Monitor monitor;
    private final ObjectMapper objectMapper;

    public EuropeanaSqlAssetIndex(DataSourceRegistry dataSourceRegistry, String dataSourceName,
                                  TransactionContext transactionContext, ObjectMapper objectMapper,
                                  EuropeanaAssetStatements assetStatements, QueryExecutor queryExecutor,
                                  EuropeanaSqlDistributionIndex distributionIndex, Monitor monitor) {
        super(dataSourceRegistry, dataSourceName, transactionContext, objectMapper, assetStatements, queryExecutor);
        this.assetStatements = Objects.requireNonNull(assetStatements);
        this.distributionIndex = distributionIndex;
        this.monitor = monitor;
        this.objectMapper = objectMapper;
    }

    /**
     * Queries assets matching the specified query and returns them as Europeana assets with their associated distributions.
     *
     * 1. super.queryAssets() executes the existing EDC query and maps each ResultSet into a standard Asset.
     * 2. We retrieve the distributions for each asset.
     * 3. wrapEuropeanaAsset() combines the standard asset and its distributions into EuropeanaAsset.
     *
     * @param querySpec the query specification used to filter, sort, and page the assets
     * @return a stream of Europeana assets matching the query, each containing the asset data and its associated distributions
     * @throws NullPointerException if querySpec is null
     */
    public Stream<EuropeanaAsset> queryEuropeanaAssets(QuerySpec querySpec) {
        Objects.requireNonNull(querySpec);

        return transactionContext.execute(() -> {
            try (var connection = getConnection()) {
                var statement = assetStatements.createQuery(querySpec);
                var sql = assetStatements.getQueryEuropeanaAssetsTemplate(removeSemicolon(statement.getQueryAsString()));

                return queryExecutor.query(
                        connection,
                        true,
                        this::mapEuropeanaAsset,
                        sql,
                        statement.getParameters()
                );
            } catch (Exception e) {
                throw new EdcPersistenceException(e);
            }
        });
    }

    private String removeSemicolon(String query) {
        if (query.endsWith(";")) {
            return query.substring(0, query.length() - 1);
        }
        return query;
    }

    /**
     * Finds a Europeana asset by its identifier, including the associated DCAT distributions.
     *
     * @param assetId the identifier of the asset to look up
     * @return the Europeana asset with its distributions, or null if no asset with the specified identifier exists
     * @throws NullPointerException if assetId is null
     */
    public @Nullable EuropeanaAsset findEuropeanaAssetById(String assetId) {
        Objects.requireNonNull(assetId);
        var asset = super.findById(assetId);
        if (asset == null) {
            return null;
        }
        var distributions = distributionIndex.findByAssetId(assetId);
        var europeanaAsset = new EuropeanaAsset();
        europeanaAsset.setAsset(asset);
        europeanaAsset.setDistributions(distributions);

        return europeanaAsset;
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


    public StoreResult<Asset> updateAsset(EuropeanaAsset europeanaAsset, List<EuropeanaDistribution> distributionsToRemove) {
        Objects.requireNonNull(europeanaAsset);
        Objects.requireNonNull(europeanaAsset.getAsset());

        return transactionContext.execute(() -> {
            try (var connection = getConnection()) {
                var asset = europeanaAsset.getAsset();
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

                    // update distributions
                    distributionIndex.insertDistributions(asset, europeanaAsset.getDistributions(), connection);

                    // remove distributions
                    if (distributionsToRemove != null && !distributionsToRemove.isEmpty()) {
                        distributionIndex.deleteDistributions(asset.getId(), distributionsToRemove, connection, monitor);
                    }
                    return StoreResult.success(asset);
                }
                return StoreResult.notFound("Asset with id '" + assetId + "' was not found");
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

    /**
     * Maps the current row of the supplied result set to a Europeana asset.
     * The important changes are:
     *  - Adds the distributions from the resultset
     *  - Asset logic is the same as in the original implementation
     *  - The distributions are added to the europeana asset
     *
     * @param resultSet the result set positioned at the row to map
     * @return the mapped Europeana asset containing the reconstructed asset and its associated distributions
     * @throws SQLException if reading values from the result set fails
     */
    private EuropeanaAsset mapEuropeanaAsset(ResultSet resultSet) throws SQLException {
        Map<String, Object> dataAddressProperties = fromJson(
                resultSet.getString(assetStatements.getDataAddressColumn()),
                getTypeRef()
        );

        var dataAddress = Optional.ofNullable(dataAddressProperties)
                .map(it -> DataAddress.Builder.newInstance()
                        .properties(it)
                        .build())
                .orElse(null);

        var asset = Asset.Builder.newInstance()
                .id(resultSet.getString(assetStatements.getAssetIdColumn()))
                .createdAt(resultSet.getLong(assetStatements.getCreatedAtColumn()))
                .properties(fromJson(
                        resultSet.getString(assetStatements.getPropertiesColumn()),
                        getTypeRef()
                ))
                .privateProperties(fromJson(
                        resultSet.getString(assetStatements.getPrivatePropertiesColumn()),
                        getTypeRef()
                ))
                .dataAddress(dataAddress)
                .participantContextId(resultSet.getString(assetStatements.getParticipantContextIdColumn()))
                .dataplaneMetadata(fromJson(
                        resultSet.getString(assetStatements.getDataplaneMetadataColumn()),
                        DataplaneMetadata.class
                ))
                .build();

        List<Map<String, Object>> distributionData = fromJson(
                resultSet.getString(DISTRIBUTION_FETCHED_IN_JOIN),
                objectMapper.getTypeFactory().constructCollectionType(
                        List.class,
                        Map.class
                )
        );

        List<EuropeanaDistribution> distributions = distributionData.stream()
                .map(distribution -> distributionIndex.mapDistribution(
                        (String) distribution.get("distribution_id"),
                        (Map<String, Object>) distribution.get("properties"),
                        (Map<String, Object>) distribution.get("data_address")
                ))
                .toList();

        return wrapEuropeanaAsset(asset, distributions);
    }

    private EuropeanaDistributionStatements getDistributionStatements() {
        return distributionIndex.getDistributionStatements();
    }
}
