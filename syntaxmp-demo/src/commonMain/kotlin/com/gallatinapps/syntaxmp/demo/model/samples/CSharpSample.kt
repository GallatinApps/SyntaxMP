package com.gallatinapps.syntaxmp.demo.model.samples

internal val CSharpSample = """
    using System;
    using System.Collections.Generic;
    using System.Linq;

    public enum NoteStatus { Draft, Review, Published }

    [Serializable]
    public sealed record NoteSummary(
        string Title,
        int Words,
        bool Pinned,
        NoteStatus Status
    ) {
        public int ReadingMinutes => Math.Max(1, (Words + 219) / 220);
        public string Label => ${'$'}"{Title} • {ReadingMinutes} min • {Status}";
    }

    public static class ReadingQueue
    {
        public static IEnumerable<NoteSummary> Top(IEnumerable<NoteSummary> notes) =>
            notes.Where(note => note is { Status: NoteStatus.Draft or NoteStatus.Review })
                 .OrderByDescending(note => note.Pinned)
                 .ThenByDescending(note => note.ReadingMinutes);

        public static string Describe(NoteSummary? note) =>
            note?.Label ?? "No active note";
    }
""".trimIndent()
