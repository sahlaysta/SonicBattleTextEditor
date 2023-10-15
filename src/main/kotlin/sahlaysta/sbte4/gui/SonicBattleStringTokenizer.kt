package sahlaysta.sbte4.gui

import sahlaysta.sbte4.rom.blob.string.SBTECharset
import sahlaysta.sbte4.rom.blob.string.SBTECodePoint
import sahlaysta.sbte4.rom.blob.string.SonicBattleChar
import sahlaysta.sbte4.rom.blob.string.UTF32Char

//the more detailed tokenizer for Sonic Battle strings. [SBTEStringEncoder]
internal object SonicBattleStringTokenizer {
    
    sealed class Token {
        data class CodePointToken(val codePoint: SBTECodePoint) : Token()
        data class AlternativeCodePointToken(val codePoint: SBTECodePoint) : Token()
        data class UnknownUTF32CharToken(val utf32Char: UTF32Char) : Token()
        data class UnknownSonicBattleCharToken(val sbChar: SonicBattleChar) : Token()
        data object NewLineToken : Token()
        data object BlackToken : Token()
        data object RedToken : Token()
        data object BlueToken : Token()
        data object GreenToken : Token()
        data object PurpleToken : Token()
        data object BadLeftBracketToken : Token()
        data object BadRightBracketToken : Token()
    }

    fun tokenizeSonicBattleString(cs: CharSequence) = sequence {
        val len = cs.length
        var i = 0
        var fbff = false
        var f9ff = false
        fun yieldFBFFF9FF(): Token? {
            if (fbff) { fbff = false; return Token.UnknownSonicBattleCharToken(SonicBattleChar(0xFBFF.toShort())) }
            if (f9ff) { f9ff = false; return Token.UnknownSonicBattleCharToken(SonicBattleChar(0xF9FF.toShort())) }
            return null
        }
        while (true) {
            if (i >= len) {
                yieldFBFFF9FF()?.let { yield(it) }
                break
            }
            when (cs[i]) {
                '[' -> {
                    when {
                        startsWithCaseInsensitive(cs, "[BLACK]", "[black]", i) -> {
                            yieldFBFFF9FF()?.let { yield(it) }
                            yield(Token.BlackToken)
                            i += "[BLACK]".length
                        }
                        startsWithCaseInsensitive(cs, "[RED]", "[red]", i) -> {
                            yieldFBFFF9FF()?.let { yield(it) }
                            yield(Token.RedToken)
                            i += "[RED]".length
                        }
                        startsWithCaseInsensitive(cs, "[BLUE]", "[blue]", i) -> {
                            yieldFBFFF9FF()?.let { yield(it) }
                            yield(Token.BlueToken)
                            i += "[BLUE]".length
                        }
                        startsWithCaseInsensitive(cs, "[GREEN]", "[green]", i) -> {
                            yieldFBFFF9FF()?.let { yield(it) }
                            yield(Token.GreenToken)
                            i += "[GREEN]".length
                        }
                        startsWithCaseInsensitive(cs, "[PURPLE]", "[purple]", i) -> {
                            yieldFBFFF9FF()?.let { yield(it) }
                            yield(Token.PurpleToken)
                            i += "[PURPLE]".length
                        }
                        startsWithBracketedHex(cs, i) -> { //hex "[XXXX]"
                            val sbChar = decodeBracketedHex(cs, i)
                            when {
                                fbff -> { //color sequence: [FBFFXXXX]
                                    when (sbChar.value) {
                                        0x0300.toShort() -> yield(Token.BlackToken)
                                        0x0400.toShort() -> yield(Token.RedToken)
                                        0x0500.toShort() -> yield(Token.BlueToken)
                                        0x0600.toShort() -> yield(Token.GreenToken)
                                        0x0700.toShort() -> yield(Token.PurpleToken)
                                        else -> {
                                            yield(Token.UnknownSonicBattleCharToken(SonicBattleChar(0xFBFF.toShort())))
                                            yield(Token.UnknownSonicBattleCharToken(sbChar))
                                        }
                                    }
                                    fbff = false
                                }
                                f9ff -> { //placeholder sequence: [F9FFXXXX]
                                    yield(Token.UnknownSonicBattleCharToken(SonicBattleChar(0xF9FF.toShort())))
                                    yield(Token.UnknownSonicBattleCharToken(sbChar))
                                    f9ff = false
                                }
                                else -> when (sbChar.value) {
                                    0xFDFF.toShort() -> yield(Token.NewLineToken)
                                    0xFBFF.toShort() -> fbff = true
                                    0xF9FF.toShort() -> f9ff = true
                                    else -> {
                                        val cp = SBTECharset.decodeCodePoint(sbChar)
                                        if (cp == null) {
                                            yield(Token.UnknownSonicBattleCharToken(sbChar))
                                        } else {
                                            if (cp.hasAlternative && cp.alternativeSBChar == sbChar) {
                                                yield(Token.AlternativeCodePointToken(cp))
                                            } else {
                                                yield(Token.CodePointToken(cp))
                                            }
                                        }
                                    }
                                }
                            }
                            i += "[XXXX]".length
                        }
                        else -> {
                            yieldFBFFF9FF()?.let { yield(it) }
                            yield(Token.BadLeftBracketToken)
                            i++
                        }
                    }
                }
                ']' -> {
                    yieldFBFFF9FF()?.let { yield(it) }
                    yield(Token.BadRightBracketToken)
                    i++
                }
                '\r' -> {
                    yieldFBFFF9FF()?.let { yield(it) }
                    yield(Token.NewLineToken)
                    i++
                }
                '\n' -> {
                    if (i == 0 || cs[i - 1] != '\r') {
                        yieldFBFFF9FF()?.let { yield(it) }
                        yield(Token.NewLineToken)
                    }
                    i++
                }
                else -> {
                    val utf32CodePoint = getUTF32CodePoint(cs, i)
                    val utf32Char = UTF32Char(utf32CodePoint)
                    val cp = SBTECharset.encodeCodePoint(utf32Char)
                    if (cp == null) {
                        yieldFBFFF9FF()?.let { yield(it) }
                        yield(Token.UnknownUTF32CharToken(utf32Char))
                    } else {
                        val sbChar = cp.sbChar
                        when {
                            fbff -> { //color sequence: [FBFFXXXX]
                                when (sbChar.value) {
                                    0x0300.toShort() -> yield(Token.BlackToken)
                                    0x0400.toShort() -> yield(Token.RedToken)
                                    0x0500.toShort() -> yield(Token.BlueToken)
                                    0x0600.toShort() -> yield(Token.GreenToken)
                                    0x0700.toShort() -> yield(Token.PurpleToken)
                                    else -> {
                                        yield(Token.UnknownSonicBattleCharToken(SonicBattleChar(0xFBFF.toShort())))
                                        yield(Token.UnknownSonicBattleCharToken(sbChar))
                                    }
                                }
                                fbff = false
                            }
                            f9ff -> { //placeholder sequence: [F9FFXXXX]
                                yield(Token.UnknownSonicBattleCharToken(SonicBattleChar(0xF9FF.toShort())))
                                yield(Token.UnknownSonicBattleCharToken(sbChar))
                                f9ff = false
                            }
                            else -> when (sbChar.value) {
                                0xFDFF.toShort() -> yield(Token.NewLineToken)
                                0xFBFF.toShort() -> fbff = true
                                0xF9FF.toShort() -> f9ff = true
                                else -> yield(Token.CodePointToken(cp))
                            }
                        }
                    }
                    i += Character.charCount(utf32CodePoint)
                }
            }
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

    private const val DIGITS_16_OFF = 48
    private val digits16 = intArrayOf(
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, //'0' to '9'
        -1, -1, -1, -1, -1, -1, -1, //...
        10, 11, 12, 13, 14, 15, //'A' to 'F'
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, //...
        10, 11, 12, 13, 14, 15 //'a' to 'f'
    )
    private fun digit16(ch: Char): Int = digits16[ch.code - DIGITS_16_OFF]

    private fun decodeBracketedHex(cs: CharSequence, startIndex: Int): SonicBattleChar {
        return SonicBattleChar(
            ((digit16(cs[startIndex + 1]) shl 12)
                    or (digit16(cs[startIndex + 2]) shl 8)
                    or (digit16(cs[startIndex + 3]) shl 4)
                    or (digit16(cs[startIndex + 4]))).toShort())
    }

}