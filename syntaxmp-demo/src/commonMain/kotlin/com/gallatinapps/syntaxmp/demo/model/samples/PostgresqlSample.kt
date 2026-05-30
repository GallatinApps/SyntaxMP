package com.gallatinapps.syntaxmp.demo.model.samples

internal val PostgresqlSample = """
    CREATE TYPE file_kind AS ENUM ('markdown', 'image', 'unsupported');

    CREATE TABLE library_file_events (
        id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
        library_id uuid NOT NULL,
        path text NOT NULL,
        payload jsonb NOT NULL,
        recorded_at timestamptz NOT NULL DEFAULT now()
    );

    CREATE INDEX ON library_file_events USING gin (payload jsonb_path_ops);

    CREATE OR REPLACE FUNCTION note_label(payload jsonb)
    RETURNS text
    LANGUAGE sql
    STABLE
    AS ${'$'}${'$'}
        SELECT format('%s (%s)', payload->>'title', payload->>'kind')
    ${'$'}${'$'};

    SELECT
        payload->>'title' AS title,
        payload #>> '{metadata,source}' AS source,
        note_label(payload) AS label,
        recorded_at
    FROM library_file_events
    WHERE payload @> '{"kind": "markdown"}'::jsonb
    ORDER BY recorded_at DESC
    LIMIT 50;
""".trimIndent()
