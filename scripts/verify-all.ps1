[CmdletBinding()]
param()

. "$PSScriptRoot\Common.ps1"
Initialize-NuvioEnvironment | Out-Null
$repo = Get-RepoRoot
$manifests = Get-ChildItem (Join-Path $repo 'testing\patches') -Recurse -Filter patch.json |
    ForEach-Object { Get-Content $_.FullName -Raw | ConvertFrom-Json }
if (-not $manifests) { throw 'No patch compartments were found.' }

& "$PSScriptRoot\build.ps1" -Patch $manifests[0].id
if ($LASTEXITCODE -ne 0) { throw 'Combined bundle build failed.' }

$results = foreach ($manifest in $manifests) {
    $run = New-PatchRunDirectory $manifest.id
    & "$PSScriptRoot\patch.ps1" -Patch $manifest.id -Abi x86_64 -RunDirectory $run -NoBuild
    if ($LASTEXITCODE -ne 0) { throw "Isolated application failed for $($manifest.id)." }
    [pscustomobject]@{ Patch = $manifest.id; Run = $run; Result = 'PASS' }
}
$results | Format-Table -AutoSize
