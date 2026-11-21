<#
.SYNOPSIS
    Registra (o elimina) las tareas programadas de mantenimiento en el Programador de tareas de Windows.
.DESCRIPTION
    Crea, para el usuario actual:
      Ferresys-Backup          todos los dias a -HoraBackup   (backup.ps1)
      Ferresys-LimpiarLogs     cada domingo a -HoraLimpieza   (limpiar-logs.ps1)
      Ferresys-ProbarRestauracion (opcional, con -IncluirPruebaRestauracion) cada domingo a -HoraPrueba
    Si el equipo estaba apagado a la hora programada, la tarea se ejecuta al encenderlo (StartWhenAvailable).
    Debe ejecutarse con el MISMO usuario que configuro pgpass.conf.
.EXAMPLE
    .\mantenimiento\instalar-tareas.ps1
    .\mantenimiento\instalar-tareas.ps1 -HoraBackup 23:30
    .\mantenimiento\instalar-tareas.ps1 -Desinstalar
#>
param(
    [string]$HoraBackup = '02:00',
    [string]$HoraLimpieza = '03:00',
    [string]$HoraPrueba = '04:00',
    [switch]$IncluirPruebaRestauracion,
    [switch]$Desinstalar
)
$ErrorActionPreference = 'Stop'
$nombres = @('Ferresys-Backup', 'Ferresys-LimpiarLogs', 'Ferresys-ProbarRestauracion')

if ($Desinstalar) {
    foreach ($nombre in $nombres) {
        if (Get-ScheduledTask -TaskName $nombre -ErrorAction SilentlyContinue) {
            Unregister-ScheduledTask -TaskName $nombre -Confirm:$false
            Write-Host "Tarea eliminada: $nombre"
        }
    }
    return
}

function Nueva-Accion {
    param([string]$Script)
    $ruta = Join-Path $PSScriptRoot $Script
    $argumentos = '-NoProfile -ExecutionPolicy Bypass -WindowStyle Hidden -File "{0}"' -f $ruta
    return New-ScheduledTaskAction -Execute 'powershell.exe' -Argument $argumentos -WorkingDirectory (Split-Path -Parent $PSScriptRoot)
}

$ajustes = New-ScheduledTaskSettingsSet -StartWhenAvailable -AllowStartIfOnBatteries -DontStopIfGoingOnBatteries `
    -ExecutionTimeLimit (New-TimeSpan -Hours 1) -RestartCount 2 -RestartInterval (New-TimeSpan -Minutes 10)

Register-ScheduledTask -TaskName 'Ferresys-Backup' -Action (Nueva-Accion 'backup.ps1') `
    -Trigger (New-ScheduledTaskTrigger -Daily -At $HoraBackup) -Settings $ajustes `
    -Description 'Ferresys: backup diario de la base de datos (pg_dump)' -Force | Out-Null
Write-Host "Tarea creada: Ferresys-Backup (diaria a las $HoraBackup)"

Register-ScheduledTask -TaskName 'Ferresys-LimpiarLogs' -Action (Nueva-Accion 'limpiar-logs.ps1') `
    -Trigger (New-ScheduledTaskTrigger -Weekly -DaysOfWeek Sunday -At $HoraLimpieza) -Settings $ajustes `
    -Description 'Ferresys: limpieza semanal de logs antiguos' -Force | Out-Null
Write-Host "Tarea creada: Ferresys-LimpiarLogs (domingos a las $HoraLimpieza)"

if ($IncluirPruebaRestauracion) {
    Register-ScheduledTask -TaskName 'Ferresys-ProbarRestauracion' -Action (Nueva-Accion 'probar-restauracion.ps1') `
        -Trigger (New-ScheduledTaskTrigger -Weekly -DaysOfWeek Sunday -At $HoraPrueba) -Settings $ajustes `
        -Description 'Ferresys: prueba semanal de restauracion del ultimo backup' -Force | Out-Null
    Write-Host "Tarea creada: Ferresys-ProbarRestauracion (domingos a las $HoraPrueba)"
}
Write-Host ''
Write-Host 'Para verificar: Get-ScheduledTask -TaskName "Ferresys-*" | Get-ScheduledTaskInfo'
Write-Host 'Para probar ya: Start-ScheduledTask -TaskName "Ferresys-Backup"'
