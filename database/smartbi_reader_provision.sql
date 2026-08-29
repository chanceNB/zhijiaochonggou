-- Run as a database administrator with psql variables:
-- psql -d zhijiao -v database_name=zhijiao -v smartbi_reader_password='replace-me'
-- Never commit the real password or place it in application configuration.

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'smartbi_reader') THEN
        CREATE ROLE smartbi_reader LOGIN;
    END IF;
END
$$;

ALTER ROLE smartbi_reader PASSWORD :'smartbi_reader_password';
GRANT CONNECT ON DATABASE :database_name TO smartbi_reader;

REVOKE ALL ON SCHEMA app FROM smartbi_reader;
REVOKE ALL ON ALL TABLES IN SCHEMA app FROM smartbi_reader;
REVOKE ALL ON ALL SEQUENCES IN SCHEMA app FROM smartbi_reader;
REVOKE CREATE ON SCHEMA app FROM smartbi_reader;

GRANT USAGE ON SCHEMA smartbi_exchange TO smartbi_reader;
GRANT SELECT ON ALL TABLES IN SCHEMA smartbi_exchange TO smartbi_reader;
REVOKE INSERT, UPDATE, DELETE, TRUNCATE, REFERENCES, TRIGGER ON ALL TABLES IN SCHEMA smartbi_exchange FROM smartbi_reader;
REVOKE CREATE ON SCHEMA smartbi_exchange FROM smartbi_reader;

ALTER DEFAULT PRIVILEGES IN SCHEMA smartbi_exchange
    GRANT SELECT ON TABLES TO smartbi_reader;
ALTER DEFAULT PRIVILEGES IN SCHEMA smartbi_exchange
    REVOKE INSERT, UPDATE, DELETE, TRUNCATE, REFERENCES, TRIGGER ON TABLES FROM smartbi_reader;
