$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Push-Location (Join-Path $root "frontend")
try { npm ci; npm run check; npm run build } finally { Pop-Location }
Push-Location $root
try { mvn clean package } finally { Pop-Location }
Write-Host "WAR created at target/EmergencyBloodDonorFinder.war"
