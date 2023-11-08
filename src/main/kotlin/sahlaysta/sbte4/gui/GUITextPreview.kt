package sahlaysta.sbte4.gui

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import gnu.trove.map.hash.TCharObjectHashMap
import gnu.trove.map.hash.TShortObjectHashMap
import sahlaysta.sbte4.rom.SBTEROMType
import sahlaysta.sbte4.rom.blob.string.SonicBattleChar
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.Window
import java.awt.event.KeyEvent
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.AbstractListModel
import javax.swing.DefaultListCellRenderer
import javax.swing.Icon
import javax.swing.JCheckBoxMenuItem
import javax.swing.JDialog
import javax.swing.JList
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JScrollPane
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.border.EmptyBorder

//the text-preview dialog for Sonic Battle text
internal class GUITextPreview(val gui: GUI) {

    private class Glyph(val bitmap: BooleanArray, val width: Int, val height: Int)
    private class GlyphData(val glyphDict: TShortObjectHashMap<Glyph>)
    private class HexSquareGlyphData(val glyphDict: TCharObjectHashMap<Glyph>)
    private val glyphDataJP = GlyphReader.readGlyphData("/sonicbattleglyphs_jp.json")
    private val glyphDataUS = GlyphReader.readGlyphData("/sonicbattleglyphs_us.json")
    private val glyphDataEU = GlyphReader.readGlyphData("/sonicbattleglyphs_eu.json")

    private val tpd = TextPreviewDialog(gui.jFrame)

    val isShown get() = tpd.jDialog.isVisible

    fun showTextPreview() {
        if (tpd.jDialog.isVisible) {
            if (tpd.jDialog.focusableWindowState)
                tpd.jDialog.requestFocus()
        } else {
            tpd.show()
            updateText()
        }
    }

    fun setText(text: String, isUserEdit: Boolean, romType: SBTEROMType) {
        if (tpd.jDialog.isVisible) {
            val glyphData = when (romType) {
                SBTEROMType.JP -> glyphDataJP
                SBTEROMType.US -> glyphDataUS
                SBTEROMType.EU -> glyphDataEU
            }
            tpd.setText(text, glyphData, isUserEdit)
        }
    }

    fun clearText() {
        if (tpd.jDialog.isVisible)
            tpd.clearText()
    }

    private fun updateText() {
        val editorString = gui.editor.selectedString
        if (editorString == null) {
            clearText()
        } else {
            val text = editorString.callInfo { text, _, _, _ -> text }
            if (text == null) {
                clearText()
            } else {
                setText(text, isUserEdit = false, gui.editor.romData!!.romType)
            }
        }
    }

    private companion object {

        val hexSquareGlyphData = GlyphReader.getHexSquareGlyphData()

        val SONIC_BATTLE_BLACK: Color = Color(0, 0, 0)
        val SONIC_BATTLE_RED: Color = Color(255, 0, 49)
        val SONIC_BATTLE_BLUE: Color = Color(0, 0, 214)
        val SONIC_BATTLE_GREEN: Color = Color(0, 173, 74)
        val SONIC_BATTLE_PURPLE: Color = Color(148, 49, 222)

        //double-tokenize Sonic Battle strings with SonicBattleStringTokenizer
        sealed class TextToken
        data object NewLineTextToken : TextToken()
        data class GlyphTextToken(val glyph: Glyph) : TextToken()
        data class ColorTextToken(val color: Color) : TextToken()
        data class UnknownSonicBattleCharTextToken(val sbChar: SonicBattleChar) : TextToken()
        data class UnparsablePartTextToken(val unparsablePart: String) : TextToken()

        fun retokenizeSonicBattleString(cs: CharSequence, glyphData: GlyphData) = sequence {
            val unparsablePart = StringBuilder()
            for (token in SonicBattleStringTokenizer.tokenizeSonicBattleString(cs)) {
                val yieldValue: TextToken? = when (token) {
                    is SonicBattleStringTokenizer.Token.NewLineToken -> NewLineTextToken
                    is SonicBattleStringTokenizer.Token.BlackToken -> ColorTextToken(SONIC_BATTLE_BLACK)
                    is SonicBattleStringTokenizer.Token.RedToken -> ColorTextToken(SONIC_BATTLE_RED)
                    is SonicBattleStringTokenizer.Token.BlueToken -> ColorTextToken(SONIC_BATTLE_BLUE)
                    is SonicBattleStringTokenizer.Token.GreenToken -> ColorTextToken(SONIC_BATTLE_GREEN)
                    is SonicBattleStringTokenizer.Token.PurpleToken -> ColorTextToken(SONIC_BATTLE_PURPLE)
                    is SonicBattleStringTokenizer.Token.UnknownSonicBattleCharToken ->
                        UnknownSonicBattleCharTextToken(token.sbChar)
                    is SonicBattleStringTokenizer.Token.CodePointToken -> {
                        val cp = token.codePoint
                        val glyph = glyphData.glyphDict[cp.sbChar.value]
                        if (glyph != null) {
                            GlyphTextToken(glyph)
                        } else {
                            unparsablePart.appendCodePoint(cp.utf32Char.value)
                            null
                        }
                    }
                    is SonicBattleStringTokenizer.Token.AlternativeCodePointToken -> {
                        val cp = token.codePoint
                        val glyph = glyphData.glyphDict[cp.alternativeSBChar.value]
                        if (glyph != null) {
                            GlyphTextToken(glyph)
                        } else {
                            unparsablePart.appendCodePoint(cp.utf32Char.value)
                            null
                        }
                    }
                    is SonicBattleStringTokenizer.Token.UnknownUTF32CharToken -> {
                        unparsablePart.appendCodePoint(token.utf32Char.value)
                        null
                    }
                    is SonicBattleStringTokenizer.Token.BadLeftBracketToken -> {
                        unparsablePart.append('[')
                        null
                    }
                    is SonicBattleStringTokenizer.Token.BadRightBracketToken -> {
                        unparsablePart.append(']')
                        null
                    }
                }
                if (yieldValue != null) {
                    if (unparsablePart.isNotEmpty()) {
                        yield(UnparsablePartTextToken(unparsablePart.toString()))
                        unparsablePart.setLength(0)
                    }
                    yield(yieldValue)
                }
            }
            if (unparsablePart.isNotEmpty())
                yield(UnparsablePartTextToken(unparsablePart.toString()))
        }

    }

    private class TextPreviewDialog(val parent: Window) {

        companion object {

            private const val INITIAL_LETTER_X: Int = 15
            private const val INITIAL_LETTER_Y: Int = 8
            private val INITIAL_LETTER_COLOR: Color = SONIC_BATTLE_BLACK
            private const val HORIZONTAL_LETTER_GAP: Int = 1
            private const val VERTICAL_LETTER_GAP: Int = 16
            private const val FONT_BASELINE_Y: Int = 13
            private const val LINES_PER_TEXT_FRAME: Int = 2
            private const val FRAME_BACKGROUND_BLUEARROW_X: Int = 218
            private const val FRAME_BACKGROUND_BLUEARROW_Y: Int = 37

        }

        private class Frame(val tokenizedText: Collection<TextToken>, val initialColor: Color,
                            val isLastFrame: Boolean)

        private inner class CustomListModel : AbstractListModel<Any?>() {
            override fun getSize() = frames.size
            override fun getElementAt(index: Int) = frames[index]
            fun fireIntervalAdded(index0: Int, index1: Int) = super.fireIntervalAdded(this, index0, index1)
            fun fireIntervalRemoved(index0: Int, index1: Int) = super.fireIntervalRemoved(this, index0, index1)
        }

        val jDialog = JDialog(parent)
        private var hasShown = false
        private var frames: List<Frame> = emptyList()
        private val framesListModel = CustomListModel()
        private val frameCellRenderer = FrameCellRenderer()
        private val framesJList = JList<Any?>(framesListModel)
        private val framesJListJScrollPane =
            JScrollPane(framesJList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)
        private val jMenuBar = JMenuBar()
        private val optionsJMenu = JMenu("...")
        private val exportSelectedFrameJMenuItem = JMenuItem()
        private val toggleBlueArrowJMenuItem = JCheckBoxMenuItem("Show blue arrows")
        private val scaleJMenu = JMenu("Scale")
        private val scale1JMenuItem = JCheckBoxMenuItem("1X")
        private val scale2JMenuItem = JCheckBoxMenuItem("2X")
        private val scale3JMenuItem = JCheckBoxMenuItem("3X")
        private val scale4JMenuItem = JCheckBoxMenuItem("4X")
        private val scale5JMenuItem = JCheckBoxMenuItem("5X")
        private var scale = 1
        private var paintBlueArrow = true
        private val frameBackgroundImage: BufferedImage =
            GUIUtil.getResourceStream("/sonicbattledialogueframe.png").use { ImageIO.read(it) }
        private val frameBackgroundBlueArrow: BufferedImage =
            GUIUtil.getResourceStream("/sonicbattledialogueframebluearrow.png").use { ImageIO.read(it) }
        private val badCodePointColor = Color(255, 0, 0, 60)

        fun show() {
            if (!hasShown) {
                jDialog.setLocationRelativeTo(parent)
                jDialog.defaultCloseOperation = JDialog.HIDE_ON_CLOSE
                hasShown = true
            }
            jDialog.isVisible = true
        }

        fun setText(text: String, glyphData: GlyphData, isUserEdit: Boolean) {
            tokenizeSonicBattleStringInBackground(text, glyphData, isUserEdit)
        }

        fun clearText() {
            val oldSize = frames.size
            frames = emptyList()
            if (oldSize > 0)
                framesListModel.fireIntervalRemoved(0, oldSize)
            updateExportSelectedFrameButton()
        }

        private fun initLayout() {
            jDialog.focusableWindowState = false
            framesJList.cellRenderer = frameCellRenderer
            framesJListJScrollPane.border = EmptyBorder(0, 0, 0, 0)
            framesJList.addListSelectionListener { updateExportSelectedFrameButton() }
            exportSelectedFrameJMenuItem.setMnemonic('f')
            exportSelectedFrameJMenuItem.addActionListener { exportSelectedFrames() }
            toggleBlueArrowJMenuItem.setMnemonic('b')
            toggleBlueArrowJMenuItem.isSelected = true
            toggleBlueArrowJMenuItem.addActionListener { updateToggleBlueArrow() }
            scaleJMenu.mnemonic = KeyEvent.VK_S
            scale1JMenuItem.mnemonic = KeyEvent.VK_1
            scale2JMenuItem.mnemonic = KeyEvent.VK_2
            scale3JMenuItem.mnemonic = KeyEvent.VK_3
            scale4JMenuItem.mnemonic = KeyEvent.VK_4
            scale5JMenuItem.mnemonic = KeyEvent.VK_5
            scale1JMenuItem.addActionListener { scale = 1; updateScale() }
            scale2JMenuItem.addActionListener { scale = 2; updateScale() }
            scale3JMenuItem.addActionListener { scale = 3; updateScale() }
            scale4JMenuItem.addActionListener { scale = 4; updateScale() }
            scale5JMenuItem.addActionListener { scale = 5; updateScale() }
            scaleJMenu.add(scale1JMenuItem)
            scaleJMenu.add(scale2JMenuItem)
            scaleJMenu.add(scale3JMenuItem)
            scaleJMenu.add(scale4JMenuItem)
            scaleJMenu.add(scale5JMenuItem)
            jDialog.add(framesJListJScrollPane)
            optionsJMenu.mnemonic = KeyEvent.VK_PERIOD
            jMenuBar.add(optionsJMenu)
            optionsJMenu.add(exportSelectedFrameJMenuItem)
            optionsJMenu.add(scaleJMenu)
            optionsJMenu.add(toggleBlueArrowJMenuItem)
            jDialog.jMenuBar = jMenuBar
            updateExportSelectedFrameButton()
            updateScale()
            updateToggleBlueArrow()
        }

        private fun updateExportSelectedFrameButton() {
            val selection = framesJList.selectedIndices
            exportSelectedFrameJMenuItem.text =
                if (selection.size <= 1) "Export selected frame" else "Export selected frames (${selection.size})"
            exportSelectedFrameJMenuItem.isEnabled = selection.isNotEmpty()
        }

        private fun updateToggleBlueArrow() {
            paintBlueArrow = toggleBlueArrowJMenuItem.isSelected
            framesJList.revalidate()
            framesJList.repaint()
        }

        private fun updateScale() {
            scale1JMenuItem.isSelected = scale == 1
            scale2JMenuItem.isSelected = scale == 2
            scale3JMenuItem.isSelected = scale == 3
            scale4JMenuItem.isSelected = scale == 4
            scale5JMenuItem.isSelected = scale == 5
            framesJList.fixedCellWidth = frameBackgroundImage.width * scale
            framesJList.fixedCellHeight = frameBackgroundImage.height * scale
            framesJListJScrollPane.verticalScrollBar.unitIncrement = frameBackgroundImage.height * scale
            framesJListJScrollPane.viewport.preferredSize =
                Dimension(frameBackgroundImage.width * scale, frameBackgroundImage.height * scale)
            jDialog.pack()
        }

        private fun exportSelectedFrames() {
            val selectedFrames = framesJList.selectedIndices.map { frames[it] }
            if (selectedFrames.size == 1) {
                val saveFile = GUI.gui?.fileChooser?.fileChooserSaveFile(
                    "export", jDialog, "Export frame", "PNG Files", "png")
                if (saveFile != null)
                    exportFrame(selectedFrames[0], saveFile)
            } else if (selectedFrames.size > 1) {
                val saveDir = GUI.gui?.fileChooser?.fileChooserSaveDir("exportdir", jDialog, "Export frames")
                if (saveDir != null) {
                    var i: Long = 1
                    while (File(saveDir, "frame_$i.png").exists()) {
                        i++
                    }
                    for (frame in selectedFrames) {
                        if (!exportFrame(frame, File(saveDir, "frame_$i.png").toString()))
                            return
                        i++
                    }
                }
            }
        }

        private fun exportFrame(frame: Frame, filePath: String): Boolean {
            val bufferedImage = BufferedImage(
                frameBackgroundImage.width * scale, frameBackgroundImage.height * scale, BufferedImage.TYPE_INT_ARGB)
            val g = bufferedImage.createGraphics()
            paintFrame(frame, g, 0, 0, scale, paintBlueArrow)
            g.dispose()
            try {
                val formatName = "png"
                val result = ImageIO.write(bufferedImage, formatName, File(filePath))
                if (!result) throw IllegalArgumentException("No writer found for name: $formatName")
            } catch (e: Exception) {
                e.printStackTrace()
                GUIDialogs.showFileWriteErrDialog(jDialog, filePath, e, "Export frame")
                return false
            }
            return true
        }

        private fun tokenizeSonicBattleStringInBackground(str: String, glyphData: GlyphData, isUserEdit: Boolean) {
            class Result(val frames: List<Frame>, val isUserEdit: Boolean)
            GUI.gui?.backgroundQueue?.enqueue(
                key = "textpreview",
                backgroundFunction = { actionState ->
                    val frames = ArrayList<Frame>()
                    for (frame in tokenizeSonicBattleStringAndSplitIntoFrames(str, glyphData)) {
                        if (actionState.actionIsCanceled()) return@enqueue null
                        frames.add(frame)
                    }
                    return@enqueue Result(frames, isUserEdit)
                },
                callbackFunction = { result ->
                    result!!
                    SwingUtilities.invokeLater {
                        val oldFrames = frames
                        clearText()
                        frames = result.frames
                        if (frames.isNotEmpty())
                            framesListModel.fireIntervalAdded(0, frames.size)
                        updateExportSelectedFrameButton()
                        if (result.isUserEdit && frames.size > oldFrames.size
                            && jScrollPaneIsScrolledToBottom(framesJListJScrollPane)) {
                            scrollJScrollPaneToBottom(framesJListJScrollPane)
                        }
                    }
                },
                errorCallbackFunction = { exception -> exception.printStackTrace() }
            )
        }

        private fun jScrollPaneIsScrolledToBottom(jScrollPane: JScrollPane): Boolean {
            val vsb = jScrollPane.verticalScrollBar
            return vsb.value + vsb.model.extent == vsb.maximum
        }

        private fun scrollJScrollPaneToBottom(jScrollPane: JScrollPane) {
            SwingUtilities.invokeLater { //setting the scroll value never works without invokeLater()
                val vsb = jScrollPane.verticalScrollBar
                vsb.value = vsb.maximum
            }
        }

        private fun tokenizeSonicBattleStringAndSplitIntoFrames(str: String, glyphData: GlyphData) = sequence {
            val textTokens = ArrayList<TextToken>()
            var lines = 0
            var frameInitialColor = INITIAL_LETTER_COLOR
            var frameNewInitialColor = frameInitialColor
            for (textToken in retokenizeSonicBattleString(str, glyphData)) {
                textTokens.add(textToken)
                if (textToken is NewLineTextToken) {
                    lines++
                    if (lines % LINES_PER_TEXT_FRAME == 0) {
                        yield(Frame(textTokens.toList(), frameInitialColor, false))
                        textTokens.clear()
                        frameInitialColor = frameNewInitialColor
                    }
                } else if (textToken is ColorTextToken) {
                    frameNewInitialColor = textToken.color
                }
            }
            yield(Frame(textTokens.toList(), frameInitialColor, true))
        }

        private inner class FrameCellRenderer : DefaultListCellRenderer() {

            val emptyBorder = EmptyBorder(0, 0, 0, 0)

            lateinit var frame: Frame

            val framePainter = object : Icon {
                override fun getIconWidth() = frameBackgroundImage.width * scale
                override fun getIconHeight() = frameBackgroundImage.height * scale
                override fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {
                    paintFrame(frame, g, x, y, scale, paintBlueArrow)
                }
            }

            override fun getListCellRendererComponent(list: JList<*>?, value: Any?, index: Int, isSelected: Boolean,
                                                      cellHasFocus: Boolean): Component {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
                border = emptyBorder
                frame = value as Frame
                icon = framePainter
                text = null
                if (isSelected) {
                    //always use focus colors
                    background = UIManager.getColor("List.selectionBackground")?.let { Color(it.rgb, true) }
                    foreground = UIManager.getColor("List.selectionForeground")?.let { Color(it.rgb, true) }
                }
                return this
            }

        }

        @JvmInline private value class PaintedWidth(val value: Int)

        private fun paintFrame(frame: Frame, g: Graphics, x: Int, y: Int, scale: Int, paintBlueArrows: Boolean) {
            paintBufferedImage(frameBackgroundImage, g, x, y, scale)
            val originalColor = g.color
            g.color = frame.initialColor
            var glyphX = x + (INITIAL_LETTER_X * scale)
            var glyphY = y + (INITIAL_LETTER_Y * scale)
            for (textToken in frame.tokenizedText) {
                when (textToken) {
                    is NewLineTextToken -> {
                        glyphY += VERTICAL_LETTER_GAP * scale
                        glyphX = x + (INITIAL_LETTER_X * scale)
                    }
                    is GlyphTextToken -> {
                        val paintedWidth = paintGlyph(textToken.glyph, g, x + glyphX, y + glyphY, scale)
                        glyphX += paintedWidth.value + (HORIZONTAL_LETTER_GAP * scale)
                    }
                    is ColorTextToken -> g.color = textToken.color
                    is UnknownSonicBattleCharTextToken -> {
                        val paintedWidth = paintHexSquare(textToken.sbChar, g, x + glyphX, y + glyphY, scale)
                        glyphX += paintedWidth.value + (HORIZONTAL_LETTER_GAP * scale)
                    }
                    is UnparsablePartTextToken -> {
                        val text = textToken.unparsablePart
                        val font = getUnparsableTextFont(scale)
                        val paintedWidth = paintHighlightedFont(font, text, g,
                            x + glyphX, y + glyphY + (FONT_BASELINE_Y * scale), badCodePointColor)
                        glyphX += paintedWidth.value + (HORIZONTAL_LETTER_GAP * scale)
                    }
                }
            }
            if (paintBlueArrows && !frame.isLastFrame) {
                paintBufferedImage(frameBackgroundBlueArrow, g,
                    x + (FRAME_BACKGROUND_BLUEARROW_X * scale), y + (FRAME_BACKGROUND_BLUEARROW_Y * scale), scale)
            }
            g.color = originalColor
        }

        private fun paintBufferedImage(bufferedImage: BufferedImage, g: Graphics, x: Int, y: Int, scale: Int) {
            val oldColor = g.color
            for (pixelY in 0..<bufferedImage.height) {
                for (pixelX in 0..<bufferedImage.width) {
                    g.color = Color(bufferedImage.getRGB(pixelX, pixelY), true)
                    g.fillRect(x + (pixelX * scale), y + (pixelY * scale), scale, scale)
                }
            }
            g.color = oldColor
        }

        private fun paintGlyph(glyph: Glyph, g: Graphics, x: Int, y: Int, scale: Int): PaintedWidth {
            var pixelX = 0
            var pixelY = 0
            for (bit in glyph.bitmap) {
                if (bit) {
                    g.fillRect(x + (pixelX * scale), y + (pixelY * scale), scale, scale)
                }
                pixelX++
                if (pixelX % glyph.width == 0) {
                    pixelX = 0
                    pixelY++
                }
            }
            return PaintedWidth(glyph.width * scale)
        }

        private fun paintHexSquare(sbChar: SonicBattleChar, g: Graphics, x: Int, y: Int, scale: Int): PaintedWidth {
            val sbCharValue = sbChar.value.toInt()
            val hexDigits = "0123456789ABCDEF"
            val hexDigit1 = hexDigits[sbCharValue shr (3 shl 2) and 0xF]
            val hexDigit2 = hexDigits[sbCharValue shr (2 shl 2) and 0xF]
            val hexDigit3 = hexDigits[sbCharValue shr (1 shl 2) and 0xF]
            val hexDigit4 = hexDigits[sbCharValue shr (0 shl 2) and 0xF]
            paintRect(g, x, y + (1 * scale), 13 * scale, 15 * scale, scale)
            paintGlyph(hexSquareGlyphData.glyphDict[hexDigit1], g, x + (2 * scale), y + (3 * scale), scale)
            paintGlyph(hexSquareGlyphData.glyphDict[hexDigit2], g, x + (7 * scale), y + (3 * scale), scale)
            paintGlyph(hexSquareGlyphData.glyphDict[hexDigit3], g, x + (2 * scale), y + (9 * scale), scale)
            paintGlyph(hexSquareGlyphData.glyphDict[hexDigit4], g, x + (7 * scale), y + (9 * scale), scale)
            return PaintedWidth(13 * scale)
        }

        private fun paintRect(g: Graphics, x: Int, y: Int, width: Int, height: Int, thickness: Int) {
            g.fillRect(x, y, width, thickness)
            g.fillRect(x, y, thickness, height)
            g.fillRect(x + width - thickness, y, thickness, height)
            g.fillRect(x, y + height - thickness, width, thickness)
        }

        private val fontScale1 = Font(Font.DIALOG, Font.PLAIN, 13)
        private val fontScale2 = Font(Font.DIALOG, Font.PLAIN, 28)
        private val fontScale3 = Font(Font.DIALOG, Font.PLAIN, 42)
        private val fontScale4 = Font(Font.DIALOG, Font.PLAIN, 56)
        private val fontScale5 = Font(Font.DIALOG, Font.PLAIN, 70)
        private fun getUnparsableTextFont(scale: Int): Font {
            return when (scale) {
                1 -> fontScale1 2 -> fontScale2 3 -> fontScale3 4 -> fontScale4 else -> fontScale5
            }
        }

        private fun paintHighlightedFont(font: Font, text: String, g: Graphics, x: Int, y: Int,
                                         highlight: Color): PaintedWidth {
            val oldColor = g.color
            g.color = highlight
            val fontMetrics = g.getFontMetrics(font)
            val strWidth = fontMetrics.stringWidth(text)
            g.fillRect(x, y - fontMetrics.ascent - fontMetrics.leading, strWidth, fontMetrics.height)
            g.color = oldColor
            val oldFont = g.font
            g.font = fontMetrics.font
            g.drawString(text, x, y)
            g.font = oldFont
            return PaintedWidth(strWidth)
        }

        init {
            initLayout()
            jDialog.title = "Text preview"
            jDialog.minimumSize = Dimension(50, 70)
        }

    }

    private class GlyphReader {

        companion object {

            fun readGlyphData(rsrc: String): GlyphData {
                val glyphDict = TShortObjectHashMap<Glyph>()
                for ((sbChar: SonicBattleChar, glyph: Glyph) in readGlyphRsrc(rsrc)) {
                    require(glyph.height == 16) { "Glyph height must be 16" }
                    require(!glyphDict.containsKey(sbChar.value)) { "Duplicate code point $sbChar" }
                    glyphDict.put(sbChar.value, glyph)
                }
                return GlyphData(glyphDict)
            }

            private fun readGlyphRsrc(rsrc: String) = sequence {
                val inputStream = GUIUtil.getResourceStream(rsrc)
                val jp: JsonParser = JsonFactory().createParser(inputStream)
                inputStream.use { jp.use {
                    jp.requireNext(JsonToken.START_OBJECT)
                    while (jp.requireNext(JsonToken.FIELD_NAME, JsonToken.END_OBJECT) == JsonToken.FIELD_NAME) {
                        val sbChar = readSBChar(jp.valueAsString)
                        jp.requireNext(JsonToken.VALUE_STRING)
                        val glyph = readPBMGlyph(jp.valueAsString)
                        yield(Pair(sbChar, glyph))
                    }
                } }
            }

            private fun JsonParser.requireNext(vararg tokens: JsonToken): JsonToken {
                val token = nextToken()
                require(tokens.contains(token)) { "Unexpected token $token, expected ${tokens.joinToString(" or ")}" }
                return token
            }

            private fun readSBChar(str: String): SonicBattleChar {
                require(str.length == 4 && str.all { "0123456789ABCDEFabcdef".contains(it) }) {
                    "Invalid HEX string: $str" }
                return SonicBattleChar(str.toInt(16).toShort())
            }

            private fun readPBMGlyph(pbm: String): Glyph {
                require(pbm.length <= 256) { "String length > 256" }
                val splits = pbm.split(' ')
                require(splits.size == 4) { "Invalid format: $pbm" }
                require(splits.all { it.isNotEmpty() }) { "Invalid format: $pbm" }
                require(splits[0] == "P1") { "Invalid format: $pbm" }
                require(splits[1].all { "0123456789".contains(it) }) { "Invalid format: $pbm" }
                require(splits[2].all { "0123456789".contains(it) }) { "Invalid format: $pbm" }
                require(splits[1].length <= 2 && splits[2].length <= 2) { "Dimensions too big: $pbm" }
                require(splits[3].all { it == '0' || it == '1' }) { "Invalid format: $pbm" }
                val glyphWidth = splits[1].toInt()
                val glyphHeight = splits[2].toInt()
                val glyphPixels = splits[3]
                require(glyphWidth * glyphHeight == glyphPixels.length) { "Dimensions do not match pixels: $pbm" }
                val bitmap = BooleanArray(glyphPixels.length) { glyphPixels[it] == '1' }
                return Glyph(bitmap, glyphWidth, glyphHeight)
            }

            fun getHexSquareGlyphData(): HexSquareGlyphData {
                val glyphDict = TCharObjectHashMap<Glyph>()
                fun put(glyphDict: TCharObjectHashMap<Glyph>, pbm: String, vararg digits: Char) {
                    val glyph = readPBMGlyph(pbm)
                    digits.forEach { glyphDict.put(it, glyph) }
                }
                put(glyphDict, "P1 4 5 01101001100110010110", '0')
                put(glyphDict, "P1 4 5 00100110001000100010", '1')
                put(glyphDict, "P1 4 5 11100001011010001111", '2')
                put(glyphDict, "P1 4 5 11100001011000011110", '3')
                put(glyphDict, "P1 4 5 10011001111100010001", '4')
                put(glyphDict, "P1 4 5 11111000111000011110", '5')
                put(glyphDict, "P1 4 5 01111000111010010110", '6')
                put(glyphDict, "P1 4 5 11110001001000100010", '7')
                put(glyphDict, "P1 4 5 01101001011010010110", '8')
                put(glyphDict, "P1 4 5 01101001011100010110", '9')
                put(glyphDict, "P1 4 5 01101001111110011001", 'A', 'a')
                put(glyphDict, "P1 4 5 11101001111010011110", 'B', 'b')
                put(glyphDict, "P1 4 5 01111000100010000111", 'C', 'c')
                put(glyphDict, "P1 4 5 11101001100110011110", 'D', 'd')
                put(glyphDict, "P1 4 5 11111000111110001111", 'E', 'e')
                put(glyphDict, "P1 4 5 11111000111110001000", 'F', 'f')
                return HexSquareGlyphData(glyphDict)
            }

        }

    }

}
