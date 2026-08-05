package org.eclipse.edc.connector.controlplane.services;


import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

/**
 * The CatalogMetadataExtension class is a service extension component that initializes and registers
 * catalog metadata within the runtime context. This extension is responsible for creating and configuring
 * a {@link CatalogMetadata} instance based on the provided configuration properties.
 *
 *  @author Srishti singh
 *  @since 27 July 2026
 */
@Extension(value = "Catalog Metadata Extension")
public class CatalogMetadataExtension implements ServiceExtension {

    @Override
    public void initialize(ServiceExtensionContext context) {

        var config = context.getConfig();

        var metadata = new CatalogMetadata(
                config.getString("edc.catalog.title", ""),
                config.getString("edc.catalog.description", ""),
                config.getString("edc.catalog.publisher", "")
        );

        context.registerService(CatalogMetadata.class, metadata);

        context.getMonitor().info("Catalog Metadata Extension initialized");
    }
}
