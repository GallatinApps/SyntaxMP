package com.gallatinapps.syntaxmp.benchmarks.fixtures.config

import com.gallatinapps.syntaxmp.benchmarks.fixtures.LanguageBenchmarkFamily
import com.gallatinapps.syntaxmp.benchmarks.fixtures.LanguageBenchmarkFixture

private val JsonRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "json",
    languageLabel = "json",
    displayName = "JSON",
    family = LanguageBenchmarkFamily.ConfigData,
    targetBodyLines = 60,
    header = """
        {
          "${'$'}schema": "https://example.test/hashjot.workspace.schema.json",
          "workspace": "Research Notes",
          "version": 1,
          "sync": {
            "enabled": false,
            "lastIndexedAt": "2026-05-23T09:42:00Z",
            "remote": null
          },
          "libraries": [
            {
              "id": "core",
              "name": "Core",
              "path": "~/Notes/Core",
              "include": ["**/*.md", "**/*.txt"],
              "exclude": ["archive/**", ".hashjot/**"],
              "limits": {
                "readableBytes": 2000000,
                "enhancedEditBytes": 256000
              }
            }
    """,
    body = """
            , {
              "id": "library-{{index}}",
              "name": "Library {{index}}",
              "path": "~/Notes/Library{{index}}",
              "include": [
                "**/*.md",
                "**/*.markdown",
                "**/*.txt",
                "projects/**/*.json"
              ],
              "exclude": [
                "archive/**",
                ".hashjot/**",
                "exports/tmp/**"
              ],
              "display": {
                "accent": "#2563eb",
                "icon": "folder",
                "collapsed": false
              },
              "limits": {
                "readableBytes": 2000000,
                "enhancedEditBytes": 256000,
                "metadataOnlyBytes": 50000000
              },
              "search": {
                "enabled": true,
                "indexCodeBlocks": true,
                "weights": {
                  "title": 3.0,
                  "path": 1.25,
                  "body": 1.0
                }
              },
              "recent": {
                "maxItems": 40,
                "includeExternal": true,
                "sort": ["pinned", "modifiedAt"]
              },
              "features": [
                "scan",
                "search",
                "preview",
                "export"
              ]
            }
    """,
    footer = """
          ]
        }
    """,
)

private val YamlRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "yaml",
    languageLabel = "yaml",
    displayName = "YAML",
    family = LanguageBenchmarkFamily.ConfigData,
    targetBodyLines = 60,
    body = """
        library_{{index}}:
          name: "Library {{index}}"
          path: "~/Notes/Library{{index}}"
          enabled: true
          scan:
            include:
              - "**/*.md"
              - "**/*.markdown"
              - "**/*.txt"
              - "projects/**/*.json"
            exclude:
              - "archive/**"
              - ".hashjot/**"
              - "exports/tmp/**"
            follow_symlinks: false
            max_depth: 8
          limits:
            readable_bytes: 2000000
            enhanced_edit_bytes: 256000
            metadata_only_bytes: 50000000
          display:
            accent: "#2563eb"
            icon: folder
            collapsed: false
          search:
            enabled: true
            index_code_blocks: true
            weights:
              title: 3.0
              path: 1.25
              body: 1.0
          recent:
            max_items: 40
            include_external: true
            sort:
              - pinned
              - modifiedAt
          workflows:
            - id: scan-{{index}}
              trigger: manual
              steps:
                - restore-grants
                - enumerate-files
                - extract-text
                - write-index
            - id: export-{{index}}
              trigger: on-demand
              options:
                format: markdown
                include_assets: false
          notes:
            owner: docs
            tags:
              - release
              - review
              - local-first
    """,
)

private val TomlRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "toml",
    languageLabel = "toml",
    displayName = "TOML",
    family = LanguageBenchmarkFamily.ConfigData,
    targetBodyLines = 60,
    body = """
        [library_{{index}}]
        name = "Library {{index}}"
        path = "~/Notes/Library{{index}}"
        enabled = true
        accent = "#2563eb"

        [library_{{index}}.scan]
        follow_symlinks = false
        max_depth = 8
        include_hidden = false

        [library_{{index}}.scan.patterns]
        include = ["**/*.md", "**/*.markdown", "**/*.txt", "projects/**/*.json"]
        exclude = ["archive/**", ".hashjot/**", "exports/tmp/**"]

        [library_{{index}}.limits]
        readable_bytes = 2000000
        enhanced_edit_bytes = 256000
        metadata_only_bytes = 50000000

        [library_{{index}}.search]
        enabled = true
        index_code_blocks = true

        [library_{{index}}.search.weights]
        title = 3.0
        path = 1.25
        body = 1.0
        tags = 1.5

        [[library_{{index}}.workflows]]
        id = "scan-{{index}}"
        trigger = "manual"
        steps = ["restore-grants", "enumerate-files", "extract-text", "write-index"]

        [[library_{{index}}.workflows]]
        id = "export-{{index}}"
        trigger = "on-demand"
        steps = ["load-session", "render-artifact", "show-save-panel"]

        [[library_{{index}}.recent.filters]]
        name = "review"
        query = "status:review"
        pinned = true

        [[library_{{index}}.recent.filters]]
        name = "draft"
        query = "status:draft"
        pinned = false

        [library_{{index}}.owner]
        team = "docs"
        slack = "#notes"
        priority = 2
    """,
)

private val IniRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "ini",
    languageLabel = "ini",
    displayName = "INI",
    family = LanguageBenchmarkFamily.ConfigData,
    targetBodyLines = 60,
    body = """
        [library-{{index}}]
        name=Library {{index}}
        path=~/Notes/Library{{index}}
        enabled=true
        accent=#2563eb
        icon=folder
        collapsed=false

        [library-{{index}}.scan]
        include.1=**/*.md
        include.2=**/*.markdown
        include.3=**/*.txt
        include.4=projects/**/*.json
        exclude.1=archive/**
        exclude.2=.hashjot/**
        exclude.3=exports/tmp/**
        followSymlinks=false
        maxDepth=8
        includeHidden=false

        [library-{{index}}.limits]
        readableBytes=2000000
        enhancedEditBytes=256000
        metadataOnlyBytes=50000000

        [library-{{index}}.search]
        enabled=true
        indexCodeBlocks=true
        weight.title=3.0
        weight.path=1.25
        weight.body=1.0
        weight.tags=1.5

        [library-{{index}}.recent]
        maxItems=40
        includeExternal=true
        sort.1=pinned
        sort.2=modifiedAt
        filter.review=status:review
        filter.draft=status:draft

        [library-{{index}}.workflow.scan]
        trigger=manual
        step.1=restore-grants
        step.2=enumerate-files
        step.3=extract-text
        step.4=write-index

        [library-{{index}}.workflow.export]
        trigger=on-demand
        step.1=load-session
        step.2=render-artifact
        step.3=show-save-panel
    """,
)

private val PropertiesRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "properties",
    languageLabel = "properties",
    displayName = "Properties",
    family = LanguageBenchmarkFamily.ConfigData,
    targetBodyLines = 60,
    body = """
        app.name=SyntaxMP Preview {{index}}
        app.version=1.0.{{index}}
        app.enabled=true
        app.mode=review
        app.owner=docs
        app.timeoutMillis=2500
        app.cache.enabled=true
        app.cache.maxEntries=400
        app.cache.expireAfterMinutes=45

        library.root=${'$'}{user.home}/Notes/Library{{index}}
        library.include.1=**/*.md
        library.include.2=**/*.markdown
        library.include.3=**/*.txt
        library.exclude.1=archive/**
        library.exclude.2=.hashjot/**
        library.exclude.3=exports/tmp/**

        editor.defaultMode=preview
        editor.wrap=true
        editor.tabWidth=4
        editor.fontFamily=JetBrains Mono
        editor.fontSize=14

        search.enabled=true
        search.indexCodeBlocks=true
        search.weight.title=3.0
        search.weight.path=1.25
        search.weight.body=1.0
        search.weight.tags=1.5

        workflow.scan.trigger=manual
        workflow.scan.step.1=restore-grants
        workflow.scan.step.2=enumerate-files
        workflow.scan.step.3=extract-text
        workflow.scan.step.4=write-index

        workflow.export.trigger=on-demand
        workflow.export.format=markdown
        workflow.export.includeAssets=false
    """,
)

private val DotenvRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "dotenv",
    languageLabel = "dotenv",
    displayName = "Dotenv",
    family = LanguageBenchmarkFamily.ConfigData,
    targetBodyLines = 60,
    body = """
        # Local preview environment {{index}}
        APP_NAME=SyntaxMP
        APP_ENV=development
        APP_DEBUG=true
        APP_PORT=808{{index}}
        API_BASE_URL=https://api{{index}}.example.test
        API_TIMEOUT_SECONDS=30
        FEATURE_MARKDOWN=true
        FEATURE_SYNTAX_HIGHLIGHTING=true
        FEATURE_EXPORT=false
        LOG_LEVEL=debug
        LOG_FORMAT=json

        export LIBRARY_ROOT="${'$'}HOME/Notes/Library{{index}}"
        export LIBRARY_INCLUDE="**/*.md,**/*.markdown,**/*.txt"
        export LIBRARY_EXCLUDE="archive/**,.hashjot/**,exports/tmp/**"
        export SEARCH_INDEX_CODE_BLOCKS=true
        export SEARCH_TITLE_WEIGHT=3.0
        export SEARCH_PATH_WEIGHT=1.25
        export SEARCH_BODY_WEIGHT=1.0

        CACHE_ENABLED=true
        CACHE_MAX_ENTRIES=400
        CACHE_TTL_MINUTES=45
        SYNC_ENABLED=false
        SYNC_REMOTE_URL=
        SECRET_TOKEN="replace-me-{{index}}"
        RELEASE_CHANNEL=local
        REVIEW_QUEUE_LIMIT=20
        EXPORT_DEFAULT_FORMAT=markdown
    """,
)


internal val ConfigDataFixtures: List<LanguageBenchmarkFixture> =
    listOf(
        JsonRepresentativeFixture,
        YamlRepresentativeFixture,
        TomlRepresentativeFixture,
        IniRepresentativeFixture,
        PropertiesRepresentativeFixture,
        DotenvRepresentativeFixture,
    )
