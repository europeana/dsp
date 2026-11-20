package eu.europeana.dsp.controlplane.store.sql.catalogindex;

/**
 * SQL statements and column names for querying Europeana catalog data.
 *
 * @author Srishti Singh
 * @since 2026-11-19
 */
public class EuropeanaCatalogStatements {

    public String getCatalogTable() {
        return "edc_catalog";
    }

    public String getParticipantContextIdColumn() {
        return "participant_context_id";
    }

    public String getPropertiesColumn() {
        return "properties";
    }

    public String getCatalogPropertiesByParticipantContextIdTemplate() {
        return """
            SELECT properties
            FROM edc_catalog
            WHERE participant_context_id = ?
            """;
    }
}
