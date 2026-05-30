package com.gallatinapps.syntaxmp.engine.spans

internal fun MutableList<SyntaxTokenSpan>.appendEmbeddedSpans(
    parentCode: String,
    bodyStart: Int,
    bodyEnd: Int,
    languageLabel: String,
    tokenizeEmbedded: (String, String) -> List<SyntaxTokenSpan>,
) {
    if (bodyEnd <= bodyStart) return
    val safeBodyStart = bodyStart.coerceIn(0, parentCode.length)
    val safeBodyEnd = bodyEnd.coerceIn(safeBodyStart, parentCode.length)
    if (safeBodyEnd <= safeBodyStart) return
    val bodyCode = parentCode.substring(safeBodyStart, safeBodyEnd)
    val bodyLength = safeBodyEnd - safeBodyStart
    tokenizeEmbedded(bodyCode, languageLabel).forEach { sub ->
        val start = sub.start.coerceIn(0, bodyLength)
        val end = sub.endExclusive.coerceIn(start, bodyLength)
        if (end > start) {
            add(
                SyntaxTokenSpan(
                    start = safeBodyStart + start,
                    endExclusive = safeBodyStart + end,
                    role = sub.role,
                    languageId = sub.languageId,
                ),
            )
        }
    }
}
