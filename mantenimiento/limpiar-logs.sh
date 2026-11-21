#!/usr/bin/env bash
# Borra logs con mas de DIAS dias (por defecto 30) y rota el log de mantenimiento (max 2 MB).
# Uso: ./limpiar-logs.sh [CARPETA_DE_LOGS]   (por defecto $CATALINA_BASE/logs o ../logs)
set -uo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/comun.sh"
DIAS="${DIAS:-30}"
RUTA="${1:-${CATALINA_BASE:+$CATALINA_BASE/logs}}"
RUTA="${RUTA:-$RAIZ/logs}"
[ -d "$RUTA" ] || { log ERROR "No existe la carpeta de logs: $RUTA"; exit 1; }
n="$(find "$RUTA" -maxdepth 1 -type f \( -name '*.log' -o -name '*.txt' \) -mtime +"$DIAS" -print -delete | wc -l)"
log INFO "Limpieza de logs en $RUTA: $n archivo(s) con mas de $DIAS dias eliminados"
if [ -f "$LOG" ] && [ "$(stat -c %s "$LOG")" -gt 2097152 ]; then mv -f "$LOG" "$LOG.1"; fi
