package eu.europeana.dsp.connector.controlplane.catalog.spi.definitions;

import eu.europeana.dsp.connector.controlplane.catalog.spi.Resource;
import eu.europeana.dsp.connector.controlplane.catalog.spi.ResourceDescriptionProvider;
import org.eclipse.edc.spi.types.domain.DataAddress;
import java.util.Map;

/**
 * The EuropeanaDataAddress class extends the DataAddress class and implements the ResourceDescriptionProvider
 * interface. It provides mechanisms to handle metadata associated with a resource in compliance with DCAT
 * specifications. This class acts as a container for resource-specific information and provides methods to
 * retrieve and build metadata based on specified properties.
 *
 * @author  Srishti Singh
 * @since 2026-09-1
 */
public class EuropeanaDataAddress extends DataAddress implements ResourceDescriptionProvider {

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
