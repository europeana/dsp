package eu.europeana.dsp.controlplane.store.sql.distributionindex;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.dsp.connector.controlplane.catalog.spi.definitions.EuropeanaDistribution;
import org.eclipse.edc.connector.controlplane.asset.spi.domain.Asset;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.persistence.EdcPersistenceException;
import org.eclipse.edc.spi.types.domain.DataAddress;
import org.eclipse.edc.sql.QueryExecutor;
import org.eclipse.edc.sql.store.AbstractSqlStore;
import org.eclipse.edc.transaction.datasource.spi.DataSourceRegistry;
import org.eclipse.edc.transaction.spi.TransactionContext;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SQL-backed index for persisting Europeana distribution records associated with
 * an asset.
 *
 * @author Srishti Singh
 * @since 2026-11-16
 */
public class EuropeanaSqlDistributionIndex extends AbstractSqlStore {

    private final EuropeanaDistributionStatements statements;

    public EuropeanaSqlDistributionIndex(
            DataSourceRegistry dataSourceRegistry,
            String dataSourceName,
            TransactionContext transactionContext,
            ObjectMapper objectMapper,
            QueryExecutor queryExecutor,
            EuropeanaDistributionStatements statements) {
        super(dataSourceRegistry, dataSourceName, transactionContext, objectMapper, queryExecutor);
        this.statements = Objects.requireNonNull(statements);
    }

    /**
     * Finds all Europeana distribution records associated with the specified asset identifier.
     *
     * @param assetId the identifier of the asset whose distributions are to be retrieved
     * @return the list of Europeana distributions associated with the specified asset, ordered by creation time
     * @throws NullPointerException if assetId is null
     * @throws EdcPersistenceException if an SQL error occurs while retrieving the distributions
     */
    public List<EuropeanaDistribution> findByAssetId(String assetId) {
        try (var connection = getConnection()) {
            Objects.requireNonNull(assetId);
            return queryExecutor.query(
                    connection,
                    true,
                    this::mapDistribution,
                    statements.getFindDistributionsByAssetIdTemplate(),
                    assetId
            ).toList();
        } catch (SQLException e) {
            throw new EdcPersistenceException(e);
        }

    }

    /**
     * Persists the given Europeana distribution records for the specified asset using the provided database connection.
     *
     * @param asset the asset to which the distributions belong
     * @param distributions the distribution records to persist
     * @param connection the database connection used to execute the insert statements
     */
    public void insertDistributions(Asset asset, List<EuropeanaDistribution> distributions, Connection connection) {
        if (distributions == null || distributions.isEmpty()) {
            return;
        }

        for (var distribution : distributions) {
            queryExecutor.execute(
                    connection,
                    statements.getUpsertDistributionTemplate(),
                    asset.getId() + "_" + distribution.getDistributionId(),
                    asset.getId(),
                    System.currentTimeMillis(), // created_at
                    distribution.getDistributionId(),
                    toJson(distribution.getProperties()),
                    toJson(Optional.ofNullable(distribution.getDataAddress())
                            .map(DataAddress::getProperties)
                            .orElse(null)),
                    asset.getParticipantContextId()
            );
        }
    }


    public void deleteDistributions(
            String assetId,
            List<EuropeanaDistribution> distributions,
            Connection connection,
            Monitor monitor
    ) {
        monitor.info("Removing distributions for asset with id: " + assetId
                + ". Distribution IDs: " + distributions.stream()
                .map(EuropeanaDistribution::getDistributionId)
                .collect(Collectors.joining(", ")));

        var sql = statements.getDeleteDistributionTemplate();

        for (var distribution : distributions) {
            queryExecutor.execute(
                    connection,
                    sql,
                    assetId,
                    distribution.getDistributionId()
            );
        }
    }

    private EuropeanaDistribution mapDistribution(ResultSet resultSet) throws SQLException {
        Map<String, Object> dataAddressProperties = fromJson(
                resultSet.getString(statements.getDataAddressColumn()),
                getTypeRef()
        );

        var dataAddress = Optional.ofNullable(dataAddressProperties)
                .map(it -> DataAddress.Builder.newInstance()
                        .properties(it)
                        .build())
                .orElse(null);

        var distribution = new EuropeanaDistribution(
                resultSet.getString(statements.getDistributionIdColumn())
        );

        distribution.setProperties(fromJson(
                resultSet.getString(statements.getPropertiesColumn()),
                getTypeRef()
        ));

        distribution.setDataAddress(dataAddress);

        return distribution;
    }
}
