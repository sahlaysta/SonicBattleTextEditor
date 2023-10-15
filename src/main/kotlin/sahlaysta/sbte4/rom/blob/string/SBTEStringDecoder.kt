package sahlaysta.sbte4.rom.blob.string

import sahlaysta.sbte4.rom.SBTEByteBuffer

/** Decodes Sonic Battle binary strings. */
object SBTEStringDecoder {

    fun decodeString(binary: SBTEByteBuffer): String {
        val sb = StringBuilder()
        val len = binary.size
        if (len == 0)
            throw IllegalArgumentException("String blob must not be empty")
        if (len % 2 != 0)
            throw IllegalArgumentException("String blob must have even number of bytes")
        var i = 0
        var fbff = false //color sequence: FBFFXXXX
        var f9ff = false //placeholder sequence: F9FFXXXX
        while (true) {
            val byte1: Byte = binary[i++]
            val byte2: Byte = binary[i++]
            val sbChar = SonicBattleChar(toShort(byte1, byte2))
            when {
                sbChar.value == 0xFEFF.toShort() -> { //FEFF (string end)
                    if (fbff) { sb.append("[FBFF]"); fbff = false }
                    else if (f9ff) { sb.append("[F9FF]"); f9ff = false }
                    if (i == len) {
                        return sb.toString()
                    } else {
                        sb.append("[FEFF]")
                    }
                }
                i == len -> throw IllegalArgumentException("String blob must end in FEFF")
                fbff -> { //color sequence: FBFFXXXX
                    when (sbChar.value) {
                        0x0300.toShort() -> sb.append("[BLACK]")
                        0x0400.toShort() -> sb.append("[RED]")
                        0x0500.toShort() -> sb.append("[BLUE]")
                        0x0600.toShort() -> sb.append("[GREEN]")
                        0x0700.toShort() -> sb.append("[PURPLE]")
                        else -> { sb.append("[FBFF]"); appendBracketedHex(sb, sbChar) }
                    }
                    fbff = false
                }
                f9ff -> { //placeholder sequence: F9FFXXXX
                    sb.append("[F9FF]")
                    appendBracketedHex(sb, sbChar)
                    f9ff = false
                }
                else -> when (sbChar.value) {
                    0xFDFF.toShort() -> sb.append('\n') //FDFF (line break)
                    0xFBFF.toShort() -> fbff = true //color sequence: FBFFXXXX
                    0xF9FF.toShort() -> f9ff = true //placeholder sequence: F9FFXXXX
                    else -> { //normal code point
                        val cp = SBTECharset.decodeCodePoint(sbChar)
                        when {
                            cp == null -> appendBracketedHex(sb, sbChar)
                            cp.hasAlternative && cp.alternativeSBChar == sbChar -> appendBracketedHex(sb, sbChar)
                            cp.utf32Char.value == '['.code || cp.utf32Char.value == ']'.code ->
                                appendBracketedHex(sb, sbChar)
                            else -> sb.appendCodePoint(cp.utf32Char.value)
                        }
                    }
                }
            }
        }
    }

    private fun toShort(byte1: Byte, byte2: Byte) = (byte1.toInt() shl 8 or (byte2.toInt() and 0xFF)).toShort()

    private fun appendBracketedHex(sb: StringBuilder, sbChar: SonicBattleChar) {
        sb.append('[')
        val sbCharValue = sbChar.value.toInt()
        val hexDigits = "0123456789ABCDEF"
        sb.append(hexDigits[sbCharValue shr (3 shl 2) and 0xF])
        sb.append(hexDigits[sbCharValue shr (2 shl 2) and 0xF])
        sb.append(hexDigits[sbCharValue shr (1 shl 2) and 0xF])
        sb.append(hexDigits[sbCharValue shr (0 shl 2) and 0xF])
        sb.append(']')
    }

}