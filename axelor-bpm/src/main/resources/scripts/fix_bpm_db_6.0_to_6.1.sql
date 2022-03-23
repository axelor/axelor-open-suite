-- This script is for fix deployment issue of bpm diagram when use db of AOS 6.0 for AOS 6.1.
-- This script is used to delete all tables of camunda library and bpm module of AOS.
-- WARNING : It will remove all BPM data.
-- For fix the issue, Apply it on db of AOS 6.0 and restart the server.

DO $$DECLARE r Record;
BEGIN
    FOR r IN
    	SELECT table_name FROM information_schema.tables WHERE table_name LIKE 'act_%' OR table_name LIKE 'bpm_%'
    LOOP
    EXECUTE 'DROP TABLE ' || quote_ident(r.table_name) || ' CASCADE;';
    END LOOP;
END$$;
