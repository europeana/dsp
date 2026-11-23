package eu.europeana.dsp.connector.controlplane.catalog.spi.service;

import eu.europeana.dsp.connector.controlplane.catalog.spi.definitions.EuropeanaAsset;
import eu.europeana.dsp.controlplane.store.sql.assetindex.EuropeanaSqlAssetIndex;
import org.eclipse.edc.connector.controlplane.asset.spi.domain.Asset;
import org.eclipse.edc.connector.controlplane.catalog.DatasetResolverImpl;
import org.eclipse.edc.connector.controlplane.catalog.spi.*;
import org.eclipse.edc.connector.controlplane.contract.spi.ContractOfferId;
import org.eclipse.edc.connector.controlplane.contract.spi.types.offer.ContractDefinition;
import org.eclipse.edc.connector.controlplane.policy.spi.PolicyDefinition;
import org.eclipse.edc.connector.controlplane.policy.spi.store.PolicyDefinitionStore;
import org.eclipse.edc.participant.spi.ParticipantAgent;
import org.eclipse.edc.participantcontext.spi.types.ParticipantContext;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.policy.model.PolicyType;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.query.CriterionOperatorRegistry;
import org.eclipse.edc.spi.query.QuerySpec;
import org.jetbrains.annotations.NotNull;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.lang.Integer.MAX_VALUE;
import static org.eclipse.edc.jsonld.spi.PropertyAndTypeNames.DCAT_ENDPOINT_URL_ATTRIBUTE;
import static org.eclipse.edc.participantcontext.spi.types.ParticipantResource.filterByParticipantContextId;
import static org.eclipse.edc.spi.constants.CoreConstants.EDC_NAMESPACE;

/**
 * Europeana-specific dataset resolver that adapts catalog dataset resolution to assets enriched with
 * Europeana distribution data.
 *
 * @author Srishti Singh
 * @since 2026-12-18
 */
public class EuropeanaDatasetResolver extends DatasetResolverImpl {

    private final ContractDefinitionResolver contractDefinitionResolver;
    private final PolicyDefinitionStore policyDefinitionStore;
    private final CriterionOperatorRegistry criterionOperatorRegistry;
    private final Monitor monitor;
    private final EuropeanaSqlAssetIndex europeanaSqlAssetIndex;
    private final EuropeanaDistributionResolver europeanaDistributionResolver;

    public EuropeanaDatasetResolver(ContractDefinitionResolver contractDefinitionResolver,
                                    EuropeanaSqlAssetIndex europeanaSqlAssetIndex,
                                    PolicyDefinitionStore policyDefinitionStore,
                                    EuropeanaDistributionResolver europeanaDistributionResolver,
                                    CriterionOperatorRegistry criterionOperatorRegistry,
                                    Monitor monitor) {
        super(contractDefinitionResolver, europeanaSqlAssetIndex, policyDefinitionStore, europeanaDistributionResolver, criterionOperatorRegistry, monitor);
        this.contractDefinitionResolver = contractDefinitionResolver;
        this.policyDefinitionStore = policyDefinitionStore;
        this.criterionOperatorRegistry = criterionOperatorRegistry;
        this.monitor = monitor;
        this.europeanaSqlAssetIndex = europeanaSqlAssetIndex;
        this.europeanaDistributionResolver = europeanaDistributionResolver;
    }

    /**
     * Queries the catalog for Europeana datasets that are visible to the specified participant and agent.
     *
     * The important changes are:
     *  - europeanaSqlAssetIndex queries the Europeana assets with distributions from the db
     *  - Maps to Dataset using our method now, which returns EuropeanaDistribution
     *  - Rest remains unchanged
     *
     * @param participantContext the participant context used to resolve contract definitions and constrain the asset query
     * @param agent the participant agent used to resolve applicable contract definitions
     * @param querySpec the query specification containing filters and paging information for the catalog query
     * @param protocol the protocol for which dataset distributions are resolved
     * @return a stream of matching datasets with contract offers, or an empty stream when no applicable contract definitions exist
     */
    @Override
    @NotNull
    public Stream<Dataset> query(ParticipantContext participantContext, ParticipantAgent agent, QuerySpec querySpec, String protocol) {
        var resolved = contractDefinitionResolver.resolveFor(participantContext, agent);
        var contractDefinitions = resolved.contractDefinitions();
        if (contractDefinitions.isEmpty()) {
            return Stream.empty();
        }

        var assetsQuery = QuerySpec.Builder.newInstance()
                .offset(0).limit(MAX_VALUE).filter(querySpec.getFilterExpression())
                .filter(filterByParticipantContextId(participantContext.getParticipantContextId()))
                .build();

        // invoke our asset Index and query the europeana assets
        return europeanaSqlAssetIndex.queryEuropeanaAssets(assetsQuery)
                .map(asset -> toDataset(contractDefinitions, asset, resolved.policies(), protocol))
                .filter(Dataset::hasOffers)
                .skip(querySpec.getOffset())
                .limit(querySpec.getLimit());
    }

    @Override
    public Dataset getById(ParticipantContext participantContext, ParticipantAgent agent, String id, String protocol) {
        var resolved = contractDefinitionResolver.resolveFor(participantContext, agent);
        var contractDefinitions = resolved.contractDefinitions();
        if (contractDefinitions.isEmpty()) {
            return null;
        }

        return Optional.of(id)
                .map(europeanaSqlAssetIndex::findEuropeanaAssetById)
                .map(asset -> toDataset(contractDefinitions, asset, resolved.policies(), protocol))
                .filter(Dataset::hasOffers)
                .orElse(null);
    }


    /**
     * Converts the supplied Europeana asset into a catalog dataset for the specified protocol.
     *
     * The important changes are:
     * - The method accepts EuropeanaAsset.
     * - We extract the standard EDC Asset using getAsset().
     * - We use the distributions already loaded by the SQL join.
     * - Contract definition matching and offer generation remain unchanged.
     *
     * @param contractDefinitions the contract definitions used to determine which contract offers apply to the asset
     * @param europeanaAsset the Europeana asset containing the underlying asset and its related distributions
     * @param policies the policy cache used to reuse or store policies by policy definition identifier
     * @param protocol the protocol for which Europeana distributions are resolved
     * @return the dataset representing the Europeana asset, including matching contract offers and distributions
     */
    private Dataset toDataset(
            List<ContractDefinition> contractDefinitions,
            EuropeanaAsset europeanaAsset,
            Map<String, Policy> policies,
            String protocol) {

        var asset = europeanaAsset.getAsset();

        List<Distribution> distributions = (List<Distribution>) europeanaDistributionResolver
                .getEuropeanaDistributions(
                protocol,
                europeanaAsset);

        var datasetBuilder = buildDataset(asset)
                .id(asset.getId())
                .distributions(distributions)
                .properties(asset.getProperties());

        contractDefinitions.stream()
                .filter(definition -> definition.getAssetsSelector().stream()
                        .map(criterionOperatorRegistry::toPredicate)
                        .reduce(x -> true, Predicate::and)
                        .test(asset)
                )
                .forEach(contractDefinition -> {
                    var policy = policies.computeIfAbsent(
                            contractDefinition.getContractPolicyId(),
                            policyId -> Optional.ofNullable(
                                            policyDefinitionStore.findById(policyId))
                                    .map(PolicyDefinition::getPolicy)
                                    .orElse(null)
                    );

                    if (policy != null) {
                        var contractId = ContractOfferId.create(
                                contractDefinition.getId(),
                                asset.getId()
                        );

                        var offerPolicy = policy.toBuilder()
                                .type(PolicyType.OFFER)
                                .build();

                        datasetBuilder.offer(contractId.toString(), offerPolicy);
                    }
                });

        return datasetBuilder.build();
    }

    private Dataset.Builder<?, ?> buildDataset(Asset asset) {
        if (!asset.isCatalog()) {
            return Dataset.Builder.newInstance();
        }

        var endpointUrl = asset.getPropertyAsString(DCAT_ENDPOINT_URL_ATTRIBUTE);
        if (endpointUrl == null) {
            monitor.warning("""
                        Asset %s has no 'endpointURL' property and the DataAddress baseUrl is used instead, please adapt it as the
                        DataAddress will be removed from Asset in the forthcoming versions"""
                    .formatted(asset.getId()));
            endpointUrl = asset.getDataAddress().getStringProperty(EDC_NAMESPACE + "baseUrl", null);
        }

        return Catalog.Builder.newInstance()
                .dataService(DataService.Builder.newInstance()
                        .id(Base64.getUrlEncoder().encodeToString(asset.getId().getBytes()))
                        .endpointDescription(asset.getDescription())
                        .endpointUrl(endpointUrl)
                        .build());
    }


}
