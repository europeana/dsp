package eu.europeana.dsp.connector.controlplane.catalog.spi.extension;

import eu.europeana.dsp.connector.controlplane.catalog.spi.service.EuropeanaCatalogProtocolServiceImpl;
import org.eclipse.edc.connector.controlplane.catalog.spi.DataServiceRegistry;
import org.eclipse.edc.connector.controlplane.catalog.spi.DatasetResolver;
import org.eclipse.edc.connector.controlplane.services.spi.catalog.CatalogProtocolService;
import org.eclipse.edc.connector.controlplane.services.spi.protocol.ProtocolTokenValidator;
import org.eclipse.edc.participantcontext.spi.identity.ParticipantIdentityResolver;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provides;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.transaction.spi.TransactionContext;

import java.util.Map;

/**
 * The EuropeanaCatalogProtocolServiceExtension integrates a protocol-based catalog service into the runtime.
 * It provides configuration-driven metadata for the catalog such as title, description, and publisher,
 * and utilizes a number of injected dependencies to manage catalog queries, dataset resolution, and participant
 * identity resolution. This extension registers the catalog service for use at runtime.
 *
 * Configuration Properties:
 * - `edc.catalog.title`: The title of the catalog (default: "Europeana Dataset Catalog").
 * - `edc.catalog.description`: A brief description of the catalog (default: "Europeana datasets available through DSP").
 * - `edc.catalog.publisher`: Publisher information for the catalog (default: "Europeana Foundation").
 *
 * This extension is annotated with `@Extension` and is designed to execute after `ControlPlaneServicesExtension`.
 *
 * @author Srishti Singh
 * @since 2026-09-5
 */
@Extension(EuropeanaCatalogProtocolServiceExtension.NAME)
@Provides(CatalogProtocolService.class)
public class EuropeanaCatalogProtocolServiceExtension implements ServiceExtension {

    public static final String NAME = "Europeana Catalog Protocol Service Extension";

    @Inject
    private DatasetResolver datasetResolver;

    @Inject
    private DataServiceRegistry dataServiceRegistry;

    @Inject
    private ParticipantIdentityResolver identityResolver;

    @Inject
    private TransactionContext transactionContext;

    @Inject
    private ProtocolTokenValidator protocolTokenValidator;

    @Setting(
            description = "Catalog title",
            defaultValue = "Europeana Dataset Catalog",
            key = "edc.catalog.title"
    )
    private String catalogTitle;

    @Setting(
            description = "Catalog description",
            defaultValue = "Europeana datasets available through DSP",
            key = "edc.catalog.description"
    )
    private String catalogDescription;

    @Setting(
            description = "Catalog Publisher",
            defaultValue = "Europeana Foundation",
            key = "edc.catalog.publisher"
    )
    private String catalogPublisher;

    @Override
    public void initialize(ServiceExtensionContext context) {
        context.getMonitor().info(NAME + "Loaded ....");

        var properties = Map.<String, Object>of(
                "title", catalogTitle,
                "description", catalogDescription,
                "publisher", catalogPublisher
        );

        var catalogService = new EuropeanaCatalogProtocolServiceImpl(
                datasetResolver,
                dataServiceRegistry,
                protocolTokenValidator,
                identityResolver,
                transactionContext,
                properties
        );

        context.registerService(CatalogProtocolService.class, catalogService);
    }
}