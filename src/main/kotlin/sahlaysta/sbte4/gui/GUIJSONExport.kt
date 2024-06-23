package sahlaysta.sbte4.gui

import com.fasterxml.jackson.core.JsonEncoding
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import gnu.trove.map.hash.TIntObjectHashMap
import gnu.trove.set.hash.TIntHashSet
import sahlaysta.sbte4.rom.SBTEROMType
import sahlaysta.sbte4.rom.SBTEStringDescription
import sahlaysta.sbte4.rom.SBTEStringLanguage
import java.io.File
import java.util.Locale

//export and import the strings to JSON
internal class GUIJSONExport(val gui: GUI) {

    fun exportJSON(filePath: String) {
        gui.editor.waitForBackgroundThread()
        val romType = gui.editor.romData!!.romType
        val prefix = when (romType) { SBTEROMType.US -> "US"; SBTEROMType.EU -> "EU"; SBTEROMType.JP -> "JP" }
        val jg: JsonGenerator = JsonFactory().createGenerator(File(filePath), JsonEncoding.UTF8)
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
            jg.writeFieldName("strings")
            jg.writeStartArray()
            gui.editor.stringData!!.forEach { (language, map) ->
                jg.writeStartObject()
                jg.writeFieldName("language")
                jg.writeString(language.toString())
                jg.writeFieldName("strings")
                jg.writeStartArray()
                map.forEach { (description, editorStrings) ->
                    jg.writeStartObject()
                    jg.writeFieldName("description")
                    jg.writeString(description.toString())
                    jg.writeFieldName("strings")
                    jg.writeStartObject()
                    for (editorString in editorStrings) {
                        jg.writeFieldName(String.format("$prefix-%06X", editorString.romBlob.pointerAddress))
                        jg.writeString(editorString.callInfo { text, _, _, _ -> text })
                    }
                    jg.writeEndObject()
                    jg.writeEndObject()
                }
                jg.writeEndArray()
                jg.writeEndObject()
            }
            jg.writeEndArray()
        }
    }

    fun importJSON(filePath: String) {
        gui.editor.waitForBackgroundThread()
        val romType = gui.editor.romData!!.romType
        val prefix = when (romType) { SBTEROMType.US -> "US"; SBTEROMType.EU -> "EU"; SBTEROMType.JP -> "JP" }
        val jsonStrings = TIntObjectHashMap<String?>()
        val jsonDuplicateStrings = TIntHashSet()
        val validPtrAddresses = gui.editor.allStrings().map { it.romBlob.pointerAddress }.toHashSet()
        val errorLog = ArrayList<String>()
        val jp: JsonParser = JsonFactory().createParser(File(filePath))
        jp.use {
            jp.requireNext(JsonToken.START_OBJECT)
            jp.requireNext(JsonToken.FIELD_NAME)
            jp.requireFieldName("strings")
            require(jp.valueAsString == "strings") { "Unexpected key ${jp.valueAsString}, expected \"strings\"" }
            jp.requireNext(JsonToken.START_ARRAY)
            while (jp.requireNext(JsonToken.START_OBJECT, JsonToken.END_ARRAY) == JsonToken.START_OBJECT) {
                jp.requireNext(JsonToken.FIELD_NAME)
                jp.requireFieldName("language")
                jp.requireNext(JsonToken.VALUE_STRING)
                jp.requireNext(JsonToken.FIELD_NAME)
                jp.requireFieldName("strings")
                jp.requireNext(JsonToken.START_ARRAY)
                while (jp.requireNext(JsonToken.START_OBJECT, JsonToken.END_ARRAY) == JsonToken.START_OBJECT) {
                    jp.requireNext(JsonToken.FIELD_NAME)
                    jp.requireFieldName("description")
                    jp.requireNext(JsonToken.VALUE_STRING)
                    jp.requireNext(JsonToken.FIELD_NAME)
                    jp.requireFieldName("strings")
                    jp.requireNext(JsonToken.START_OBJECT)
                    while (jp.requireNext(JsonToken.FIELD_NAME, JsonToken.END_OBJECT) == JsonToken.FIELD_NAME) {
                        val key = jp.valueAsString
                        val value = if (jp.requireNext(JsonToken.VALUE_STRING, JsonToken.VALUE_NULL)
                            == JsonToken.VALUE_NULL) null else jp.valueAsString
                        val pair = readStringPointerAddress(key)
                        if (pair == null) {
                            errorLog.add("Invalid key: $key")
                        } else {
                            val (strRomType, strPtrAddress) = pair
                            if (strRomType != romType) {
                                errorLog.add("Mismatch ROM type: $key")
                            } else if (!validPtrAddresses.contains(strPtrAddress)) {
                                errorLog.add("Invalid key: $key")
                            } else if (jsonStrings.containsKey(strPtrAddress)) {
                                errorLog.add("Duplicate key: $key")
                                jsonDuplicateStrings.add(strPtrAddress)
                            } else {
                                jsonStrings.put(strPtrAddress, value)
                            }
                        }
                    }
                    jp.requireNext(JsonToken.END_OBJECT)
                }
                jp.requireNext(JsonToken.END_OBJECT)
            }
            jp.requireNext(JsonToken.END_OBJECT)
        }
        jsonDuplicateStrings.forEach { jsonStrings.remove(it); true }

        gui.editor.allStrings().forEach { editorString ->
            editorString.callInfo { text, _, _, _ ->
                if (!jsonStrings.containsKey(editorString.romBlob.pointerAddress)) {
                    if (!jsonDuplicateStrings.contains(editorString.romBlob.pointerAddress)) {
                        errorLog.add("Missing string " +
                                String.format("$prefix-%06X", editorString.romBlob.pointerAddress) +
                                " (" + editorString.language + ", " + editorString.description + ")")
                    }
                } else {
                    val jsonString = jsonStrings[editorString.romBlob.pointerAddress]
                    if (text != null && jsonString == null) {
                        errorLog.add("Cannot set null string " +
                                String.format("$prefix-%06X", editorString.romBlob.pointerAddress) +
                                " to non-null string")
                        jsonStrings.remove(editorString.romBlob.pointerAddress)
                    } else if (text == null && jsonString != null) {
                        errorLog.add("Cannot set null string " +
                                String.format("$prefix-%06X", editorString.romBlob.pointerAddress))
                        jsonStrings.remove(editorString.romBlob.pointerAddress)
                    }
                }
            }
        }

        gui.editor.userModifyingTextBox = false
        try {
            gui.editor.allStrings().forEach { editorString ->
                editorString.callInfo { text, _, _, _ ->
                    if (jsonStrings.containsKey(editorString.romBlob.pointerAddress)) {
                        val jsonString = jsonStrings[editorString.romBlob.pointerAddress]
                        if (jsonString != text) {
                            editorString.updateText(jsonString)
                            editorString.updateCaret(0)
                            gui.editor.hasUnsavedImportedStrings = true
                        }
                    }
                }
            }
        } finally {
            gui.editor.userModifyingTextBox = true
        }

        gui.undoManager.clear()

        if (errorLog.isNotEmpty()) {
            GUIDialogs.showTextBlockMsgDialog(gui.jFrame,
                "Import succeeded with errors:\n" + errorLog.joinToString("\n"),
                gui.menuBar.jsonImportJMenuItem.text)
        }
    }

    private fun JsonParser.requireNext(vararg tokens: JsonToken): JsonToken {
        val token = nextToken()
        require(tokens.contains(token)) { "Unexpected token $token, expected ${tokens.joinToString(" or ")}" }
        return token
    }

    private fun JsonParser.requireFieldName(fieldName: String) {
        require(fieldName == valueAsString) { "Unexpected key \"$valueAsString\", expected \"$fieldName\"" }
    }

    private fun readStringPointerAddress(fieldName: String): Pair<SBTEROMType, Int>? {
        val upper = fieldName.uppercase(Locale.ENGLISH)
        val romType = when {
            upper.startsWith("US-") -> SBTEROMType.US
            upper.startsWith("EU-") -> SBTEROMType.EU
            upper.startsWith("JP-") -> SBTEROMType.JP
            else -> return null
        }
        val hex = fieldName.substring(3)
        if (hex.length != 6 && !hex.all { "ABCDEFabcdef0123456789".contains(it) }) {
            return null
        }
        val ptrAddress = hex.toInt(16)
        return Pair(romType, ptrAddress)
    }

}
