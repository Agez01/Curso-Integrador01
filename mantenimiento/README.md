# Mantenimiento de Ferresys

Backups automáticos de PostgreSQL, restauración, prueba de restauración y limpieza de logs.
Procedimiento completo: `Procedimientos_Mantenimiento_Ferresys.docx`.

## Instalación (una vez por integrante)
1. Agrega al `.gitignore` del proyecto:
   ```
   backups/
   mantenimiento/logs/
   ```
2. Crea `%APPDATA%\postgresql\pgpass.conf` (Linux: `~/.pgpass`, `chmod 600`) con la clave, sin ponerla en los scripts:
   ```
   localhost:5432:ferresys:ferresys:CLAVE_DE_FERRESYS
   localhost:5432:*:postgres:CLAVE_DE_POSTGRES
   ```
3. Permisos de restauración (como administrador):
   `psql -U postgres -h localhost -d ferresys -f mantenimiento/preparar-permisos.sql`
4. Programa las tareas: `.\mantenimiento\instalar-tareas.ps1` (opcional: `-IncluirPruebaRestauracion`).

## Uso
| Acción | Windows | Linux |
|---|---|---|
| Backup | `.\mantenimiento\backup.ps1` | `mantenimiento/backup.sh` |
| Restaurar | `.\mantenimiento\restaurar.ps1 -Archivo backups\X.dump -Confirmar` | `mantenimiento/restaurar.sh backups/X.dump --confirmar` |
| Probar restauración | `.\mantenimiento\probar-restauracion.ps1` | `mantenimiento/probar-restauracion.sh` |
| Limpiar logs | `.\mantenimiento\limpiar-logs.ps1 -Dias 30` | `mantenimiento/limpiar-logs.sh` |
| Programar | `.\mantenimiento\instalar-tareas.ps1` | `crontab -e` con `crontab.example` |

Registro de todo lo ejecutado: `mantenimiento/logs/mantenimiento.log`.
