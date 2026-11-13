-- THIS SCHEMA HAS BEEN WRITTEN AND TESTED ONLY FOR POSTGRES

-- table: edc_catalog
CREATE TABLE edc_catalog (
                             catalog_id VARCHAR,
                             created_at BIGINT,
                             modified_at BIGINT,
                             properties JSON,
                             participant_context_id VARCHAR
);