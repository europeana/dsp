-- THIS SCHEMA HAS BEEN WRITTEN AND TESTED ONLY FOR POSTGRES

-- table: edc_distribution
CREATE TABLE edc_distribution (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  asset_id VARCHAR NOT NULL,
                                  created_at BIGINT,
                                  distribution_id VARCHAR NOT NULL,
                                  properties JSON,
                                  data_address JSON,
                                  participant_context_id VARCHAR,
                                  CONSTRAINT uq_edc_distribution_asset_slug
                                      UNIQUE (asset_id, distribution_id)
);