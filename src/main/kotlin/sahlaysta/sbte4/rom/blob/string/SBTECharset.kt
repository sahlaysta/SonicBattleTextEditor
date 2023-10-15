package sahlaysta.sbte4.rom.blob.string

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import gnu.trove.map.hash.TIntObjectHashMap
import gnu.trove.map.hash.TShortObjectHashMap
import sahlaysta.sbte4.gui.GUIUtil
import sahlaysta.sbte4.rom.ImmutableCollections

/** The charset data for decoding/encoding Sonic Battle ROM text. */
object SBTECharset {

    private val decodeMap = TShortObjectHashMap<SBTECodePoint>()
    private val encodeMap = TIntObjectHashMap<SBTECodePoint>()

    /** All chars in the charset. */
    val codePoints: List<SBTECodePoint>

    /** Returns the code point in the charset with the two-byte value. */
    fun decodeCodePoint(sbChar: SonicBattleChar): SBTECodePoint? {
        return decodeMap[sbChar.value]
    }

    /** Returns the code point in the charset with the UTF-32 value. */
    fun encodeCodePoint(utf32Char: UTF32Char): SBTECodePoint? {
        return encodeMap[utf32Char.value]
    }

    init {
        val cps = ArrayList<SBTECodePoint>()
        for (cp: SBTECodePoint in readCharsetRsrc()) {
            require(!encodeMap.containsKey(cp.utf32Char.value)) { "Duplicate UTF-32 code point ${cp.utf32Char}" }
            require(!decodeMap.containsKey(cp.sbChar.value)) { "Duplicate SB code point ${cp.sbChar}" }
            if (cp.hasAlternative) {
                require(!decodeMap.containsKey(cp.alternativeSBChar.value)) {
                    "Duplicate SB code point ${cp.alternativeSBChar}" }
            }

            cps.add(cp)
            encodeMap.put(cp.utf32Char.value, cp)
            decodeMap.put(cp.sbChar.value, cp)
            if (cp.hasAlternative)
                decodeMap.put(cp.alternativeSBChar.value, cp)
        }
        cps.trimToSize()
        codePoints = ImmutableCollections.immutableList(cps)
    }

    private fun readCharsetRsrc() = sequence {
        val inputStream = GUIUtil.getResourceStream("/sonicbattlecharset.json")
        val jp: JsonParser = JsonFactory().createParser(inputStream)
        inputStream.use { jp.use {
            jp.requireNext(JsonToken.START_OBJECT)
            while (jp.requireNext(JsonToken.FIELD_NAME, JsonToken.END_OBJECT) == JsonToken.FIELD_NAME) {
                val utf32Char = readUTF32Char(jp.valueAsString)
                jp.requireNext(JsonToken.START_ARRAY)
                jp.requireNext(JsonToken.VALUE_STRING)
                val sbChar = readSBChar(jp.valueAsString)
                if (jp.requireNext(JsonToken.END_ARRAY, JsonToken.VALUE_STRING) == JsonToken.VALUE_STRING) {
                    val alternativeSBChar = readSBChar(jp.valueAsString)
                    jp.requireNext(JsonToken.END_ARRAY)
                    val codePoint = SBTECodePoint(utf32Char, sbChar, alternativeSBChar)
                    yield(codePoint)
                } else {
                    val codePoint = SBTECodePoint(utf32Char, sbChar)
                    yield(codePoint)
                }
            }
        } }
    }

    private fun JsonParser.requireNext(vararg tokens: JsonToken): JsonToken {
        val token = nextToken()
        require(tokens.contains(token)) { "Unexpected token $token, expected ${tokens.joinToString(" or ")}" }
        return token
    }

    private fun readUTF32Char(str: String): UTF32Char {
        require(str.length in 1..2 && str.codePoints().count() == 1L) { "Invalid single UTF-32 code point: $str" }
        return UTF32Char(str.codePointAt(0))
    }

    private fun readSBChar(str: String): SonicBattleChar {
        require(str.length == 4 && str.all { "0123456789ABCDEFabcdef".contains(it) }) { "Invalid HEX string: $str" }
        return SonicBattleChar(str.toInt(16).toShort())
    }

}