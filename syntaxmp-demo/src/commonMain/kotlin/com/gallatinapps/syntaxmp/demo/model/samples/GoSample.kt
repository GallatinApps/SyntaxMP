package com.gallatinapps.syntaxmp.demo.model.samples

internal val GoSample = """
    package library

    import (
        "fmt"
        "regexp"
        "sort"
        "time"
    )

    type Status string

    const (
        Draft  Status = "draft"
        Review Status = "review"
    )

    type Note struct {
        Title     string
        Words     int
        Pinned    bool
        UpdatedAt time.Time
        Status    Status
    }

    func ReadingMinutes(note Note) int {
        minutes := (note.Words + 219) / 220
        if minutes < 1 {
            return 1
        }
        return minutes
    }

    func Label(note Note) string {
        mention := regexp.MustCompile("@\\w+").FindString(note.Title)
        if mention == "" {
            mention = "@local"
        }
        return fmt.Sprintf("%s • %d min • %s", note.Title, ReadingMinutes(note), mention)
    }

    func SortRecent(notes []Note) {
        sort.Slice(notes, func(i, j int) bool {
            return notes[i].UpdatedAt.After(notes[j].UpdatedAt)
        })
    }
""".trimIndent()
