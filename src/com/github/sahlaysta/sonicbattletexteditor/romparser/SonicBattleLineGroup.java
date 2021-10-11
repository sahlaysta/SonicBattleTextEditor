package com.github.sahlaysta.sonicbattletexteditor.romparser;

import java.util.Iterator;

/** This class represents a group of lines inside of
 * a Sonic Battle ROM. It is a wrapper of an array of
 * SonicBattleLines. <br>Example: From the very first
 * line of Story Mode, "Fight! Why don't you fight?!" to the
 * very last, "How you use its power is up to you,"
 * is one entire group of lines in a Sonic Battle ROM.
 * A separate group of lines in the ROM would be the
 * Options Menu's text lines in Sonic Battle.<br>
 * Call <code>item(index)</code> to get an item. */
public class SonicBattleLineGroup implements Iterable<SonicBattleLine> {
	/** The wrapped array of SonicBattleLines */
	protected final SonicBattleLine[] lines;
	/** The size of the collection */
	public final int size;
	/** The offset of this line group parsed in the Sonic Battle ROM */
	public final int offset;
	
	/** Constructor, meant to be used by the parser */
	protected SonicBattleLineGroup(SonicBattleLine[] lines, int offset) {
		this.lines = lines;
		this.size = lines.length;
		this.offset = offset;
	}
	
	/** Return the SonicBattleLine item of the passed index. */
	public SonicBattleLine item(int index) {
		return lines[index];
	}
	
	/** This iterator of SonicBattleLines. */
	@Override
	public Iterator<SonicBattleLine> iterator() {
		// Iterate through the 'lines' array
		return new Iterator<SonicBattleLine>() {
			private int i = 0;
			@Override
			public boolean hasNext() {
				return i < size;
			}
			@Override
			public SonicBattleLine next() {
				return lines[i++];
			}
		};
	}
}