-- Ejecutar UNA vez como superusuario (postgres):  psql -U postgres -f db/01_crear_bd.sql
-- Cambia la clave antes de ejecutar y usa la misma en src/main/resources/db.properties
CREATE ROLE ferresys LOGIN PASSWORD '123456';
CREATE DATABASE ferresys OWNER ferresys ENCODING 'UTF8';
ALTER DATABASE ferresys SET timezone TO 'America/Lima';
