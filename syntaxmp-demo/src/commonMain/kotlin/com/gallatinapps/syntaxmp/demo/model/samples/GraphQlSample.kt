package com.gallatinapps.syntaxmp.demo.model.samples

internal val GraphQlSample = """
    # Fetch a library preview with reusable note fields.
    query LibraryOverview(${'$'}libraryId: ID!, ${'$'}limit: Int = 20) {
      library(id: ${'$'}libraryId) {
        name
        files(first: ${'$'}limit, orderBy: UPDATED_AT_DESC) {
          nodes {
            ...NoteRow
            preview(maxLength: 160)
          }
        }
      }
    }

    fragment NoteRow on LibraryFile {
      id
      title
      path
      status @include(if: true)
      metadata {
        words
        updatedAt
      }
    }

    mutation RenameNote(${'$'}id: ID!, ${'$'}title: String!, ${'$'}pinned: Boolean = false) {
      renameNote(id: ${'$'}id, title: ${'$'}title) {
        ...NoteRow
      }
      setPinned(id: ${'$'}id, pinned: ${'$'}pinned) {
        id
        pinned
      }
    }
""".trimIndent()
