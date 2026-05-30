package com.gallatinapps.syntaxmp.benchmarks.fixtures.source

import com.gallatinapps.syntaxmp.benchmarks.fixtures.LanguageBenchmarkFamily
import com.gallatinapps.syntaxmp.benchmarks.fixtures.LanguageBenchmarkFixture

// General-source fixtures for dynamic scripting languages (Lua, R, Elixir, Perl). Grouped by
// runtime category only to keep files manageable; all are aggregated in GeneralSourceFixtures.

internal val LuaRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "lua",
    languageLabel = "lua",
    displayName = "Lua",
    family = LanguageBenchmarkFamily.GeneralSource,
    targetBodyLines = 100,
    body = """
        local NoteStatus{{index}} = {
          draft = "draft",
          review = "review",
          published = "published",
        }

        local Note{{index}} = {}
        Note{{index}}.__index = Note{{index}}

        function Note{{index}}.new(fields)
          return setmetatable({
            id = fields.id,
            title = fields.title,
            path = fields.path,
            words = fields.words or 0,
            pinned = fields.pinned == true,
            status = fields.status or NoteStatus{{index}}.draft,
            tags = fields.tags or {},
          }, Note{{index}})
        end

        function Note{{index}}:reading_minutes()
          return math.max(1, math.ceil(self.words / 220))
        end

        function Note{{index}}:label()
          return string.format("%s / %d min / %s", self.title, self:reading_minutes(), self.status)
        end

        local NoteQueue{{index}} = {}
        NoteQueue{{index}}.__index = NoteQueue{{index}}

        function NoteQueue{{index}}.new(notes)
          return setmetatable({ notes = notes or {} }, NoteQueue{{index}})
        end

        function NoteQueue{{index}}:select(status)
          local selected = {}
          for _, note in ipairs(self.notes) do
            if note.status == status then
              table.insert(selected, note)
            end
          end
          table.sort(selected, function(left, right)
            if left.pinned ~= right.pinned then
              return left.pinned
            end
            return left:reading_minutes() < right:reading_minutes()
          end)
          return selected
        end

        function NoteQueue{{index}}:render_report(status)
          local rows = {}
          for position, note in ipairs(self:select(status or NoteStatus{{index}}.review)) do
            rows[position] = string.format("%d. %s", position, note:label())
          end
          return table.concat(rows, "\n")
        end

        return NoteQueue{{index}}.new({
          Note{{index}}.new({
            id = "launch-{{index}}",
            title = "Launch plan {{index}}",
            path = "notes/launch-{{index}}.md",
            words = 1240 + {{index}},
            pinned = true,
            status = NoteStatus{{index}}.review,
            tags = { "release", "review" },
          }),
        })
    """,
)

internal val RRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "r",
    languageLabel = "r",
    displayName = "R",
    family = LanguageBenchmarkFamily.GeneralSource,
    targetBodyLines = 100,
    body = """
        library(dplyr)
        library(stringr)
        library(tibble)

        note_status_{{index}} <- c("draft", "review", "published")

        make_note_{{index}} <- function(id, title, path, words, pinned, status, tags = character()) {
          tibble(
            id = id,
            title = title,
            path = path,
            words = words,
            pinned = pinned,
            status = factor(status, levels = note_status_{{index}}),
            tags = list(tags)
          )
        }

        reading_minutes_{{index}} <- function(words) {
          pmax(1L, ceiling(words / 220))
        }

        label_note_{{index}} <- function(note) {
          str_glue("{note${'$'}title} / {reading_minutes_{{index}}(note${'$'}words)} min / {note${'$'}status}")
        }

        select_notes_{{index}} <- function(notes, status = "review") {
          notes |>
            filter(.data${'$'}status == status) |>
            mutate(reading_minutes = reading_minutes_{{index}}(.data${'$'}words)) |>
            arrange(desc(.data${'$'}pinned), .data${'$'}reading_minutes, .data${'$'}title)
        }

        first_pinned_{{index}} <- function(notes) {
          notes |>
            filter(.data${'$'}pinned) |>
            arrange(.data${'$'}title) |>
            slice_head(n = 1)
        }

        summarize_queue_{{index}} <- function(notes) {
          selected <- select_notes_{{index}}(notes, "review")
          selected |>
            mutate(
              position = row_number(),
              label = str_glue("{position}. {title} / {reading_minutes} min")
            ) |>
            select(.data${'$'}id, .data${'$'}label, .data${'$'}path)
        }

        notes_{{index}} <- bind_rows(
          make_note_{{index}}(
            "launch-{{index}}",
            "Launch plan {{index}}",
            "notes/launch-{{index}}.md",
            1240 + {{index}},
            TRUE,
            "review",
            c("release", "review")
          ),
          make_note_{{index}}(
            "sync-{{index}}",
            "Sync notes {{index}}",
            "notes/sync-{{index}}.md",
            820,
            FALSE,
            "draft",
            c("sync")
          )
        )
    """,
)

internal val ElixirRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "elixir",
    languageLabel = "elixir",
    displayName = "Elixir",
    family = LanguageBenchmarkFamily.GeneralSource,
    targetBodyLines = 100,
    body = """
        defmodule Benchmark.Library{{index}} do
          @moduledoc "Representative note queue fixture {{index}}."

          @statuses [:draft, :review, :published]

          defmodule Note do
            @enforce_keys [:id, :title, :path, :words, :status]
            defstruct [:id, :title, :path, :words, :status, pinned: false, tags: []]

            def reading_minutes(%__MODULE__{words: words}) do
              max(1, ceil(words / 220))
            end

            def label(%__MODULE__{} = note) do
              "#{note.title} / #{reading_minutes(note)} min / #{note.status}"
            end
          end

          def statuses, do: @statuses

          def sample_notes do
            [
              %Note{
                id: "launch-{{index}}",
                title: "Launch plan {{index}}",
                path: "notes/launch-{{index}}.md",
                words: 1240 + {{index}},
                pinned: true,
                status: :review,
                tags: ["release", "review"]
              },
              %Note{
                id: "sync-{{index}}",
                title: "Sync notes {{index}}",
                path: "notes/sync-{{index}}.md",
                words: 820,
                pinned: false,
                status: :draft,
                tags: ["sync"]
              }
            ]
          end

          def select(notes, status \\ :review) do
            notes
            |> Enum.filter(&(&1.status == status))
            |> Enum.sort_by(fn note -> {not note.pinned, Note.reading_minutes(note), note.title} end)
          end

          def render_report(notes, status \\ :review) do
            notes
            |> select(status)
            |> Enum.with_index(1)
            |> Enum.map(fn {note, position} -> "#{position}. #{Note.label(note)}" end)
            |> Enum.join("\n")
          end

          def first_pinned(notes) do
            Enum.find(notes, & &1.pinned)
          end

          def count_by_status(notes) do
            Enum.frequencies_by(notes, & &1.status)
          end

          def update_status(notes, id, status) when status in @statuses do
            Enum.map(notes, fn
              %Note{id: ^id} = note -> %{note | status: status}
              note -> note
            end)
          end
        end
    """,
)

internal val PerlRepresentativeFixture = LanguageBenchmarkFixture(
    fixtureId = "perl",
    languageLabel = "perl",
    displayName = "Perl",
    family = LanguageBenchmarkFamily.GeneralSource,
    targetBodyLines = 100,
    body = """
        use strict;
        use warnings;
        use feature qw(say signatures);
        no warnings qw(experimental::signatures);

        package Benchmark::Library{{index}};

        sub reading_minutes_{{index}} (${'$'}note) {
            my ${'$'}minutes = int((${ '$' }note->{words} + 219) / 220);
            return ${'$'}minutes > 0 ? ${'$'}minutes : 1;
        }

        sub label_note_{{index}} (${'$'}note) {
            return sprintf(
                '%s / %d min / %s',
                ${ '$' }note->{title},
                reading_minutes_{{index}}(${ '$' }note),
                ${ '$' }note->{status}
            );
        }

        sub select_notes_{{index}} (${'$'}notes, ${'$'}status = 'review') {
            my @selected = grep { ${ '$' }_->{status} eq ${'$'}status } @{${ '$' }notes};
            return sort {
                (${ '$' }b->{pinned} <=> ${ '$' }a->{pinned})
                    || (reading_minutes_{{index}}(${ '$' }a) <=> reading_minutes_{{index}}(${ '$' }b))
                    || (${ '$' }a->{title} cmp ${ '$' }b->{title})
            } @selected;
        }

        sub render_report_{{index}} (${'$'}notes) {
            my @rows;
            my ${'$'}position = 1;
            for my ${'$'}note (select_notes_{{index}}(${ '$' }notes, 'review')) {
                push @rows, sprintf('%d. %s', ${'$'}position++, label_note_{{index}}(${ '$' }note));
            }
            return join "\n", @rows;
        }

        sub first_pinned_{{index}} (${'$'}notes) {
            for my ${'$'}note (@{${ '$' }notes}) {
                return ${'$'}note if ${ '$' }note->{pinned};
            }
            return undef;
        }

        sub count_by_status_{{index}} (${'$'}notes) {
            my %counts;
            ${'$'}counts{${ '$' }_->{status}}++ for @{${ '$' }notes};
            return \%counts;
        }

        sub sample_notes_{{index}} () {
            return [
                {
                    id => 'launch-{{index}}',
                    title => 'Launch plan {{index}}',
                    path => 'notes/launch-{{index}}.md',
                    words => 1240 + {{index}},
                    pinned => 1,
                    status => 'review',
                    tags => [qw(release review)],
                },
                {
                    id => 'sync-{{index}}',
                    title => 'Sync notes {{index}}',
                    path => 'notes/sync-{{index}}.md',
                    words => 820,
                    pinned => 0,
                    status => 'draft',
                    tags => [qw(sync)],
                },
            ];
        }

        1;
    """,
)
