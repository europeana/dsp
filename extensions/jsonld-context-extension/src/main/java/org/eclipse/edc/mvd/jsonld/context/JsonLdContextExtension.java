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

    private static final String CONTEXT_URL = "https://api.test.eanadev.org/context/edc.jsonld";

    @Inject
    private JsonLd jsonLd;

    @Override
    public void initialize(ServiceExtensionContext context) {
        jsonLd.registerContext(CONTEXT_URL);
        context.getMonitor().info("Registered Europeana JSON-LD context: " + CONTEXT_URL);
    }
}