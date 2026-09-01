package org.eclipse.edc.europeana.catalog;

import jakarta.json.JsonBuilderFactory;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.TypeManager;
import org.eclipse.edc.transform.spi.TypeTransformerRegistry;

import static org.eclipse.edc.protocol.dsp.spi.type.Dsp2025Constants.DSP_TRANSFORMER_CONTEXT_V_2025_1;

@Extension(
        value = CatalogDistributionMetadataExtension.NAME
)
public class CatalogDistributionMetadataExtension implements ServiceExtension {

    public static final String NAME =
            "Catalog Distribution Metadata Extension";

        @Inject
        private TypeTransformerRegistry transformerRegistry;

        @Inject
        private JsonBuilderFactory jsonFactory;

        @Inject
        private TypeManager typeManager;

        @Override
        public void initialize(ServiceExtensionContext context) {

            transformerRegistry
                    .forContext(DSP_TRANSFORMER_CONTEXT_V_2025_1)
                    .register(
                            new JsonObjectFromDatasetWithDistributionMetadataTransformer(
                                    jsonFactory,
                                    typeManager,
                                    DSP_TRANSFORMER_CONTEXT_V_2025_1
                            )
                    );
        }
    }
