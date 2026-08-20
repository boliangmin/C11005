param(
    [Parameter(Mandatory = $true)]
    [string]$Content,

    [string[]]$Details = @("NULL"),

    [string]$Remark = "NULL",

    [string[]]$Paths = @(),

    [switch]$SkipRunApp
)

$ErrorActionPreference = "Stop"

function Format-ChangeNumber {
    param([int]$Number)
    return "C11005-{0:D4}" -f $Number
}

function Invoke-Git {
    param([string[]]$Arguments)
    & git @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "git $($Arguments -join ' ') failed with exit code $LASTEXITCODE"
    }
}

function Invoke-GitCapture {
    param([string[]]$Arguments)
    $output = & git @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "git $($Arguments -join ' ') failed with exit code $LASTEXITCODE"
    }
    return $output
}

function Split-Values {
    param([string[]]$Values, [string]$DefaultValue = "")
    $normalized = @()
    foreach ($value in $Values) {
        if ($value -match "\|") { $normalized += $value -split "\|" }
        elseif ($Values.Count -eq 1 -and $value -match ";") { $normalized += $value -split ";" }
        elseif ($Values.Count -eq 1 -and $value -match ",") { $normalized += $value -split "," }
        else { $normalized += $value }
    }
    $normalized = @($normalized | ForEach-Object { $_.Trim() } | Where-Object { $_ })
    if (-not $normalized -and $DefaultValue) { return @($DefaultValue) }
    return $normalized
}

$repoRoot = Invoke-GitCapture -Arguments @("rev-parse", "--show-toplevel")
if (-not $repoRoot) { throw "Not a Git repository." }
$repoRoot = $repoRoot | Select-Object -First 1
Set-Location $repoRoot

$statePath = Join-Path $repoRoot ".auto_commit_state.json"
$messagePath = Join-Path ([System.IO.Path]::GetTempPath()) ("C11005_commit_message_{0}.tmp" -f ([guid]::NewGuid().ToString("N")))
$pendingStatus = Invoke-GitCapture -Arguments @("status", "--short")
if (-not $pendingStatus) {
    Write-Host "No changes to commit."
    exit 0
}

$nextNumber = 1
$stateExisted = Test-Path -LiteralPath $statePath
if ($stateExisted) {
    $state = Get-Content -LiteralPath $statePath -Encoding UTF8 -Raw | ConvertFrom-Json
    if ($state.prefix -and $state.prefix -ne "C11005") {
        throw "Unexpected commit prefix in state file: $($state.prefix)"
    }
    if ($state.nextNumber -as [int]) { $nextNumber = [int]$state.nextNumber }
}

$changeNumber = Format-ChangeNumber -Number $nextNumber
$timestamp = Get-Date -Format "yyyy.MM.dd HH:mm:ss"
$normalizedDetails = Split-Values -Values $Details -DefaultValue "NULL"
$normalizedPaths = Split-Values -Values $Paths
$detailLines = @()
for ($index = 0; $index -lt $normalizedDetails.Count; $index++) {
    $detailLines += ("{0}).{1}" -f ($index + 1), $normalizedDetails[$index])
}
$messageLines = @(
    "修改日期： $timestamp",
    "1.修改编号： $changeNumber",
    "2.修改内容： $Content",
    "3.细节："
) + $detailLines + @("4.修改备注： $Remark")

try {
    @{ prefix = "C11005"; nextNumber = $nextNumber + 1 } |
        ConvertTo-Json | Set-Content -LiteralPath $statePath -Encoding UTF8
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllLines($messagePath, $messageLines, $utf8NoBom)
    if ($normalizedPaths.Count -gt 0) {
        Invoke-Git -Arguments (@("add", "--") + $normalizedPaths + @($statePath))
    }
    else { Invoke-Git -Arguments @("add", "-A") }
    Invoke-Git -Arguments @("commit", "-F", $messagePath)
    Write-Host "Committed: $changeNumber"
}
catch {
    if ($stateExisted) {
        @{ prefix = "C11005"; nextNumber = $nextNumber } |
            ConvertTo-Json | Set-Content -LiteralPath $statePath -Encoding UTF8
    }
    else { Remove-Item -LiteralPath $statePath -Force -ErrorAction SilentlyContinue }
    throw
}
finally {
    Remove-Item -LiteralPath $messagePath -Force -ErrorAction SilentlyContinue
}
