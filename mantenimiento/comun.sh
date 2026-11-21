#!/usr/bin/env bash
# Funciones y valores por defecto compartidos por los scripts .sh de mantenimiento.
# Conexion: variables estandar de PostgreSQL (PGHOST, PGPORT, PGUSER, PGDATABASE) y la clave
# en ~/.pgpass (chmod 600) o en PGPASSWORD. Nunca se escribe la clave en los scripts.
DIR_MANT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RAIZ="$(dirname "$DIR_MANT")"
DIR_LOGS="$DIR_MANT/logs"
LOG="$DIR_LOGS/mantenimiento.log"

export PGHOST="${PGHOST:-localhost}"
export PGPORT="${PGPORT:-5432}"
export PGUSER="${PGUSER:-ferresys}"
BASE="${PGDATABASE:-ferresys}"
DESTINO="${DESTINO:-$RAIZ/backups}"

log() {  # log NIVEL mensaje...
    local nivel="$1"; shift
    mkdir -p "$DIR_LOGS"
    printf '%s [%s] %s\n' "$(date '+%Y-%m-%d %H:%M:%S')" "$nivel" "$*" | tee -a "$LOG" >&2
}
