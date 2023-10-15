package sahlaysta.sbte4.gui

import sahlaysta.sbte4.rom.blob.string.SBTECharset
import sahlaysta.sbte4.rom.blob.string.UTF32Char
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.Shape
import javax.swing.JTextArea
import javax.swing.SwingUtilities
import javax.swing.border.EmptyBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.DefaultHighlighter
import javax.swing.text.JTextComponent
import javax.swing.text.Position
import javax.swing.text.View

//the editor text box. uses highlighting to hint the format for SBTE-styled strings.
internal class GUIEditorTextBox(val gui: GUI) {

    val jTextArea = JTextArea()
    private val document = jTextArea.document

    init {
        jTextArea.highlighter = CustomHighlighter()
        CaretBlinker.blinkCaret(jTextArea, blinkWhenNonEditable = false)

        jTextArea.addPropertyChangeListener("UI") {
            jTextArea.highlighter = CustomHighlighter() //must refresh the highlighter on UI change
        }
        jTextArea.addPropertyChangeListener("caret") {
            CaretBlinker.blinkCaret(jTextArea, blinkWhenNonEditable = false)
        }

        jTextArea.border = EmptyBorder(0, 0, 0, 0)
        jTextArea.font = Font(Font.MONOSPACED, Font.PLAIN, 13)

        document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) = updateHighlights()
            override fun removeUpdate(e: DocumentEvent) = updateHighlights()
            override fun changedUpdate(e: DocumentEvent) = updateHighlights()
        })
    }

    private class HighlightRegion(val color: Color, val start: Position, val end: Position)
    private var highlightRegions: List<HighlightRegion> = emptyList()
    private var currentEditorString: GUIEditorString? = null
    private inner class CustomHighlighter : DefaultHighlighter() {
        override fun paintLayeredHighlights(g: Graphics?, p0: Int, p1: Int, viewBounds: Shape?,
                                            editor: JTextComponent?, view: View?) {
            if (document.length > 0) {
                for (hr: HighlightRegion in groupAdjacentHighlightRegions(highlightRegions)) {
                    val startOffset = hr.start.offset
                    val endOffset = hr.end.offset + 1
                    if (startOffset in p0..p1 || endOffset in p0..p1) {
                        val minOffset = startOffset.coerceAtLeast(p0)
                        val maxOffset = endOffset.coerceAtMost(p1)
                        DefaultHighlightPainter(hr.color).paintLayer(g, minOffset, maxOffset, viewBounds, editor, view)
                    }
                }
            }
            super.paintLayeredHighlights(g, p0, p1, viewBounds, editor, view)
        }
    }

    private fun updateHighlights() {
        val editorString = gui.editor.selectedString
        if (editorString !== currentEditorString) {
            highlightRegions = emptyList()
            jTextArea.revalidate()
            jTextArea.repaint()
            currentEditorString = editorString
        }
        getHighlightRegionsInBackground()
    }

    private val bracketedSequenceColor = Color(20, 20, 20, 40)
    private val badCodePointColor = Color(255, 0, 0, 60)
    private fun getHighlightRegionsInBackground() {
        class Result(val highlightRegions: List<HighlightRegion>)
        gui.backgroundQueue.enqueue(
            key = "editortextboxhighlight",
            backgroundFunction = { actionState ->
                val highlightRegions = ArrayList<HighlightRegion>()
                for (mr in matchAll()) {
                    if (actionState.actionIsCanceled()) return@enqueue null
                    val highlightColor = when (mr.name) {
                        MatchName.BRACKETED_SEQUENCE -> bracketedSequenceColor
                        MatchName.BAD_CODE_POINT -> badCodePointColor
                    }
                    highlightRegions.add(HighlightRegion(highlightColor, mr.start, mr.end))
                }
                return@enqueue Result(highlightRegions)
            },
            callbackFunction = { result ->
                result!!
                SwingUtilities.invokeLater {
                    highlightRegions = result.highlightRegions
                    jTextArea.revalidate()
                    jTextArea.repaint()
                }
            },
            errorCallbackFunction = { exception -> exception.printStackTrace() }
        )
    }

    //adjacent highlight-regions must be grouped to avoid a visual highlight glitch
    private fun groupAdjacentHighlightRegions(highlightRegions: List<HighlightRegion>) = sequence<HighlightRegion> {
        var previoushr: HighlightRegion? = null
        for (hr in highlightRegions) {
            previoushr = if (previoushr != null
                && previoushr.color == hr.color && previoushr.end.offset + 1 == hr.start.offset) {
                HighlightRegion(hr.color, previoushr.start, hr.end)
            } else {
                if (previoushr != null) yield(previoushr)
                hr
            }
        }
        if (previoushr != null) yield(previoushr)
    }

    //parse the Sonic Battle string formatting [SBTEStringEncoder]
    private enum class MatchName { BRACKETED_SEQUENCE, BAD_CODE_POINT }
    private class MatchResult(val name: MatchName, val start: Position, val end: Position)
    private fun matchAll() = sequence {
        val cs = DocumentOptimizer.documentAsCharSequence(document)
        var i = 0
        while (true) {
            val start = i
            var matchResult: MatchResult? = null
            var end = false
            document.render {
                val matchName: MatchName?
                if (i >= cs.length) {
                    end = true
                    matchName = null
                } else {
                    end = false
                    matchName = when (cs[i]) {
                        '[' -> {
                            when {
                                startsWithCaseInsensitive(cs, "[BLACK]", "[black]", i) -> {
                                    i += "[BLACK]".length
                                    MatchName.BRACKETED_SEQUENCE
                                }
                                startsWithCaseInsensitive(cs, "[RED]", "[red]", i) -> {
                                    i += "[RED]".length
                                    MatchName.BRACKETED_SEQUENCE
                                }
                                startsWithCaseInsensitive(cs, "[BLUE]", "[blue]", i) -> {
                                    i += "[BLUE]".length
                                    MatchName.BRACKETED_SEQUENCE
                                }
                                startsWithCaseInsensitive(cs, "[GREEN]", "[green]", i) -> {
                                    i += "[GREEN]".length
                                    MatchName.BRACKETED_SEQUENCE
                                }
                                startsWithCaseInsensitive(cs, "[PURPLE]", "[purple]", i) -> {
                                    i += "[PURPLE]".length
                                    MatchName.BRACKETED_SEQUENCE
                                }
                                startsWithBracketedHex(cs, i) -> {
                                    i += "[XXXX]".length
                                    MatchName.BRACKETED_SEQUENCE
                                }
                                else -> {
                                    i++
                                    MatchName.BAD_CODE_POINT //bad left bracket
                                }
                            }
                        }
                        ']' -> {
                            i++
                            MatchName.BAD_CODE_POINT //bad right bracket
                        }
                        '\r', '\n' -> {
                            i++
                            null
                        }
                        else -> {
                            val utf32CodePoint = getUTF32CodePoint(cs, i)
                            val utf32Char = UTF32Char(utf32CodePoint)
                            val cp = SBTECharset.encodeCodePoint(utf32Char)
                            i += Character.charCount(utf32CodePoint)
                            if (cp == null)
                                MatchName.BAD_CODE_POINT //bad unknown code point
                            else
                                null
                        }
                    }
                }
                matchResult = if (matchName == null) null else MatchResult(
                    matchName, document.createPosition(start), document.createPosition(i - 1))
            }
            matchResult?.let { yield(it) }
            if (end) break
        }
    }

    private fun startsWithCaseInsensitive(cs: CharSequence, uppercasePrefix: CharSequence,
                                          lowercasePrefix: CharSequence, startIndex: Int): Boolean {
        if (startIndex + uppercasePrefix.length > cs.length) return false
        for (i in uppercasePrefix.indices) {
            val ch = cs[i + startIndex]
            if (uppercasePrefix[i] != ch && lowercasePrefix[i] != ch) {
                return false
            }
        }
        return true
    }

    private fun startsWithBracketedHex(cs: CharSequence, startIndex: Int): Boolean {
        if (startIndex + 6 > cs.length) return false
        if (cs[startIndex + 0] != '[') return false
        if (cs[startIndex + 5] != ']') return false
        for (i in startIndex + 1..startIndex + 4) {
            val ch = cs[i]
            if (ch !in '0'..'9' && ch !in 'A'..'F' && ch !in 'a'..'f')
                return false
        }
        return true
    }

    private fun getUTF32CodePoint(cs: CharSequence, index: Int): Int {
        val high = cs[index]
        if (Character.isHighSurrogate(high) && index + 1 < cs.length) {
            val low = cs[index + 1]
            if (Character.isLowSurrogate(low))
                return Character.toCodePoint(high, low)
        }
        return high.code
    }

}