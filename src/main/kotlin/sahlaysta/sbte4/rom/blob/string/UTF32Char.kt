package sahlaysta.sbte4.rom.blob.string

/** A UTF-32 Unicode code point value of a character. The int value may be a surrogate pair. [String.codePoints] */
@JvmInline
value class UTF32Char(val value: Int) {

    override fun toString(): String = StringBuilder().appendCodePoint(value).toString()

}