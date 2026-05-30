package com.gallatinapps.syntaxmp.demo.model.samples

internal val ScalaSample = """
    enum NoteStatus:
      case Draft, Review, Published

    final case class Note(title: String, words: Int, pinned: Boolean, status: NoteStatus):
      def minutes: Int = math.max(1, math.ceil(words.toDouble / 220).toInt)
      def label: String = s"${'$'}title • ${'$'}minutes min • ${'$'}status"

    object ReadingQueue:
      def priority(note: Note): Int =
        val pinBoost = if note.pinned then 10 else 0
        pinBoost + note.minutes

      def top(notes: List[Note]): List[Note] =
        notes
          .collect { case note if note.status != NoteStatus.Published => note }
          .sortBy(note => -priority(note))
          .take(5)

      val queue = ReadingQueue.top(
        List(Note("Launch plan", 1240, pinned = true, status = NoteStatus.Review))
      )
""".trimIndent()
