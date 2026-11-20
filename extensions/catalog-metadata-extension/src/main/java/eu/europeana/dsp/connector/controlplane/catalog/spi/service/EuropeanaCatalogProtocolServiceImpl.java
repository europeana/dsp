package eu.europeana.dsp.connector.controlplane.catalog.spi.service;

import eu.europeana.dsp.controlplane.store.sql.catalogindex.EuropeanaSqlCatalogIndex;
import org.eclipse.edc.connector.controlplane.catalog.spi.Catalog;
import org.eclipse.edc.connector.controlplane.catalog.spi.CatalogRequestMessage;
import org.eclipse.edc.connector.controlplane.catalog.spi.DataServiceRegistry;
import org.eclipse.edc.connector.controlplane.catalog.spi.Dataset;
import org.eclipse.edc.connector.controlplane.catalog.spi.DatasetRequestMessage;
import org.eclipse.edc.connector.controlplane.services.spi.catalog.CatalogProtocolService;
import org.eclipse.edc.connector.controlplane.services.spi.protocol.ProtocolTokenValidator;
import org.eclipse.edc.participantcontext.spi.identity.ParticipantIdentityResolver;
import org.eclipse.edc.participantcontext.spi.types.ParticipantContext;
import org.eclipse.edc.policy.context.request.spi.RequestCatalogPolicyContext;
import org.eclipse.edc.spi.iam.TokenRepresentation;
import org.eclipse.edc.spi.result.ServiceResult;
import org.eclipse.edc.transaction.spi.TransactionContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import static java.lang.String.format;

/**
 * Europeana-specific implementation of the catalog protocol service.
 *
 * @author Srishti Singh
 * @since 2026-12-19
 */
public class EuropeanaCatalogProtocolServiceImpl implements CatalogProtocolService {

    private final EuropeanaDatasetResolver europeanaDatasetResolver;
    private final DataServiceRegistry dataServiceRegistry;
    private final ParticipantIdentityResolver identityResolver;
    private final TransactionContext transactionContext;

    private final ProtocolTokenValidator protocolTokenValidator;
    private final EuropeanaSqlCatalogIndex sqlCatalogIndex;

    public EuropeanaCatalogProtocolServiceImpl(EuropeanaDatasetResolver europeanaDatasetResolver,
                                               DataServiceRegistry dataServiceRegistry,
                                               ProtocolTokenValidator protocolTokenValidator,
                                               ParticipantIdentityResolver identityResolver,
                                               TransactionContext transactionContext,
                                               EuropeanaSqlCatalogIndex sqlCatalogIndex) {
        this.europeanaDatasetResolver = europeanaDatasetResolver;
        this.dataServiceRegistry = dataServiceRegistry;
        this.protocolTokenValidator = protocolTokenValidator;
        this.identityResolver = identityResolver;
        this.transactionContext = transactionContext;
        this.sqlCatalogIndex = sqlCatalogIndex;
    }

    @Override
    @NotNull
    public ServiceResult<Catalog> getCatalog(ParticipantContext participantContext, CatalogRequestMessage message, TokenRepresentation tokenRepresentation) {
        return transactionContext.execute(() -> protocolTokenValidator.verify(participantContext, tokenRepresentation, RequestCatalogPolicyContext::new, message)
                .map(agent -> {
                    try (var datasets = europeanaDatasetResolver.query(participantContext, agent, message.getQuerySpec(), message.getProtocol())) {
                        var dataServices = dataServiceRegistry.getDataServices(participantContext.getId(), message.getProtocol());

                        return Catalog.Builder.newInstance()
                                .dataServices(dataServices)
                                .datasets(datasets.toList())
                                .participantId(identityResolver.getParticipantId(participantContext.getId(), message.getProtocol()))
                                .properties(sqlCatalogIndex.findPropertiesByParticipantContextId(
                                                participantContext.getParticipantContextId()
                                        ).orElseGet(HashMap::new)
                                )
                                .build();
                    }
                })
        );
    }

    @Override
    public @NotNull ServiceResult<Dataset> getDataset(ParticipantContext participantContext, DatasetRequestMessage message, TokenRepresentation tokenRepresentation) {
        var datasetId = message.getDatasetId();
        return transactionContext.execute(() -> protocolTokenValidator.verify(participantContext, tokenRepresentation, RequestCatalogPolicyContext::new, message)
                .map(agent -> europeanaDatasetResolver.getById(participantContext, agent, datasetId, message.getProtocol()))
                .compose(dataset -> {
                    if (dataset == null) {
                        return ServiceResult.notFound(format("Dataset %s does not exist", datasetId));
                    }

                    return ServiceResult.success(dataset);
                }));
    }
}