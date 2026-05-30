package com.gallatinapps.syntaxmp.demo.model.samples

internal val PhpSample = """
    <?php

    enum NoteStatus: string {
        case Draft = 'draft';
        case Review = 'review';
        case Published = 'published';
    }

    #[Attribute]
    final class NoteSummary
    {
        public function __construct(
            public readonly string ${'$'}title,
            public readonly int ${'$'}words,
            public readonly bool ${'$'}pinned = false,
            public readonly NoteStatus ${'$'}status = NoteStatus::Draft,
        ) {}

        public function readingMinutes(): int
        {
            return max(1, (int) ceil(${'$'}this->words / 220));
        }

        public function label(): string
        {
            return "${'$'}this->title • {${'$'}this->readingMinutes()} min • {${'$'}this->status->value}";
        }
    }

    ${'$'}queue = array_filter(
        ${'$'}notes,
        fn (NoteSummary ${'$'}note) => !str_contains(${'$'}note->title, 'Archive')
    );

    foreach (${'$'}queue as ${'$'}note) {
        echo ${'$'}note->label() . PHP_EOL;
    }
""".trimIndent()
