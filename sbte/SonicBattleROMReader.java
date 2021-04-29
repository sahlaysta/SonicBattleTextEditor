package sbte;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class SonicBattleROMReader {
	public static final byte[] delimiter = new byte[] { (byte) 0xFE, (byte) 0xFF }; //the end of line is FEFF
	
	public static List<byte[]> readUSAROM(File arg0) throws IOException {
		ROM rom = new ROM(arg0);
		
		List<byte[]> output = new ArrayList<>();
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
	private static class ROM {
		public final byte[] rom;
		public ROM(File arg0) throws IOException {
			rom = Files.readAllBytes(arg0.toPath());
		}
		public void addLines(List<byte[]> arg0, String arg1, int arg2) {
			int pointer = ByteTools.hexToInt(arg1);
			for (int i = 0; i < arg2; i++) {
				byte[] b = getFromROM(pointer + (i * 4));
				arg0.add(b);
			}
		}
		private byte[] getFromROM(int pointer) {
			byte[] posPointer = new byte[3];
			posPointer[2] = rom[pointer];
			posPointer[1] = rom[pointer + 1];
			posPointer[0] = rom[pointer + 2];
			final int pos = ByteTools.hexToInt(ByteTools.toHexString(posPointer));
			
			List<Byte> bytes = new ArrayList<>();
			f :for (int i = pos; ; i++) {
				{//check if byte array equals delimiter
					boolean end = true;
					for (int ii = 0; ii < delimiter.length; ii++) {
						if (delimiter[ii] != rom[i + ii]) {
							end = false;
							break;
						}
						if (end) break f;
					}
				}
				
				bytes.add(rom[i]);
			}
			
			byte[] output = new byte[bytes.size()];
			for (int i = 0; i < output.length; i++)
				output[i] = bytes.get(i);
			return output;
		}
	}
}
