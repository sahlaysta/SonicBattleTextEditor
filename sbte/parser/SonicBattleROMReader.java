package sbte.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sbte.util.ByteTools;
import sbte.util.FileTools;

public class SonicBattleROMReader {
	public static final byte[] delimiter = new byte[] { (byte) 0xFE, (byte) 0xFF }; //the end of line is FEFF
	
	public static List<SonicBattleLine> readUSAROM(ROM rom) throws IOException {
		List<SonicBattleLine> output = new ArrayList<>();
		rom.addLines(output, "EDFE8C", 2299); //story mode
		rom.addLines(output, "ED9C2C", 309); //Emerl card descriptions
		rom.addLines(output, "EDCD7C", 17); //options menu lines
		rom.addLines(output, "EDB9CC", 8); //battle menu lines
		rom.addLines(output, "EDBF60", 35); //battle rules lines
		rom.addLines(output, "EDD3B0", 16); //training mode menu lines
		rom.addLines(output, "EDC3FC", 9); //minigame name lines (e.g. SONICLASH)
		rom.addLines(output, "EDCFD0", 40); //battle record menu lines
		rom.addLines(output, "EDC9D0", 7); //captured technique dialog lines (7 of the same thing in a USA rom)
		rom.addLines(output, "EDD5D0", 8); //story mode episode select menu lines
		return output;
	}
	public static class SonicBattleLine{
		public final byte[] content;
		public final int pointer;
		public final int position;
		public final int group;
		public final int member;
		public SonicBattleLine(byte[] content, int pointer, int position, int group, int member) {
			this.content = content;
			this.pointer = pointer;
			this.position = position;
			this.group = group;
			this.member = member;
		}
//		public String toString() {
//			return
//			"Content: " + ByteTools.toHexString(content) + ByteTools.toHexString(delimiter) +
//			"\nROM Pointer: " + pointer +
//			"\nROM Location: " + position +
//			"\nGroup " + (1 + group) + ", Member " + (1 + member) + "\n";
//		}
	}
	public static class ROM {
		public final byte[] content;
		public final File romPath;
		public ROM(File arg0) throws IOException {
			romPath = arg0;
			content = FileTools.readFileToByteArray(arg0);
		}
		
		private int groupIndex = 0; //indexing
		private int memberIndex = 0;
		public void addLines(List<SonicBattleLine> arg0, String arg1, int arg2) {
			final int pos = ByteTools.hexToInt(arg1);
			for (int i = 0; i < arg2; i++) {
				final int pointer = pos + (i * 4);
				SonicBattleLine sbl = getFromROM(pointer);
				memberIndex++;
				arg0.add(sbl);
			}
			groupIndex++;
			memberIndex = 0;
		}
		private SonicBattleLine getFromROM(int pointer) {
			byte[] posPointer = new byte[] {
					content[pointer + 2],
					content[pointer + 1],
					content[pointer],
			};
			final int pos = ByteTools.hexToInt(ByteTools.toHexString(posPointer));
			
			List<Byte> bytes = new ArrayList<>();
			loop: for (int i = pos; ; i++) {
				{//check if byte array equals delimiter
					boolean end = true;
					for (int ii = 0; ii < delimiter.length; ii++) {
						if (delimiter[ii] != content[i + ii]) {
							end = false;
							break;
						}
						if (end) {
							break loop;
						}
					}
				}
				
				bytes.add(content[i]);
			}

			byte[] output = new byte[bytes.size()];
			for (int i = 0; i < output.length; i++)
				output[i] = bytes.get(i);
			
			return new SonicBattleLine(output, pointer, pos, groupIndex, memberIndex);
		}
	}
}
