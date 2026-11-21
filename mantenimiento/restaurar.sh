#!/usr/bin/env bash
# Restaura la base desde un .dump. REEMPLAZA los datos actuales: exige --confirmar.
# Uso: ./restaurar.sh ARCHIVO.dump --confirmar [--sin-copia-previa]
# Antes de restaurar guarda una copia del estado actual; la restauracion es una sola transaccion.
set -uo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/comun.sh"
ARCHIVO="${1:-}"; CONFIRMAR=0; COPIA=1
for a in "${@:2}"; do
    case "$a" in
        --confirmar) CONFIRMAR=1 ;;
        --sin-copia-previa) COPIA=0 ;;
        *) echo "Opcion desconocida: $a" >&2; exit 2 ;;
    esac
done
fallar() { log ERROR "$*"; exit 1; }

[ -f "$ARCHIVO" ] || fallar "Uso: $0 ARCHIVO.dump --confirmar  (no existe el archivo '$ARCHIVO')"
[ "$CONFIRMAR" -eq 1 ] || fallar "Esto REEMPLAZA los datos de '$BASE'. Repite agregando --confirmar si estas seguro."
log INFO "Inicio de restauracion de '$BASE' desde $(basename "$ARCHIVO")"
err="$(pg_restore --list "$ARCHIVO" 2>&1 >/dev/null)" || fallar "El archivo no es un backup valido: $err"
if [ "$COPIA" -eq 1 ]; then
    log INFO "Guardando copia previa del estado actual..."
    ETIQUETA=previo "$DIR_MANT/backup.sh" || fallar "No se pudo guardar la copia previa; se cancela la restauracion."
fi
err="$(pg_restore -d "$BASE" --clean --if-exists --no-owner --single-transaction --no-password "$ARCHIVO" 2>&1)" \
    || fallar "pg_restore fallo; la base no fue modificada: $err"
log INFO "Restauracion completada en '$BASE'."
