package com.gallatinapps.syntaxmp.demo.model.samples

internal val PowerShellSample = """
    param(
        [string] ${'$'}Workspace = "${'$'}HOME/Notes",
        [string] ${'$'}Query = "syntax",
        [switch] ${'$'}PinnedOnly
    )

    class NoteSummary {
        [string] ${'$'}Title
        [int] ${'$'}Words

        [string] Label() {
            return "${'$'}(${'$'}this.Title) - ${'$'}([Math]::Max(1, [Math]::Ceiling(${'$'}this.Words / 220))) min"
        }
    }

    Get-ChildItem -Path ${'$'}Workspace -Filter *.md -Recurse |
        Where-Object { Select-String -Path ${'$'}_.FullName -Pattern ${'$'}Query -Quiet } |
        ForEach-Object {
            try {
                ${'$'}content = Get-Content ${'$'}_.FullName -Raw -ErrorAction Stop
                ${'$'}words = ${'$'}content.Split().Count
            } catch {
                Write-Warning "Skipped ${'$'}(${'$'}_.FullName): ${'$'}(${'$'}_.Exception.Message)"
                return
            }
            [PSCustomObject]@{
                Title = ${'$'}_.BaseName
                Path = ${'$'}_.FullName
                Words = ${'$'}words
                Pinned = ${'$'}PinnedOnly.IsPresent
            }
        }
""".trimIndent()
