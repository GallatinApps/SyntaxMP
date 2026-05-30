package com.gallatinapps.syntaxmp.benchmarks.fixtures.source

import com.gallatinapps.syntaxmp.benchmarks.fixtures.LanguageBenchmarkFamily
import com.gallatinapps.syntaxmp.benchmarks.fixtures.LanguageBenchmarkFixture

private val PythonRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "python",
    languageLabel = "python",
    displayName = "Python",
    family = LanguageBenchmarkFamily.ScriptLikeSource,
    targetBodyLines = 90,
    header = """
        from __future__ import annotations

        from dataclasses import dataclass, field
        from enum import StrEnum
        from pathlib import Path
        from typing import Iterable, Iterator, Protocol
        import re
    """,
    body = """
        class Status{{index}}(StrEnum):
            DRAFT = "draft"
            REVIEW = "review"
            PUBLISHED = "published"


        class NoteSource{{index}}(Protocol):
            def load(self, path: Path) -> str:
                ...


        @dataclass(frozen=True, slots=True)
        class Note{{index}}:
            title: str
            path: Path
            words: int
            pinned: bool = False
            status: Status{{index}} = Status{{index}}.DRAFT
            tags: tuple[str, ...] = field(default_factory=tuple)

            @property
            def reading_minutes(self) -> int:
                return max(1, round(self.words / 220))

            def label(self) -> str:
                mention = re.search(r"@(?P<name>\w+)", self.title)
                owner = mention.group("name") if mention else "local"
                return f"{self.title} / {self.reading_minutes} min / @{owner}"

            def with_status(self, status: Status{{index}}) -> "Note{{index}}":
                return Note{{index}}(
                    title=self.title.strip(),
                    path=self.path,
                    words=self.words,
                    pinned=self.pinned,
                    status=status,
                    tags=self.tags,
                )


        def load_notes{{index}}(source: NoteSource{{index}}, paths: Iterable[Path]) -> Iterator[Note{{index}}]:
            for path in paths:
                text = source.load(path)
                heading = next((line.removeprefix("# ").strip() for line in text.splitlines() if line.startswith("# ")), path.stem)
                tags = tuple(sorted({part for part in text.split() if part.startswith("#")}))
                yield Note{{index}}(
                    title=f"{heading} {{index}}",
                    path=path,
                    words=len(text.split()),
                    pinned="[pinned]" in text,
                    status=Status{{index}}.REVIEW if "[review]" in text else Status{{index}}.DRAFT,
                    tags=tags,
                )


        def queue_notes{{index}}(notes: Iterable[Note{{index}}], queue_mode: str) -> list[Note{{index}}]:
            match queue_mode:
                case "review":
                    filtered = [note for note in notes if note.status is Status{{index}}.REVIEW]
                case "pinned":
                    filtered = [note for note in notes if note.pinned]
                case _:
                    filtered = list(notes)

            return sorted(
                filtered,
                key=lambda note: (note.pinned, note.reading_minutes, note.title.casefold()),
                reverse=True,
            )


        def render_report{{index}}(notes: Iterable[Note{{index}}]) -> str:
            rows = []
            for position, note in enumerate(notes, start=1):
                rows.append(f"{position:02d}. {note.label()} -> {note.path.name}")
            if not rows:
                return "No notes ready for review"
            return "\n".join(rows)
    """,
)

private val RubyRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "ruby",
    languageLabel = "ruby",
    displayName = "Ruby",
    family = LanguageBenchmarkFamily.ScriptLikeSource,
    targetBodyLines = 90,
    body = """
        # frozen_string_literal: true

        module BenchmarkLibrary{{index}}
          Status = Struct.new(:draft, :review, :published, keyword_init: true)

          Note = Data.define(:id, :title, :path, :words, :pinned, :status, :tags) do
            def reading_minutes
              [(words / 220.0).ceil, 1].max
            end

            def label
              "#{title} / #{reading_minutes} min / #{status}"
            end
          end

          class Queue
            include Enumerable

            def initialize(notes)
              @notes = notes
            end

            def each(&block)
              @notes.each(&block)
            end

            def select_for(mode)
              @notes
                .select { |note| note.status == mode }
                .sort_by { |note| [note.pinned ? 0 : 1, -note.reading_minutes] }
            end

            def summarize(mode: :review)
              select_for(mode).map.with_index(1) do |note, position|
                "#{position}. #{note.label} -> #{note.path}"
              end
            end
          end

          module_function

          def build_notes(paths)
            paths.map.with_index(1) do |path, offset|
              Note.new(
                id: "note-{{index}}-#{offset}",
                title: File.basename(path, ".md").split("-").map(&:capitalize).join(" "),
                path: path,
                words: 900 + offset,
                pinned: offset.even?,
                status: offset.even? ? :review : :draft,
                tags: %w[release review benchmark]
              )
            end
          end

          def render_report(paths)
            queue = Queue.new(build_notes(paths))
            queue.summarize(mode: :review).join("\n")
          rescue StandardError => error
            warn "failed to render queue {{index}}: #{error.message}"
            ""
          end
        end
    """,
)

private val PhpRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "php",
    languageLabel = "php",
    displayName = "PHP",
    family = LanguageBenchmarkFamily.ScriptLikeSource,
    targetBodyLines = 90,
    body = """
        <?php

        declare(strict_types=1);

        namespace Benchmark\Library{{index}};

        enum NoteStatus{{index}}: string
        {
            case Draft = 'draft';
            case Review = 'review';
            case Published = 'published';
        }

        final readonly class Note{{index}}
        {
            public function __construct(
                public string ${'$'}id,
                public string ${'$'}title,
                public string ${'$'}path,
                public int ${'$'}words,
                public bool ${'$'}pinned,
                public NoteStatus{{index}} ${'$'}status,
                public array ${'$'}tags = [],
            ) {}

            public function readingMinutes(): int
            {
                return max(1, (int) ceil(${ '$' }this->words / 220));
            }

            public function label(): string
            {
                return sprintf('%s / %d min / %s', ${ '$' }this->title, ${ '$' }this->readingMinutes(), ${ '$' }this->status->value);
            }
        }

        final class NoteQueue{{index}}
        {
            /** @param list<Note{{index}}> ${'$'}notes */
            public function __construct(private array ${'$'}notes) {}

            /** @return list<Note{{index}}> */
            public function select(NoteStatus{{index}} ${'$'}status): array
            {
                ${'$'}filtered = array_filter(
                    ${ '$' }this->notes,
                    static fn (Note{{index}} ${'$'}note): bool => ${'$'}note->status === ${'$'}status,
                );
                usort(
                    ${'$'}filtered,
                    static fn (Note{{index}} ${'$'}left, Note{{index}} ${'$'}right): int =>
                        ((int) ${'$'}right->pinned <=> (int) ${'$'}left->pinned)
                            ?: (${ '$' }right->readingMinutes() <=> ${ '$' }left->readingMinutes()),
                );
                return array_values(${'$'}filtered);
            }

            public function report(): string
            {
                ${'$'}rows = array_map(
                    static fn (Note{{index}} ${'$'}note): string => ${'$'}note->label(),
                    ${ '$' }this->select(NoteStatus{{index}}::Review),
                );
                return implode(PHP_EOL, ${'$'}rows);
            }
        }
    """,
)

private val ShellRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "shell",
    languageLabel = "shell",
    displayName = "Shell",
    family = LanguageBenchmarkFamily.ScriptLikeSource,
    targetBodyLines = 90,
    body = """
        #!/usr/bin/env sh
        set -eu

        library_id="library-{{index}}"
        library_root="${'$'}{HOME}/Notes/Library{{index}}"
        report_dir="${'$'}{TMPDIR:-/tmp}/syntaxmp-${'$'}{library_id}"

        log_{{index}}() {
          printf '%s %s\n' "${'$'}(date -u +%Y-%m-%dT%H:%M:%SZ)" "${'$'}*"
        }

        ensure_report_dir_{{index}}() {
          if [ ! -d "${'$'}report_dir" ]; then
            mkdir -p "${'$'}report_dir"
          fi
        }

        scan_files_{{index}}() {
          find "${'$'}library_root" \
            -type f \
            \( -name '*.md' -o -name '*.markdown' -o -name '*.txt' \) \
            ! -path '*/.hashjot/*' \
            ! -path '*/archive/*' \
            -print
        }

        write_manifest_{{index}}() {
          manifest="${'$'}report_dir/manifest-${'$'}library_id.tsv"
          : > "${'$'}manifest"
          scan_files_{{index}} | while IFS= read -r file; do
            bytes="${'$'}(wc -c < "${'$'}file" | tr -d ' ')"
            title="${'$'}(sed -n '1s/^# *//p' "${'$'}file")"
            if [ -z "${'$'}title" ]; then
              title="${'$'}(basename "${'$'}file")"
            fi
            printf '%s\t%s\t%s\n' "${'$'}file" "${'$'}bytes" "${'$'}title" >> "${'$'}manifest"
          done
        }

        render_summary_{{index}}() {
          manifest="${'$'}report_dir/manifest-${'$'}library_id.tsv"
          awk -F '\t' '
            BEGIN { count = 0; bytes = 0 }
            { count += 1; bytes += ${'$'}2 }
            END { printf "files=%d bytes=%d\n", count, bytes }
          ' "${'$'}manifest"
        }

        print_usage_{{index}}() {
          cat <<'USAGE'
        usage: scan-library [root]
        writes a tab-separated manifest for editable text files
        USAGE
        }

        main_{{index}}() {
          trap 'log_{{index}} "scan interrupted"' INT TERM
          ensure_report_dir_{{index}}
          log_{{index}} "scanning ${'$'}library_root"
          write_manifest_{{index}}
          render_summary_{{index}}
        }

        main_{{index}} "${'$'}@"
    """,
)

private val PowerShellRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "powershell",
    languageLabel = "powershell",
    displayName = "PowerShell",
    family = LanguageBenchmarkFamily.ScriptLikeSource,
    targetBodyLines = 90,
    body = """
        Set-StrictMode -Version Latest
        ${'$'}ErrorActionPreference = "Stop"

        enum NoteStatus{{index}} {
            Draft
            Review
            Published
        }

        class Note{{index}} {
            [string] ${'$'}Id
            [string] ${'$'}Title
            [string] ${'$'}Path
            [int] ${'$'}Words
            [bool] ${'$'}Pinned
            [NoteStatus{{index}}] ${'$'}Status

            Note{{index}}([string] ${'$'}id, [string] ${'$'}title, [string] ${'$'}path, [int] ${'$'}words) {
                ${'$'}this.Id = ${'$'}id
                ${'$'}this.Title = ${'$'}title
                ${'$'}this.Path = ${'$'}path
                ${'$'}this.Words = ${'$'}words
                ${'$'}this.Pinned = ${'$'}false
                ${'$'}this.Status = [NoteStatus{{index}}]::Draft
            }

            [int] ReadingMinutes() {
                return [Math]::Max(1, [Math]::Ceiling(${ '$' }this.Words / 220))
            }

            [string] Label() {
                return "{0} / {1} min / {2}" -f ${ '$' }this.Title, ${ '$' }this.ReadingMinutes(), ${ '$' }this.Status
            }
        }

        function Get-LibraryNotes{{index}} {
            param(
                [Parameter(Mandatory)]
                [string] ${'$'}Root
            )

            Get-ChildItem -Path ${'$'}Root -Recurse -File -Include *.md,*.markdown,*.txt |
                Where-Object { ${'$'}_.FullName -notmatch "\\.hashjot|archive" } |
                ForEach-Object {
                    ${'$'}title = (Get-Content -Path ${'$'}_.FullName -TotalCount 1) -replace '^#\s*', ''
                    ${'$'}note = [Note{{index}}]::new(
                        "note-{{index}}-${'$'}(${'$'}_.BaseName)",
                        ${'$'}title,
                        ${'$'}_.FullName,
                        (Get-Content -Path ${'$'}_.FullName -Raw).Split().Count
                    )
                    ${'$'}note.Status = [NoteStatus{{index}}]::Review
                    ${'$'}note
                }
        }

        function Write-ReviewReport{{index}} {
            param([Note{{index}}[]] ${'$'}Notes)

            ${'$'}Notes |
                Where-Object Status -eq ([NoteStatus{{index}}]::Review) |
                Sort-Object -Property @{ Expression = "Pinned"; Descending = ${'$'}true }, Words |
                ForEach-Object { ${'$'}_.Label() }
        }
    """,
)

internal val ScriptLikeFixtures: List<LanguageBenchmarkFixture> =
    listOf(
        PythonRepresentativeFixture,
        RubyRepresentativeFixture,
        PhpRepresentativeFixture,
        ShellRepresentativeFixture,
        PowerShellRepresentativeFixture,
    )
