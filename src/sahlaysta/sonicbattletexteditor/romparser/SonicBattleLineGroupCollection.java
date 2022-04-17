package sahlaysta.sonicbattletexteditor.romparser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;

/** This class represents the parse result returned by the SonicBattleROMDialogueReader.
 * It is a wrapper of an array of SonicBattleLineGroups. Call <code>group(index)</code>
 * to get a group. */
public class SonicBattleLineGroupCollection implements Iterable<SonicBattleLineGroup> {
	
	/** The wrapped array of SonicBattleLineGroups */
	protected final SonicBattleLineGroup[] groups;
	/** The size of the collection */
	public final int size;
	
	/** Constructor, meant to be used by the parser */
	protected SonicBattleLineGroupCollection(SonicBattleLineGroup[] groups) {
		this.groups = groups;
		this.size = groups.length;
	}
	
	/** Return the SonicBattleLineGroup of the passed index. */
	public SonicBattleLineGroup group(int index) {
		return groups[index];
	}

	/** This iterator of SonicBattleLineGroups. */
	@Override
	public Iterator<SonicBattleLineGroup> iterator() {
		// Iterate through the 'groups' array
		return new Iterator<SonicBattleLineGroup>() {
			private int i = 0;
			@Override
			public boolean hasNext() {
				return i < size;
			}
			@Override
			public SonicBattleLineGroup next() {
				return groups[i++];
			}
		};
	}
	
	//ROM
	/** The byte array of the file of the parsed Sonic Battle ROM. Used in save operations */
	protected byte[] rom;
	/** Set the byte array ROM: byte array of all bytes of a Sonic Battle ROM (for save operations) */
	public void setRom(byte[] rom) {
		this.rom = rom;
	}
	/** Set the ROM to the byte array of all bytes of the File (for save operations) */
	public void setRom(File file) throws IOException {
		rom = Files.readAllBytes(file.toPath());
	}
	/** Return the set Sonic Battle ROM byte array */
	public byte[] getRom() {
		return rom;
	}
	/** Save all the dialogue lines to a Sonic Battle ROM. Must call <code>setRom</code> before */
	public void save() {
		SonicBattleROMDialogueSaver.save(this);
	}
}