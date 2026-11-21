<#
.SYNOPSIS
    Borra logs antiguos (Tomcat y aplicacion) y rota el log de mantenimiento.
.DESCRIPTION
    Por defecto limpia la carpeta logs de Tomcat definida en run.config.ps1 ($TomcatHome) y,
    si no existe, la carpeta logs del proyecto. Borra *.log y *.txt con mas de -Dias dias.
    Los archivos en uso (el log de hoy) se omiten sin error.
.EXAMPLE
    .\mantenimiento\limpiar-logs.ps1 -Dias 30
#>
param(
    [int]$Dias = 30,
    [string]$Ruta = ''
)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'comun.ps1')

try {
    if (-not $Ruta) {
        $config = Join-Path $script:RaizProyecto 'run.config.ps1'
        if (Test-Path -LiteralPath $config) {
            . $config
            if ($TomcatHome) { $Ruta = Join-Path $TomcatHome 'logs' }
        }
        if (-not $Ruta) { $Ruta = Join-Path $script:RaizProyecto 'logs' }
    }
    if (-not (Test-Path -LiteralPath $Ruta -PathType Container)) { throw "No existe la carpeta de logs: $Ruta" }

    $limite = (Get-Date).AddDays(-$Dias)
    $borrados = 0
    $liberado = 0
    $archivos = @(Get-ChildItem -LiteralPath $Ruta -File | Where-Object {
        ($_.Extension -eq '.log' -or $_.Extension -eq '.txt') -and $_.LastWriteTime -lt $limite })
    foreach ($archivo in $archivos) {
        try {
            $tam = $archivo.Length
            Remove-Item -LiteralPath $archivo.FullName -Force
            $borrados++
            $liberado += $tam
        }
        catch {
            Escribir-Log "Se omitio $($archivo.Name) (en uso): $($_.Exception.Message)" 'WARN'
        }
    }
    Escribir-Log ("Limpieza de logs en {0}: {1} archivo(s) con mas de {2} dias eliminados ({3} KB liberados)" -f $Ruta, $borrados, $Dias, [math]::Round($liberado / 1KB, 1))

    # Rotacion simple del propio log de mantenimiento (maximo 2 MB).
    if ((Test-Path -LiteralPath $script:ArchivoLog) -and (Get-Item -LiteralPath $script:ArchivoLog).Length -gt 2MB) {
        Move-Item -LiteralPath $script:ArchivoLog -Destination ($script:ArchivoLog + '.1') -Force
    }
    exit 0
}
catch {
    Escribir-Log $_.Exception.Message 'ERROR'
    exit 1
}
