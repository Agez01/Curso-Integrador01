#!/usr/bin/env bash
# Backup de la base ferresys con pg_dump (formato custom comprimido).
# Verifica el archivo, aplica retencion y sale con codigo 0 (ok) o 1 (error).
# Variables opcionales: PGHOST PGPORT PGUSER PGDATABASE DESTINO DIAS_RETENCION MINIMO_COPIAS
set -uo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/comun.sh"
DIAS_RETENCION="${DIAS_RETENCION:-14}"
MINIMO_COPIAS="${MINIMO_COPIAS:-3}"
# ETIQUETA opcional (la usa restaurar.sh: "previo") para distinguir la copia de seguridad previa a una restauracion.
ARCHIVO="$DESTINO/${BASE}${ETIQUETA:+_$ETIQUETA}_$(date '+%Y%m%d_%H%M%S').dump"

fallar() { log ERROR "$*"; rm -f "$ARCHIVO"; exit 1; }

log INFO "Inicio de backup de '$BASE' en $PGHOST:$PGPORT (usuario $PGUSER)"
mkdir -p "$DESTINO" || fallar "No se pudo crear $DESTINO"
# Nunca sobrescribe un backup existente (dos copias en el mismo segundo).
if [ -e "$ARCHIVO" ]; then log ERROR "Ya existe $(basename "$ARCHIVO"); espera un segundo y reintenta."; exit 1; fi
err="$(pg_dump -d "$BASE" -F c --no-owner --no-password -f "$ARCHIVO" 2>&1)" \
    || fallar "pg_dump fallo: $err"
[ -s "$ARCHIVO" ] || fallar "El archivo de backup no se creo o esta vacio."
err="$(pg_restore --list "$ARCHIVO" 2>&1 >/dev/null)" \
    || fallar "El backup no paso la verificacion de pg_restore: $err"
log INFO "Backup correcto: $(basename "$ARCHIVO") ($(du -k "$ARCHIVO" | cut -f1) KB, verificado con pg_restore --list)"

# Retencion: borra copias de mas de DIAS_RETENCION dias, conservando siempre las ultimas MINIMO_COPIAS.
mapfile -t copias < <(ls -1t "$DESTINO/${BASE}"_*.dump 2>/dev/null)
for ((i = MINIMO_COPIAS; i < ${#copias[@]}; i++)); do
    if [ -n "$(find "${copias[$i]}" -mtime +"$DIAS_RETENCION" -print 2>/dev/null)" ]; then
        rm -f "${copias[$i]}" && log INFO "Copia antigua eliminada: $(basename "${copias[$i]}")"
    fi
done
log INFO "Fin de backup. Copias guardadas: $(ls -1 "$DESTINO/${BASE}"_*.dump 2>/dev/null | wc -l)"
