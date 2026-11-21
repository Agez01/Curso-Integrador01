<#
.SYNOPSIS
    Copia de seguridad de la base de datos ferresys (pg_dump, formato custom comprimido).
.DESCRIPTION
    1) Genera backups\ferresys_AAAAMMDD_HHMMSS.dump
    2) Verifica que el archivo no este vacio y que pg_restore pueda leerlo
    3) Elimina copias mas antiguas que -DiasRetencion, conservando siempre las ultimas -MinimoCopias
    La clave NO va en el script: usa el archivo pgpass.conf (ver docs de mantenimiento).
    Codigo de salida: 0 = correcto, 1 = error (queda registrado en mantenimiento\logs\mantenimiento.log).
.EXAMPLE
    .\mantenimiento\backup.ps1
    .\mantenimiento\backup.ps1 -Destino D:\respaldos -DiasRetencion 30
#>
param(
    [string]$Servidor = 'localhost',
    [int]$Puerto = 5432,
    [string]$Base = 'ferresys',
    [string]$Usuario = 'ferresys',
    [string]$Destino = '',
    [int]$DiasRetencion = 14,
    [int]$MinimoCopias = 3,
    [string]$CarpetaPgBin = '',
    [string]$Etiqueta = ''   # la usa restaurar.ps1 (previo) para distinguir la copia previa a una restauracion
)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'comun.ps1')
if (-not $Destino) { $Destino = Join-Path $script:RaizProyecto 'backups' }
$archivo = ''

try {
    Escribir-Log "Inicio de backup de '$Base' en ${Servidor}:${Puerto} (usuario $Usuario)"
    $pgDump    = Buscar-ProgramaPostgres 'pg_dump' $CarpetaPgBin
    $pgRestore = Buscar-ProgramaPostgres 'pg_restore' $CarpetaPgBin
    if (-not (Test-Path -LiteralPath $Destino)) { New-Item -ItemType Directory -Path $Destino | Out-Null }

    $prefijo = $Base
    if ($Etiqueta) { $prefijo = $Base + '_' + $Etiqueta }
    $nuevo = Join-Path $Destino ('{0}_{1}.dump' -f $prefijo, (Get-Date -Format 'yyyyMMdd_HHmmss'))
    # Nunca sobrescribe un backup existente (dos copias en el mismo segundo).
    if (Test-Path -LiteralPath $nuevo) { throw "Ya existe $(Split-Path -Leaf $nuevo); espera un segundo y reintenta." }
    $archivo = $nuevo
    $r = Invoke-Nativo $pgDump @('-h', $Servidor, '-p', "$Puerto", '-U', $Usuario, '-d', $Base,
        '-F', 'c', '--no-owner', '--no-password', '-f', (Q $archivo))
    if ($r.Codigo -ne 0) { throw "pg_dump fallo (codigo $($r.Codigo)): $($r.Error)" }

    if (-not (Test-Path -LiteralPath $archivo) -or (Get-Item -LiteralPath $archivo).Length -eq 0) {
        throw 'El archivo de backup no se creo o esta vacio.'
    }
    $v = Invoke-Nativo $pgRestore @('--list', (Q $archivo))
    if ($v.Codigo -ne 0) { throw "El backup no paso la verificacion de pg_restore: $($v.Error)" }

    $kb = [math]::Round((Get-Item -LiteralPath $archivo).Length / 1KB, 1)
    Escribir-Log "Backup correcto: $(Split-Path -Leaf $archivo) ($kb KB, verificado con pg_restore --list)"

    $limite = (Get-Date).AddDays(-$DiasRetencion)
    $copias = @(Get-ChildItem -LiteralPath $Destino -Filter ('{0}_*.dump' -f $Base) | Sort-Object LastWriteTime -Descending)
    $viejas = @($copias | Select-Object -Skip $MinimoCopias | Where-Object { $_.LastWriteTime -lt $limite })
    foreach ($copia in $viejas) {
        Remove-Item -LiteralPath $copia.FullName -Force
        Escribir-Log "Copia antigua eliminada: $($copia.Name)"
    }
    Escribir-Log "Fin de backup. Copias guardadas: $($copias.Count - $viejas.Count)"
    exit 0
}
catch {
    if ($archivo -and (Test-Path -LiteralPath $archivo)) { Remove-Item -LiteralPath $archivo -Force -ErrorAction SilentlyContinue }
    Escribir-Log $_.Exception.Message 'ERROR'
    exit 1
}
