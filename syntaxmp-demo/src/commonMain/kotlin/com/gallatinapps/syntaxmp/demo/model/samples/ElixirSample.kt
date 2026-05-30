package com.gallatinapps.syntaxmp.demo.model.samples

internal val ElixirSample = """
    defmodule NoteKit.LibraryIndex do
      @moduledoc "Scores notes for the recent list."
      @statuses ~w(draft review published)a

      def rank(%{pinned: true, words: words}), do: minutes(words) + 10
      def rank(%{words: words}), do: minutes(words)

      def label(%{title: title, status: status, words: words}) when status in @statuses do
        "#{title} • #{minutes(words)} min • #{status}"
      end

      defp minutes(words) when words > 0 do
        words
        |> Kernel./(220)
        |> Float.ceil()
        |> trunc()
      end
    end

    notes
    |> Enum.reject(&String.match?(&1.title, ~r/archive/i))
    |> Enum.sort_by(&NoteKit.LibraryIndex.rank/1, :desc)
    |> Enum.map(&NoteKit.LibraryIndex.label/1)
""".trimIndent()
