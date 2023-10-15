package sahlaysta.sbte4.rom

/**
 * A Sonic Battle ROM data object.
 *
 * @property binary         the raw bytes of the object, the initial value is null if it could not be found
 * @property pointerAddress the address of the pointer in the ROM that points to the object
 */
class SBTEMutableBlob(var binary: SBTEByteBuffer?, val pointerAddress: Int)