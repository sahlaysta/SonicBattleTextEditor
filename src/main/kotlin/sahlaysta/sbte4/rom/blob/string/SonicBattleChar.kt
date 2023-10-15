package sahlaysta.sbte4.rom.blob.string

/** A two-byte value of a character in Sonic Battle ROM data. For example, the value 'a' in hex is '2100'. */
@JvmInline
value class SonicBattleChar(val value: Short) {

    override fun toString(): String = String.format("%04X", value)

}