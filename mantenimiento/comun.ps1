# Funciones compartidas por los scripts de mantenimiento (se cargan con ". .\comun.ps1").
# Este archivo es solo ASCII a proposito: PowerShell 5.1 lee mal los acentos en archivos sin BOM.
$script:RaizProyecto = Split-Path -Parent $PSScriptRoot
$script:DirLogs      = Join-Path $PSScriptRoot 'logs'
$script:ArchivoLog   = Join-Path $script:DirLogs 'mantenimiento.log'

function Escribir-Log {
    param([string]$Mensaje, [string]$Nivel = 'INFO')
    if (-not (Test-Path -LiteralPath $script:DirLogs)) {
        New-Item -ItemType Directory -Path $script:DirLogs | Out-Null
    }
    $linea = '{0} [{1}] {2}' -f (Get-Date -Format 'yyyy-MM-dd HH:mm:ss'), $Nivel, $Mensaje
    Add-Content -LiteralPath $script:ArchivoLog -Value $linea
    Write-Host $linea
}

# Comillas para argumentos con espacios (rutas de Windows).
function Q {
    param([string]$Texto)
    return '"' + $Texto + '"'
}

# Busca pg_dump / pg_restore / psql: carpeta indicada, PATH o la version mas reciente en Program Files.
function Buscar-ProgramaPostgres {
    param([string]$Nombre, [string]$CarpetaBin = '')
    if ($CarpetaBin) {
        $ruta = Join-Path $CarpetaBin ($Nombre + '.exe')
        if (Test-Path -LiteralPath $ruta) { return $ruta }
        throw "No se encontro $Nombre.exe en $CarpetaBin"
    }
    $comando = Get-Command $Nombre -ErrorAction SilentlyContinue
    if ($comando) { return $comando.Source }
    $candidatos = @(
        Get-ChildItem -Path ('C:\Program Files\PostgreSQL\*\bin\' + $Nombre + '.exe') -ErrorAction SilentlyContinue
        Get-ChildItem -Path ('D:\Program Files\PostgreSQL\*\bin\' + $Nombre + '.exe') -ErrorAction SilentlyContinue
    )
    $candidato = $candidatos |
        Sort-Object { [int]($_.Directory.Parent.Name -replace '\D', '0') } -Descending |
        Select-Object -First 1
    if ($candidato) { return $candidato.FullName }
    throw "No se encontro $Nombre.exe. Usa -CarpetaPgBin '<directorio de PostgreSQL>\bin'."
}

# Ejecuta un programa externo sin que su salida de error rompa el script. Devuelve Codigo, Salida y Error.
function Invoke-Nativo {
    param([string]$Programa, [string[]]$Argumentos)
    $archivoErr = [System.IO.Path]::GetTempFileName()
    $archivoOut = [System.IO.Path]::GetTempFileName()
    try {
        $preferenciaAnterior = $ErrorActionPreference
        try {
            $ErrorActionPreference = 'Continue'
            & $Programa @Argumentos 2> $archivoErr 1> $archivoOut
            $codigo = $LASTEXITCODE
        }
        finally {
            $ErrorActionPreference = $preferenciaAnterior
        }
        $salida = Get-Content -LiteralPath $archivoOut -Raw
        $errorTexto = Get-Content -LiteralPath $archivoErr -Raw
        if ($null -eq $salida) { $salida = '' }
        if ($null -eq $errorTexto) { $errorTexto = '' }
        return [pscustomobject]@{
            Codigo = $codigo
            Salida = ([string]$salida).Trim()
            Error  = ([string]$errorTexto).Trim()
        }
    }
    finally {
        Remove-Item -LiteralPath $archivoErr, $archivoOut -ErrorAction SilentlyContinue
    }
}
