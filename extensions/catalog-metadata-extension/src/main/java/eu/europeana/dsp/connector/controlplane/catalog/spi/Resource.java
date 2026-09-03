package eu.europeana.dsp.connector.controlplane.catalog.spi;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a generic interface for accessing various metadata properties of a dcat:resource.
 * It follows the DCAT specifications to support metadata interoperability for cataloged resources.
 *
 * The properties provided by this interface include both descriptive and administrative metadata
 * which can be utilized in resource management or catalog-related operations.
 * See: @link https://www.w3.org/TR/vocab-dcat/#resource
 * @author  Srishti Singh
 * @since 2026-09-1
 */
public interface Resource {

    String getTitle();

    String getDescription();

    String getIdentifier();

    Object getIssued();

    Object getModified();

    Object getLanguage();

    Object getPublisher();

    Object getCreator();

    Object getContactPoint();

    Object getKeyword();

    Object getTheme();

    Object getLandingPage();

    Object getAccessRights();

    Object getLicense();

    Object getRights();

    Object getConformsTo();

    Object getProvenance();

    Object getQualifiedRelation();

    default Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap<>();

        putIfNotNull(properties, "dct:title", getTitle());
        putIfNotNull(properties, "dct:description", getDescription());
        putIfNotNull(properties, "dct:identifier", getIdentifier());
        putIfNotNull(properties, "dct:issued", getIssued());
        putIfNotNull(properties, "dct:modified", getModified());
        putIfNotNull(properties, "dct:language", getLanguage());
        putIfNotNull(properties, "dct:publisher", getPublisher());
        putIfNotNull(properties, "dct:creator", getCreator());

        putIfNotNull(properties, "dcat:contactPoint", getContactPoint());
        putIfNotNull(properties, "dcat:keyword", getKeyword());
        putIfNotNull(properties, "dcat:theme", getTheme());
        putIfNotNull(properties, "dcat:landingPage", getLandingPage());
        putIfNotNull(properties, "dcat:qualifiedRelation", getQualifiedRelation());

        putIfNotNull(properties, "dct:accessRights", getAccessRights());
        putIfNotNull(properties, "dct:license", getLicense());
        putIfNotNull(properties, "dct:rights", getRights());
        putIfNotNull(properties, "dct:conformsTo", getConformsTo());
        putIfNotNull(properties, "dct:provenance", getProvenance());

        return properties;
    }

    private void putIfNotNull(
            Map<String, Object> properties,
            String key,
            Object value
    ) {
        if (value != null) {
            properties.put(key, value);
        }
    }
}
