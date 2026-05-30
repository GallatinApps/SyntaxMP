package com.gallatinapps.syntaxmp.benchmarks.fixtures.query

import com.gallatinapps.syntaxmp.benchmarks.fixtures.LanguageBenchmarkFamily
import com.gallatinapps.syntaxmp.benchmarks.fixtures.LanguageBenchmarkFixture

private val SqlRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "sql",
    languageLabel = "sql",
    displayName = "SQL",
    family = LanguageBenchmarkFamily.QuerySchema,
    targetBodyLines = 80,
    header = """
        -- Portable SQL report fixtures for the recent-note queue.
        BEGIN TRANSACTION;
    """,
    body = """
        CREATE TABLE IF NOT EXISTS note_events_{{index}} (
            id INTEGER PRIMARY KEY,
            library_id TEXT NOT NULL,
            file_path TEXT NOT NULL,
            title TEXT NOT NULL,
            words INTEGER NOT NULL DEFAULT 0,
            status TEXT NOT NULL DEFAULT 'draft',
            pinned BOOLEAN NOT NULL DEFAULT FALSE,
            updated_at TIMESTAMP NOT NULL,
            metadata_json TEXT
        );

        CREATE INDEX IF NOT EXISTS note_events_{{index}}_library_idx
            ON note_events_{{index}} (library_id, updated_at DESC);

        WITH recent_notes_{{index}} AS (
            SELECT
                id,
                library_id,
                title,
                updated_at,
                COALESCE(words, 0) AS words,
                CASE
                    WHEN pinned THEN 0
                    WHEN status = 'review' THEN 1
                    ELSE 2
                END AS queue_rank
            FROM note_events_{{index}}
            WHERE status IN ('draft', 'review', 'published')
              AND updated_at >= CURRENT_DATE - INTERVAL '14 days'
        ),
        ranked_notes_{{index}} AS (
            SELECT
                library_id,
                title,
                updated_at,
                words,
                ROW_NUMBER() OVER (
                    PARTITION BY library_id
                    ORDER BY queue_rank ASC, updated_at DESC
                ) AS position
            FROM recent_notes_{{index}}
            WHERE words BETWEEN 1 AND 200000
        ),
        queue_summary_{{index}} AS (
            SELECT
                library_id,
                COUNT(*) AS note_count,
                SUM(words) AS total_words,
                MAX(updated_at) AS newest_update
            FROM ranked_notes_{{index}}
            GROUP BY library_id
        )
        SELECT
            ranked.title || ' #' || CAST(ranked.position AS VARCHAR(8)) AS label,
            ranked.updated_at,
            summary.note_count,
            summary.total_words
        FROM ranked_notes_{{index}} ranked
        JOIN queue_summary_{{index}} summary
          ON summary.library_id = ranked.library_id
        WHERE ranked.title IS NOT NULL
          AND ranked.position <= 20
        ORDER BY ranked.updated_at DESC
        FETCH FIRST 20 ROWS ONLY;
    """,
    footer = """
        COMMIT;
    """,
)

private val SqliteRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "sqlite",
    languageLabel = "sqlite",
    displayName = "SQLite",
    family = LanguageBenchmarkFamily.QuerySchema,
    targetBodyLines = 80,
    body = """
        PRAGMA foreign_keys = ON;
        PRAGMA journal_mode = WAL;

        CREATE TABLE IF NOT EXISTS libraries_{{index}} (
            id TEXT PRIMARY KEY,
            display_name TEXT NOT NULL,
            root_path TEXT NOT NULL,
            created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
        );

        CREATE TABLE IF NOT EXISTS files_{{index}} (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            library_id TEXT NOT NULL REFERENCES libraries_{{index}}(id) ON DELETE CASCADE,
            relative_path TEXT NOT NULL,
            title TEXT NOT NULL,
            extension TEXT,
            byte_size INTEGER NOT NULL DEFAULT 0,
            content_hash TEXT,
            modified_at TEXT,
            UNIQUE(library_id, relative_path)
        );

        CREATE VIRTUAL TABLE IF NOT EXISTS file_search_{{index}}
        USING fts5(title, path, body, content='');

        CREATE TRIGGER IF NOT EXISTS files_{{index}}_delete_search
        AFTER DELETE ON files_{{index}}
        BEGIN
            INSERT INTO file_search_{{index}}(file_search_{{index}}, rowid, title, path, body)
            VALUES('delete', old.rowid, old.title, old.relative_path, '');
        END;

        INSERT INTO libraries_{{index}} (id, display_name, root_path)
        VALUES ('library-{{index}}', 'Library {{index}}', '~/Notes/Library{{index}}')
        ON CONFLICT(id) DO UPDATE SET
            display_name = excluded.display_name,
            root_path = excluded.root_path;

        WITH ranked AS (
            SELECT
                f.id,
                f.title,
                f.relative_path,
                f.modified_at,
                rank
            FROM file_search_{{index}} s
            JOIN files_{{index}} f ON f.rowid = s.rowid
            WHERE file_search_{{index}} MATCH 'syntax NEAR benchmark'
            ORDER BY rank
            LIMIT 20
        )
        SELECT
            json_object(
                'id', id,
                'title', title,
                'path', relative_path,
                'modifiedAt', modified_at
            ) AS result_json
        FROM ranked
        ORDER BY modified_at DESC;
    """,
)

private val PostgresqlRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "postgresql",
    languageLabel = "postgresql",
    displayName = "PostgreSQL",
    family = LanguageBenchmarkFamily.QuerySchema,
    targetBodyLines = 80,
    body = """
        CREATE EXTENSION IF NOT EXISTS pg_trgm;

        CREATE TYPE note_status_{{index}} AS ENUM ('draft', 'review', 'published', 'archived');

        CREATE TABLE IF NOT EXISTS note_events_{{index}} (
            id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
            library_id text NOT NULL,
            file_path text NOT NULL,
            title text NOT NULL,
            words integer NOT NULL CHECK (words >= 0),
            status note_status_{{index}} NOT NULL DEFAULT 'draft',
            pinned boolean NOT NULL DEFAULT false,
            metadata jsonb NOT NULL DEFAULT '{}'::jsonb,
            updated_at timestamptz NOT NULL DEFAULT now()
        );

        CREATE INDEX IF NOT EXISTS note_events_{{index}}_path_trgm_idx
            ON note_events_{{index}} USING gin (file_path gin_trgm_ops);

        CREATE INDEX IF NOT EXISTS note_events_{{index}}_metadata_idx
            ON note_events_{{index}} USING gin (metadata jsonb_path_ops);

        CREATE OR REPLACE FUNCTION note_events_{{index}}_touch_updated_at()
        RETURNS trigger
        LANGUAGE plpgsql
        AS $$
        BEGIN
            NEW.updated_at = now();
            RETURN NEW;
        END;
        $$;

        DROP TRIGGER IF EXISTS note_events_{{index}}_touch ON note_events_{{index}};
        CREATE TRIGGER note_events_{{index}}_touch
            BEFORE UPDATE ON note_events_{{index}}
            FOR EACH ROW
            EXECUTE FUNCTION note_events_{{index}}_touch_updated_at();

        WITH candidate_notes AS (
            SELECT
                id,
                library_id,
                title,
                words,
                updated_at,
                metadata ->> 'owner' AS owner,
                jsonb_array_length(COALESCE(metadata -> 'tags', '[]'::jsonb)) AS tag_count
            FROM note_events_{{index}}
            WHERE status = ANY (ARRAY['draft', 'review']::note_status_{{index}}[])
              AND updated_at > now() - INTERVAL '14 days'
        ),
        scored AS (
            SELECT
                *,
                CASE WHEN owner = 'docs' THEN 2 ELSE 0 END
                + CASE WHEN tag_count > 2 THEN 1 ELSE 0 END
                + CASE WHEN pinned THEN 4 ELSE 0 END AS score
            FROM candidate_notes
            JOIN note_events_{{index}} USING (id)
        )
        SELECT library_id, title, owner, score, updated_at
        FROM scored
        WHERE words BETWEEN 1 AND 200000
        ORDER BY score DESC, updated_at DESC
        LIMIT 20;
    """,
)

private val GraphQlRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "graphql",
    languageLabel = "graphql",
    displayName = "GraphQL",
    family = LanguageBenchmarkFamily.QuerySchema,
    targetBodyLines = 80,
    body = """
        schema {
          query: Query
          mutation: Mutation
        }

        directive @cacheControl(maxAge: Int, scope: CacheScope) on FIELD_DEFINITION | OBJECT

        enum CacheScope {
          PUBLIC
          PRIVATE
        }

        enum NoteStatus{{index}} {
          DRAFT
          REVIEW
          PUBLISHED
          ARCHIVED
        }

        interface Node {
          id: ID!
        }

        type Library{{index}} implements Node @cacheControl(maxAge: 60, scope: PRIVATE) {
          id: ID!
          name: String!
          rootPath: String!
          notes(filter: NoteFilter{{index}}, first: Int = 20): [Note{{index}}!]!
        }

        type Note{{index}} implements Node {
          id: ID!
          title: String!
          words: Int!
          status: NoteStatus{{index}}!
          pinned: Boolean!
          tags: [String!]!
          updatedAt: String!
          owner: User{{index}}
        }

        type User{{index}} {
          id: ID!
          displayName: String!
          email: String
        }

        input NoteFilter{{index}} {
          query: String
          status: [NoteStatus{{index}}!]
          tags: [String!]
          pinned: Boolean
        }

        type Query {
          library{{index}}(id: ID!): Library{{index}}
          searchNotes{{index}}(query: String!, first: Int = 20): [Note{{index}}!]!
        }

        type Mutation {
          updateNoteStatus{{index}}(id: ID!, status: NoteStatus{{index}}!): Note{{index}}!
          pinNote{{index}}(id: ID!, pinned: Boolean!): Note{{index}}!
        }

        query ReviewQueue{{index}}(${'$'}libraryId: ID!, ${'$'}query: String!) {
          library{{index}}(id: ${'$'}libraryId) {
            notes(filter: { query: ${'$'}query, status: [DRAFT, REVIEW] }, first: 20) {
              id
              title
              words
              status
              tags
            }
          }
        }
    """,
)

private val ProtobufRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "protobuf",
    languageLabel = "protobuf",
    displayName = "Protocol Buffers",
    family = LanguageBenchmarkFamily.QuerySchema,
    targetBodyLines = 80,
    body = """
        syntax = "proto3";

        package benchmark.library.v{{index}};

        option java_package = "com.example.library.v{{index}}";
        option java_multiple_files = true;
        option swift_prefix = "HJ";

        import "google/protobuf/timestamp.proto";

        enum NoteStatus{{index}} {
          NOTE_STATUS_UNSPECIFIED = 0;
          NOTE_STATUS_DRAFT = 1;
          NOTE_STATUS_REVIEW = 2;
          NOTE_STATUS_PUBLISHED = 3;
          NOTE_STATUS_ARCHIVED = 4;
        }

        message Library{{index}} {
          string id = 1;
          string display_name = 2;
          string root_path = 3;
          repeated string include_globs = 4;
          repeated string exclude_globs = 5;
          LibraryLimits{{index}} limits = 6;
          map<string, string> metadata = 7;
        }

        message LibraryLimits{{index}} {
          int64 readable_bytes = 1;
          int64 enhanced_edit_bytes = 2;
          int64 metadata_only_bytes = 3;
        }

        message Note{{index}} {
          string id = 1;
          string library_id = 2;
          string relative_path = 3;
          string title = 4;
          int32 words = 5;
          bool pinned = 6;
          NoteStatus{{index}} status = 7;
          repeated string tags = 8;
          google.protobuf.Timestamp updated_at = 9;
        }

        message SearchRequest{{index}} {
          string library_id = 1;
          string query = 2;
          repeated NoteStatus{{index}} statuses = 3;
          int32 first = 4;
        }

        message SearchResponse{{index}} {
          repeated Note{{index}} notes = 1;
          string next_page_token = 2;
        }

        service LibrarySearch{{index}} {
          rpc Search(SearchRequest{{index}}) returns (SearchResponse{{index}});
          rpc StreamRecent(SearchRequest{{index}}) returns (stream Note{{index}});
        }
    """,
)


internal val QueryInfrastructureFixtures: List<LanguageBenchmarkFixture> =
    listOf(
        SqlRepresentativeFixture,
        SqliteRepresentativeFixture,
        PostgresqlRepresentativeFixture,
        GraphQlRepresentativeFixture,
        ProtobufRepresentativeFixture,
    )
