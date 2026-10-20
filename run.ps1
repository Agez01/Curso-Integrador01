# Las rutas de cada computadora viven en run.config.ps1 (no se sube a Git).
# Si no existe, copia run.config.example.ps1 como run.config.ps1 y edítalo.
$ProjectRoot = $PSScriptRoot
$configLocal = Join-Path $ProjectRoot 'run.config.ps1'
if (-not (Test-Path -LiteralPath $configLocal)) {
    throw "Falta run.config.ps1. Copia run.config.example.ps1 como run.config.ps1 y ajusta tus rutas."
}
. $configLocal

$ErrorActionPreference = 'Stop'

if (-not (Test-Path -LiteralPath $JavaHome)) {
    throw "No se encontró JAVA_HOME: $JavaHome"
}
if (-not (Test-Path -LiteralPath $MavenCommand -PathType Leaf)) {
    throw "No se encontró Maven: $MavenCommand"
}
if (-not (Test-Path -LiteralPath (Join-Path $TomcatHome 'bin\catalina.bat') -PathType Leaf)) {
    throw "No se encontró Tomcat en: $TomcatHome"
}

$env:JAVA_HOME = $JavaHome
$env:Path = "$JavaHome\bin;$env:Path"
$env:CATALINA_HOME = $TomcatHome
$env:CATALINA_BASE = $TomcatHome

# Comprueba que Tomcat conserve el puerto HTTP configurado para esta aplicación.
$serverXml = Join-Path $TomcatHome 'conf\server.xml'
$serverConfiguration = Get-Content -LiteralPath $serverXml -Raw
$connectorPattern = '<Connector\b(?=[^>]*\bport="' + $TomcatHttpPort + '")'
if ($serverConfiguration -notmatch $connectorPattern) {
    throw "Configura el puerto HTTP $TomcatHttpPort en $serverXml antes de iniciar Tomcat."
}

# Detiene la instancia anterior antes de reemplazar el WAR.
& (Join-Path $ProjectRoot 'stop.ps1') -TomcatHome $TomcatHome -TomcatHttpPort $TomcatHttpPort

# Construye el WAR y detiene el proceso ante cualquier error de Maven.
Push-Location $ProjectRoot
try {
    & $MavenCommand -f (Join-Path $ProjectRoot 'pom.xml') clean package
    if ($LASTEXITCODE -ne 0) {
        throw "Maven terminó con código $LASTEXITCODE."
    }
}
finally {
    Pop-Location
}

$warSource = Join-Path $ProjectRoot 'target\ferresys.war'
$warDestination = Join-Path $TomcatHome 'webapps\ferresys.war'
if (-not (Test-Path -LiteralPath $warSource -PathType Leaf)) {
    throw "Maven no generó el WAR esperado: $warSource"
}
Copy-Item -LiteralPath $warSource -Destination $warDestination -Force

$portListener = Get-NetTCPConnection -State Listen -LocalPort $TomcatHttpPort -ErrorAction SilentlyContinue
if ($portListener) {
    throw "El puerto $TomcatHttpPort sigue ocupado. No se iniciará otra instancia de Tomcat."
}

Write-Host "Aplicación desplegada. Abre http://localhost:$TomcatHttpPort/ferresys/dashboard.jsp"
Write-Host 'Tomcat se ejecuta en primer plano. Presiona Ctrl+C aquí para detenerlo.'
Push-Location $TomcatHome
try {
    & (Join-Path $TomcatHome 'bin\catalina.bat') run
    if ($LASTEXITCODE -ne 0) {
        throw "Tomcat terminó con código $LASTEXITCODE."
    }
}
finally {
    Pop-Location
}
