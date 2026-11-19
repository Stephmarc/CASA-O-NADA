Set-Location (Split-Path $PSScriptRoot -Parent)
docker compose -f compose.json --profile s07 down
