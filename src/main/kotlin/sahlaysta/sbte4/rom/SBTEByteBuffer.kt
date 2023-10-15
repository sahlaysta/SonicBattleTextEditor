package sahlaysta.sbte4.rom

/** Stores an array of bytes. */
class SBTEByteBuffer internal constructor(internal val byteArray: ByteArray) {

    val size get() = byteArray.size

    operator fun get(index: Int) = byteArray[index]

}