package eu.europeana.dsp.connector.controlplane.catalog.spi;

import jakarta.json.JsonBuilderFactory;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.transform.spi.TypeTransformerRegistry;

public class EuropeanaDcatTransformerExtension implements ServiceExtension {

    @Inject
    private TypeTransformerRegistry transformerRegistry;

    @Inject
    private JsonBuilderFactory jsonFactory;

    @Override
    public String name() {
        return "Europeana DCAT Transformer Extension";
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        transformerRegistry.register(
                new JsonObjectFromEuropeanaDcatDistributionTransformer(jsonFactory)
        );
    }
}