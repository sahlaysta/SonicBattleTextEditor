package sahlaysta.sbte4.rom

/**
 * A region of memory in a Sonic Battle ROM.
 *
 * @property address the address of the region in the ROM
 * @property size    the number of bytes of the region
 */
class SBTEMemoryRange(val address: Int, val size: Int)