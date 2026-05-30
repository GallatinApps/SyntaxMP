package com.gallatinapps.syntaxmp.demo.model.samples

internal val RSample = """
    library(dplyr)
    library(stringr)

    notes <- tibble(
      title = c("Launch plan", "Browser indexing", "Wasm demo"),
      status = c("draft", "review", "active"),
      words = c(1240, 870, 540),
      pinned = c(TRUE, FALSE, TRUE)
    )

    summary <- notes |>
      filter(status != "done") |>
      mutate(
        reading_minutes = ceiling(words / 220),
        label = str_glue("{title} - {reading_minutes} min")
      ) |>
      arrange(desc(reading_minutes))

    model <- lm(reading_minutes ~ words + pinned, data = summary)
    message(sprintf("Top note: %s", summary${'$'}label[[1]]))
    print(summary${'$'}title)
""".trimIndent()
