package com.github.sahlaysta.sonicbattletexteditor.romparser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Class to save the edited SonicBattleLines of a SonicBattleLineGroupCollection */
public final class SonicBattleROMDialogueSaver {
	
	//Invis constructor
	private SonicBattleROMDialogueSaver() { }
	
	/** Save all the SonicBattleLines of a SonicBattleLineGroupCollection
 	to a Sonic Battle ROM */
	public static final void save(SonicBattleLineGroupCollection sblgc) {
		//Null check
		if (sblgc.rom == null)
			throw new NullPointerException("SonicBattleLineGroupCollection: call the 'setRom' method");
		
		for (SonicBattleLineGroup sblg: sblgc) {
			//Get List of bytes of all lines in the line group
			List<List<Byte>> lines = new ArrayList<>(sblg.size);
			for (int i = 0; i < sblg.size; i++) {
				List<Byte> line = SonicBattleTextParser.parse(sblg.item(i).getContent());
				line.add((byte)0xFE); //add delimiter FEFF
				line.add((byte)0xFF);
				lines.add(line);
			}
			
			//Create searchLines from lines, sorted by amount of bytes (highest first)
			List<List<Byte>> searchLines = new ArrayList<>(lines);
			Collections.sort(searchLines, new Comparator<List<Byte>>() {
				@Override
				public int compare(List<Byte> arg0, List<Byte> arg1) {
					return arg1.size() - arg0.size();
				}
			});
			
			/* Compare all the byte data of the Sonic Battle Lines to find duplicate
			 * data and reuse it to save space.
			 * 
			 * Reuse lines:
			 * Say there's the two lines. Line A, "Hello, Sonic." and Line B, "Good day. Hello, Sonic."
			 * These two lines are equivalent where it says "Hello, Sonic."
			 * The shorter line, Line A will have a pointer to Line B, reusing data to save space. */
			Map<List<Byte>, List<LineIndex>> indexes = new LinkedHashMap<>();
			for (int i = 0; i < sblg.size; i++) {
				List<Byte> line = lines.get(i);
				
				//Search through all lines to find duplicate byte data
				searchLoop: for (List<Byte> searchLine: searchLines) {
					int searchIndex = searchLine.size() - 1;
					int lineIndex = line.size() - 1;
					while (lineIndex > -1) { //compare each byte, starting from last to first
						if (searchLine.get(searchIndex--) != line.get(lineIndex--))
							continue searchLoop;
					}
					searchIndex++;
					
					//Reaching this codepath means a match was found. Add it to 'indexes' HashMap
					if (indexes.containsKey(searchLine))
						indexes.get(searchLine).add(new LineIndex(i, searchIndex));
					else {
						List<LineIndex> list = new ArrayList<>();
						list.add(new LineIndex(i, searchIndex));
						indexes.put(searchLine, list);
					}
					
					break;
				}
			}
			
			//Write the dialogue byte data to the ROM byte array and generate pointers to write
			int pos = (0 //set the position the first pointer of the first Sonic Battle line in the group
					| ((sblgc.rom[sblg.offset + 2] & 0xff) << 16)
					| ((sblgc.rom[sblg.offset + 1] & 0xff) << 8)
					| (sblgc.rom[sblg.offset + 0] & 0xff));
			int[] pointers = new int[sblg.size];
			for (List<Byte> bytes: indexes.keySet()) {
				
				//Create pointers array
				for (LineIndex index: indexes.get(bytes))
					pointers[index.lineIndex] = pos + index.byteIndex;
				
				//Write dialogue lines to ROM
				for (byte b: bytes)
					sblgc.rom[pos++] = b;
			}
			
			//Write pointers
			pos = sblg.offset;
			for (int pointer: pointers) { //reverse bytes, e.g. FCB31D to 1DB3FC
				sblgc.rom[pos++] = (byte) (pointer & 0xff);
				sblgc.rom[pos++] = (byte) ((pointer & 0xff00) >> 8);
				sblgc.rom[pos++] = (byte) ((pointer & 0xff0000) >> 16);
				sblgc.rom[pos++] = 0x08;
			}
		}
	}
	
	/** LineIndex class, holds two ints: lineIndex and byteIndex */
	private static final class LineIndex {
		/** The line index of the Sonic Battle line, e.g. 0 to 2747 */
		public final int lineIndex;
		/** The byte index of where the line starts in a reused line */
		public final int byteIndex;
		/** Construct a LineIndex with the index of the Sonic Battle Line and the byte start index */
		public LineIndex(int lineIndex, int byteIndex) {
			this.lineIndex = lineIndex;
			this.byteIndex = byteIndex;
		}
	}
}