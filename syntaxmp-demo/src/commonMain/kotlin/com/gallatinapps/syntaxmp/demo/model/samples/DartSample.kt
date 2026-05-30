package com.gallatinapps.syntaxmp.demo.model.samples

internal val DartSample = """
    import 'package:flutter/material.dart';

    enum NoteStatus { draft, review, published }

    class ReadingProgressCard extends StatefulWidget {
      const ReadingProgressCard({
        super.key,
        required this.title,
        required this.total,
        this.status = NoteStatus.draft,
      });

      final String title;
      final int total;
      final NoteStatus status;

      @override
      State<ReadingProgressCard> createState() => _ReadingProgressCardState();
    }

    class _ReadingProgressCardState extends State<ReadingProgressCard> {
      int done = 0;

      @override
      Widget build(BuildContext context) {
        return Card(
          child: ListTile(
            title: Text('${'$'}{widget.title} • ${'$'}{widget.status.name}'),
            subtitle: Text('${'$'}done of ${'$'}{widget.total} items'),
            trailing: IconButton(
              icon: const Icon(Icons.add),
              onPressed: done >= widget.total ? null : () => setState(() => done += 1),
            ),
          ),
        );
      }
    }
""".trimIndent()
