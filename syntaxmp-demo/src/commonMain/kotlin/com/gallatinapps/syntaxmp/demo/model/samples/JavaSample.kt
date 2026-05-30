package com.gallatinapps.syntaxmp.demo.model.samples

internal val JavaSample = """
    package demo;

    import java.time.Instant;
    import java.util.Comparator;
    import java.util.List;
    import java.util.Optional;

    @Deprecated(forRemoval = false)
    public record NoteSummary(
        String title,
        int words,
        boolean pinned,
        Instant updatedAt,
        Status status
    ) {
        enum Status { DRAFT, REVIEW, PUBLISHED }

        int readingMinutes() {
            return Math.max(1, (words + 219) / 220);
        }

        String label() {
            return "%s • %d min • %s".formatted(title, readingMinutes(), status);
        }

        Optional<String> warning() {
            return words > 20_000 ? Optional.of("Large note: " + title) : Optional.empty();
        }

        static List<NoteSummary> recentFirst(List<NoteSummary> notes) {
            return notes.stream()
                .filter(note -> note.status() != Status.PUBLISHED || note.pinned())
                .sorted(Comparator.comparing(NoteSummary::updatedAt).reversed())
                .toList();
        }
    }
""".trimIndent()
