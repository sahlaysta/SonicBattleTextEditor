package sahlaysta.sbte4.gui

import com.fasterxml.jackson.core.JsonEncoding
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import gnu.trove.map.hash.TIntObjectHashMap
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
        val jsonStrings = TIntObjectHashMap<String>()
        val validPtrAddresses = gui.editor.allStrings().map { it.romBlob.pointerAddress }.toHashSet()
        val validLanguages = gui.editor.allStrings().groupBy { it.language }.keys
        val jp: JsonParser = JsonFactory().createParser(File(filePath))
        jp.use {
            val jsonLanguages = HashSet<SBTEStringLanguage>()
            jp.requireNext(JsonToken.START_OBJECT)
            jp.requireNext(JsonToken.FIELD_NAME)
            jp.requireFieldName("strings")
            require(jp.valueAsString == "strings") { "Unexpected key ${jp.valueAsString}" }
            jp.requireNext(JsonToken.START_ARRAY)
            while (jp.requireNext(JsonToken.START_OBJECT, JsonToken.END_ARRAY) == JsonToken.START_OBJECT) {
                val jsonDescriptions = HashSet<SBTEStringDescription>()
                jp.requireNext(JsonToken.FIELD_NAME)
                jp.requireFieldName("language")
                jp.requireNext(JsonToken.VALUE_STRING)
                val language = validLanguages.find { it.name == jp.valueAsString }
                requireNotNull(language) { "Invalid identifier: ${jp.valueAsString}" }
                require(jsonLanguages.add(language)) { "Duplicate identifier: ${jp.valueAsString}" }
                val validDescriptions =
                    gui.editor.allStrings().filter { it.language == language }.groupBy { it.description }.keys
                jp.requireNext(JsonToken.FIELD_NAME)
                jp.requireFieldName("strings")
                jp.requireNext(JsonToken.START_ARRAY)
                while (jp.requireNext(JsonToken.START_OBJECT, JsonToken.END_ARRAY) == JsonToken.START_OBJECT) {
                    jp.requireNext(JsonToken.FIELD_NAME)
                    jp.requireFieldName("description")
                    jp.requireNext(JsonToken.VALUE_STRING)
                    val description = validDescriptions.find { it.name == jp.valueAsString }
                    requireNotNull(description) { "Invalid identifier: ${jp.valueAsString}" }
                    require(jsonDescriptions.add(description)) { "Duplicate identifier: ${jp.valueAsString}" }
                    jp.requireNext(JsonToken.FIELD_NAME)
                    jp.requireFieldName("strings")
                    jp.requireNext(JsonToken.START_OBJECT)
                    while (jp.requireNext(JsonToken.FIELD_NAME, JsonToken.END_OBJECT) == JsonToken.FIELD_NAME) {
                        val key = jp.valueAsString
                        val (strRomType, strPtrAddress) = readStringPointerAddress(key)
                        require(strRomType == romType) { "Mismatch ROM type: $key" }
                        require(validPtrAddresses.contains(strPtrAddress)) { "Invalid key: $key" }
                        require(!jsonStrings.containsKey(strPtrAddress)) { "Duplicate key: $key" }
                        if (jp.requireNext(JsonToken.VALUE_STRING, JsonToken.VALUE_NULL) == JsonToken.VALUE_STRING) {
                            jsonStrings.put(strPtrAddress, jp.valueAsString)
                        } else {
                            jsonStrings.put(strPtrAddress, null)
                        }
                    }
                    jp.requireNext(JsonToken.END_OBJECT)
                }
                jp.requireNext(JsonToken.END_OBJECT)
                validDescriptions.forEach { require(jsonDescriptions.contains(it)) {
                    "Missing group $it in language $language" } }
            }
            jp.requireNext(JsonToken.END_OBJECT)
            validLanguages.forEach { require(jsonLanguages.contains(it)) { "Missing language: $it" } }
        }

        gui.editor.allStrings().forEach { editorString ->
            editorString.callInfo { text, _, _, _ ->
                require(jsonStrings.containsKey(editorString.romBlob.pointerAddress)) {
                    "Missing string " + String.format("$prefix-%06X", editorString.romBlob.pointerAddress) +
                            " in language " + editorString.language + " in group " + editorString.description }
                val jsonString = jsonStrings[editorString.romBlob.pointerAddress]
                if (text == null) {
                    require(jsonString == null) { "Cannot set null string " +
                            String.format("$prefix-%06X", editorString.romBlob.pointerAddress) +
                            " to non-null string" }
                } else {
                    require(jsonString != null) { "Cannot set null string " +
                            String.format("$prefix-%06X", editorString.romBlob.pointerAddress) }
                }
            }
        }

        gui.editor.userModifyingTextBox = false
        try {
            gui.editor.allStrings().forEach { editorString ->
                editorString.callInfo { text, _, _, _ ->
                    val jsonString = jsonStrings[editorString.romBlob.pointerAddress]
                    if (jsonString != text) {
                        editorString.updateText(jsonString)
                        editorString.updateCaret(0)
                        gui.editor.hasUnsavedImportedStrings = true
                    }
                }
            }
        } finally {
            gui.editor.userModifyingTextBox = true
        }

        gui.undoManager.clear()
    }

    private fun JsonParser.requireNext(vararg tokens: JsonToken): JsonToken {
        val token = nextToken()
        require(tokens.contains(token)) { "Unexpected token $token, expected ${tokens.joinToString(" or ")}" }
        return token
    }

    private fun JsonParser.requireFieldName(fieldName: String) {
        require(fieldName == valueAsString) { "Unexpected key \"$valueAsString\", expected \"$fieldName\"" }
    }

    private fun readStringPointerAddress(fieldName: String): Pair<SBTEROMType, Int> {
        val upper = fieldName.uppercase(Locale.ENGLISH)
        val romType = when {
            upper.startsWith("US-") -> SBTEROMType.US
            upper.startsWith("EU-") -> SBTEROMType.EU
            upper.startsWith("JP-") -> SBTEROMType.JP
            else -> throw IllegalArgumentException("Invalid pointer address: $fieldName")
        }
        val hex = fieldName.substring(3)
        val ptrAddress = hex.toInt(16)
        return Pair(romType, ptrAddress)
    }

}