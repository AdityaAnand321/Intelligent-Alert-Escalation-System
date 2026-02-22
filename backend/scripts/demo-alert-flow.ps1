param(
    [string]$BaseUrl = "http://localhost:8080/api"
)

$ErrorActionPreference = "Stop"

function Invoke-JsonApi {
    param(
        [Parameter(Mandatory = $true)][string]$Method,
        [Parameter(Mandatory = $true)][string]$Url,
        [object]$Body,
        [string]$Token
    )

    $headers = @{}
    if ($Token -and $Token.Trim().Length -gt 0) {
        $headers["Authorization"] = "Bearer $Token"
    }

    if ($null -ne $Body) {
        $json = $Body | ConvertTo-Json -Depth 10
        return Invoke-RestMethod -Method $Method -Uri $Url -Headers $headers -ContentType "application/json" -Body $json
    }

    return Invoke-RestMethod -Method $Method -Uri $Url -Headers $headers
}

$driverId = "DRV-DEMO-$(Get-Date -Format 'yyyyMMddHHmmss')"
$vehicleId = "VEH-DEMO-01"

Write-Host "Using driverId: $driverId" -ForegroundColor Cyan
Write-Host "API base: $BaseUrl" -ForegroundColor Cyan

$token = ""

try {
    Write-Host "`n0) Authenticating demo user..." -ForegroundColor Yellow
    $demoUsername = "demo_user_$(Get-Date -Format 'yyyyMMddHHmmss')"
    $demoPassword = "Demo@12345"

    $authResponse = Invoke-JsonApi -Method "POST" -Url "$BaseUrl/auth/register" -Body @{
        username = $demoUsername
        password = $demoPassword
        role = "USER"
    }

    $token = $authResponse.token
    if (-not $token) {
        throw "Authentication token not received from /api/auth/register"
    }

    Write-Host "Authenticated as $demoUsername" -ForegroundColor Green

    Write-Host "`n1) Creating 3 overspeed alerts with historical timestamps to trigger escalation..." -ForegroundColor Yellow

    # These three are within 60 minutes of each other (so escalation triggers),
    # but older than the current window (so de-escalation can be demonstrated immediately later).
    $baseTime = (Get-Date).ToUniversalTime().AddHours(-2)
    $overspeedTimestamps = @(
        $baseTime,
        $baseTime.AddMinutes(1),
        $baseTime.AddMinutes(2)
    )

    0..2 | ForEach-Object {
        $body = @{
            sourceType = "OVERSPEEDING"
            severity   = "WARNING"
            driverId   = $driverId
            timestamp  = $overspeedTimestamps[$_].ToString("o")
            metadata   = @{
                driverId  = $driverId
                vehicleId = $vehicleId
                speedKmph = (81 + $_)
            }
        }

        $response = Invoke-JsonApi -Method "POST" -Url "$BaseUrl/alerts" -Body $body -Token $token
        Write-Host "Created overspeed alert: $($response.alertId) status=$($response.status) severity=$($response.severity)" -ForegroundColor Gray
    }

    $alerts = Invoke-JsonApi -Method "GET" -Url "$BaseUrl/alerts" -Token $token
    $overspeedAlert = $alerts |
        Where-Object { $_.driverId -eq $driverId -and $_.sourceType -eq "OVERSPEEDING" } |
        Sort-Object timestamp -Descending |
        Select-Object -First 1

    if ($null -eq $overspeedAlert) {
        throw "Could not find overspeed alert for demo driver"
    }

    Write-Host "Escalation check -> alertId=$($overspeedAlert.alertId), status=$($overspeedAlert.status), severity=$($overspeedAlert.severity)" -ForegroundColor Green

    Write-Host "`n2) Triggering re-evaluation to demonstrate de-escalation..." -ForegroundColor Yellow
    [void](Invoke-JsonApi -Method "POST" -Url "$BaseUrl/alerts/compliance-renewed" -Body @{ driverId = "DRV-NOOP" } -Token $token)

    Start-Sleep -Seconds 1
    $overspeedDetails = Invoke-JsonApi -Method "GET" -Url "$BaseUrl/alerts/$($overspeedAlert.alertId)" -Token $token
    $overspeedAfter = $overspeedDetails.alert

    Write-Host "De-escalation check -> alertId=$($overspeedAfter.alertId), status=$($overspeedAfter.status), severity=$($overspeedAfter.severity)" -ForegroundColor Green

    $deEscEvent = $overspeedDetails.history |
        Where-Object { $_.eventType -eq "DE_ESCALATED" } |
        Select-Object -Last 1

    if ($null -ne $deEscEvent) {
        Write-Host "De-escalation event found -> $($deEscEvent.timestamp) | $($deEscEvent.reason)" -ForegroundColor Gray
    } else {
        Write-Host "Warning: DE_ESCALATED event not found in history" -ForegroundColor DarkYellow
    }

    Write-Host "`n3) Creating compliance alert and auto-closing via document renewal..." -ForegroundColor Yellow
    $complianceBody = @{
        sourceType = "COMPLIANCE"
        severity   = "WARNING"
        driverId   = $driverId
        timestamp  = (Get-Date).ToUniversalTime().ToString("o")
        metadata   = @{
            driverId            = $driverId
            vehicleId           = $vehicleId
            document_valid      = "false"
            document_expiry_date = (Get-Date).AddDays(-1).ToUniversalTime().ToString("o")
        }
    }

    $createdCompliance = Invoke-JsonApi -Method "POST" -Url "$BaseUrl/alerts" -Body $complianceBody -Token $token
    $renewed = Invoke-JsonApi -Method "POST" -Url "$BaseUrl/alerts/compliance-renewed" -Body @{ driverId = $driverId } -Token $token

    Write-Host "Compliance renewed -> updatedAlerts=$($renewed.updatedAlerts), autoClosedAlerts=$($renewed.autoClosedAlerts)" -ForegroundColor Green

    $complianceDetails = Invoke-JsonApi -Method "GET" -Url "$BaseUrl/alerts/$($createdCompliance.alertId)" -Token $token
    $complianceAlert = $complianceDetails.alert
    Write-Host "Compliance alert status=$($complianceAlert.status), reason=$($complianceAlert.autoCloseReason)" -ForegroundColor Green

    Write-Host "Recent overspeed lifecycle events:" -ForegroundColor Gray
    ($overspeedDetails.history | Select-Object -Last 5) | ForEach-Object {
        Write-Host " - $($_.timestamp) | $($_.eventType) | $($_.fromStatus) -> $($_.toStatus) | $($_.reason)" -ForegroundColor Gray
    }

    Write-Host "`nDemo completed successfully." -ForegroundColor Cyan
}
catch {
    Write-Host "`nDemo failed: $($_.Exception.Message)" -ForegroundColor Red
    throw
}
