package eu.europeana.dsp.connector.controlplane.catalog.spi;

import static org.eclipse.edc.spi.constants.CoreConstants.EDC_NAMESPACE;

/**
 * The DspVocabulary class provides a centralized definition for static constants
 * used within the DSP (Digital Services Platform) context to ensure consistency
 * across various parts of the application. These constants include context
 * definitions, versioning, and policy-related attributes.
 *
 * @author Srishti Singh
 * @since 2026-09-15
 */
public class DspVocabulary {

    // TODO need to add the dependency for the static constants, for now adding them like this
    // DSP context
    public static final String DSP_CONTEXT_SEPARATOR            = ":";
    public static final String V_2025_1_VERSION                 = "2025-1";
    public static final String DSP_TRANSFORMER_CONTEXT          = "dsp-api";
    public static final String DSP_TRANSFORMER_CONTEXT_V_2025_1 = DSP_TRANSFORMER_CONTEXT +
                                                                  DSP_CONTEXT_SEPARATOR + V_2025_1_VERSION;
    // policy definition
    public static final String POLICY_DEFINITION_EUROPEANA_PROPERTIES  =   "europeanaProperties";
    public static final String EDC_POLICY_DEFINITION_PUBLIC_PROPERTIES = EDC_NAMESPACE + "properties";
}
