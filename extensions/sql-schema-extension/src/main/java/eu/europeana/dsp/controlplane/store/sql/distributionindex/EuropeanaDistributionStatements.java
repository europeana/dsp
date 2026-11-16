package eu.europeana.dsp.controlplane.store.sql.distributionindex;

/**
 * Provides SQL statement templates for persisting Europeana distribution records.
 *
 * The statements produced by this class target the distribution index table and
 * are intended to be used with prepared statement parameters supplied by the
 * SQL store layer.
 *
 * @author Srishti Singh
 * @since 2026-11-16
 */
public class EuropeanaDistributionStatements {

    public static String getInsertDistributionTemplate() {
        return """
            INSERT INTO edc_distribution (
                id,
                asset_id,
                created_at,
                distribution_id,
                properties,
                data_address,
                participant_context_id
            ) VALUES (?, ?, ?, ?, ?::json, ?::json, ?)
            """;
    }
}
