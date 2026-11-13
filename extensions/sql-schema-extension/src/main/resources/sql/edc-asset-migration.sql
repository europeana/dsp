-- THIS SCHEMA HAS BEEN WRITTEN AND TESTED ONLY FOR POSTGRES

-- table: edc_asset
-- altering the already existing edc_asset table by adding the modified_at column
ALTER TABLE edc_asset
    ADD COLUMN IF NOT EXISTS modified_at BIGINT;