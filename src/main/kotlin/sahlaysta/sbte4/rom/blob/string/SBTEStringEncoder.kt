package sahlaysta.sbte4.rom.blob.string

import gnu.trove.list.TByteList
import gnu.trove.list.array.TByteArrayList
import sahlaysta.sbte4.rom.SBTEByteBuffer

/** Encodes Sonic Battle strings to binary. */
object SBTEStringEncoder {

    fun encodeString(cs: CharSequence): SBTEByteBuffer {
        val byteList = TByteArrayList()
        val len = cs.length
        var i = 0
        while (true) {
            if (i >= len) {
                addAll(byteList, 0xFE.toByte(), 0xFF.toByte())
                return SBTEByteBuffer(byteList.toArray())
            }
            when (cs[i]) {
                '[' -> {
                    when {
                        startsWithCaseInsensitive(cs, "[BLACK]", "[black]", i) -> {
                            addAll(byteList, 0xFB.toByte(), 0xFF.toByte(), 0x03.toByte(), 0x00.toByte())
                            i += "[BLACK]".length
                        }
                        startsWithCaseInsensitive(cs, "[RED]", "[red]", i) -> {
                            addAll(byteList, 0xFB.toByte(), 0xFF.toByte(), 0x04.toByte(), 0x00.toByte())
                            i += "[RED]".length
                        }
                        startsWithCaseInsensitive(cs, "[BLUE]", "[blue]", i) -> {
                            addAll(byteList, 0xFB.toByte(), 0xFF.toByte(), 0x05.toByte(), 0x00.toByte())
                            i += "[BLUE]".length
                        }
                        startsWithCaseInsensitive(cs, "[GREEN]", "[green]", i) -> {
                            addAll(byteList, 0xFB.toByte(), 0xFF.toByte(), 0x06.toByte(), 0x00.toByte())
                            i += "[GREEN]".length
                        }
                        startsWithCaseInsensitive(cs, "[PURPLE]", "[purple]", i) -> {
                            addAll(byteList, 0xFB.toByte(), 0xFF.toByte(), 0x07.toByte(), 0x00.toByte())
                            i += "[PURPLE]".length
                        }
                        startsWithBracketedHex(cs, i) -> { //hex "[XXXX]"
                            val sbChar = decodeBracketedHex(cs, i)
                            val sbCharValue = sbChar.value
                            val byte1 = getFirstByteOfShort(sbCharValue)
                            val byte2 = getSecondByteOfShort(sbCharValue)
                            addAll(byteList, byte1, byte2)
                            i += "[XXXX]".length
                        }
                        else -> throw IllegalArgumentException("Bad left bracket")
                    }
                }
                ']' -> throw IllegalArgumentException("Bad right bracket")
                '\r' -> {
                    addAll(byteList, 0xFD.toByte(), 0xFF.toByte())
                    i++
                }
                '\n' -> {
                    if (i == 0 || cs[i - 1] != '\r')
                        addAll(byteList, 0xFD.toByte(), 0xFF.toByte())
                    i++
                }
                else -> {
                    val utf32CodePoint = getUTF32CodePoint(cs, i)
                    val utf32Char = UTF32Char(utf32CodePoint)
                    val cp = SBTECharset.encodeCodePoint(utf32Char)
                        ?: throw IllegalArgumentException("Unknown UTF-32 char $utf32Char")
                    val sbCharValue = cp.sbChar.value
                    val byte1 = getFirstByteOfShort(sbCharValue)
                    val byte2 = getSecondByteOfShort(sbCharValue)
                    addAll(byteList, byte1, byte2)
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

    private fun getUTF32CodePoint(cs: CharSequence, index: Int): Int {
        val high = cs[index]
        if (Character.isHighSurrogate(high) && index + 1 < cs.length) {
            val low = cs[index + 1]
            if (Character.isLowSurrogate(low))
                return Character.toCodePoint(high, low)
        }
        return high.code
    }

    private fun getFirstByteOfShort(s: Short) = ((s.toInt() shr 8) and 0xFF).toByte()
    private fun getSecondByteOfShort(s: Short) = (s.toInt() and 0xFF).toByte()

    private fun addAll(byteList: TByteList, byte1: Byte, byte2: Byte) {
        byteList.add(byte1)
        byteList.add(byte2)
    }

    private fun addAll(byteList: TByteList, byte1: Byte, byte2: Byte, byte3: Byte, byte4: Byte) {
        byteList.add(byte1)
        byteList.add(byte2)
        byteList.add(byte3)
        byteList.add(byte4)
    }

}