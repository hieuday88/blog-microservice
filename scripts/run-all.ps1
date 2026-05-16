param(
    [switch]$SkipBuild,
    [switch]$SkipDocker,
    [switch]$NoBrowser,
    [int]$FrontendPort = 3002,
    [string]$MysqlUsername,
    [string]$MysqlPassword
)

$ErrorActionPreference = "Stop"

# ===============================================
# LOCAL CONFIG
# Sua 2 dong ben duoi neu MySQL cua ban dung
# username/password khac. Sau do chi can chay script.
# ===============================================
$DefaultMysqlUsername = "root"
$DefaultMysqlPassword = "123456"

if ([string]::IsNullOrWhiteSpace($MysqlUsername)) {
    $MysqlUsername = $DefaultMysqlUsername
}
if ([string]::IsNullOrWhiteSpace($MysqlPassword)) {
    $MysqlPassword = $DefaultMysqlPassword
}

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$Root = Split-Path -Parent $ScriptDir

function Write-Step {
    param([string]$Message)
    Write-Host "`n$Message" -ForegroundColor Yellow
}

function Write-Ok {
    param([string]$Message)
    Write-Host $Message -ForegroundColor Green
}

function Write-Warn {
    param([string]$Message)
    Write-Host $Message -ForegroundColor DarkYellow
}

function Assert-Command {
    param(
        [string]$Name,
        [string]$InstallHint
    )

    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        Write-Host "Missing command: $Name" -ForegroundColor Red
        Write-Host $InstallHint -ForegroundColor Red
        exit 1
    }
}

function Test-Port {
    param([int]$Port)
    try {
        $client = New-Object System.Net.Sockets.TcpClient
        $async = $client.BeginConnect("127.0.0.1", $Port, $null, $null)
        $connected = $async.AsyncWaitHandle.WaitOne(300, $false)
        if ($connected) {
            $client.EndConnect($async)
        }
        $client.Close()
        return $connected
    } catch {
        return $false
    }
}

function Wait-Port {
    param(
        [string]$Name,
        [int]$Port,
        [int]$TimeoutSeconds = 90
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if (Test-Port -Port $Port) {
            Write-Ok "$Name is ready on port $Port."
            return $true
        }
        Start-Sleep -Seconds 2
    }

    Write-Warn "$Name did not become ready on port $Port within $TimeoutSeconds seconds."
    return $false
}

function Escape-ForSingleQuotedPowerShellString {
    param([string]$Value)
    return $Value.Replace("'", "''")
}

function Start-ServiceWindow {
    param(
        [string]$Name,
        [string]$WorkingDirectory,
        [string]$Command,
        [int]$Port
    )

    if (Test-Port -Port $Port) {
        Write-Warn "$Name port $Port is already in use. Skipping start."
        return
    }

    $safeName = Escape-ForSingleQuotedPowerShellString $Name
    $safePath = Escape-ForSingleQuotedPowerShellString $WorkingDirectory
    $windowCommand = @"
Set-Location -LiteralPath '$safePath'
`$Host.UI.RawUI.WindowTitle = '$safeName'
$Command
"@

    Write-Ok "Starting $Name on port $Port..."
    Start-Process powershell.exe -ArgumentList @(
        "-NoExit",
        "-ExecutionPolicy",
        "Bypass",
        "-Command",
        $windowCommand
    )
}

Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "   BLOG MICROSERVICES - ONE CLICK RUNNER" -ForegroundColor Cyan
Write-Host "===============================================" -ForegroundColor Cyan

Set-Location $Root

$env:MYSQL_USERNAME = $MysqlUsername
$env:MYSQL_PASSWORD = $MysqlPassword
$env:MYSQL_ROOT_PASSWORD = $MysqlPassword

Assert-Command -Name "java" -InstallHint "Install JDK 17+ and add java to PATH."
Assert-Command -Name "mvn" -InstallHint "Install Maven and add mvn to PATH."
Assert-Command -Name "python" -InstallHint "Install Python 3 and add python to PATH."

if (-not $SkipDocker) {
    if (Get-Command docker -ErrorAction SilentlyContinue) {
        Write-Step "[1/5] Starting MySQL and RabbitMQ with Docker Compose..."
        docker compose up -d mysql-db rabbitmq
        if ($LASTEXITCODE -ne 0) {
            Write-Host "Docker Compose failed. Start MySQL on 3306 and RabbitMQ on 5672 manually, or run with -SkipDocker." -ForegroundColor Red
            exit 1
        }
        Wait-Port -Name "MySQL" -Port 3306 -TimeoutSeconds 120 | Out-Null
        Wait-Port -Name "RabbitMQ" -Port 5672 -TimeoutSeconds 90 | Out-Null
    } else {
        Write-Warn "Docker was not found. Continuing without starting MySQL/RabbitMQ."
        Write-Warn "Make sure MySQL is on port 3306 and RabbitMQ is on port 5672, or install Docker Desktop."
    }
} else {
    Write-Warn "Skipping Docker startup."
}

if (-not $SkipBuild) {
    Write-Step "[2/5] Building all Java services with Maven..."
    mvn clean package -DskipTests
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Build failed. Please check the Maven logs above." -ForegroundColor Red
        exit 1
    }
} else {
    Write-Warn "Skipping Maven build."
}

Write-Step "[3/5] Checking static frontend..."
$frontendIndex = Join-Path $Root "frontend\index.html"
if (-not (Test-Path $frontendIndex)) {
    Write-Host "Missing frontend entry file: $frontendIndex" -ForegroundColor Red
    exit 1
}
Write-Ok "Static frontend is ready."

$requestedFrontendPort = $FrontendPort
while (Test-Port -Port $FrontendPort) {
    $FrontendPort++
}
if ($FrontendPort -ne $requestedFrontendPort) {
    Write-Warn "Frontend port $requestedFrontendPort is busy. Using $FrontendPort instead."
}

$requiredPorts = @(8080, 8081, 8082, 8083, $FrontendPort) | Select-Object -Unique
$busyPorts = @($requiredPorts | Where-Object { Test-Port -Port $_ })
if ($busyPorts.Count -gt 0) {
    Write-Warn "Some ports are already in use: $($busyPorts -join ', ')"
    Write-Warn "The runner will skip services whose ports are already active."
}

Write-Step "[4/5] Starting services in order..."

$javaServices = @(
    @{ Name = "Auth Service"; Path = "backend\auth-service"; Port = 8081 },
    @{ Name = "Post Service"; Path = "backend\post-service"; Port = 8082 },
    @{ Name = "Comment Service"; Path = "backend\comment-service"; Port = 8083 },
    @{ Name = "API Gateway"; Path = "backend\api-gateway"; Port = 8080 }
)

foreach ($svc in $javaServices) {
    $servicePath = Join-Path $Root $svc.Path
    $jarPath = Join-Path $servicePath "target\quarkus-app\quarkus-run.jar"
    if (-not (Test-Path $jarPath)) {
        Write-Host "Missing jar for $($svc.Name): $jarPath" -ForegroundColor Red
        Write-Host "Run without -SkipBuild or build the project first." -ForegroundColor Red
        exit 1
    }

    $serviceCommand = "& java -jar 'target\quarkus-app\quarkus-run.jar'"
    if ($svc.Name -eq "Auth Service") {
        $serviceCommand = "`$env:AUTH_DATASOURCE_USERNAME='$MysqlUsername'; `$env:AUTH_DATASOURCE_PASSWORD='$MysqlPassword'; & java -jar 'target\quarkus-app\quarkus-run.jar'"
    }
    if ($svc.Name -eq "Post Service") {
        $serviceCommand = "`$env:POST_DATASOURCE_USERNAME='$MysqlUsername'; `$env:POST_DATASOURCE_PASSWORD='$MysqlPassword'; & java -jar 'target\quarkus-app\quarkus-run.jar'"
    }
    if ($svc.Name -eq "Comment Service") {
        $serviceCommand = "`$env:COMMENT_DATASOURCE_USERNAME='$MysqlUsername'; `$env:COMMENT_DATASOURCE_PASSWORD='$MysqlPassword'; & java -jar 'target\quarkus-app\quarkus-run.jar'"
    }
    if ($svc.Name -eq "API Gateway") {
        $serviceCommand = "`$env:QUARKUS_HTTP_CORS_ORIGINS='http://localhost:3000,http://localhost:3002,http://localhost:$FrontendPort,http://127.0.0.1:3000,http://127.0.0.1:3002,http://127.0.0.1:$FrontendPort'; & java -jar 'target\quarkus-app\quarkus-run.jar'"
    }

    Start-ServiceWindow `
        -Name $svc.Name `
        -WorkingDirectory $servicePath `
        -Command $serviceCommand `
        -Port $svc.Port

    Wait-Port -Name $svc.Name -Port $svc.Port -TimeoutSeconds 90 | Out-Null
}

$frontendCommand = "& python -m http.server $FrontendPort"
Start-ServiceWindow `
    -Name "Blog Frontend" `
    -WorkingDirectory (Join-Path $Root "frontend") `
    -Command $frontendCommand `
    -Port $FrontendPort
Wait-Port -Name "Blog Frontend" -Port $FrontendPort -TimeoutSeconds 90 | Out-Null

$frontendUrl = "http://localhost:$FrontendPort"

Write-Step "[5/5] Ready"
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "   API Gateway : http://localhost:8080" -ForegroundColor Cyan
Write-Host "   Auth Service: http://localhost:8081" -ForegroundColor Cyan
Write-Host "   Post Service: http://localhost:8082" -ForegroundColor Cyan
Write-Host "   Comment API : http://localhost:8083" -ForegroundColor Cyan
Write-Host "   Frontend    : $frontendUrl" -ForegroundColor Cyan
Write-Host "   Admin login : admin / admin" -ForegroundColor Cyan
Write-Host "===============================================" -ForegroundColor Cyan

if (-not $NoBrowser) {
    Start-Process $frontendUrl
}

Write-Host "`nTips:" -ForegroundColor Cyan
Write-Host "  .\scripts\run-all.ps1 -SkipBuild       # start from existing jars" -ForegroundColor Cyan
Write-Host "  .\scripts\run-all.ps1 -SkipDocker      # use manually started MySQL/RabbitMQ" -ForegroundColor Cyan
Write-Host "  .\scripts\run-all.ps1 -FrontendPort 3002 -NoBrowser" -ForegroundColor Cyan
