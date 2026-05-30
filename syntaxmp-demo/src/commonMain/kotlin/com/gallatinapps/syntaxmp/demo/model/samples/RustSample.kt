package com.gallatinapps.syntaxmp.demo.model.samples

internal val RustSample = """
    #[derive(Debug, Clone, Copy, PartialEq, Eq)]
    enum Status {
        Draft,
        Review,
        Published,
    }

    #[derive(Debug, Clone)]
    struct Note<'a> {
        title: &'a str,
        words: usize,
        pinned: bool,
        status: Status,
    }

    impl<'a> Note<'a> {
        fn reading_minutes(&self) -> usize {
            (self.words + 219) / 220
        }

        fn label(&self) -> String {
            format!("{} • {} min • {:?}", self.title, self.reading_minutes(), self.status)
        }
    }

    fn priority(note: &Note<'_>) -> usize {
        let pin_boost = if note.pinned { 10 } else { 0 };
        pin_boost + note.reading_minutes()
    }

    let mut queue = notes.clone();
    queue.retain(|note| matches!(note.status, Status::Draft | Status::Review));
    queue.sort_by_key(|note| std::cmp::Reverse(priority(note)));
""".trimIndent()
