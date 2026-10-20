param(
    [string]$TomcatHome = 'C:\Users\Diego\AppData\Local\Programs\apache-tomcat-10.1.61',
    [int]$TomcatHttpPort = 8090,
    [int]$TimeoutSeconds = 20
)

$ErrorActionPreference = 'Stop'

if (-not (Test-Path -LiteralPath (Join-Path $TomcatHome 'bin\shutdown.bat') -PathType Leaf)) {
    throw "No se encontró Tomcat en: $TomcatHome"
}

function Get-TomcatProcesses {
    @(Get-CimInstance Win32_Process -Filter "Name='java.exe' OR Name='javaw.exe'" |
        Where-Object {
            $_.CommandLine -and
            $_.CommandLine.IndexOf($TomcatHome, [StringComparison]::OrdinalIgnoreCase) -ge 0
        })
}

$tomcatProcesses = Get-TomcatProcesses
$portListener = Get-NetTCPConnection -State Listen -LocalPort $TomcatHttpPort -ErrorAction SilentlyContinue

if (-not $tomcatProcesses) {
    if ($portListener) {
        throw "El puerto $TomcatHttpPort está ocupado por otro proceso; no se detuvo."
    }
    Write-Host 'Tomcat ya está detenido.'
    return
}

$env:CATALINA_HOME = $TomcatHome
$env:CATALINA_BASE = $TomcatHome
& (Join-Path $TomcatHome 'bin\shutdown.bat')

$deadline = (Get-Date).AddSeconds($TimeoutSeconds)
do {
    Start-Sleep -Seconds 1
    $tomcatProcesses = Get-TomcatProcesses
    $portListener = Get-NetTCPConnection -State Listen -LocalPort $TomcatHttpPort -ErrorAction SilentlyContinue

    if (-not $tomcatProcesses -and -not $portListener) {
        Write-Host 'Tomcat se detuvo correctamente.'
        return
    }
} while ((Get-Date) -lt $deadline)

throw "Tomcat no se detuvo dentro de $TimeoutSeconds segundos. No se terminaron procesos a la fuerza."
