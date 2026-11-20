package eu.europeana.dsp.connector.controlplane.catalog.spi.extension;

import eu.europeana.dsp.connector.controlplane.catalog.spi.service.EuropeanaCatalogProtocolServiceImpl;
import eu.europeana.dsp.connector.controlplane.catalog.spi.service.EuropeanaDatasetResolver;
import eu.europeana.dsp.controlplane.store.sql.catalogindex.EuropeanaSqlCatalogIndex;
import org.eclipse.edc.connector.controlplane.catalog.spi.DataServiceRegistry;
import org.eclipse.edc.connector.controlplane.services.spi.catalog.CatalogProtocolService;
import org.eclipse.edc.connector.controlplane.services.spi.protocol.ProtocolTokenValidator;
import org.eclipse.edc.participantcontext.spi.identity.ParticipantIdentityResolver;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provides;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.transaction.spi.TransactionContext;

/**
 * The EuropeanaCatalogProtocolServiceExtension integrates a protocol-based catalog service into the runtime.
 * It provides configuration-driven metadata for the catalog such as title, description, and publisher,
 * and utilizes a number of injected dependencies to manage catalog queries, dataset resolution, and participant
 * identity resolution. This extension registers the catalog service for use at runtime.
 *
 * @author Srishti Singh
 * @since 2026-09-5
 */
@Extension(EuropeanaCatalogProtocolServiceExtension.NAME)
@Provides(CatalogProtocolService.class)
public class EuropeanaCatalogProtocolServiceExtension implements ServiceExtension {

    public static final String NAME = "Europeana Catalog Protocol Service Extension";

    @Inject
    private EuropeanaDatasetResolver datasetResolver;

    @Inject
    private DataServiceRegistry dataServiceRegistry;

    @Inject
    private ParticipantIdentityResolver identityResolver;

    @Inject
    private TransactionContext transactionContext;

    @Inject
    private ProtocolTokenValidator protocolTokenValidator;

    @Inject
    EuropeanaSqlCatalogIndex sqlCatalogIndex;

    @Override
    public void initialize(ServiceExtensionContext context) {
        var catalogService = new EuropeanaCatalogProtocolServiceImpl(
                datasetResolver,
                dataServiceRegistry,
                protocolTokenValidator,
                identityResolver,
                transactionContext,
                sqlCatalogIndex);

        context.registerService(CatalogProtocolService.class, catalogService);
        context.getMonitor().info("Europeana Catalog Protocol Service initialized");
    }
}