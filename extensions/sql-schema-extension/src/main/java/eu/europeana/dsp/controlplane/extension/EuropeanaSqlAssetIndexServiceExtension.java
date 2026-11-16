package eu.europeana.dsp.controlplane.extension;

import eu.europeana.dsp.controlplane.services.EuropeanaAssetServiceImpl;
import eu.europeana.dsp.controlplane.store.sql.assetindex.EuropeanaAssetStatements;
import eu.europeana.dsp.controlplane.store.sql.assetindex.EuropeanaSqlAssetIndex;
import eu.europeana.dsp.controlplane.store.sql.distributionindex.EuropeanaDistributionStatements;
import eu.europeana.dsp.controlplane.store.sql.distributionindex.EuropeanaSqlDistributionIndex;
import org.eclipse.edc.connector.controlplane.asset.spi.index.AssetIndex;
import org.eclipse.edc.connector.controlplane.asset.spi.index.DataAddressResolver;
import org.eclipse.edc.connector.controlplane.asset.spi.observe.AssetObservableImpl;
import org.eclipse.edc.connector.controlplane.contract.spi.negotiation.store.ContractNegotiationStore;
import org.eclipse.edc.connector.controlplane.services.asset.AssetEventListener;
import org.eclipse.edc.connector.controlplane.services.asset.AssetQueryValidator;
import org.eclipse.edc.connector.controlplane.services.spi.asset.AssetService;
import org.eclipse.edc.runtime.metamodel.annotation.*;
import org.eclipse.edc.spi.event.EventRouter;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.edc.sql.bootstrapper.SqlSchemaBootstrapper;
import org.eclipse.edc.transaction.spi.TransactionContext;
import org.eclipse.edc.sql.QueryExecutor;
import org.eclipse.edc.transaction.datasource.spi.DataSourceRegistry;

/**
 * Service extension that configures the Europeana SQL-backed asset store.
 *
 * During initialization, this extension creates and registers a Europeana-specific SQL asset index
 * as both an {@code AssetIndex} and a {@code DataAddressResolver}. It also registers the SQL schema
 * bootstrap statements required for the standard asset index tables and the Europeana-specific asset,
 * catalog, and distribution schemas.
 *
 * @author Srishti Singh
 * @since 2026-12-11
 */
@Extension(EuropeanaSqlAssetIndexServiceExtension.NAME)
public class EuropeanaSqlAssetIndexServiceExtension implements ServiceExtension {

    public static final String NAME = "Europeana SQL Schema Service Extension";

    @Setting(description = "The datasource to be used", defaultValue = DataSourceRegistry.DEFAULT_DATASOURCE, key = "edc.sql.store.asset.datasource")
    private String dataSourceName;

    @Inject
    private DataSourceRegistry dataSourceRegistry;

    @Inject
    private TransactionContext transactionContext;

    @Inject
    private TypeManager typeManager;

    @Inject
    private QueryExecutor queryExecutor;

    @Inject
    private SqlSchemaBootstrapper sqlSchemaBootstrapper;

    @Inject
    private ContractNegotiationStore contractNegotiationStore;

    @Inject
    private EventRouter eventRouter;

    @Inject
    private Monitor monitor;

    @Override
    public void initialize(ServiceExtensionContext context) {
        // initialise EuropeanaSqlAssetIndex with EuropeanaAssetStatements and
        // EuropeanaSqlDistributionIndex with EuropeanaDistributionStatements
        EuropeanaSqlAssetIndex sqlAssetIndex = new EuropeanaSqlAssetIndex(dataSourceRegistry, dataSourceName,
                transactionContext,
                typeManager.getMapper(), getDialect(), queryExecutor,
                new EuropeanaSqlDistributionIndex(
                        dataSourceRegistry,
                        dataSourceName,
                        transactionContext,
                        typeManager.getMapper(),
                        queryExecutor,
                        new EuropeanaDistributionStatements()));

        context.registerService(AssetIndex.class, sqlAssetIndex);
        context.registerService(DataAddressResolver.class, sqlAssetIndex);
        context.getMonitor().info("Registering Europeana SQL schema extension" + sqlAssetIndex
                .getClass().getName());

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

        // register the AssetService
        var assetObservable = new AssetObservableImpl();
        assetObservable.registerListener(new AssetEventListener(eventRouter));

        var assetService = new EuropeanaAssetServiceImpl(sqlAssetIndex,
                contractNegotiationStore,
                transactionContext,
                assetObservable,
                new AssetQueryValidator(),
                context.getMonitor());

          context.registerService(AssetService.class, assetService);

        context.getMonitor().info("Europeana Asset Service registered successfully"
        );
    }

    private EuropeanaAssetStatements getDialect() {
        return new EuropeanaAssetStatements();
    }
}