#!/usr/bin/env bash
# Comprueba que el ultimo backup se puede restaurar, usando una base temporal (no toca la real).
# Necesita un usuario que pueda crear bases: PGUSER_ADMIN (por defecto postgres).
# Uso: ./probar-restauracion.sh [ARCHIVO.dump]
set -uo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/comun.sh"
export PGUSER="${PGUSER_ADMIN:-postgres}"
TEMP="${BASE_TEMPORAL:-ferresys_verif}"
ARCHIVO="${1:-$(ls -1t "$DESTINO/${BASE}"_*.dump 2>/dev/null | head -n1)}"
CREADA=0
limpiar() { [ "$CREADA" -eq 1 ] && psql -d postgres -qtAc "DROP DATABASE IF EXISTS $TEMP" >/dev/null 2>&1; }
trap limpiar EXIT
fallar() { log ERROR "$*"; exit 1; }

[ -f "$ARCHIVO" ] || fallar "No hay backups que probar en $DESTINO"
log INFO "Prueba de restauracion con $(basename "$ARCHIVO") en la base temporal '$TEMP'"
psql -d postgres -v ON_ERROR_STOP=1 -qtAc "DROP DATABASE IF EXISTS $TEMP" >/dev/null || fallar "No se pudo preparar la base temporal"
psql -d postgres -v ON_ERROR_STOP=1 -qtAc "CREATE DATABASE $TEMP" >/dev/null || fallar "No se pudo crear la base temporal"
CREADA=1
err="$(pg_restore -d "$TEMP" --no-owner --no-password --exit-on-error "$ARCHIVO" 2>&1)" || fallar "La restauracion de prueba fallo: $err"
conteo="$(psql -d "$TEMP" -v ON_ERROR_STOP=1 -qtA -c "SELECT 'usuarios=' || (SELECT count(*) FROM usuario) || ', productos=' || (SELECT count(*) FROM producto) || ', ventas=' || (SELECT count(*) FROM venta) || ', movimientos=' || (SELECT count(*) FROM movimiento_kardex)")" \
    || fallar "No se pudieron contar las filas"
usuarios="$(psql -d "$TEMP" -qtAc 'SELECT count(*) FROM usuario')"
[ "${usuarios:-0}" -ge 1 ] || fallar "La base restaurada no tiene usuarios: el backup es sospechoso."
log INFO "Prueba de restauracion CORRECTA ($conteo)"
