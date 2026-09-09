package eu.europeana.dsp.connector.controlplane.catalog.spi.extension;

import eu.europeana.dsp.connector.controlplane.catalog.spi.JsonObjectFromEuropeanaDcatDistributionTransformer;
import jakarta.json.JsonBuilderFactory;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.transform.spi.TypeTransformerRegistry;

@Extension(EuropeanaDcatTransformerExtension.NAME)
public class EuropeanaDcatTransformerExtension implements ServiceExtension {

    public static final String NAME = "Europeana DCAT Distribution Transformer Extension";

    @Inject
    private TypeTransformerRegistry transformerRegistry;

    @Inject
    private JsonBuilderFactory jsonFactory;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        transformerRegistry.register(
                new JsonObjectFromEuropeanaDcatDistributionTransformer(jsonFactory)
        );
    }
}