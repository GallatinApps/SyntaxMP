package com.gallatinapps.syntaxmp.demo.model.samples

internal val XmlSample = """
    <?xml version="1.0" encoding="utf-8"?>
    <workspace xmlns="https://syntaxmp.dev/workspace" name="Research Notes" version="1">
        <!-- Local filesystem grants are restored before scanning. -->
        <library id="product" path="~/Notes/Product" enabled="true">
            <include pattern="**/*.md" />
            <include pattern="**/*.txt" />
            <exclude pattern=".hashjot/**" />
            <description><![CDATA[
                Keep <draft> notes searchable without parsing image bytes.
            ]]></description>
        </library>
        <editor sourceMode="rich" wrapColumn="88" />
        <tag key="owner" value="docs &amp; tooling" />
    </workspace>
""".trimIndent()
