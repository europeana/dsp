package eu.europeana.dsp.controlplane.store.sql.catalogindex;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.spi.persistence.EdcPersistenceException;
import org.eclipse.edc.sql.QueryExecutor;
import org.eclipse.edc.sql.store.AbstractSqlStore;
import org.eclipse.edc.transaction.datasource.spi.DataSourceRegistry;
import org.eclipse.edc.transaction.spi.TransactionContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * SQL-backed catalog index for retrieving Europeana catalog metadata.
 *
 * @author Srishti Singh
 * @since 2026-11-19
 */
public class EuropeanaSqlCatalogIndex extends AbstractSqlStore {

    private final EuropeanaCatalogStatements statements;

    public EuropeanaSqlCatalogIndex(DataSourceRegistry dataSourceRegistry, String dataSourceName,
                                    TransactionContext transactionContext, ObjectMapper objectMapper,
                                    QueryExecutor queryExecutor, EuropeanaCatalogStatements statements) {
        super(dataSourceRegistry, dataSourceName, transactionContext, objectMapper, queryExecutor);
        this.statements = statements;
    }

    /**
     * Finds the catalog properties associated with the specified participant context identifier.
     *
     * @param participantContextId the participant context identifier used to look up catalog properties
     * @return an {@link Optional} containing the catalog properties as a map if a matching catalog exists,
     * or an empty {@link Optional} otherwise
     */
    public Optional<Map<String, Object>> findPropertiesByParticipantContextId(
            String participantContextId) {

        Objects.requireNonNull(participantContextId);

        try (var connection = getConnection()) {
            return queryExecutor.query(
                    connection,
                    true,
                    this::mapCatalogProperties,
                    statements.getCatalogPropertiesByParticipantContextIdTemplate(),
                    participantContextId
            ).findFirst();
        } catch (SQLException e) {
            throw new EdcPersistenceException(e);
        }
    }

    private Map<String, Object> mapCatalogProperties(ResultSet resultSet) throws SQLException {
        return fromJson(
                resultSet.getString(statements.getPropertiesColumn()),
                getTypeRef()
        );
    }
}