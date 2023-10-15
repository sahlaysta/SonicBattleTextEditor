package sahlaysta.sbte4.gui

import com.fasterxml.jackson.core.JsonEncoding
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import java.awt.Dimension
import java.awt.Point
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.io.File
import javax.swing.JFrame
import javax.swing.SwingUtilities

//the gui user preferences
internal class GUIPrefs(val gui: GUI) {

    private companion object {

        val WINDOW_DEFAULT_SIZE = Dimension(500, 400)

        val DEFAULT_DIVIDER_LOCATION = WINDOW_DEFAULT_SIZE.height - 143

        const val WINDOW_WIDTH_PREF = "windowWidth"
        const val WINDOW_HEIGHT_PREF = "windowHeight"
        const val WINDOW_X_PREF = "windowX"
        const val WINDOW_Y_PREF = "windowY"
        const val WINDOW_MAXIMIZED_PREF = "windowMaximized"
        const val DIVIDER_LOCATION_PREF = "dividerLocation"
        const val DARK_THEME_PREF = "darkTheme"
        const val OPEN_RECENT_FILE_PATHS_PREF = "openRecent"
        const val FILE_CHOOSER_LAST_PREF = "fileChooserLast"
        const val FILE_CHOOSER_PREF_PREFIX = "fileChoosers"

        val PREFS_FILE_PATH = getPrefsFilePath()

        private fun getPrefsFilePath(): String? {
            var runningJarPath: String? = null
            try {
                runningJarPath = File(object { }.javaClass.protectionDomain.codeSource.location.toURI()).toString()
            } catch(_: Exception) { }
            return if (runningJarPath == null) null else "$runningJarPath.prefs"
        }

    }

    private var unmaximizedSize = gui.jFrame.size
    private var unmaximizedLocation = gui.jFrame.location

    init {
        gui.jFrame.addComponentListener(object : ComponentAdapter() {
            override fun componentMoved(e: ComponentEvent?) {
                if (!isMaximized(gui.jFrame))
                    unmaximizedLocation = gui.jFrame.location
            }
            override fun componentResized(e: ComponentEvent?) {
                if (!isMaximized(gui.jFrame))
                    unmaximizedSize = gui.jFrame.size
            }
        })
    }

    private fun isMaximized(jFrame: JFrame) = jFrame.extendedState and JFrame.MAXIMIZED_BOTH != 0

    fun loadPrefs() {
        readPrefs { windowSize, windowLocation, windowMaximized, dividerLocation, darkTheme, openRecentFilePaths,
                    fileChooserLast, fileSelections ->

            gui.jFrame.size = windowSize ?: WINDOW_DEFAULT_SIZE

            if (windowLocation != null) {
                gui.jFrame.location = windowLocation
            } else {
                gui.jFrame.setLocationRelativeTo(null)
            }

            if (windowSize != null) unmaximizedSize = windowSize
            if (windowLocation != null) unmaximizedLocation = windowLocation

            if (windowMaximized == true) {
                gui.jFrame.extendedState = gui.jFrame.extendedState or JFrame.MAXIMIZED_BOTH
            }

            SwingUtilities.invokeLater {
                gui.editor.jSplitPane.dividerLocation = dividerLocation ?: DEFAULT_DIVIDER_LOCATION
            }

            if (darkTheme == false) {
                gui.menuBar.darkThemeJMenuItem.isSelected = false
                gui.actions.darkTheme.action()
            }

            openRecentFilePaths?.reversed()?.forEach { it?.let { gui.openRecentMenu.addFilePath(it) } }

            fileChooserLast?.let { gui.fileChooser.lastDirectory = it }

            gui.fileChooser.fileSelections.putAll(fileSelections)

        }
    }

    private fun readPrefs(fn: (windowSize: Dimension?,
                               windowLocation: Point?,
                               windowMaximized: Boolean?,
                               dividerLocation: Int?,
                               darkTheme: Boolean?,
                               openRecentFilePaths: Array<String?>?,
                               fileChooserLast: String?,
                               fileSelections: Map<String, GUIFileChooser.FileSelection>) -> Unit) {
        var windowWidth: Int? = null
        var windowHeight: Int? = null
        var windowX: Int? = null
        var windowY: Int? = null
        var windowMaximized: Boolean? = null
        var dividerLocation: Int? = null
        var darkTheme: Boolean? = null
        var openRecentFilePaths: Array<String?>? = null
        var fileChooserLast: String? = null
        val fileChooserFileFilterIndices = HashMap<String, Int?>()
        val fileChooserFileDirectories = HashMap<String, String?>()

        if (PREFS_FILE_PATH != null && File(PREFS_FILE_PATH).exists()) {
            try {
                val jp: JsonParser = JsonFactory().createParser(File(PREFS_FILE_PATH))
                jp.use {
                    jp.requireNext(JsonToken.START_OBJECT)
                    while (jp.requireNext(JsonToken.FIELD_NAME, JsonToken.END_OBJECT) == JsonToken.FIELD_NAME) {
                        val key = jp.valueAsString
                        jp.nextToken()
                        when (key) {
                            WINDOW_WIDTH_PREF -> windowWidth = jp.intOrNull()
                            WINDOW_HEIGHT_PREF -> windowHeight = jp.intOrNull()
                            WINDOW_X_PREF -> windowX = jp.intOrNull()
                            WINDOW_Y_PREF -> windowY = jp.intOrNull()
                            WINDOW_MAXIMIZED_PREF -> windowMaximized = jp.booleanOrNull()
                            DIVIDER_LOCATION_PREF -> dividerLocation = jp.intOrNull()
                            DARK_THEME_PREF -> darkTheme = jp.booleanOrNull()
                            OPEN_RECENT_FILE_PATHS_PREF -> openRecentFilePaths = jp.stringArrayOrNull()
                            FILE_CHOOSER_LAST_PREF -> fileChooserLast = jp.stringOrNull()
                            else -> {
                                if (key.startsWith("$FILE_CHOOSER_PREF_PREFIX.")) {
                                    val prefixless = key.removePrefix("$FILE_CHOOSER_PREF_PREFIX.")
                                    if (key.endsWith(".fileFilterIndex")) {
                                        val suffixless = prefixless.removeSuffix(".fileFilterIndex")
                                        fileChooserFileFilterIndices[suffixless] = jp.intOrNull()
                                    } else if (key.endsWith(".fileDirectory")) {
                                        val suffixless = prefixless.removeSuffix(".fileDirectory")
                                        fileChooserFileDirectories[suffixless] = jp.stringOrNull()
                                    }
                                }
                            }
                        }
                    }
                }
            } catch(e: Exception) {
                System.err.println("Failed to load prefs")
                e.printStackTrace()
            }
        }

        val windowLocation = run { Point(windowX ?: return@run null, windowY ?: return@run null) }
        val windowSize = run { Dimension(windowWidth ?: return@run null, windowHeight ?: return@run null) }
        val fileSelections = HashMap<String, GUIFileChooser.FileSelection>().apply {
            for (key in fileChooserFileFilterIndices.keys + fileChooserFileDirectories.keys) {
                val fileFilterIndex = fileChooserFileFilterIndices[key]
                val fileDirectory = fileChooserFileDirectories[key]
                if (fileFilterIndex != null && fileDirectory != null)
                    put(key, GUIFileChooser.FileSelection(fileFilterIndex, fileDirectory))
            }
        }
        fn(windowSize, windowLocation, windowMaximized, dividerLocation, darkTheme, openRecentFilePaths,
            fileChooserLast, fileSelections)
    }

    private fun JsonParser.requireNext(vararg tokens: JsonToken): JsonToken {
        val token = nextToken()
        require(tokens.contains(token)) { "Unexpected token $token, expected ${tokens.joinToString(" or ")}" }
        return token
    }

    private fun JsonParser.intOrNull() = if (currentToken == JsonToken.VALUE_NULL) null else valueAsInt
    private fun JsonParser.booleanOrNull() = if (currentToken == JsonToken.VALUE_NULL) null else valueAsBoolean
    private fun JsonParser.stringOrNull() = if (currentToken == JsonToken.VALUE_NULL) null else valueAsString
    private fun JsonParser.stringArrayOrNull(): Array<String?>? {
        if (currentToken == JsonToken.VALUE_NULL) return null
        val list = ArrayList<String?>()
        while (nextToken() != JsonToken.END_ARRAY)
            list.add(stringOrNull())
        return list.toTypedArray()
    }

    fun writePrefs() {
        if (PREFS_FILE_PATH == null) return
        try {
            val jg: JsonGenerator = JsonFactory().createGenerator(File(PREFS_FILE_PATH), JsonEncoding.UTF8)
            jg.setPrettyPrinter(object : DefaultPrettyPrinter() {
                init {
                    val indenter = DefaultIndenter("    ", DefaultIndenter.SYS_LF)
                    indentObjectsWith(indenter)
                    indentArraysWith(indenter)
                }
                override fun writeObjectFieldValueSeparator(g: JsonGenerator) = g.writeRaw(": ")
            })
            jg.use {
                jg.writeStartObject()
                jg.writeFieldName(WINDOW_WIDTH_PREF)
                jg.writeNumber(unmaximizedSize.width)
                jg.writeFieldName(WINDOW_HEIGHT_PREF)
                jg.writeNumber(unmaximizedSize.height)
                jg.writeFieldName(WINDOW_X_PREF)
                jg.writeNumber(unmaximizedLocation.x)
                jg.writeFieldName(WINDOW_Y_PREF)
                jg.writeNumber(unmaximizedLocation.y)
                jg.writeFieldName(WINDOW_MAXIMIZED_PREF)
                jg.writeBoolean(isMaximized(gui.jFrame))
                jg.writeFieldName(DIVIDER_LOCATION_PREF)
                jg.writeNumber(gui.editor.jSplitPane.dividerLocation)
                jg.writeFieldName(DARK_THEME_PREF)
                jg.writeBoolean(gui.menuBar.darkThemeJMenuItem.isSelected)
                jg.writeFieldName(OPEN_RECENT_FILE_PATHS_PREF)
                jg.writeStartArray()
                gui.openRecentMenu.filePaths.forEach { jg.writeString(it) }
                jg.writeEndArray()
                jg.writeFieldName(FILE_CHOOSER_LAST_PREF)
                jg.writeString(gui.fileChooser.lastDirectory)
                gui.fileChooser.fileSelections.forEach { (key, fileSelection) ->
                    jg.writeFieldName("$FILE_CHOOSER_PREF_PREFIX.$key.fileFilterIndex")
                    jg.writeNumber(fileSelection.fileFilterIndex)
                    jg.writeFieldName("$FILE_CHOOSER_PREF_PREFIX.$key.fileDirectory")
                    jg.writeString(fileSelection.fileDirectory)
                }
                jg.writeEndObject()
            }
        } catch (e: Exception) {
            System.err.println("Failed to write prefs")
            e.printStackTrace()
        }
    }

}