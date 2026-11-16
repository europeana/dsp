package eu.europeana.dsp.controlplane.store.sql.distributionindex;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.dsp.connector.controlplane.catalog.spi.definitions.EuropeanaDistribution;
import org.eclipse.edc.connector.controlplane.asset.spi.domain.Asset;
import org.eclipse.edc.spi.types.domain.DataAddress;
import org.eclipse.edc.sql.QueryExecutor;
import org.eclipse.edc.sql.store.AbstractSqlStore;
import org.eclipse.edc.transaction.datasource.spi.DataSourceRegistry;
import org.eclipse.edc.transaction.spi.TransactionContext;

import java.sql.Connection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
                    statements.getInsertDistributionTemplate(),
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
}
