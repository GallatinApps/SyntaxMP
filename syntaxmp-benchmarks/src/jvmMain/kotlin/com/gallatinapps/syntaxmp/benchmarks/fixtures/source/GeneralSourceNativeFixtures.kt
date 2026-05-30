package com.gallatinapps.syntaxmp.benchmarks.fixtures.source

import com.gallatinapps.syntaxmp.benchmarks.fixtures.LanguageBenchmarkFamily
import com.gallatinapps.syntaxmp.benchmarks.fixtures.LanguageBenchmarkFixture

// General-source fixtures for compiled-to-native languages (C, C++, Swift, Go, Rust, Dart). Grouped by runtime category only to keep files manageable; all are aggregated in
// GeneralSourceFixtures.

internal val CRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "c",
    languageLabel = "c",
    displayName = "C",
    family = LanguageBenchmarkFamily.GeneralSource,
    targetBodyLines = 100,
    body = """
        #include <stdbool.h>
        #include <stdio.h>
        #include <stdlib.h>
        #include <string.h>

        typedef enum note_status_{{index}} {
            NOTE_STATUS_DRAFT_{{index}},
            NOTE_STATUS_REVIEW_{{index}},
            NOTE_STATUS_PUBLISHED_{{index}}
        } note_status_{{index}};

        typedef struct note_{{index}} {
            const char *id;
            const char *title;
            const char *path;
            int words;
            bool pinned;
            note_status_{{index}} status;
        } note_{{index}};

        static int reading_minutes_{{index}}(const note_{{index}} *note) {
            int minutes = (note->words + 219) / 220;
            return minutes > 0 ? minutes : 1;
        }

        static const char *status_label_{{index}}(note_status_{{index}} status) {
            switch (status) {
                case NOTE_STATUS_DRAFT_{{index}}:
                    return "draft";
                case NOTE_STATUS_REVIEW_{{index}}:
                    return "review";
                case NOTE_STATUS_PUBLISHED_{{index}}:
                    return "published";
                default:
                    return "unknown";
            }
        }

        static void render_note_{{index}}(const note_{{index}} *note, char *buffer, size_t length) {
            snprintf(
                buffer,
                length,
                "%s / %d min / %s",
                note->title,
                reading_minutes_{{index}}(note),
                status_label_{{index}}(note->status)
            );
        }

        static size_t select_review_notes_{{index}}(
            const note_{{index}} *notes,
            size_t count,
            note_{{index}} *out_notes,
            size_t capacity
        ) {
            size_t written = 0;
            for (size_t i = 0; i < count && written < capacity; ++i) {
                if (notes[i].status == NOTE_STATUS_REVIEW_{{index}}) {
                    out_notes[written++] = notes[i];
                }
            }
            return written;
        }

        int benchmark_c_fixture_{{index}}(void) {
            note_{{index}} notes[] = {
                { "launch-{{index}}", "Launch plan {{index}}", "notes/launch.md", 1240 + {{index}}, true, NOTE_STATUS_REVIEW_{{index}} },
                { "sync-{{index}}", "Sync notes {{index}}", "notes/sync.md", 820, false, NOTE_STATUS_DRAFT_{{index}} }
            };
            note_{{index}} selected[4];
            char line[128];
            size_t count = select_review_notes_{{index}}(notes, 2, selected, 4);
            if (count > 0) {
                render_note_{{index}}(&selected[0], line, sizeof(line));
                puts(line);
            }
            return (int) count;
        }
    """,
)

internal val CppRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "cpp",
    languageLabel = "cpp",
    displayName = "C++",
    family = LanguageBenchmarkFamily.GeneralSource,
    targetBodyLines = 100,
    body = """
        #include <algorithm>
        #include <chrono>
        #include <format>
        #include <optional>
        #include <string>
        #include <string_view>
        #include <vector>

        namespace benchmark::library_{{index}} {

        enum class NoteStatus{{index}} {
            Draft,
            Review,
            Published,
        };

        struct Note{{index}} {
            std::string id;
            std::string title;
            std::string path;
            int words = 0;
            bool pinned = false;
            NoteStatus{{index}} status = NoteStatus{{index}}::Draft;
            std::vector<std::string> tags;

            [[nodiscard]] int readingMinutes() const {
                return std::max(1, (words + 219) / 220);
            }

            [[nodiscard]] std::string label() const {
                return std::format("{} / {} min", title, readingMinutes());
            }
        };

        class NoteQueue{{index}} final {
        public:
            explicit NoteQueue{{index}}(std::vector<Note{{index}}> notes)
                : notes_(std::move(notes)) {}

            [[nodiscard]] std::vector<Note{{index}}> select(NoteStatus{{index}} status) const {
                std::vector<Note{{index}}> result;
                std::copy_if(notes_.begin(), notes_.end(), std::back_inserter(result), [status](const auto& note) {
                    return note.status == status;
                });
                std::ranges::sort(result, {}, [](const auto& note) {
                    return std::tuple(!note.pinned, note.readingMinutes(), note.title);
                });
                return result;
            }

            [[nodiscard]] std::optional<Note{{index}}> firstPinned() const {
                auto iter = std::ranges::find_if(notes_, [](const auto& note) { return note.pinned; });
                if (iter == notes_.end()) {
                    return std::nullopt;
                }
                return *iter;
            }

        private:
            std::vector<Note{{index}}> notes_;
        };

        NoteQueue{{index}} sampleQueue{{index}}() {
            return NoteQueue{{index}}({
                Note{{index}}{
                    .id = "launch-{{index}}",
                    .title = "Launch plan {{index}}",
                    .path = "notes/launch-{{index}}.md",
                    .words = 1240 + {{index}},
                    .pinned = true,
                    .status = NoteStatus{{index}}::Review,
                    .tags = {"release", "review"},
                },
            });
        }

        } // namespace benchmark::library_{{index}}
    """,
)


internal val SwiftRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "swift",
    languageLabel = "swift",
    displayName = "Swift",
    family = LanguageBenchmarkFamily.GeneralSource,
    targetBodyLines = 100,
    body = """
        import Foundation

        enum NoteStatus{{index}}: String, Codable, CaseIterable {
            case draft
            case review
            case published
        }

        struct Note{{index}}<Metadata: Codable>: Identifiable, Codable, Hashable {
            let id: String
            var title: String
            var path: String
            var words: Int
            var pinned: Bool
            var status: NoteStatus{{index}}
            var tags: [String]
            var metadata: Metadata

            var readingMinutes: Int {
                max(1, Int(ceil(Double(words) / 220.0)))
            }

            var label: String {
                "\(title) / \(readingMinutes) min / \(status.rawValue)"
            }

            func updating(status: NoteStatus{{index}}) -> Self {
                var copy = self
                copy.status = status
                return copy
            }
        }

        actor NoteQueue{{index}}<Metadata: Codable> {
            private var notes: [Note{{index}}<Metadata>]

            init(notes: [Note{{index}}<Metadata>]) {
                self.notes = notes
            }

            func select(status: NoteStatus{{index}}) -> [Note{{index}}<Metadata>] {
                notes
                    .filter { $0.status == status }
                    .sorted {
                        if $0.pinned != $1.pinned {
                            return $0.pinned && !$1.pinned
                        }
                        return $0.readingMinutes < $1.readingMinutes
                    }
            }

            func update(id: String, status: NoteStatus{{index}}) {
                guard let index = notes.firstIndex(where: { $0.id == id }) else {
                    return
                }
                notes[index] = notes[index].updating(status: status)
            }

            func report(status: NoteStatus{{index}} = .review) -> String {
                select(status: status)
                    .map(\.label)
                    .joined(separator: "\n")
            }

            func firstPinned() -> Note{{index}}<Metadata>? {
                notes.first(where: { $0.pinned })
            }

            func replaceAll(with newNotes: [Note{{index}}<Metadata>]) {
                notes = newNotes.sorted {
                    $0.title.localizedCaseInsensitiveCompare($1.title) == .orderedAscending
                }
            }
        }
    """,
)

internal val GoRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "go",
    languageLabel = "go",
    displayName = "Go",
    family = LanguageBenchmarkFamily.GeneralSource,
    targetBodyLines = 100,
    body = """
        package library{{index}}

        import (
            "fmt"
            "math"
            "sort"
            "strings"
            "time"
        )

        type NoteStatus{{index}} string

        const (
            StatusDraft{{index}}     NoteStatus{{index}} = "draft"
            StatusReview{{index}}    NoteStatus{{index}} = "review"
            StatusPublished{{index}} NoteStatus{{index}} = "published"
        )

        type Note{{index}} struct {
            ID        string
            Title     string
            Path      string
            Words     int
            Pinned    bool
            Status    NoteStatus{{index}}
            Tags      []string
            UpdatedAt time.Time
        }

        func (note Note{{index}}) ReadingMinutes() int {
            return int(math.Max(1, math.Ceil(float64(note.Words)/220.0)))
        }

        func (note Note{{index}}) Label() string {
            return fmt.Sprintf("%s / %d min / %s", note.Title, note.ReadingMinutes(), note.Status)
        }

        type NoteQueue{{index}} struct {
            notes []Note{{index}}
        }

        func NewNoteQueue{{index}}(notes []Note{{index}}) *NoteQueue{{index}} {
            copied := append([]Note{{index}}{}, notes...)
            return &NoteQueue{{index}}{notes: copied}
        }

        func (queue *NoteQueue{{index}}) Select(status NoteStatus{{index}}) []Note{{index}} {
            result := make([]Note{{index}}, 0, len(queue.notes))
            for _, note := range queue.notes {
                if note.Status == status {
                    result = append(result, note)
                }
            }
            sort.Slice(result, func(left, right int) bool {
                if result[left].Pinned != result[right].Pinned {
                    return result[left].Pinned
                }
                return result[left].ReadingMinutes() < result[right].ReadingMinutes()
            })
            return result
        }

        func (queue *NoteQueue{{index}}) RenderReport(status NoteStatus{{index}}) string {
            rows := make([]string, 0)
            for _, note := range queue.Select(status) {
                rows = append(rows, note.Label())
            }
            return strings.Join(rows, "\n")
        }

        func SampleQueue{{index}}() *NoteQueue{{index}} {
            return NewNoteQueue{{index}}([]Note{{index}}{
                {ID: "launch-{{index}}", Title: "Launch plan {{index}}", Path: "notes/launch.md", Words: 1240, Pinned: true, Status: StatusReview{{index}}},
            })
        }
    """,
)

internal val RustRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "rust",
    languageLabel = "rust",
    displayName = "Rust",
    family = LanguageBenchmarkFamily.GeneralSource,
    targetBodyLines = 100,
    body = """
        use std::cmp::Ordering;
        use std::fmt;
        use std::path::PathBuf;

        #[derive(Debug, Clone, Copy, PartialEq, Eq)]
        pub enum NoteStatus{{index}} {
            Draft,
            Review,
            Published,
        }

        #[derive(Debug, Clone, PartialEq, Eq)]
        pub struct Note{{index}} {
            pub id: String,
            pub title: String,
            pub path: PathBuf,
            pub words: usize,
            pub pinned: bool,
            pub status: NoteStatus{{index}},
            pub tags: Vec<String>,
        }

        impl Note{{index}} {
            pub fn reading_minutes(&self) -> usize {
                ((self.words + 219) / 220).max(1)
            }

            pub fn label(&self) -> String {
                format!("{} / {} min / {:?}", self.title, self.reading_minutes(), self.status)
            }

            pub fn with_status(mut self, status: NoteStatus{{index}}) -> Self {
                self.status = status;
                self
            }
        }

        pub struct NoteQueue{{index}} {
            notes: Vec<Note{{index}}>,
        }

        impl NoteQueue{{index}} {
            pub fn new(notes: impl IntoIterator<Item = Note{{index}}>) -> Self {
                Self { notes: notes.into_iter().collect() }
            }

            pub fn select(&self, status: NoteStatus{{index}}) -> Vec<&Note{{index}}> {
                let mut selected: Vec<_> = self.notes.iter()
                    .filter(|note| note.status == status)
                    .collect();
                selected.sort_by(|left, right| match right.pinned.cmp(&left.pinned) {
                    Ordering::Equal => left.reading_minutes().cmp(&right.reading_minutes()),
                    other => other,
                });
                selected
            }

            pub fn render_report(&self, status: NoteStatus{{index}}) -> String {
                self.select(status)
                    .into_iter()
                    .map(Note{{index}}::label)
                    .collect::<Vec<_>>()
                    .join("\n")
            }
        }

        impl fmt::Display for NoteStatus{{index}} {
            fn fmt(&self, formatter: &mut fmt::Formatter<'_>) -> fmt::Result {
                write!(formatter, "{:?}", self)
            }
        }
    """,
)

internal val DartRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "dart",
    languageLabel = "dart",
    displayName = "Dart",
    family = LanguageBenchmarkFamily.GeneralSource,
    targetBodyLines = 100,
    body = """
        import 'dart:math' as math;

        enum NoteStatus{{index}} {
          draft,
          review,
          published,
        }

        class Note{{index}} {
          const Note{{index}}({
            required this.id,
            required this.title,
            required this.path,
            required this.words,
            required this.pinned,
            required this.status,
            this.tags = const [],
          });

          final String id;
          final String title;
          final String path;
          final int words;
          final bool pinned;
          final NoteStatus{{index}} status;
          final List<String> tags;

          int get readingMinutes => math.max(1, (words / 220).ceil());

          String get label => title + ' / ' + readingMinutes.toString() + ' min';

          Note{{index}} copyWith({
            String? title,
            bool? pinned,
            NoteStatus{{index}}? status,
          }) {
            return Note{{index}}(
              id: id,
              title: title ?? this.title,
              path: path,
              words: words,
              pinned: pinned ?? this.pinned,
              status: status ?? this.status,
              tags: tags,
            );
          }
        }

        class NoteQueue{{index}} {
          NoteQueue{{index}}(Iterable<Note{{index}}> notes) : _notes = List.of(notes);

          final List<Note{{index}}> _notes;

          List<Note{{index}}> select(NoteStatus{{index}} status) {
            final selected = _notes.where((note) => note.status == status).toList();
            selected.sort((left, right) {
              final pinned = right.pinned.toString().compareTo(left.pinned.toString());
              if (pinned != 0) return pinned;
              return left.readingMinutes.compareTo(right.readingMinutes);
            });
            return selected;
          }

          String renderReport([NoteStatus{{index}} status = NoteStatus{{index}}.review]) {
            return select(status).map((note) => note.label).join('\n');
          }

          Note{{index}}? firstPinned() {
            for (final note in _notes) {
              if (note.pinned) return note;
            }
            return null;
          }

          void replaceAll(Iterable<Note{{index}}> notes) {
            _notes
              ..clear()
              ..addAll(notes);
          }
        }
    """,
)
