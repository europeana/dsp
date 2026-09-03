package eu.europeana.dsp.connector.controlplane.catalog.spi.definitions;

import eu.europeana.dsp.connector.controlplane.catalog.spi.Resource;
import eu.europeana.dsp.connector.controlplane.catalog.spi.ResourceDescriptionProvider;
import org.eclipse.edc.connector.controlplane.catalog.spi.Catalog;

import java.util.Map;

/**
 * Represents an implementation of a catalog that extends the base {@code Catalog} class
 * and adheres to the {@code ResourceDescriptionProvider} interface.
 * This class provides functionality for managing resource-related metadata
 * and description within a cataloging system.
 * @author  Srishti Singh
 * @since 2026-09-1
 */
public class EdcCatalog extends Catalog implements ResourceDescriptionProvider {

    private Resource resourceInfo;

    @Override
    public Resource getResourceInfo() {
        return resourceInfo != null
                ? resourceInfo
                : buildResourceInfo(getProperties());
    }

    @Override
    public Resource buildResourceInfo(Map<String, Object> properties) {
        return new ResourceResolver(properties);
    }

    @Override
    public void setResourceInfo(Resource resourceInfo) {
        this.resourceInfo = resourceInfo;
    }

}
