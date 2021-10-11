package com.github.sahlaysta.sonicbattletexteditor.romparser;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/** Class to parse a SonicBattleLineGroupCollection from a Sonic Battle ROM file */
public final class SonicBattleROMDialogueReader {

	//Invisible constructor
	private SonicBattleROMDialogueReader() {}
	
	/** Parse a SonicBattleLineGroupCollection from a Sonic Battle ROM using the passed SonicBattleROMDialogueReaderOptions. */
	public static SonicBattleLineGroupCollection parse(File sonicBattleROM, SonicBattleROMDialogueReaderOptions options) throws IOException {
		SonicBattleRandomAccessFile sbraf = new SonicBattleRandomAccessFile(sonicBattleROM, options.bufferSize);
		
		//Initialize the array of SonicBattleLineGroups for the parse result
		SonicBattleLineGroup[] groups = new SonicBattleLineGroup[options.lineGroupOptions.size()];
		
		//Iterate through each dialogue line group in the passed SonicBattleROMDialogueReaderOptions
		int i = 0;
		for (SonicBattleROMDialogueReaderOptions.LineGroupOption lgo: options.lineGroupOptions) {
			sbraf.seekBuffer(lgo.offset); //Seek to the offset of the dialogue line group
			int[] dialoguePointers = sbraf.readDialoguePointers(lgo.amount); //Read the pointers of the line group's dialogue lines
			SonicBattleLine[] lines = new SonicBattleLine[dialoguePointers.length]; //Initialize array of SonicBattleLines for the parse result
			
			//Iterate through each dialogue pointer
			for (int ii = 0; ii < dialoguePointers.length; ii++) {
				sbraf.seekBuffer(dialoguePointers[ii]); //Seek to the offset of the dialogue line
				lines[ii] = new SonicBattleLine(sbraf.readDialogueLine()); //Read the dialogue line and set result
			}
			groups[i++] = new SonicBattleLineGroup(lines, lgo.offset); //Set parse result
		}
		sbraf.close();
		
		return new SonicBattleLineGroupCollection(groups); //Return result
	}
	
	//Static method parse abstractions (will add more languages in future updates)
	/** Parse a SonicBattleLineGroupCollection of English and Spanish lines in a Sonic Battle USA ROM */
	public static SonicBattleLineGroupCollection parseUSARom(File sonicBattleROM) throws IOException {
		return parse(sonicBattleROM, SonicBattleROMDialogueReaderOptions.newUSARomOptions());
	}
	
	
	// Helper class to read a Sonic Battle ROM file
	private static final class SonicBattleRandomAccessFile extends RandomAccessFile {
		
		//Buffer bytes from file
		private final byte[] buffer;
		private int bufferPos = Integer.MAX_VALUE;
		private int streamSize = -1;
		private byte nextByte() throws IOException {
			if (bufferPos >= buffer.length) {
				streamSize = read(buffer);
				bufferPos = 0;
			}
			if (streamSize > bufferPos)
				return buffer[bufferPos++];
			else
				throw new IOException("Unexpected end of file");
		}
				
		//Constructor
		public SonicBattleRandomAccessFile(File file, int bufferSize) throws IOException {
			super(file, "r");
			buffer = new byte[bufferSize];
		}
		
		/** Seek the RandomAccessFile and reset the buffer */
		public void seekBuffer(long pos) throws IOException {
			seek(pos);
			bufferPos = Integer.MAX_VALUE; //Reset buffer
		}
		
		//Sonic Battle read methods
		/** Read line pointers from a Sonic Battle ROM. Takes the 'amount' parameter for the amount of the pointers */
		public int[] readDialoguePointers(int amount) throws IOException {
			int[] dialoguePointers = new int[amount];
			for (int i = 0; i < amount; i++)
				dialoguePointers[i] = readPointer();
			return dialoguePointers;
		}
		
		/** Read a pointer in a Sonic Battle ROM */
		public int readPointer() throws IOException {
			byte byte1 = nextByte();
			byte byte2 = nextByte();
			byte byte3 = nextByte();
			bufferPos++; //skip a byte
			
			//Convert to int from bytes
			return (0
					| ((byte3 & 0xff) << 16)
					| ((byte2 & 0xff) << 8)
					| (byte1 & 0xff));  
		}
		
		/** Read the bytes of a dialogue line in a Sonic Battle ROM */
		public List<Byte> readDialogueLine() throws IOException {
			/*
			 * Keep reading bytes from the RandomAccessFile until Sonic Battle's
			 * delimiter, [0xFE, 0xFF], is hit
			 */
			List<Byte> bytes = new ArrayList<>(25);
			while (true) {
				byte b = nextByte();
				if (b == -2) { //-2 = 0xFE
					byte b2 = nextByte();
					if (b2 == -1) //-1 = 0xFF
						break;
					else {
						bytes.add(b);
						bytes.add(b2);
						continue;
					}
				}
				else
					bytes.add(b);
			}
			return bytes;
		}
	}
}
