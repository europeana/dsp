package org.eclipse.edc.mvd.jsonld.context;

import org.eclipse.edc.jsonld.spi.JsonLd;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import static org.eclipse.edc.protocol.dsp.spi.type.Dsp2025Constants.DSP_SCOPE_V_2025_1;

/**
 * Service extension that registers the Europeana DSP JSON-LD catalog context
 * with the EDC JSON-LD processor.
 *
 * @see {@link org.eclipse.edc.protocol.dsp.http.api.configuration.v2025.DspApiConfigurationV2025Extension#registerNamespaces()}
 *
 * @author Srishti singh
 * @since 2026-11-26
 */
@Extension(value = EuropeanaDspApiContextConfigurationExtension.NAME)
public class EuropeanaDspApiContextConfigurationExtension implements ServiceExtension {

    public static final String NAME         = "Europeana Dataspace Protocol 2025/1 API Context Configuration Extension";
    private static final String CATALOG_URL = "https://api.test.eanadev.org/context/catalog.jsonld";

    @Inject
    private JsonLd jsonLd;

    @Override
    public void initialize(ServiceExtensionContext context) {
        jsonLd.registerContext(
                CATALOG_URL,
                DSP_SCOPE_V_2025_1
        );

        context.getMonitor().info(
                "Registered Europeana JSON-LD context for DSP 2025/1"
        );
    }
}