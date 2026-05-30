package com.gallatinapps.syntaxmp.demo.model.samples

internal val CSample = """
    #include <stdio.h>
    #include <stdbool.h>
    #include <stddef.h>

    #define HJ_MINUTES(words) (((words) + 219u) / 220u)

    enum NoteStatus {
        NOTE_DRAFT = 0,
        NOTE_REVIEW = 1,
        NOTE_PUBLISHED = 2,
    };

    typedef struct {
        const char *title;
        size_t words;
        bool pinned;
        enum NoteStatus status;
    } NoteSummary;

    size_t reading_minutes(NoteSummary note) {
        size_t minutes = HJ_MINUTES(note.words);
        return minutes == 0u ? 1u : minutes;
    }

    bool should_pin(NoteSummary note) {
        return note.pinned || (note.status == NOTE_REVIEW && note.words > 2000u);
    }

    void print_label(const NoteSummary *note) {
        printf("%s - %zu min\n", note->title, reading_minutes(*note));
    }
""".trimIndent()
