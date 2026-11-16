package eu.europeana.dsp.controlplane.store.sql.assetindex;

import org.eclipse.edc.connector.controlplane.store.sql.assetindex.schema.postgres.PostgresDialectStatements;

/**
 * Asset SQL statement provider for the Europeana asset index schema.
 *
 * Extends the base EDC SQL dialect statements by adding support for the
 * {@code modified_at} column in asset persistence operations. Insert templates
 * are augmented with an additional placeholder for the modification timestamp,
 * and update templates are adjusted so the column is refreshed whenever an asset
 * is updated.
 *
 * @author Srishti Singh
 * @since 2026-12-11
 */
public class EuropeanaAssetStatements extends PostgresDialectStatements {

    private static final String MODIFIED_AT_COLUMN = "modified_at";

    /**
     * Returns the asset insert SQL template with the modified timestamp column
     * appended to the insert column list and a corresponding parameter placeholder
     * appended to the values list.
     *
     * @return the insert asset SQL template including a placeholder for the
     *         modified_at column
     * @throws IllegalStateException if the base insert template does not match the
     *         expected structure
     */
    @Override
    public String getInsertAssetTemplate() {
        String template = super.getInsertAssetTemplate();
        // Add modified_at to the INSERT column list.
        int valuesIndex = template.indexOf(") VALUES (");
        if (valuesIndex < 0) {
            throw new IllegalStateException("Unexpected asset INSERT template: " + template);
        }

        template = template.substring(0, valuesIndex)
                + ", " + MODIFIED_AT_COLUMN
                + template.substring(valuesIndex);

        // Add the timestamp expression to the VALUES list.
        int valuesEndIndex = template.lastIndexOf(')');

        return template.substring(0, valuesEndIndex)
                + ", ?"
                + template.substring(valuesEndIndex);
    }

    /**
     * Returns the asset update SQL template with the modified timestamp column
     * refreshed on every update.
     *
     * @return the update asset SQL template including a placeholder for the
     *         modified_at column
     */
    @Override
    public String getUpdateAssetTemplate() {
        String template = super.getUpdateAssetTemplate();

        // Refresh modified_at on every asset update.
        return template.replace(
                " SET ",
                " SET " + MODIFIED_AT_COLUMN + " = "
                         + "?, "
        );
    }
}