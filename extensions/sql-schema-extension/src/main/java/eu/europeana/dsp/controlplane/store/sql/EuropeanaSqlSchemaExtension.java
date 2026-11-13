package eu.europeana.dsp.controlplane.store.sql;

import org.eclipse.edc.connector.controlplane.asset.spi.index.AssetIndex;
import org.eclipse.edc.connector.controlplane.asset.spi.index.DataAddressResolver;
import org.eclipse.edc.connector.controlplane.store.sql.assetindex.SqlAssetIndex;
import org.eclipse.edc.connector.controlplane.store.sql.assetindex.schema.AssetStatements;
import org.eclipse.edc.connector.controlplane.store.sql.assetindex.schema.postgres.PostgresDialectStatements;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.edc.sql.bootstrapper.SqlSchemaBootstrapper;
import org.eclipse.edc.transaction.spi.TransactionContext;
import org.eclipse.edc.sql.QueryExecutor;
import org.eclipse.edc.transaction.datasource.spi.DataSourceRegistry;

@Extension(EuropeanaSqlSchemaExtension.NAME)
public class EuropeanaSqlSchemaExtension implements ServiceExtension {

    public static final String NAME = "Europeana SQL Schema Service Extension";

    @Setting(description = "The datasource to be used", defaultValue = DataSourceRegistry.DEFAULT_DATASOURCE, key = "edc.sql.store.asset.datasource")
    private String dataSourceName;

    @Inject
    private DataSourceRegistry dataSourceRegistry;

    @Inject
    private TransactionContext transactionContext;

    @Inject(required = false)
    private AssetStatements dialect;

    @Inject
    private TypeManager typeManager;

    @Inject
    private QueryExecutor queryExecutor;

    @Inject
    private SqlSchemaBootstrapper sqlSchemaBootstrapper;

    @Override
    public void initialize(ServiceExtensionContext context) {
        var sqlAssetLoader = new SqlAssetIndex(dataSourceRegistry, dataSourceName, transactionContext,
                typeManager.getMapper(), getDialect(), queryExecutor);

        context.registerService(AssetIndex.class, sqlAssetLoader);
        context.registerService(DataAddressResolver.class, sqlAssetLoader);

        // Register existing EDC asset schema
        sqlSchemaBootstrapper.addStatementFromResource(dataSourceName, "asset-index-schema.sql");

        // Register Europeana asset migration
        sqlSchemaBootstrapper.addStatementFromResource(
                dataSourceName,
                "sql/edc-asset-migration.sql"
        );

        // Register Europeana catalog schema
        sqlSchemaBootstrapper.addStatementFromResource(
                dataSourceName,
                "sql/edc-catalog-schema.sql"
        );

        // Register Europeana distribution schema
        sqlSchemaBootstrapper.addStatementFromResource(
                dataSourceName,
                "sql/edc-distribution-schema.sql"
        );

        context.getMonitor().info(
                "Europeana SQL schema statements registered successfully"
        );
    }

    private AssetStatements getDialect() {
        return dialect != null ? dialect : new PostgresDialectStatements();
    }
}