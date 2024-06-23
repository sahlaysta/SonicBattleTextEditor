package sahlaysta.sbte4.rom

import com.google.common.collect.Range
import com.google.common.collect.TreeRangeSet
import gnu.trove.list.array.TByteArrayList

/** The Sonic Battle ROM type. */
enum class SBTEROMType { US, EU, JP }

/** The dialogue languages of Sonic Battle. */
enum class SBTEStringLanguage { JAPANESE, ENGLISH, FRENCH, GERMAN, SPANISH, ITALIAN }

/** The names for the dialogue groups in Sonic Battle. */
enum class SBTEStringDescription {
    STORY_MODE, EMERL_CARD_DESCRIPTIONS, OPTIONS_MENU, BATTLE_MENU, BATTLE_RULES_MENU, TRAINING_MODE_MENU,
    MINIGAME_MENU, BATTLE_RECORD_MENU, CAPTURED_SKILL, STORY_MODE_MENU, EMERL_SKILLS }

/** A group of strings that were read from a Sonic Battle ROM. */
class SBTEStringGroup(val strings: List<SBTEMutableBlob>,
                      val language: SBTEStringLanguage, val description: SBTEStringDescription)

/**
 * The string data that was read from a Sonic Battle ROM.
 *
 * @property stringGroups the string groups that were read
 * @property memoryRanges the regions of memory consisting the strings that were read
 */
class SBTEROMStringData(val stringGroups: List<SBTEStringGroup>, val memoryRanges: List<SBTEMemoryRange>)

/**
 * The data that was read from a Sonic Battle ROM.
 *
 * @property rom           the ROM
 * @property romType       the type of ROM
 * @property romStringData the strings that were read from the ROM
 */
class SBTEROMData(val rom: ByteArray, val romType: SBTEROMType, val romStringData: SBTEROMStringData)

class SBTEROMReadException(msg: String? = null) : RuntimeException(msg)

class SBTEROMSaveException(msg: String? = null) : RuntimeException(msg)

object SBTEROM {

    /** Read the Sonic Battle ROM data. */
    fun readROMData(rom: ByteArray): SBTEROMData {
        val romType: SBTEROMType = readROMType(rom) ?: throw SBTEROMReadException("Failed to read ROM type")
        val romStringData: SBTEROMStringData = readROMStringData(rom, romType)
        return SBTEROMData(rom, romType, romStringData)
    }

    /** Save the Sonic Battle ROM data. */
    fun saveROMData(romData: SBTEROMData) {
        saveROMStringData(romData.romStringData, romData.rom)
    }

    private fun readROMType(rom: ByteArray): SBTEROMType? {
        if (rom.size <= 0xAF) return null
        return when (rom[0xAF]) {
            0x45.toByte() -> SBTEROMType.US
            0x50.toByte() -> SBTEROMType.EU
            0x4A.toByte() -> SBTEROMType.JP
            else -> null
        }
    }

    private const val POINTER_DELIM = 0x08.toByte()

    private fun readROMStringData(rom: ByteArray, romType: SBTEROMType): SBTEROMStringData {
        val ptrGroups: Array<SBTEStringPointerGroup> = when (romType) {
            SBTEROMType.US -> usROMStringPointers
            SBTEROMType.EU -> euROMStringPointers
            SBTEROMType.JP -> jpROMStringPointers
        }
        val byteList = TByteArrayList()
        val memoryRangeSet = TreeRangeSet.create<Int>()
        val stringGroups = Array(size = ptrGroups.size) { ptrGroupIndex ->
            val ptrGroup = ptrGroups[ptrGroupIndex]
            val language = ptrGroup.language
            val description = ptrGroup.description
            val strings = Array(size = ptrGroup.count) { stringIndex ->
                val ptrAddress = ptrGroup.address + (stringIndex * 4)
                if (rom[ptrAddress + 3] != POINTER_DELIM) {
                    SBTEMutableBlob(null, ptrAddress) //invalid pointer, nulled blob
                } else {
                    byteList.resetQuick()
                    val ptrValue = int24LE(rom[ptrAddress + 0], rom[ptrAddress + 1], rom[ptrAddress + 2])
                    var pos = ptrValue
                    while (true) {
                        val byte1 = rom[pos++]
                        val byte2 = rom[pos++]
                        byteList.add(byte1)
                        byteList.add(byte2)
                        if (byte1 == 0xFE.toByte() && byte2 == 0xFF.toByte()) { //string end (FEFF)
                            //also include consecutive trailing FEFF's in the size
                            while (pos < rom.size - 2 && rom[pos] == 0xFE.toByte() && rom[pos + 1] == 0xFF.toByte()) {
                                pos += 2
                            }
                            break
                        }
                    }
                    memoryRangeSet.add(Range.closed(ptrValue, pos))
                    SBTEMutableBlob(SBTEByteBuffer(byteList.toArray()), ptrAddress)
                }
            }.let { ImmutableCollections.immutableList(it) }
            SBTEStringGroup(strings, language, description)
        }.let { ImmutableCollections.immutableList(it) }
        val memoryRanges = memoryRangeSet.asRanges()
            .map { SBTEMemoryRange(it.lowerEndpoint(), it.upperEndpoint() - it.lowerEndpoint()) }
            .let { ImmutableCollections.immutableList(it) }
        return SBTEROMStringData(stringGroups, memoryRanges)
    }

    //save strings in BEST-FIT DECREASING manner
    private fun saveROMStringData(romStringData: SBTEROMStringData, rom: ByteArray) {
        class StringBlob(val pointerAddress: Int, val byteArray: ByteArray)
        val stringBlobs = ArrayList<StringBlob>()
        for (stringGroup in romStringData.stringGroups) {
            for (stringBlob in stringGroup.strings) {
                val pointerAddress = stringBlob.pointerAddress
                val binary = stringBlob.binary
                if (binary != null) {
                    stringBlobs.add(StringBlob(pointerAddress, binary.byteArray))
                }
            }
        }

        class MemoryRange(val address: Int, val size: Int, var sizeUsed: Int = 0)
        val memoryRanges = romStringData.memoryRanges.mapToArray { MemoryRange(it.address, it.size) }
        memoryRanges.sortBy { it.size }
        stringBlobs.sortByDescending { it.byteArray.size }

        for (stringBlob in stringBlobs) {
            val memoryRange = memoryRanges.find { it.sizeUsed == 0 && it.size >= stringBlob.byteArray.size }
                ?: memoryRanges.find { it.size - it.sizeUsed >= stringBlob.byteArray.size }
                ?: throw SBTEROMSaveException("Too much data to save")
            val stringBlobOffset = memoryRange.address + memoryRange.sizeUsed
            stringBlob.byteArray.copyInto(rom, stringBlobOffset, 0, stringBlob.byteArray.size)
            rom[stringBlob.pointerAddress + 0] = int24LEbyte1(stringBlobOffset)
            rom[stringBlob.pointerAddress + 1] = int24LEbyte2(stringBlobOffset)
            rom[stringBlob.pointerAddress + 2] = int24LEbyte3(stringBlobOffset)
            rom[stringBlob.pointerAddress + 3] = POINTER_DELIM
            memoryRange.sizeUsed += stringBlob.byteArray.size
        }

        //fill remaining empty space with trailing FEFF's
        for (memoryRange in memoryRanges) {
            if (memoryRange.sizeUsed < memoryRange.size) {
                var pos = memoryRange.address + memoryRange.sizeUsed
                while (pos < memoryRange.address + memoryRange.size) {
                    rom[pos++] = 0xFE.toByte()
                    rom[pos++] = 0xFF.toByte()
                }
            }
        }
    }

    //convert int24 little-endian
    private fun int24LE(byte1: Byte, byte2: Byte, byte3: Byte): Int =
        (byte3.toInt() and 0xFF shl 16) or (byte2.toInt() and 0xFF shl 8) or (byte1.toInt() and 0xFF)
    private fun int24LEbyte1(value: Int): Byte = value.toByte()
    private fun int24LEbyte2(value: Int): Byte = (value ushr 8).toByte()
    private fun int24LEbyte3(value: Int): Byte = (value ushr 16).toByte()

    private inline fun <T, reified R> Collection<T>.mapToArray(transform: (T) -> R): Array<R> {
        val iterator = iterator()
        return Array(size) { transform(iterator.next()) }
    }

}
