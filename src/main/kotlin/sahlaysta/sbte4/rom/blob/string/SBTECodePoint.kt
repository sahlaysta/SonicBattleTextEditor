package sahlaysta.sbte4.rom.blob.string

/**
 * A charset code point of Sonic Battle text.
 *
 * @property utf32Char         the UTF-32 value of the character
 * @property sbChar            the two-byte value of the character in Sonic Battle ROM data
 * @property hasAlternative    true if this code point has an alternative two-byte value in Sonic Battle ROM data
 * @property alternativeSBChar the alternative/duplicate two-byte code point of the character
 *                             (some characters have duplicates, like 'ò' does)
 */
class SBTECodePoint(val utf32Char: UTF32Char, val sbChar: SonicBattleChar,
                    val hasAlternative: Boolean, val alternativeSBChar: SonicBattleChar
) {

    constructor(utf32Char: UTF32Char, sbChar: SonicBattleChar)
            : this(utf32Char, sbChar, false, SonicBattleChar(0))

    constructor(utf32Char: UTF32Char, sbChar: SonicBattleChar, alternativeSBChar: SonicBattleChar)
            : this(utf32Char, sbChar, true, alternativeSBChar)

}