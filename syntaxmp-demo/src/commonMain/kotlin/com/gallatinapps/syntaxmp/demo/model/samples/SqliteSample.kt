package com.gallatinapps.syntaxmp.demo.model.samples

internal val SqliteSample = """
    PRAGMA foreign_keys = ON;

    CREATE VIRTUAL TABLE IF NOT EXISTS library_file_fts
    USING fts5(title, body, tokenize = 'porter unicode61');

    SELECT
        f.path,
        json_extract(f.metadata, '${'$'}.title') AS title,
        bm25(library_file_fts) AS rank,
        strftime('%Y-%m-%d', f.modified_at) AS changed
    FROM library_file_fts
    JOIN library_files f ON f.id = library_file_fts.rowid
    WHERE library_file_fts MATCH 'workspace NEAR browser'
      AND f.deleted_at IS NULL
      AND f.path LIKE :folder || '/%'
    ORDER BY rank, f.modified_at DESC
    LIMIT 25;
""".trimIndent()
