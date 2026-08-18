package org.eclipse.edc.connector.controlplane.services;

/**
 * Represents metadata for a catalog, including details such as title, description, publisher, and language.
 * This class is immutable and provides accessor methods to retrieve the catalog's metadata attributes.
 *
 * @author Srishti singh
 * @since 27 July 2026
 */
public class CatalogMetadata {

    private final String title;
    private final String description;
    private final String publisher;

    public CatalogMetadata(String title,
                           String description,
                           String publisher) {
        this.title = title;
        this.description = description;
        this.publisher = publisher;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPublisher() {
        return publisher;
    }
}
