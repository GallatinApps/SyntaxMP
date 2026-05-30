package com.gallatinapps.syntaxmp.demo.model.samples

internal val SqlSample = """
    -- Portable SQL report for the recent-note queue.
    WITH recent_notes AS (
        SELECT id, title, updated_at, COALESCE(words, 0) AS words
        FROM library_files
        WHERE kind = 'markdown'
          AND updated_at >= CURRENT_DATE - INTERVAL '14 days'
    ),
    ranked AS (
        SELECT
            title,
            updated_at,
            ROW_NUMBER() OVER (ORDER BY updated_at DESC) AS position
        FROM recent_notes
        WHERE words BETWEEN 1 AND 200000
    )
    SELECT title || ' #' || CAST(position AS VARCHAR(8)) AS label, updated_at
    FROM ranked
    WHERE title IS NOT NULL AND position <= 20
    FETCH FIRST 20 ROWS ONLY;
""".trimIndent()
