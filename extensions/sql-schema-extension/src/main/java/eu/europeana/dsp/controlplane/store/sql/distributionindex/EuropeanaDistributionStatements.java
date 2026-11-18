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

    protected String getDistributionTable() {
        return "edc_distribution";
    }

    protected String getAssetIdColumn() {
        return "asset_id";
    }

    protected String getDistributionIdColumn() {
        return "distribution_id";
    }

    protected String getPropertiesColumn() {
        return "properties";
    }

    protected String getDataAddressColumn() {
        return "data_address";
    }

    protected String getParticipantContextIdColumn() {
        return "participant_context_id";
    }

    protected String getCreatedAtColumn() {
        return "created_at";
    }

    public static String getUpsertDistributionTemplate() {
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
        ON CONFLICT (asset_id, distribution_id)
        DO UPDATE SET
            properties = EXCLUDED.properties,
            data_address = EXCLUDED.data_address,
            participant_context_id = EXCLUDED.participant_context_id
        """;
    }

    public String getFindDistributionsByAssetIdTemplate() {
        return """
        SELECT
            id,
            asset_id,
            created_at,
            distribution_id,
            properties,
            data_address,
            participant_context_id
        FROM edc_distribution
        WHERE asset_id = ?
        ORDER BY created_at
        """;
    }

    public String getDeleteDistributionTemplate() {
        return """
        DELETE FROM edc_distribution
        WHERE asset_id = ?
          AND distribution_id = ?
        """;
    }
    
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
