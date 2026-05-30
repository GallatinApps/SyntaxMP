package com.gallatinapps.syntaxmp.demo.model.samples

internal val PythonSample = """
    from dataclasses import dataclass
    from enum import StrEnum
    from pathlib import Path
    import re

    class Status(StrEnum):
        DRAFT = "draft"
        REVIEW = "review"
        PUBLISHED = "published"

    @dataclass(frozen=True)
    class Note:
        title: str
        path: Path
        words: int
        pinned: bool = False
        status: Status = Status.DRAFT

        @property
        def reading_minutes(self) -> int:
            return max(1, round(self.words / 220))

        def label(self) -> str:
            mention = re.search(r"@(?P<name>\w+)", self.title)
            owner = mention.group("name") if mention else "local"
            return f"{self.title} • {self.reading_minutes} min • @{owner}"

    match queue_mode:
        case "review":
            notes = [note for note in notes if note.status is Status.REVIEW]
        case _:
            notes = list(notes)

    queue = sorted(
        notes,
        key=lambda note: (note.pinned, note.reading_minutes),
        reverse=True,
    )
""".trimIndent()
