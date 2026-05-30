package com.gallatinapps.syntaxmp.demo.model.samples

internal val LuaSample = """
    -- Lightweight scoring module for note queues.
    local indexer = {}
    local status_label = {
      draft = "Draft",
      review = "Needs review",
      published = "Published",
    }

    function indexer.score(note)
      local score = 0
      if note.pinned then score = score + 10 end
      if note.words > 1200 then score = score + 2 end
      return score
    end

    function indexer.describe(note)
      local minutes = math.max(1, math.ceil(note.words / 220))
      return string.format("%s • %s • %d min", note.title, status_label[note.status], minutes)
    end

    local query = [[workspace NEAR browser]]

    for _, note in ipairs(notes) do
      note.rank = indexer.score(note)
      if string.find(note.title:lower(), query:match("%w+")) then
        print(indexer.describe(note))
      end
    end

    return indexer
""".trimIndent()
