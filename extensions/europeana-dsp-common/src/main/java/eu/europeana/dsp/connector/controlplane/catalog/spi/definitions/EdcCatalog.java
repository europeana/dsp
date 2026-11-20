package eu.europeana.dsp.connector.controlplane.catalog.spi.definitions;

import eu.europeana.dsp.connector.controlplane.catalog.spi.Resource;
import eu.europeana.dsp.connector.controlplane.catalog.spi.ResourceDescriptionProvider;
import org.eclipse.edc.connector.controlplane.catalog.spi.Catalog;

import java.util.Map;

/**
 * Represents an EDC catalog that extends the base catalog model with catalog-specific
 * metadata and resource description support.
 *
 * The catalog stores an identifier, a set of metadata properties, and the participant
 * context identifier associated with the catalog. Its resource information is derived
 * from the configured properties and exposed through the ResourceDescriptionProvider
 * contract.
 *
 * @author Srishti Singh
 * @since 2026-11-18
 */
public class EdcCatalog extends Catalog implements ResourceDescriptionProvider {

    private String catalogId;
    private Map<String, Object> properties;
    private String participantContextId;

    public String getCatalogId() {
        return catalogId;
    }

    public void setCatalogId(String catalogId) {
        this.catalogId = catalogId;
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public String getParticipantContextId() {
        return participantContextId;
    }

    public void setParticipantContextId(String participantContextId) {
        this.participantContextId = participantContextId;
    }

    @Override
    public Resource getResourceInfo() {
        return buildResourceInfo(properties);
    }

    @Override
    public Resource buildResourceInfo(Map<String, Object> properties) {
        return new ResourceResolver(properties);
    }

    @Override
    public void setResourceInfo(Resource resourceInfo) {
        // left empty as we don't need ResourceInfo for DCAT distributions
    }
}