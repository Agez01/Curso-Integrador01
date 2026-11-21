# Guía de mantenimiento de Ferresys (Windows)

Esta guía cubre la preparación de PostgreSQL, copias de seguridad, tareas programadas, restauración, prueba de restauración, limpieza de logs y diagnóstico de errores.

> **Importante:** los ejemplos de restauración y borrado son para la base de demostración `ferresys`. No los ejecutes en una base con datos reales sin una autorización y un respaldo verificado.

## 1. Abrir la terminal y permitir scripts

Abre PowerShell en la carpeta raíz del proyecto. La política siguiente solo se aplica a esa ventana; no cambia permanentemente la configuración de Windows:

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
```

Ejecuta los scripts con `.\` al inicio de la ruta. Por ejemplo:

```powershell
.\mantenimiento\backup.ps1
```

`run .\run.ps1` no es una sintaxis válida para ejecutar un script.

## 2. Preparar PostgreSQL (una sola vez por equipo)

### 2.1 Herramientas de PostgreSQL

Los scripts buscan `pg_dump.exe`, `pg_restore.exe` y `psql.exe` en el `PATH` y en estas ubicaciones:

- `C:\Program Files\PostgreSQL\<versión>\bin`
- `D:\Program Files\PostgreSQL\<versión>\bin`

Si PostgreSQL está instalado en otra ruta, pasa la carpeta `bin` mediante `-CarpetaPgBin`. Ejemplo:

```powershell
.\mantenimiento\backup.ps1 -CarpetaPgBin 'D:\Program Files\PostgreSQL\18\bin'
```

### 2.2 Contraseñas locales (`pgpass.conf`)

Las contraseñas no deben escribirse en los scripts ni guardarse en Git. En Windows, crea o abre el archivo local:

```powershell
New-Item -ItemType Directory -Force "$env:APPDATA\postgresql" | Out-Null
notepad "$env:APPDATA\postgresql\pgpass.conf"
```

Agrega una entrada por usuario, reemplazando los marcadores por las contraseñas correspondientes:

```text
localhost:5432:ferresys:ferresys:<CLAVE_FERRESYS>
localhost:5432:*:postgres:<CLAVE_POSTGRES>
```

La entrada de `postgres` permite crear y eliminar la base temporal que usa `probar-restauracion.ps1`. Si una contraseña contiene `:` o `\`, escapa esos caracteres con `\` en `pgpass.conf`. Si cambias la contraseña en pgAdmin, actualiza también la entrada correspondiente en este archivo.

Para comprobar la conexión administrativa sin mostrar la contraseña:

```powershell
& 'D:\Program Files\PostgreSQL\18\bin\psql.exe' `
  -h localhost -p 5432 -U postgres -d postgres --no-password `
  -c 'SELECT current_user;'
```

El resultado esperado es `postgres`.

### 2.3 Permisos de restauración

Si las tablas pertenecen a `postgres`, un backup puede funcionar pero la restauración como `ferresys` puede fallar por permisos. Ejecuta el archivo completo [preparar-permisos.sql](./preparar-permisos.sql) una sola vez, conectado en pgAdmin como `postgres` a la base `ferresys`:

1. Abre **Query Tool** sobre `ferresys`.
2. Abre `preparar-permisos.sql`.
3. Ejecuta todo el archivo con **F5**.

El script cambia el propietario de la base, tablas y secuencias a `ferresys`. Para comprobarlo:

```sql
SELECT tablename, tableowner
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY tablename;
```

## 3. Backup manual (Evidencia 1)

Desde la raíz del proyecto:

```powershell
.\mantenimiento\backup.ps1
Get-ChildItem .\backups -Filter 'ferresys_*.dump' |
  Where-Object { $_.BaseName -notmatch '_previo_' } |
  Sort-Object LastWriteTime -Descending |
  Select-Object -First 1 Name, Length, LastWriteTime
```

El script crea `backups\ferresys_AAAAMMDD_HHMMSS.dump`, valida que no esté vacío y lo comprueba con `pg_restore --list`. Conserva por lo menos tres copias y aplica la retención configurada.

Un resultado correcto contiene líneas similares a:

```text
[INFO] Backup correcto: ferresys_....dump (... KB, verificado con pg_restore --list)
[INFO] Fin de backup. Copias guardadas: ...
```

La carpeta `backups/` está excluida de Git. No subas los archivos `.dump` al repositorio.

## 4. Registrar y probar tareas programadas (Evidencias 2 y 3)

Registra el backup diario, la limpieza semanal y, opcionalmente, la prueba semanal de restauración:

```powershell
.\mantenimiento\instalar-tareas.ps1 -IncluirPruebaRestauracion
```

La prueba semanal requiere que la entrada de `postgres` en `pgpass.conf` funcione. Horarios predeterminados: backup diario a las 02:00, limpieza los domingos a las 03:00 y prueba de restauración los domingos a las 04:00.

Comprueba que las tareas estén registradas:

```powershell
$tareas = @('Ferresys-Backup', 'Ferresys-LimpiarLogs', 'Ferresys-ProbarRestauracion')
Get-ScheduledTask -TaskName $tareas | Select-Object TaskName, State
```

Para ver el desencadenador diario, abre `taskschd.msc` y navega a **Biblioteca del Programador de tareas → Ferresys-Backup → Desencadenadores**.

Ejecuta ahora el backup programado y verifica su resultado:

```powershell
Start-ScheduledTask -TaskName 'Ferresys-Backup'
Start-Sleep -Seconds 10
Get-ScheduledTask -TaskName 'Ferresys-Backup' | Get-ScheduledTaskInfo |
  Format-List LastRunTime, LastTaskResult
Get-ChildItem .\backups -Filter 'ferresys_*.dump' |
  Where-Object { $_.BaseName -notmatch '_previo_' } |
  Sort-Object LastWriteTime -Descending |
  Select-Object -First 1 Name, Length, LastWriteTime
```

`LastTaskResult` debe ser `0`. Para ejecutar manualmente las otras tareas:

```powershell
Start-ScheduledTask -TaskName 'Ferresys-LimpiarLogs'
Start-ScheduledTask -TaskName 'Ferresys-ProbarRestauracion'
```

Comprueba cada una con `Get-ScheduledTask -TaskName '<nombre>' | Get-ScheduledTaskInfo`.

## 5. Restauración de un backup (Evidencia 4)

La restauración reemplaza objetos y datos de la base. Primero asegúrate de tener un `.dump` válido y detén la aplicación:

```powershell
.\stop.ps1
```

Selecciona el backup normal más reciente (no el backup `previo`):

```powershell
$backup = Get-ChildItem .\backups -Filter 'ferresys_*.dump' |
  Where-Object { $_.BaseName -notmatch '_previo_' } |
  Sort-Object LastWriteTime -Descending |
  Select-Object -First 1 -ExpandProperty FullName
$backup
```

Restaura con confirmación:

```powershell
.\mantenimiento\restaurar.ps1 -Archivo $backup -Confirmar
```

Antes de restaurar, el script guarda automáticamente el estado actual como `ferresys_previo_....dump`. En pgAdmin puedes comprobar que los productos regresaron:

```sql
SELECT count(*) FROM producto;
```

### Simulacro opcional de pérdida de datos (solo base de demostración)

Hazlo únicamente después de verificar el backup y estar preparado para restaurar enseguida. En pgAdmin, conectado a `ferresys`, anota primero el conteo:

```sql
SELECT count(*) FROM producto;
```

Luego ejecuta el borrado simulado:

```sql
TRUNCATE venta_detalle, venta, movimiento_kardex CASCADE;
DELETE FROM producto;
SELECT count(*) FROM producto;
```

El último conteo debe ser `0`. Captura esa evidencia **antes** de ejecutar la restauración del paso anterior; después verifica que el conteo vuelva al valor original.

## 6. Prueba de restauración en una base temporal (Evidencia 5)

```powershell
.\mantenimiento\probar-restauracion.ps1
```

La prueba restaura el backup más reciente que no sea `*_previo_*.dump` en `ferresys_verif`, cuenta registros y elimina la base temporal. Usa `postgres` por defecto y necesita permisos para crear bases. **No uses `ferresys_verif` para guardar datos importantes:** si ya existe, el script la elimina y la vuelve a crear.

Resultado esperado:

```text
[INFO] Prueba de restauracion CORRECTA (usuarios=..., productos=..., ventas=..., movimientos=...)
```

## 7. Consultar el log y probar un error controlado (Evidencia 6)

Ver las últimas entradas:

```powershell
Get-Content .\mantenimiento\logs\mantenimiento.log -Tail 15
```

Para provocar un error controlado, usa una base inexistente:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass `
  -File .\mantenimiento\backup.ps1 -Base base_que_no_existe
"ExitCode=$LASTEXITCODE"
Select-String -Path .\mantenimiento\logs\mantenimiento.log -Pattern 'ERROR' |
  Select-Object -Last 3
Get-ChildItem .\backups -Filter 'base_que_no_existe_*.dump'
```

El código esperado es `1`; el error debe aparecer en el log y el último comando no debe encontrar archivos parciales.

## 8. Limpieza de logs (Evidencia 7)

Por defecto, el script toma la ruta de logs de Tomcat de `run.config.ps1`; si no encuentra configuración, usa `logs` del proyecto. Para revisar antes y después:

```powershell
$tomcatLogs = 'C:\Users\Diego\AppData\Local\Programs\apache-tomcat-10.1.61\logs'
Get-ChildItem $tomcatLogs -File |
  Select-Object Name, LastWriteTime, Length

.\mantenimiento\limpiar-logs.ps1 -Dias 30

Get-ChildItem $tomcatLogs -File |
  Select-Object Name, LastWriteTime, Length
Get-Content .\mantenimiento\logs\mantenimiento.log -Tail 3
```

Solo elimina archivos `.log` y `.txt` con más días que el límite indicado. Para una demostración con un umbral de un día puedes usar `-Dias 1`, pero eso eliminará logs de más de un día que todavía existan.

## 9. Errores encontrados y soluciones

| Mensaje o síntoma | Causa probable | Solución |
|---|---|---|
| `run` no se reconoce | Se escribió `run .\run.ps1`. | Ejecuta `.\run.ps1` desde la carpeta del proyecto. |
| El script no está firmado digitalmente | La política de ejecución bloquea scripts locales. | Ejecuta `Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass` en esa ventana de PowerShell. No es necesario cambiar la política permanente. |
| `No se encontró pg_dump.exe` | PostgreSQL está instalado en otra unidad/ruta. | La búsqueda automática contempla `C:\Program Files` y `D:\Program Files`; para otra ruta pasa `-CarpetaPgBin '<ruta>\bin'`. |
| `no password supplied` o `password authentication failed` | Falta una entrada o la contraseña de `pgpass.conf` no coincide con la de PostgreSQL. | Actualiza la línea del usuario correcto en `%APPDATA%\postgresql\pgpass.conf`; si cambias la contraseña en pgAdmin, actualiza también este archivo. Nunca compartas ni subas la contraseña. |
| Error de propietario o permisos durante la restauración | Las tablas pertenecen a `postgres` o a otro usuario. | Ejecuta completo `preparar-permisos.sql` en pgAdmin como `postgres`. Si no cambiaste propietarios, prueba `restaurar.ps1` con `-Usuario postgres` y su entrada válida en `pgpass.conf`. |
| `permission denied` o no puede crear `ferresys_verif` | La prueba temporal necesita un usuario con permiso `CREATEDB`. | Usa `postgres` con contraseña válida en `pgpass.conf`; el parámetro predeterminado del script ya es `-Usuario postgres`. |
| La prueba reporta que no existe `ferresys_verif` | Puede aparecer un `NOTICE` normal al preparar una base temporal nueva. | Usa la versión actual de `comun.ps1`, que distingue el código de salida del proceso de los avisos enviados por `psql`. |
| La prueba selecciona un backup `previo` | Los backups `previo` pueden contener el estado vacío anterior a una restauración. | Usa la versión actual de `probar-restauracion.ps1`, que excluye esos archivos en la selección automática. |
| `LastTaskResult` indica que la tarea aún no se ejecutó | La tarea está registrada pero todavía no ha corrido. | Ejecútala con `Start-ScheduledTask`, espera unos segundos y consulta `Get-ScheduledTaskInfo` otra vez. |

El registro de mantenimiento está en `mantenimiento\logs\mantenimiento.log`. Los backups están en `backups\`. Ambos contienen información local; no los agregues al repositorio.

## 10. Nota sobre pruebas de la aplicación

`.\run.ps1` compila y ejecuta `mvn clean package` antes de iniciar Tomcat. Las pruebas de integración JDBC pueden insertar registros de demostración en PostgreSQL. Si necesitas preservar exactamente los conteos de la base, evita volver a ejecutar `run.ps1` después de la restauración; la aplicación puede iniciarse por separado desde Tomcat.
