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

    // policy definition
    public static final String POLICY_DEFINITION_EUROPEANA_PROPERTIES  =   "europeanaProperties";
    public static final String EDC_POLICY_DEFINITION_PUBLIC_PROPERTIES = EDC_NAMESPACE + "properties";

    // others
    public static final String EUROPEANA_DISTRIBUTIONS = EDC_NAMESPACE + "distribution";
    public static final String DATA_ADDRESS_ATTRIBUTE  = EDC_NAMESPACE + "dataAddress";


}
