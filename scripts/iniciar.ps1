param([ValidateSet('dev','prod')][string]$Perfil = 'prod')
$env:CASA_PROFILE = $Perfil
$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) { throw 'Instala JDK 21 y Maven 3.9 o usa los Maven Wrapper incluidos por módulo.' }
& mvn -B clean package
if ($LASTEXITCODE -ne 0) { throw 'La compilación o las pruebas fallaron.' }
& docker compose -f compose.json up -d --build
if ($LASTEXITCODE -ne 0) { throw 'Revisa docker compose -f compose.json logs' }
Write-Host 'Frontend: http://localhost:4200 · Eureka: http://localhost:18761 · Grafana: http://localhost:13000'
Write-Host 'Espera a que los servicios se registren y ejecuta python scripts/verificar.py'
