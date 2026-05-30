package com.gallatinapps.syntaxmp.demo.model.samples

internal val RubySample = """
    Note = Data.define(:title, :path, :words, :pinned, :status) do
      def reading_minutes
        [(words / 220.0).ceil, 1].max
      end

      def label
        "#{title} • #{reading_minutes} min • #{status.to_s.upcase}"
      end
    end

    queue = notes
      .reject { |note| note.path.match?(%r{/archive/}) }
      .sort_by { |note| [note.pinned ? 0 : 1, -note.reading_minutes] }

    queue.each do |note|
      case note.status
      when :draft, :review
        puts "#{note.label} -> #{note.path}"
      else
        warn "skipping #{note.title}"
      end
    end
""".trimIndent()
