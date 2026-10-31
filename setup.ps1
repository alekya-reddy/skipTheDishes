param(
    [switch]$SkipDocker,
    [switch]$SkipChrome
)

$ErrorActionPreference = "Stop"

function Test-CommandAvailable {
    param([string]$Name)
    return $null -ne (Get-Command $Name -ErrorAction SilentlyContinue)
}

function Install-WingetPackage {
    param(
        [Parameter(Mandatory)]
        [string]$Id,
        [Parameter(Mandatory)]
        [string]$DisplayName
    )

    Write-Host "Installing $DisplayName..." -ForegroundColor Cyan
    winget install --id $Id --exact --silent `
        --accept-package-agreements --accept-source-agreements
    # -1978335189: already installed and no newer version available
    if ($LASTEXITCODE -ne 0 -and $LASTEXITCODE -ne -1978335189) {
        throw "Failed to install $DisplayName with winget (exit code $LASTEXITCODE)."
    }
}

function Refresh-ProcessPath {
    $machinePath = [Environment]::GetEnvironmentVariable("Path", "Machine")
    $userPath = [Environment]::GetEnvironmentVariable("Path", "User")
    $env:Path = "$machinePath;$userPath"
}

function Get-JavaMajorVersion {
    param([string]$JavaPath)

    $startInfo = New-Object System.Diagnostics.ProcessStartInfo
    $startInfo.FileName = $JavaPath
    $startInfo.Arguments = "-version"
    $startInfo.UseShellExecute = $false
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    $startInfo.CreateNoWindow = $true

    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $startInfo
    [void]$process.Start()
    $output = $process.StandardOutput.ReadToEnd()
    $errorOutput = $process.StandardError.ReadToEnd()
    $process.WaitForExit()

    $versionOutput = "$output`n$errorOutput"
    $match = [regex]::Match($versionOutput, 'version "?(\d+)')
    if ($match.Success) {
        return [int]$match.Groups[1].Value
    }
    return $null
}

Write-Host "Courier Shifts Automation setup" -ForegroundColor Green

if (-not (Test-CommandAvailable "winget")) {
    throw "winget is required. Install 'App Installer' from the Microsoft Store, then rerun this script."
}

if (-not (Test-CommandAvailable "java")) {
    Install-WingetPackage -Id "Microsoft.OpenJDK.25" -DisplayName "Microsoft OpenJDK 25"
    Refresh-ProcessPath
} else {
    $javaMajor = Get-JavaMajorVersion -JavaPath (Get-Command java).Source
    if ($null -ne $javaMajor -and $javaMajor -lt 25) {
        Install-WingetPackage -Id "Microsoft.OpenJDK.25" -DisplayName "Microsoft OpenJDK 25"
        Refresh-ProcessPath
    }
}

if (-not (Test-CommandAvailable "mvn")) {
    # Maven is not published in winget, so download the official binary zip.
    $mavenVersion = "3.9.9"
    $mavenRoot = Join-Path $env:LOCALAPPDATA "Apache"
    $mavenHome = Join-Path $mavenRoot "apache-maven-$mavenVersion"
    if (-not (Test-Path (Join-Path $mavenHome "bin\mvn.cmd"))) {
        Write-Host "Installing Apache Maven $mavenVersion..." -ForegroundColor Cyan
        New-Item -ItemType Directory -Force -Path $mavenRoot | Out-Null
        $zipPath = Join-Path $env:TEMP "apache-maven-$mavenVersion-bin.zip"
        $url = "https://archive.apache.org/dist/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip"
        Invoke-WebRequest -Uri $url -OutFile $zipPath -UseBasicParsing
        Expand-Archive -Path $zipPath -DestinationPath $mavenRoot -Force
        Remove-Item $zipPath -Force
    }
    $mavenBin = Join-Path $mavenHome "bin"
    $userPath = [Environment]::GetEnvironmentVariable("Path", "User")
    if (($userPath -split ";") -notcontains $mavenBin) {
        [Environment]::SetEnvironmentVariable("Path", "$userPath;$mavenBin".TrimStart(";"), "User")
    }
    Refresh-ProcessPath
}

if (-not $SkipChrome -and -not (Test-CommandAvailable "chrome") -and
    -not (Test-Path "${env:ProgramFiles}\Google\Chrome\Application\chrome.exe") -and
    -not (Test-Path "${env:ProgramFiles(x86)}\Google\Chrome\Application\chrome.exe")) {
    Install-WingetPackage -Id "Google.Chrome" -DisplayName "Google Chrome"
    Refresh-ProcessPath
}

if (-not $SkipDocker -and -not (Test-CommandAvailable "docker")) {
    Install-WingetPackage -Id "Docker.DockerDesktop" -DisplayName "Docker Desktop"
}

$javaCommand = Get-Command java -ErrorAction SilentlyContinue
$mavenCommand = Get-Command mvn -ErrorAction SilentlyContinue

if (-not $javaCommand) {
    throw "Java is installed but was not found in PATH. Start a new PowerShell session and rerun setup.ps1."
}
if (-not $mavenCommand) {
    throw "Maven is installed but was not found in PATH. Start a new PowerShell session and rerun setup.ps1."
}

$javaHome = Split-Path (Split-Path $javaCommand.Source -Parent) -Parent
if ($javaCommand.Source -like "*\bin\java.exe") {
    $javaHome = Split-Path $javaCommand.Source -Parent
    $javaHome = Split-Path $javaHome -Parent
}
[Environment]::SetEnvironmentVariable("JAVA_HOME", $javaHome, "User")
$env:JAVA_HOME = $javaHome

Write-Host "`nVerifying Java and Maven..." -ForegroundColor Cyan
java -version
mvn -version

Write-Host "`nDownloading Maven dependencies..." -ForegroundColor Cyan
mvn dependency:go-offline

Write-Host "`nSetup complete." -ForegroundColor Green
Write-Host "Start the application separately with Docker Compose, then run: mvn clean verify"
