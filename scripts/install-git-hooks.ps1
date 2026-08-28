# Installs versioned git hooks from scripts/hooks/ into .git/hooks/
# Run from repo root: .\scripts\install-git-hooks.ps1

$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$sourceDir = Join-Path $repoRoot "scripts\hooks"
$targetDir = Join-Path $repoRoot ".git\hooks"

if (-not (Test-Path $targetDir)) {
    Write-Error "Not a git repository (.git/hooks not found)."
}

$hooks = Get-ChildItem -Path $sourceDir -File
if ($hooks.Count -eq 0) {
    Write-Error "No hooks found in scripts/hooks/"
}

foreach ($hook in $hooks) {
    $dest = Join-Path $targetDir $hook.Name
    Copy-Item -Path $hook.FullName -Destination $dest -Force
    Write-Host "Installed: $($hook.Name)"
}

Write-Host ""
Write-Host "Git hooks installed. pre-push will run './gradlew test' before each push."
Write-Host "Skip once (not recommended): git push --no-verify"
