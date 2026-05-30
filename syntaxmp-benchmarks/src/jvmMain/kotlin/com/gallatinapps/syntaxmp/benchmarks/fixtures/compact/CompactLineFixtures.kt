package com.gallatinapps.syntaxmp.benchmarks.fixtures.compact

import com.gallatinapps.syntaxmp.benchmarks.fixtures.LanguageBenchmarkFamily
import com.gallatinapps.syntaxmp.benchmarks.fixtures.LanguageBenchmarkFixture

private val CsvRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "csv",
    languageLabel = "csv",
    displayName = "CSV",
    family = LanguageBenchmarkFamily.CompactLineFormat,
    targetBodyLines = 12,
    body = """
        library_id,path,title,words,status,pinned,updated_at,owner
        library-{{index}},notes/launch-{{index}}.md,Launch Plan {{index}},1240,review,true,2026-05-20T09:10:00Z,docs
        library-{{index}},notes/sync-{{index}}.md,Sync Notes {{index}},820,draft,false,2026-05-20T10:15:00Z,platform
        library-{{index}},notes/export-{{index}}.md,Export Checklist {{index}},560,published,false,2026-05-19T14:30:00Z,docs
        library-{{index}},notes/mobile-{{index}}.md,Touch Flow {{index}},910,review,false,2026-05-18T16:45:00Z,product
        library-{{index}},notes/index-{{index}}.md,Index Policy {{index}},730,draft,true,2026-05-17T08:20:00Z,search
        library-{{index}},notes/render-{{index}}.md,Render Harness {{index}},680,review,false,2026-05-16T11:00:00Z,editor
        library-{{index}},notes/security-{{index}}.md,Grant Audit {{index}},430,draft,false,2026-05-15T12:00:00Z,platform
        library-{{index}},notes/storage-{{index}}.md,Storage Plan {{index}},1020,review,true,2026-05-14T13:00:00Z,data
        library-{{index}},notes/theme-{{index}}.md,Theme Pass {{index}},350,published,false,2026-05-13T15:00:00Z,design
        library-{{index}},notes/search-{{index}}.md,Search QA {{index}},610,draft,false,2026-05-12T17:00:00Z,search
        library-{{index}},notes/release-{{index}}.md,Release Packet {{index}},1500,review,true,2026-05-11T18:00:00Z,docs
    """,
)

private val DiffRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "diff",
    languageLabel = "diff",
    displayName = "Diff",
    family = LanguageBenchmarkFamily.CompactLineFormat,
    targetBodyLines = 30,
    body = """
        diff --git a/library-{{index}}/Launch.md b/library-{{index}}/Launch.md
        index 29b8aa1..53cd8f0 100644
        --- a/library-{{index}}/Launch.md
        +++ b/library-{{index}}/Launch.md
        @@ -1,15 +1,20 @@
         # Launch Plan {{index}}

        -The first scan should read every file eagerly.
        +The first scan should read only editable text under the threshold.
         It should keep image entries metadata-only.

        -- [ ] Rebuild the whole index on every open
        +- [x] Restore library grants before scanning
        +- [x] Hash editable text files after extraction
        +- [ ] Surface conflicts before saving

         ## Limits

        -Readable text max bytes: 4000000
        -Enhanced edit max bytes: 500000
        +Readable text max bytes: 2000000
        +Enhanced edit max bytes: 256000

         ## Notes
         Keep user-authored files as the source of truth.
        +Keep preview-only sessions outside autosave.
        +Do not rewrite external files during scan.
        \ No newline at end of file
    """,
)

private val DockerfileRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "dockerfile",
    languageLabel = "dockerfile",
    displayName = "Dockerfile",
    family = LanguageBenchmarkFamily.CompactLineFormat,
    targetBodyLines = 35,
    body = """
        # syntax=docker/dockerfile:1.7
        ARG JDK_VERSION=17
        FROM eclipse-temurin:${'$'}{JDK_VERSION}-jdk AS build-{{index}}
        WORKDIR /workspace
        ENV GRADLE_USER_HOME=/workspace/.gradle
        COPY gradle gradle
        COPY gradlew settings.gradle.kts build.gradle.kts ./
        COPY syntaxmp syntaxmp
        COPY syntaxmp-benchmarks syntaxmp-benchmarks
        RUN --mount=type=cache,target=/workspace/.gradle \
            ./gradlew -p . :syntaxmp-benchmarks:compileKotlinJvm

        FROM eclipse-temurin:${'$'}{JDK_VERSION}-jre AS runtime-{{index}}
        LABEL org.opencontainers.image.title="syntaxmp-benchmarks"
        LABEL org.opencontainers.image.description="Representative benchmark runner {{index}}"
        WORKDIR /app
        COPY --from=build-{{index}} /workspace/syntaxmp-benchmarks/build/libs ./libs
        RUN <<'EOF'
        set -eu
        mkdir -p /app/reports
        printf '%s\n' 'ready for benchmark run'
        EOF
        USER 1000:1000
        ENTRYPOINT ["java"]
        CMD [
          "-cp",
          "libs/*",
          "com.gallatinapps.syntaxmp.benchmarks.MainKt"
        ]
    """,
)

private val MakefileRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "makefile",
    languageLabel = "makefile",
    displayName = "Makefile",
    family = LanguageBenchmarkFamily.CompactLineFormat,
    targetBodyLines = 35,
    body = """
        SHELL := /bin/sh
        GRADLE ?= ../gradlew
        REPORT_DIR := syntaxmp-benchmarks/build/reports/syntaxmp-benchmarks

        .PHONY: all compile benchmarks clean report-{{index}}
        all: compile benchmarks

        compile:
          ${'$'}(GRADLE) -p . :syntaxmp-benchmarks:compileKotlinJvm

        benchmarks:
          ${'$'}(GRADLE) -p . :syntaxmp-benchmarks:runSyntaxMpBenchmarks

        report-{{index}}:
          @printf 'latest=%s\n' '${'$'}(REPORT_DIR)/latest/report.md'

        clean:
          ${'$'}(GRADLE) -p . :syntaxmp-benchmarks:clean

        syntaxmp-benchmarks/build:
          mkdir -p ${'$'}@

        syntaxmp-benchmarks/build/reports: syntaxmp-benchmarks/build
          mkdir -p ${'$'}@

        benchmark-{{index}}: syntaxmp-benchmarks/build/reports
          ${'$'}(GRADLE) -p . :syntaxmp-benchmarks:runSyntaxMpBenchmarks
    """,
)



internal val CompactLineFixtures: List<LanguageBenchmarkFixture> =
    listOf(
        CsvRepresentativeFixture,
        DiffRepresentativeFixture,
        DockerfileRepresentativeFixture,
        MakefileRepresentativeFixture,
    )
