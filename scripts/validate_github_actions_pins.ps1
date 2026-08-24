[CmdletBinding()]
param(
    [string]$WorkflowDirectory = (Join-Path $PSScriptRoot '..\.github\workflows')
)

$ErrorActionPreference = 'Stop'
$workflowPath = (Resolve-Path -LiteralPath $WorkflowDirectory).Path
$invalidReferences = [System.Collections.Generic.List[string]]::new()

Get-ChildItem -LiteralPath $workflowPath -File -Recurse -Include '*.yml', '*.yaml' |
    ForEach-Object {
        $lineNumber = 0
        foreach ($line in Get-Content -LiteralPath $_.FullName) {
            $lineNumber++
            if ($line -notmatch '^\s*-?\s*uses:\s*([^\s#]+)') {
                continue
            }

            $reference = $Matches[1]
            if ($reference -notmatch '^[^/\s]+/[^@\s]+@([0-9a-fA-F]{40})$') {
                $invalidReferences.Add("$($_.FullName):$lineNumber uses '$reference' instead of a full 40-character SHA.")
                continue
            }

            if ($line -notmatch '#\s*v?\d+(?:\.\d+){0,2}(?:\S*)?\s*$') {
                $invalidReferences.Add("$($_.FullName):$lineNumber is missing a human-readable version comment.")
            }
        }
    }

if ($invalidReferences.Count -gt 0) {
    $invalidReferences | ForEach-Object { Write-Error $_ }
    exit 1
}

Write-Host "All external GitHub Actions are pinned to full SHAs with version comments."
