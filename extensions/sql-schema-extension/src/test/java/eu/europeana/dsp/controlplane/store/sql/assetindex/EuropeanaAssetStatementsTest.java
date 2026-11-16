package eu.europeana.dsp.controlplane.store.sql.assetindex;

import org.eclipse.edc.sql.translation.PostgresqlOperatorTranslator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EuropeanaAssetStatementsTest {

    private static String INSERT_TEMPLATE = "INSERT INTO edc_asset (asset_id, created_at, properties, private_properties, " +
            "data_address, participant_context_id, dataplane_metadata, modified_at) " +
            "VALUES (?, ?, ?::json, ?::json, ?::json, ?, ?::json, ?);";

    private static String UPDATE_TEMPLATE = "UPDATE edc_asset SET modified_at = ?, " +
            "properties = ?::json, " +
            "private_properties = ?::json, " +
            "data_address = ?::json, " +
            "dataplane_metadata = ?::json " +
            "WHERE asset_id = ?;";

    EuropeanaAssetStatements statements = new EuropeanaAssetStatements();

    @Test
    void testGetInsertAssetTemplate(){
        String template = statements.getInsertAssetTemplate();
        assertEquals(INSERT_TEMPLATE, template);
    }


    @Test
    void testGetUpdateAssetTemplate(){
        String template = statements.getUpdateAssetTemplate();
        assertEquals(UPDATE_TEMPLATE, template);
    }
}
