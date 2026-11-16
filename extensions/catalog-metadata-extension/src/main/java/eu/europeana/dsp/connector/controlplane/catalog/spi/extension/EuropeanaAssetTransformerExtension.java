package eu.europeana.dsp.connector.controlplane.catalog.spi.extension;

import eu.europeana.dsp.connector.controlplane.catalog.spi.transformer.to.JsonObjectToEuropeanaAssetTransformer;
import org.eclipse.edc.connector.controlplane.transform.edc.to.JsonObjectToAssetTransformer;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.transform.spi.TypeTransformerRegistry;

import static eu.europeana.dsp.connector.controlplane.catalog.spi.DspVocabulary.DSP_TRANSFORMER_CONTEXT_V_2025_1;

/**
 * Service extension that registers Europeana-specific asset transformation support.
 *
 * The extension contributes a transformer for the management API transformer context, enabling
 * JSON objects representing assets to be converted into EDC {@link org.eclipse.edc.spi.types.domain.asset.Asset}
 * instances enriched with Europeana distribution metadata.
 *
 * @author Srishti Singh
 * @since 2026-11-16
 */
@Extension(EuropeanaAssetTransformerExtension.NAME)
public class EuropeanaAssetTransformerExtension implements ServiceExtension {

    public static final String NAME = "Europeana Asset Transformer Extension";

    @Inject
    private TypeTransformerRegistry transformerRegistry;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        context.getMonitor().info("Initializing " + NAME);
        transformerRegistry
                .forContext("management-api")
                .register(
                        new JsonObjectToEuropeanaAssetTransformer(new JsonObjectToAssetTransformer())
                );
    }
}
