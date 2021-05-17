package sbte.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sbte.parser.SonicBattleROMReader.ROM;
import sbte.parser.SonicBattleROMReader.SonicBattleLine;
import sbte.util.ByteTools;
import sbte.util.FileTools;

public class SonicBattleROMSaver {
	public static final byte[] POINTER_DELIMITER = new byte[] { 0x08 };
	
	public static void saveToUSAROM(File savePath, ROM rom, List<SonicBattleLine> data) throws IOException {
		final byte[] delimiter = SonicBattleROMReader.delimiter;
		final int groupCount = getHighestGroupValue(data);
		for (int i = 0; i < groupCount; i++)
			writeGroup(i, data, rom.content, delimiter);
		FileTools.writeByteArrayToFile(savePath, rom.content);
	}
	private static void writeGroup(int group, List<SonicBattleLine> data, byte[] rom, byte[] delimiter) {
		int pos = -1;
		int pointerPos = -1;
		for (SonicBattleLine sbl: data) { //get the first pointer (position to start writing). (loops only once)
			if (sbl.group != group) continue;
			byte[] posPointer = new byte[] {
					rom[sbl.pointer + 2],
					rom[sbl.pointer + 1],
					rom[sbl.pointer],
			};
			pos = ByteTools.hexToInt(ByteTools.toHexString(posPointer));
			pointerPos = sbl.pointer;
			
			break; //loop group only once
		}
		
		
		final List<byte[]> pointers = new ArrayList<>();
		for (SonicBattleLine sbl: data) { //write lines of group
			if (sbl.group != group) continue;
			pointers.add(ByteTools.toByteArray(ByteTools.reversePointer(ByteTools.intToHex(pos)) + ByteTools.toHexString(POINTER_DELIMITER)));
			for (byte b: sbl.content) {
				rom[pos] = b;
				pos++;
			}
			for (byte b: delimiter) {
				rom[pos] = b;
				pos++;
			}
		}
		
		for (byte[] arr: pointers) {//write pointers
			for (byte b: arr) {
				rom[pointerPos] = b;
				pointerPos++;
			}
		}
	}
	private static int getHighestGroupValue(List<SonicBattleLine> data) {
		int output = -2;
		for (SonicBattleLine sbl: data) {
			if (sbl.group <= output) continue;
			output = sbl.group;
		}
		return output + 1;
	}
}
