package com.gallatinapps.syntaxmp.demo.model.samples

internal val SvelteSample = """
    <script lang="ts">
      import { createEventDispatcher } from "svelte";

      export let title: string;
      export let notes: Array<{
        id: string;
        title: string;
        words: number;
        status: "draft" | "review" | "published";
        pinned?: boolean;
      }>;

      const dispatch = createEventDispatcher<{ open: string }>();
      let showPinnedOnly = false;
      ${'$'}: visibleNotes = showPinnedOnly
        ? notes.filter((note) => note.pinned)
        : notes;
      ${'$'}: heading = `${'$'}{title} (${'$'}{visibleNotes.length})`;

      const readingMinutes = (words: number) => Math.max(1, Math.ceil(words / 220));
    </script>

    <section class="note-queue">
      <header>
        <h1>{heading}</h1>
        <button type="button" on:click={() => (showPinnedOnly = !showPinnedOnly)}>
          {showPinnedOnly ? "Show all" : "Pinned only"}
        </button>
      </header>

      {#each visibleNotes as note (note.id)}
        <article class:pinned={note.pinned} data-status={note.status} on:click={() => dispatch("open", note.id)}>
          <h2>{note.title}</h2>
          <p>{readingMinutes(note.words)} min read</p>
        </article>
      {/each}
    </section>

    <style>
      .note-queue { display: grid; gap: 1rem; }
      .pinned { border-left: 4px solid #2563eb; }
    </style>
""".trimIndent()
