package com.gallatinapps.syntaxmp.benchmarks.fixtures.source

import com.gallatinapps.syntaxmp.benchmarks.fixtures.LanguageBenchmarkFamily
import com.gallatinapps.syntaxmp.benchmarks.fixtures.LanguageBenchmarkFixture

// General-source fixtures for managed-runtime languages (Java plus .NET C#).
// Grouped by runtime category only to keep files manageable; all are aggregated in
// GeneralSourceFixtures.

internal val JavaRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "java",
    languageLabel = "java",
    displayName = "Java",
    family = LanguageBenchmarkFamily.GeneralSource,
    targetBodyLines = 100,
    body = """
        package benchmark.java{{index}};

        import java.nio.file.Path;
        import java.time.Instant;
        import java.util.ArrayList;
        import java.util.Comparator;
        import java.util.List;
        import java.util.Map;
        import java.util.Optional;
        import java.util.stream.Collectors;

        @Deprecated(forRemoval = false)
        public final class NoteQueue{{index}} {
            public enum Status {
                DRAFT,
                REVIEW,
                PUBLISHED
            }

            public record Note<TMeta>(
                String id,
                String title,
                Path path,
                int words,
                boolean pinned,
                Status status,
                List<String> tags,
                TMeta metadata,
                Instant updatedAt
            ) {
                public int readingMinutes() {
                    return Math.max(1, (int) Math.ceil(words / 220.0));
                }

                public String label() {
                    return "%s / %d min / %s".formatted(title, readingMinutes(), status);
                }
            }

            private final List<Note<Map<String, String>>> notes;

            public NoteQueue{{index}}(List<Note<Map<String, String>>> notes) {
                this.notes = new ArrayList<>(notes);
            }

            public List<Note<Map<String, String>>> select(Status status) {
                return notes.stream()
                    .filter(note -> note.status() == status)
                    .sorted(Comparator
                        .comparing(Note<Map<String, String>>::pinned).reversed()
                        .thenComparing(Note::readingMinutes)
                        .thenComparing(Note::title))
                    .toList();
            }

            public Optional<Note<Map<String, String>>> firstPinned() {
                return notes.stream()
                    .filter(Note::pinned)
                    .findFirst();
            }

            public String renderReport(Status status) {
                return select(status).stream()
                    .map(Note::label)
                    .collect(Collectors.joining(System.lineSeparator()));
            }

            public static NoteQueue{{index}} sample() {
                var note = new Note<>(
                    "launch-{{index}}",
                    "Launch plan {{index}}",
                    Path.of("notes", "launch-{{index}}.md"),
                    1240 + {{index}},
                    true,
                    Status.REVIEW,
                    List.of("release", "review"),
                    Map.of("owner", "docs"),
                    Instant.now()
                );
                return new NoteQueue{{index}}(List.of(note));
            }
        }
    """,
)
internal val CSharpRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "csharp",
    languageLabel = "csharp",
    displayName = "C#",
    family = LanguageBenchmarkFamily.GeneralSource,
    targetBodyLines = 100,
    body = """
        using System;
        using System.Collections.Generic;
        using System.Linq;

        namespace Benchmark.Library{{index}};

        [Flags]
        public enum NoteFlags{{index}}
        {
            None = 0,
            Pinned = 1,
            Dirty = 2,
            External = 4,
        }

        public enum NoteStatus{{index}}
        {
            Draft,
            Review,
            Published,
        }

        public sealed record Note{{index}}(
            string Id,
            string Title,
            string Path,
            int Words,
            NoteStatus{{index}} Status,
            NoteFlags{{index}} Flags,
            IReadOnlyList<string> Tags)
        {
            public int ReadingMinutes => Math.Max(1, (int)Math.Ceiling(Words / 220.0));
            public bool IsPinned => Flags.HasFlag(NoteFlags{{index}}.Pinned);
            public string Label => $"{Title} / {ReadingMinutes} min / {Status}";
        }

        public sealed class NoteQueue{{index}}
        {
            private readonly List<Note{{index}}> _notes;

            public NoteQueue{{index}}(IEnumerable<Note{{index}}> notes)
            {
                _notes = notes.ToList();
            }

            public IEnumerable<Note{{index}}> Select(NoteStatus{{index}} status)
            {
                return _notes
                    .Where(note => note.Status == status)
                    .OrderByDescending(note => note.IsPinned)
                    .ThenBy(note => note.ReadingMinutes)
                    .ThenBy(note => note.Title);
            }

            public string RenderReport(NoteStatus{{index}} status)
            {
                return string.Join(Environment.NewLine, Select(status).Select(note => note.Label));
            }

            public Note{{index}} UpdateStatus(string id, NoteStatus{{index}} status)
            {
                var index = _notes.FindIndex(note => note.Id == id);
                if (index < 0)
                {
                    throw new InvalidOperationException($"Note {id} was not found.");
                }
                var updated = _notes[index] with { Status = status };
                _notes[index] = updated;
                return updated;
            }

            public static NoteQueue{{index}} Sample() => new(new[]
            {
                new Note{{index}}(
                    "launch-{{index}}",
                    "Launch plan {{index}}",
                    "notes/launch-{{index}}.md",
                    1240 + {{index}},
                    NoteStatus{{index}}.Review,
                    NoteFlags{{index}}.Pinned,
                    new[] { "release", "review" }),
            });
        }
    """,
)
