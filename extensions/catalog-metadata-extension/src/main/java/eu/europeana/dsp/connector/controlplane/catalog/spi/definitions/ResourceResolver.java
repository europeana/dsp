package eu.europeana.dsp.connector.controlplane.catalog.spi.definitions;

import eu.europeana.dsp.connector.controlplane.catalog.spi.Resource;

import java.util.Map;

/**
 * The class ResourceResolver provides a base implementation for resolving
 * and managing metadata properties of a DCAT resource.
 *
 * The class consolidates multiple metadata attributes, adhering to the DCAT specifications,
 * and maps them to keys for structured resource representation.
 * @author Srishti Singh
 * @since 2026-09-1
 */
public class ResourceResolver implements Resource {

    private String title;
    private String description;
    private String identifier;

    private Object issued;
    private Object modified;
    private Object language;
    private Object publisher;
    private Object creator;
    private Object contactPoint;
    private Object keyword;
    private Object theme;
    private Object landingPage;
    private Object accessRights;
    private Object license;
    private Object rights;
    private Object conformsTo;
    private Object provenance;
    private Object qualifiedRelation;

    public ResourceResolver() {
    }

    public ResourceResolver(
            String title,
            String description,
            String identifier) {
        this.title = title;
        this.description = description;
        this.identifier = identifier;
    }

    public ResourceResolver(Map<String, Object> properties) {
        setProperties(properties);
    }

    public void setProperties(Map<String, Object> properties) {
        if (properties == null) {
            return;
        }

        this.title = (String) properties.get("dct:title");
        this.description = (String) properties.get("dct:description");
        this.identifier = (String) properties.get("dct:identifier");

        this.issued = properties.get("dct:issued");
        this.modified = properties.get("dct:modified");
        this.language = properties.get("dct:language");

        this.publisher = properties.get("dct:publisher");
        this.creator = properties.get("dct:creator");

        this.contactPoint = properties.get("dcat:contactPoint");
        this.keyword = properties.get("dcat:keyword");
        this.theme = properties.get("dcat:theme");
        this.landingPage = properties.get("dcat:landingPage");
        this.qualifiedRelation = properties.get("dcat:qualifiedRelation");

        this.accessRights = properties.get("dct:accessRights");
        this.license = properties.get("dct:license");
        this.rights = properties.get("dct:rights");
        this.conformsTo = properties.get("dct:conformsTo");
        this.provenance = properties.get("dct:provenance");
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public Object getIssued() {
        return issued;
    }

    @Override
    public Object getModified() {
        return modified;
    }

    @Override
    public Object getLanguage() {
        return language;
    }

    @Override
    public Object getPublisher() {
        return publisher;
    }

    @Override
    public Object getCreator() {
        return creator;
    }

    @Override
    public Object getContactPoint() {
        return contactPoint;
    }

    @Override
    public Object getKeyword() {
        return keyword;
    }

    @Override
    public Object getTheme() {
        return theme;
    }

    @Override
    public Object getLandingPage() {
        return landingPage;
    }

    @Override
    public Object getAccessRights() {
        return accessRights;
    }

    @Override
    public Object getLicense() {
        return license;
    }

    @Override
    public Object getRights() {
        return rights;
    }

    @Override
    public Object getConformsTo() {
        return conformsTo;
    }

    @Override
    public Object getProvenance() {
        return provenance;
    }

    @Override
    public Object getQualifiedRelation() {
        return qualifiedRelation;
    }


    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setIssued(Object issued) {
        this.issued = issued;
    }

    public void setModified(Object modified) {
        this.modified = modified;
    }

    public void setLanguage(Object language) {
        this.language = language;
    }

    public void setPublisher(Object publisher) {
        this.publisher = publisher;
    }

    public void setCreator(Object creator) {
        this.creator = creator;
    }

    public void setContactPoint(Object contactPoint) {
        this.contactPoint = contactPoint;
    }

    public void setKeyword(Object keyword) {
        this.keyword = keyword;
    }

    public void setTheme(Object theme) {
        this.theme = theme;
    }

    public void setLandingPage(Object landingPage) {
        this.landingPage = landingPage;
    }

    public void setAccessRights(Object accessRights) {
        this.accessRights = accessRights;
    }

    public void setLicense(Object license) {
        this.license = license;
    }

    public void setRights(Object rights) {
        this.rights = rights;
    }

    public void setConformsTo(Object conformsTo) {
        this.conformsTo = conformsTo;
    }

    public void setProvenance(Object provenance) {
        this.provenance = provenance;
    }

    public void setQualifiedRelation(Object qualifiedRelation) {
        this.qualifiedRelation = qualifiedRelation;
    }
}
