-- THIS SCHEMA HAS BEEN WRITTEN AND TESTED ONLY FOR POSTGRES

-- table: edc_asset
-- altering the already existing edc_asset table by adding the modified_at column
ALTER TABLE edc_asset
    ADD COLUMN IF NOT EXISTS modified_at BIGINT;

-- The inherited getInsertAssetTemplate() does not include modified_at.
--  Rather than adding another parameter—which SqlAssetIndex.create() does not supply—give the column a PostgreSQL default.
-- ALTER TABLE edc_asset
--     ADD COLUMN IF NOT EXISTS modified_at BIGINT
--     DEFAULT ((EXTRACT(EPOCH FROM clock_timestamp()) * 1000)::BIGINT);