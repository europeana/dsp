package eu.europeana.dsp.connector.controlplane.catalog.spi.extension;

import eu.europeana.dsp.connector.controlplane.catalog.spi.JsonObjectFromEuropeanaDcatDistributionTransformer;
import jakarta.json.Json;
import org.eclipse.edc.jsonld.spi.JsonLd;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.transform.spi.TypeTransformerRegistry;

import java.util.Map;

@Extension(EuropeanaDcatTransformerExtension.NAME)
public class EuropeanaDcatTransformerExtension implements ServiceExtension {

    public static final String NAME = "Europeana DCAT Distribution Transformer Extension";

    // TODO need to add the dependency for the static constants, for now adding them like this
    String DSP_CONTEXT_SEPARATOR = ":";
    String V_2025_1_VERSION = "2025-1";
    String DSP_TRANSFORMER_CONTEXT = "dsp-api";
    String DSP_TRANSFORMER_CONTEXT_V_2025_1 = DSP_TRANSFORMER_CONTEXT + DSP_CONTEXT_SEPARATOR + V_2025_1_VERSION;

    @Inject
    private JsonLd jsonLd;

    @Inject
    private TypeTransformerRegistry transformerRegistry;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        context.getMonitor().info("Initializing " + NAME);
        var jsonFactory = Json.createBuilderFactory(Map.of());
        transformerRegistry
                .forContext(DSP_TRANSFORMER_CONTEXT_V_2025_1)
                .register(
                        new JsonObjectFromEuropeanaDcatDistributionTransformer(jsonFactory)
                );
    }
}