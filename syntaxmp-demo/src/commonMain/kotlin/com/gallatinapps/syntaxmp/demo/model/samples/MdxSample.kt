package com.gallatinapps.syntaxmp.demo.model.samples

internal val MdxSample = """
    import { Callout } from "./Callout";
    import { ReadingChart } from "./ReadingChart";

    export const metadata = {
      title: "Workspace review",
      status: "draft",
      tags: ["browser", "syntax"],
    };

    # Workspace Review

    <span data-owner="docs">MDX can mix Markdown, JSX, and expressions.</span>

    <Callout tone="info">
      Browser search is scoped to the current library on touch layouts.
      The active status is **{metadata.status.toUpperCase()}**.
    </Callout>

    ## Reading Load

    | Section | Notes |
    | :-- | --: |
    | Drafts | 7 |
    | Review | 3 |

    <ReadingChart
      data={[
        { label: "Drafts", value: 7 },
        { label: "Review", value: 3 },
        { label: "Published", value: 12 },
      ]}
    />

    ```ts
    const label = `${'$'}{metadata.title}: ${'$'}{metadata.tags.join(", ")}`;
    ```
""".trimIndent()
