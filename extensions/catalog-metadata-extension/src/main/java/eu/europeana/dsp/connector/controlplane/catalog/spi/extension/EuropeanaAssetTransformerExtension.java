package eu.europeana.dsp.connector.controlplane.catalog.spi.extension;

import eu.europeana.dsp.connector.controlplane.catalog.spi.transformer.from.JsonObjectFromEuropeanaAssetTransformer;
import eu.europeana.dsp.connector.controlplane.catalog.spi.transformer.to.JsonObjectToEuropeanaAssetTransformer;
import jakarta.json.Json;
import org.eclipse.edc.connector.controlplane.transform.edc.to.JsonObjectToAssetTransformer;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.edc.transform.spi.TypeTransformerRegistry;

import java.util.Map;

import static org.eclipse.edc.api.management.ManagementApi.MANAGEMENT_API_CONTEXT;
import static org.eclipse.edc.spi.constants.CoreConstants.JSON_LD;

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

    @Inject
    TypeManager typeManager;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        context.getMonitor().info("Initializing " + NAME);
        var jsonFactory = Json.createBuilderFactory(Map.of());

        transformerRegistry
                .forContext(MANAGEMENT_API_CONTEXT)
                .register(
                        new JsonObjectToEuropeanaAssetTransformer(new JsonObjectToAssetTransformer())
                );

        transformerRegistry
                .forContext(MANAGEMENT_API_CONTEXT)
                .register(
                        new JsonObjectFromEuropeanaAssetTransformer(jsonFactory,typeManager, JSON_LD)
                );
    }
}
