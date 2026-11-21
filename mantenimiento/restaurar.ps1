<#
.SYNOPSIS
    Restaura la base ferresys desde un archivo .dump generado por backup.ps1.
.DESCRIPTION
    ATENCION: reemplaza el contenido actual de la base. Por eso exige -Confirmar.
    Antes de restaurar guarda una copia del estado actual (salvo que uses -SinCopiaPrevia).
    La restauracion es una sola transaccion: si algo falla, la base queda como estaba.
    Recomendado: detener la aplicacion (stop.ps1) antes de restaurar.
    Si las tablas pertenecen a otro usuario (por ejemplo postgres), usa -Usuario postgres.
.EXAMPLE
    .\mantenimiento\restaurar.ps1 -Archivo .\backups\ferresys_20261113_020000.dump -Confirmar
#>
param(
    [Parameter(Mandatory = $true)][string]$Archivo,
    [string]$Servidor = 'localhost',
    [int]$Puerto = 5432,
    [string]$Base = 'ferresys',
    [string]$Usuario = 'ferresys',
    [string]$CarpetaPgBin = '',
    [switch]$Confirmar,
    [switch]$SinCopiaPrevia
)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'comun.ps1')

try {
    if (-not (Test-Path -LiteralPath $Archivo -PathType Leaf)) { throw "No existe el archivo: $Archivo" }
    if (-not $Confirmar) {
        throw "Esto REEMPLAZA los datos de '$Base'. Repite el comando agregando -Confirmar si estas seguro."
    }
    $Archivo = (Resolve-Path -LiteralPath $Archivo).Path
    $pgRestore = Buscar-ProgramaPostgres 'pg_restore' $CarpetaPgBin
    Escribir-Log "Inicio de restauracion de '$Base' desde $(Split-Path -Leaf $Archivo)"

    $v = Invoke-Nativo $pgRestore @('--list', (Q $Archivo))
    if ($v.Codigo -ne 0) { throw "El archivo no es un backup valido: $($v.Error)" }

    if (-not $SinCopiaPrevia) {
        Escribir-Log 'Guardando copia previa del estado actual...'
        & (Join-Path $PSScriptRoot 'backup.ps1') -Servidor $Servidor -Puerto $Puerto -Base $Base -Usuario $Usuario -CarpetaPgBin $CarpetaPgBin -Etiqueta 'previo'
        if ($LASTEXITCODE -ne 0) { throw 'No se pudo guardar la copia previa; se cancela la restauracion.' }
    }

    $r = Invoke-Nativo $pgRestore @('-h', $Servidor, '-p', "$Puerto", '-U', $Usuario, '-d', $Base,
        '--clean', '--if-exists', '--no-owner', '--single-transaction', '--no-password', (Q $Archivo))
    if ($r.Codigo -ne 0) { throw "pg_restore fallo (codigo $($r.Codigo)); la base no fue modificada: $($r.Error)" }

    Escribir-Log "Restauracion completada en '$Base'."
    exit 0
}
catch {
    Escribir-Log $_.Exception.Message 'ERROR'
    exit 1
}
