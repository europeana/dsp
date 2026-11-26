package org.eclipse.edc.mvd.jsonld.context;

import jakarta.json.JsonObject;
import jakarta.ws.rs.ext.WriterInterceptorContext;
import org.eclipse.edc.jsonld.spi.JsonLd;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

import static org.eclipse.edc.api.management.ManagementApi.MANAGEMENT_SCOPE_V4;

/**
 * An extension that registers a custom Json-LD context for the MANAGEMENT_SCOPE_V4 scope which is being used for
 * creating the JerseyJsonLdInterceptor in
 * @see org.eclipse.edc.connector.controlplane.api.management.asset.AssetApiExtension
 *
 * @see org.eclipse.edc.web.jersey.providers.jsonld.JerseyJsonLdInterceptor#compact(JsonObject, WriterInterceptorContext)
 *
 * @author Srishti singh
 * @since 2026-08-01
 */
@Extension(EuropeanaManagementApiAssetContextExtension.NAME)
public class EuropeanaManagementApiAssetContextExtension implements ServiceExtension {

    public static final String NAME         = "Europeana Management API : Asset Context Extension";
    private static final String CONTEXT_URL = "https://api.test.eanadev.org/context/edc.jsonld";

    @Inject
    private JsonLd jsonLd;

    @Override
    public void initialize(ServiceExtensionContext context) {
        jsonLd.registerContext(CONTEXT_URL, MANAGEMENT_SCOPE_V4);
        context.getMonitor().info("Registered Europeana JSON-LD context for scope: " + MANAGEMENT_SCOPE_V4);
    }
}