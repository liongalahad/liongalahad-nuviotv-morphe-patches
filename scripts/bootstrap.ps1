[CmdletBinding()]
param([switch]$InstallMissing)

. "$PSScriptRoot\Common.ps1"
$config = Initialize-NuvioEnvironment
$repo = Get-RepoRoot

if (-not (Get-Command node -ErrorAction SilentlyContinue)) {
    $nodePath = 'C:\Program Files\nodejs'
    if (Test-Path (Join-Path $nodePath 'node.exe')) { $env:PATH = "$nodePath;$env:PATH" }
    elseif ($InstallMissing) { winget install OpenJS.NodeJS.LTS --silent --accept-source-agreements --accept-package-agreements }
    else { throw 'Node.js LTS is missing. Re-run with -InstallMissing.' }
}

$environmentKey = 'HKCU:\Environment'
New-ItemProperty -Path $environmentKey -Name 'JAVA_HOME' -Value $config.android.studioJdk -PropertyType String -Force | Out-Null
New-ItemProperty -Path $environmentKey -Name 'ANDROID_HOME' -Value $config.android.sdk -PropertyType String -Force | Out-Null
New-ItemProperty -Path $environmentKey -Name 'ANDROID_SDK_ROOT' -Value $config.android.sdk -PropertyType String -Force | Out-Null
$userPath = [Environment]::GetEnvironmentVariable('Path', 'User')
$requiredPaths = @(
    (Join-Path $config.android.sdk 'platform-tools'),
    (Join-Path $config.android.sdk 'emulator')
)
foreach ($path in $requiredPaths) {
    if (($userPath -split ';') -notcontains $path) { $userPath = ($userPath.TrimEnd(';') + ';' + $path).Trim(';') }
}
New-ItemProperty -Path $environmentKey -Name 'Path' -Value $userPath -PropertyType ExpandString -Force | Out-Null

$tools = Join-Path $repo 'tools'
New-Item -ItemType Directory -Force $tools | Out-Null
$morpheJar = Join-Path $tools $config.morpheDesktop.file
if (-not (Test-Path $morpheJar)) { Invoke-WebRequest $config.morpheDesktop.url -OutFile $morpheJar }

$keyDir = Join-Path $repo 'local\keystore'
$keyPath = Join-Path $keyDir 'nuviotv-test.jks'
$credentialPath = Join-Path $keyDir 'credentials.json'
New-Item -ItemType Directory -Force $keyDir | Out-Null
if (-not (Test-Path $keyPath)) {
    $passwordBytes = New-Object byte[] 18
    $random = [Security.Cryptography.RandomNumberGenerator]::Create()
    try { $random.GetBytes($passwordBytes) } finally { $random.Dispose() }
    $password = [Convert]::ToBase64String($passwordBytes).Replace('+', 'A').Replace('/', 'B').TrimEnd('=')
    & keytool -genkeypair -storetype JKS -keystore $keyPath -storepass $password -keypass $password `
        -alias morphe -keyalg RSA -keysize 3072 -validity 7300 -dname 'CN=NuvioTV Morphe Local Test,OU=Local Testing,O=liongalahad,C=AU' | Out-Null
    @{ storePassword = $password; entryPassword = $password; alias = 'morphe' } |
        ConvertTo-Json | Set-Content -Encoding UTF8 $credentialPath
}

$avds = & emulator -list-avds
foreach ($name in @($config.android.phoneAvd, $config.android.tvAvd)) {
    if ($avds -notcontains $name) { throw "Required AVD '$name' is missing." }
}

$acceleration = (& emulator -accel-check 2>&1) -join [Environment]::NewLine
if ($acceleration -notmatch 'usable|installed') { throw "Emulator acceleration is unavailable: $acceleration" }

$auth = (& gh auth status --active 2>&1) -join [Environment]::NewLine
if ($auth -notmatch 'liongalahad') { throw 'GitHub CLI is not authenticated as liongalahad.' }
if ($auth -notmatch 'read:packages') {
    Write-Warning 'The active GitHub token lacks read:packages. Morphe Gradle dependencies cannot resolve until that scope is added.'
}

[pscustomobject]@{
    Java = (& java --version | Select-Object -First 1)
    Node = (& node --version)
    AndroidSdk = $env:ANDROID_HOME
    AVDs = $avds -join ', '
    MorpheDesktop = $config.morpheDesktop.version
    Keystore = $keyPath
} | Format-List
