-- THIS SCHEMA HAS BEEN WRITTEN AND TESTED ONLY FOR POSTGRES

-- table: edc_distribution

-- CONSTRAINT uq_edc_distribution_asset_slug
--     UNIQUE (asset_id, distribution_id)
-- It means:
-- - The same asset_id can appear in multiple rows.
-- - The same distribution_id can appear in multiple rows.
-- - But the same combination of both values cannot appear more than once.

CREATE TABLE IF NOT EXISTS edc_distribution
(
    id VARCHAR PRIMARY KEY,
    asset_id VARCHAR NOT NULL,
    created_at BIGINT,
    distribution_id VARCHAR NOT NULL,
    properties JSON,
    data_address JSON,
    participant_context_id VARCHAR,

    CONSTRAINT uq_edc_distribution_asset_slug
    UNIQUE (asset_id, distribution_id),

    CONSTRAINT fk_edc_distribution_asset
    FOREIGN KEY (asset_id)
    REFERENCES edc_asset(asset_id)
    ON DELETE CASCADE
    );