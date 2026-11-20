package eu.europeana.dsp.controlplane.extension;

import eu.europeana.dsp.controlplane.store.sql.catalogindex.EuropeanaCatalogStatements;
import eu.europeana.dsp.controlplane.store.sql.catalogindex.EuropeanaSqlCatalogIndex;
import org.eclipse.edc.connector.controlplane.contract.spi.negotiation.store.ContractNegotiationStore;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.event.EventRouter;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.edc.sql.QueryExecutor;
import org.eclipse.edc.sql.bootstrapper.SqlSchemaBootstrapper;
import org.eclipse.edc.transaction.datasource.spi.DataSourceRegistry;
import org.eclipse.edc.transaction.spi.TransactionContext;

@Extension(EuropeanaSqlCatalogIndexExtension.NAME)
public class EuropeanaSqlCatalogIndexExtension implements ServiceExtension {

    public static final String NAME = "Europeana Catalog Sql Index Extension";

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

    private EuropeanaSqlCatalogIndex europeanaSqlCatalogIndex;

    @Override
    public void initialize(ServiceExtensionContext context) {
        europeanaSqlCatalogIndex = new EuropeanaSqlCatalogIndex(dataSourceRegistry,
                dataSourceName,
                transactionContext,
                typeManager.getMapper(),
                queryExecutor,
                new EuropeanaCatalogStatements());

        context.registerService(EuropeanaSqlCatalogIndex.class, europeanaSqlCatalogIndex);
    }

    @Provider
    public EuropeanaSqlCatalogIndex europeanaSqlCatalogIndex() {
        return europeanaSqlCatalogIndex;
    }

}
