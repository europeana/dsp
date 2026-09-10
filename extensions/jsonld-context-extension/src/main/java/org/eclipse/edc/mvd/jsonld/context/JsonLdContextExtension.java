package org.eclipse.edc.mvd.jsonld.context;

import org.eclipse.edc.jsonld.spi.JsonLd;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

import java.net.URI;

/**
 * An extension that registers a custom JSON-LD context for use within the system.
 * This extension provides the ability to map a predefined JSON-LD context URL
 * to a local cached document and registers the context in the JSON-LD processor.
 *
 * @author Srishti singh
 * @since 2026-08-01
 */
@Extension(value = "Custom JSON-LD Context Extension")
public class JsonLdContextExtension implements ServiceExtension {

    private static final String CONTEXT_URL = "https://w3id.org/dspace/context.jsonld";

    @Inject
    private JsonLd jsonLd;

    @Override
    public void initialize(ServiceExtensionContext context) {
        try {
            var resource = getClass()
                    .getClassLoader()
                    .getResource("jsonld/dspace-context.jsonld");

            if (resource == null) {
                throw new IllegalStateException(
                        "Could not find dspace-context.jsonld"
                );
            }

            URI documentLocation = resource.toURI();

            // Tell JSON-LD about this context
            jsonLd.registerContext(CONTEXT_URL);

            // Map the URL to our local file
            jsonLd.registerCachedDocument(
                    CONTEXT_URL,
                    documentLocation
            );

            context.getMonitor()
                    .info("Registered JSON-LD context: " + CONTEXT_URL);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to register JSON-LD context",
                    e
            );
        }
    }
}