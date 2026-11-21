-- Preparacion UNICA para que el usuario ferresys pueda hacer backup Y restauracion completos.
-- Ejecutar como administrador, conectado a la base ferresys:
--   psql -U postgres -h localhost -d ferresys -f mantenimiento/preparar-permisos.sql
-- Es necesario si las tablas se crearon con el usuario postgres (en lugar de ferresys) o si la
-- restauracion falla con "must be owner of table ..." / "permission denied for schema public".
-- Es seguro repetirlo.

-- 1) ferresys pasa a ser dueno de la base (en PostgreSQL 15+ eso le da permiso de crear objetos en public)
DO $$ BEGIN EXECUTE format('ALTER DATABASE %I OWNER TO ferresys', current_database()); END $$;
GRANT ALL ON SCHEMA public TO ferresys;

-- 2) ferresys pasa a ser dueno de todas las tablas y secuencias existentes
DO $$
DECLARE r record;
BEGIN
    FOR r IN SELECT tablename FROM pg_tables WHERE schemaname = 'public' LOOP
        EXECUTE format('ALTER TABLE public.%I OWNER TO ferresys', r.tablename);
    END LOOP;
    FOR r IN SELECT sequencename FROM pg_sequences WHERE schemaname = 'public' LOOP
        EXECUTE format('ALTER SEQUENCE public.%I OWNER TO ferresys', r.sequencename);
    END LOOP;
END $$;
