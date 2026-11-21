<#
.SYNOPSIS
    Comprueba que el ultimo backup realmente se puede restaurar (en una base temporal).
.DESCRIPTION
    Crea la base ferresys_verif, restaura el backup mas reciente, cuenta filas de las tablas
    principales y borra la base temporal. No toca la base real.
    Necesita un usuario con permiso para crear bases (por defecto postgres, con su entrada en pgpass.conf).
.EXAMPLE
    .\mantenimiento\probar-restauracion.ps1
#>
param(
    [string]$Archivo = '',
    [string]$Servidor = 'localhost',
    [int]$Puerto = 5432,
    [string]$Usuario = 'postgres',
    [string]$BaseTemporal = 'ferresys_verif',
    [string]$CarpetaPgBin = ''
)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'comun.ps1')
$creada = $false
$psql = ''

function Ejecutar-Sql {
    param([string]$Base, [string]$Sentencia)
    return Invoke-Nativo $psql @('-h', $Servidor, '-p', "$Puerto", '-U', $Usuario, '-d', $Base,
        '--no-password', '-v', 'ON_ERROR_STOP=1', '-t', '-A', '-c', $Sentencia)
}

try {
    $psql      = Buscar-ProgramaPostgres 'psql' $CarpetaPgBin
    $pgRestore = Buscar-ProgramaPostgres 'pg_restore' $CarpetaPgBin
    if (-not $Archivo) {
        $ultimo = Get-ChildItem -LiteralPath (Join-Path $script:RaizProyecto 'backups') -Filter '*.dump' -ErrorAction Stop |
            Where-Object { $_.BaseName -notmatch '_previo_' } |
            Sort-Object LastWriteTime -Descending | Select-Object -First 1
        if (-not $ultimo) { throw 'No hay backups en la carpeta backups.' }
        $Archivo = $ultimo.FullName
    }
    Escribir-Log "Prueba de restauracion con $(Split-Path -Leaf $Archivo) en la base temporal '$BaseTemporal'"

    $r = Ejecutar-Sql 'postgres' "DROP DATABASE IF EXISTS $BaseTemporal"
    if ($r.Codigo -ne 0) { throw "No se pudo preparar la base temporal: $($r.Error)" }
    $r = Ejecutar-Sql 'postgres' "CREATE DATABASE $BaseTemporal"
    if ($r.Codigo -ne 0) { throw "No se pudo crear la base temporal: $($r.Error)" }
    $creada = $true

    $r = Invoke-Nativo $pgRestore @('-h', $Servidor, '-p', "$Puerto", '-U', $Usuario, '-d', $BaseTemporal,
        '--no-owner', '--no-password', '--exit-on-error', $Archivo)
    if ($r.Codigo -ne 0) { throw "La restauracion de prueba fallo: $($r.Error)" }

    $conteo = Ejecutar-Sql $BaseTemporal "SELECT 'usuarios=' || (SELECT count(*) FROM usuario) || ', productos=' || (SELECT count(*) FROM producto) || ', ventas=' || (SELECT count(*) FROM venta) || ', movimientos=' || (SELECT count(*) FROM movimiento_kardex)"
    if ($conteo.Codigo -ne 0) { throw "No se pudieron contar las filas: $($conteo.Error)" }
    $usuarios = Ejecutar-Sql $BaseTemporal 'SELECT count(*) FROM usuario'
    if ([int]$usuarios.Salida -lt 1) { throw 'La base restaurada no tiene usuarios: el backup es sospechoso.' }

    Escribir-Log "Prueba de restauracion CORRECTA ($($conteo.Salida))"
    $codigo = 0
}
catch {
    Escribir-Log $_.Exception.Message 'ERROR'
    $codigo = 1
}
finally {
    if ($creada) {
        $d = Ejecutar-Sql 'postgres' "DROP DATABASE IF EXISTS $BaseTemporal"
        if ($d.Codigo -ne 0) { Escribir-Log "No se pudo borrar la base temporal: $($d.Error)" 'WARN' }
    }
}
exit $codigo
