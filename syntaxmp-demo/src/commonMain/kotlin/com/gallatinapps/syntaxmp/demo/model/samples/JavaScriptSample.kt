package com.gallatinapps.syntaxmp.demo.model.samples

internal val JavaScriptSample = """
    const formatter = new Intl.RelativeTimeFormat("en", { numeric: "auto" });
    const mentionPattern = /(^|\s)@(?<name>[a-z0-9_]+)/gi;

    class ReadingQueue extends Array {
      get pinnedCount() {
        return this.filter((note) => note.pinned === true).length;
      }
    }

    export function describeUpdate(note) {
      const minutes = Math.round((Date.now() - note.updatedAt) / 60000);
      const mentions = [...note.title.matchAll(mentionPattern)]
        .map((match) => match.groups?.name ?? "unknown");
      return `${'$'}{note.title} • ${'$'}{formatter.format(-minutes, "minute")} • @${'$'}{mentions[0] ?? "none"}`;
    }

    export function renderQueue(notes = []) {
      return ReadingQueue.from(notes)
        .filter((note) => !note.archived)
        .sort((a, b) => Number(b.pinned) - Number(a.pinned))
        .map((note) => ({
          ...note,
          readingMinutes: Math.max(1, Math.ceil(note.words / 220)),
        }));
    }
""".trimIndent()
