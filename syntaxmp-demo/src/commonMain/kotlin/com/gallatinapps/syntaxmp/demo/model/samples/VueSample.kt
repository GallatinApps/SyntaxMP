package com.gallatinapps.syntaxmp.demo.model.samples

internal val VueSample = """
    <template>
      <section class="note-queue">
        <header>
          <h1>{{ title }}</h1>
          <button type="button" :aria-pressed="showPinnedOnly" @click="showPinnedOnly = !showPinnedOnly">
            {{ showPinnedOnly ? "Show all" : "Pinned only" }}
          </button>
        </header>

        <article
          v-for="note in visibleNotes"
          :key="note.id"
          :data-pinned="note.pinned"
          :class="{ review: note.status === 'review' }"
          @click="emit('open', note.id)"
        >
          <h2>{{ note.title }}</h2>
          <p>{{ `${'$'}{readingMinutes(note.words)} min read` }}</p>
          <slot name="meta" :note="note" />
        </article>
      </section>
    </template>

    <script setup lang="ts">
    import { computed, ref } from "vue";

    type NoteSummary = { id: string; title: string; words: number; status: "draft" | "review"; pinned?: boolean };

    const props = defineProps<{ title: string; notes: NoteSummary[] }>();
    const emit = defineEmits<{ open: [id: string] }>();
    const showPinnedOnly = ref(false);
    const visibleNotes = computed(() =>
      showPinnedOnly.value ? props.notes.filter((note) => note.pinned) : props.notes
    );
    const readingMinutes = (words: number) => Math.max(1, Math.ceil(words / 220));
    </script>

    <style scoped>
    .note-queue {
      display: grid;
        gap: 1rem;
      }
      article[data-pinned="true"],
      article.review {
        border-left: 4px solid #2563eb;
      }
    </style>
""".trimIndent()
