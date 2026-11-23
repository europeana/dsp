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
 *
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

    Map<String, Object> getProperties();
}