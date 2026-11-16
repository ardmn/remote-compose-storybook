package rcstorybook.host

import ee.schimke.composeai.rcplayer.protocol.RcDocument
import ee.schimke.composeai.rcplayer.protocol.RcNoArg
import ee.schimke.composeai.rcplayer.protocol.RcOpcodes
import ee.schimke.composeai.rcplayer.protocol.RcOperation

/**
 * A document as text, one operation per line, nested by container: what the Document tab shows
 * and what the Changes tab diffs. Each line is the decoded operation's own description.
 */
internal fun RcDocument.structureLines(): List<String> {
    val lines = ArrayList<String>(operations.size + 1)
    lines += "Header" + header.toString().removePrefix("RcHeader")
    var depth = 0
    for (op in operations) {
        if (op.opcode == RcOpcodes.CONTAINER_END) {
            depth = (depth - 1).coerceAtLeast(0)
            lines += "  ".repeat(depth) + "}"
            continue
        }
        val opens = op.opcode in RcOpcodes.CONTAINER_STARTS
        lines += "  ".repeat(depth) + describe(op) + if (opens) " {" else ""
        if (opens) depth++
    }
    return lines
}

private fun describe(op: RcOperation): String = when (op) {
    is RcNoArg -> "Op#${op.opcode}"
    else -> op.toString().removePrefix("Rc")
}

/**
 * A unified diff of two line lists (`-` removed, `+` added, three lines of context), from their
 * longest common subsequence. Documents here are a few hundred lines, so the quadratic table is fine.
 */
internal fun unifiedDiff(before: List<String>, after: List<String>, context: Int = 3): String {
    val n = before.size
    val m = after.size
    if (n.toLong() * m > 6_000_000L) return "(documents too large to diff: $n and $m lines)"
    val lcs = Array(n + 1) { IntArray(m + 1) }
    for (i in n - 1 downTo 0) for (j in m - 1 downTo 0) {
        lcs[i][j] = if (before[i] == after[j]) lcs[i + 1][j + 1] + 1 else maxOf(lcs[i + 1][j], lcs[i][j + 1])
    }
    data class Edit(val kind: Char, val text: String, val oldLine: Int, val newLine: Int)
    val edits = ArrayList<Edit>()
    var i = 0
    var j = 0
    while (i < n || j < m) {
        when {
            i < n && j < m && before[i] == after[j] -> { edits += Edit(' ', before[i], i, j); i++; j++ }
            j < m && (i == n || lcs[i][j + 1] >= lcs[i + 1][j]) -> { edits += Edit('+', after[j], i, j); j++ }
            else -> { edits += Edit('-', before[i], i, j); i++ }
        }
    }
    if (edits.none { it.kind != ' ' }) return ""
    val out = StringBuilder("--- previous\n+++ current\n")
    var k = 0
    while (k < edits.size) {
        if (edits[k].kind == ' ') { k++; continue }
        val start = (k - context).coerceAtLeast(0)
        var end = k
        // Extend the hunk while changes are within 2 * context lines of each other.
        var lastChange = k
        while (end < edits.size) {
            if (edits[end].kind != ' ') lastChange = end
            if (end - lastChange > 2 * context) break
            end++
        }
        val stop = minOf(edits.size, lastChange + context + 1)
        val hunk = edits.subList(start, stop)
        val oldCount = hunk.count { it.kind != '+' }
        val newCount = hunk.count { it.kind != '-' }
        out.append("@@ -${hunk.first().oldLine + 1},$oldCount +${hunk.first().newLine + 1},$newCount @@\n")
        for (e in hunk) out.append(e.kind).append(e.text).append('\n')
        k = stop
    }
    return out.toString().trimEnd()
}
