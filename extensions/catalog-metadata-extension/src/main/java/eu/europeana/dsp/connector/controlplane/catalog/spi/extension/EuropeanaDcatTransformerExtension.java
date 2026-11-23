package eu.europeana.dsp.connector.controlplane.catalog.spi.extension;

import eu.europeana.dsp.connector.controlplane.catalog.spi.transformer.from.JsonObjectFromEuropeanaDcatDistributionTransformer;
import jakarta.json.Json;
import org.eclipse.edc.jsonld.spi.JsonLd;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.transform.spi.TypeTransformerRegistry;

import java.util.Map;

import static eu.europeana.dsp.connector.controlplane.catalog.spi.DspVocabulary.DSP_TRANSFORMER_CONTEXT_V_2025_1;

@Extension(EuropeanaDcatTransformerExtension.NAME)
public class EuropeanaDcatTransformerExtension implements ServiceExtension {

    public static final String NAME = "Europeana DCAT Distribution Transformer Extension";

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