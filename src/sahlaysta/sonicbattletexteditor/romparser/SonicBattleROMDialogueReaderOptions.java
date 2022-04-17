package sahlaysta.sonicbattletexteditor.romparser;

import java.util.LinkedHashSet;
import java.util.Set;

/** The ROM Dialogue Reader Options Class used by the Sonic Battle
 * ROM Dialogue Reader method. */
public class SonicBattleROMDialogueReaderOptions {
	
	//Buffer size
	/** The buffer size for reading the ROM. Default is 1024 */
	protected int bufferSize = 1024;
	/** Returns the set buffer size for reading the ROM. Default is 1024 */
	public int getBufferSize() {
		return bufferSize;
	}
	/** Sets the buffer size for reading the ROM. Default is 1024 */
	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}
	
	//Line group
	/** The LineGroupOption class. It has two ints:<br>
	 * - <b>offset</b>: The ROM offset of the (pointer) start of the line group<br>
	 * - <b>amount</b>: The amount of lines.<br>
	 * Example: in a Sonic Battle USA rom, the dialogue line group offset of Story
	 * Mode is 0xEDFE8C, and the amount of lines there is 2299. */
	protected static final class LineGroupOption {
		/** The ROM offset of the (pointer) start of the line group */
		protected final int offset;
		/** The amount of lines in the line group */
		protected final int amount;
		/** Construct a LineGroupOption. Meant to be used by the parser */
		protected LineGroupOption(int offset, int amount) {
			this.offset = offset;
			this.amount = amount;
		}
	}
	/** Collection of LineGroupOptions, sorted by order of insertion. */
	protected final Set<LineGroupOption> lineGroupOptions = new LinkedHashSet<>();
	/** Add a line group to parse.<br>
	 * Example: in a Sonic Battle USA rom, the dialogue line group offset of Story
	 * Mode is 0xEDFE8C, and the amount of lines there is 2299. */
	public void addLineGroup(int offset, int amount) {
		lineGroupOptions.add(new LineGroupOption(offset, amount));
	}
	
	//Static method abstractions
	/** Returns a new SonicBattleROMDialogueReaderOptions object for a Sonic Battle USA ROM */
	public static final SonicBattleROMDialogueReaderOptions newUSARomOptions() {
		SonicBattleROMDialogueReaderOptions sonic = new SonicBattleROMDialogueReaderOptions();
		
		//ENGLISH
		sonic.addLineGroup(0xEDFE8C, 2299);	//STORY MODE
		sonic.addLineGroup(0xED9C2C, 309);	//EMERL CARD DESCRPTIONS
		sonic.addLineGroup(0xEDCD7C, 17);	//OPTIONS MENU LINES
		sonic.addLineGroup(0xEDB9CC, 8);	//BATTLE MENU LINES
		sonic.addLineGroup(0xEDBF60, 35);	//BATTLE RULES LINES
		sonic.addLineGroup(0xEDD3B0, 16);	//TRAINING MODE MENU LINES
		sonic.addLineGroup(0xEDC3FC, 9);	//MINIGAME NAME LINES
		sonic.addLineGroup(0xEDCFD0, 40);	//BATTLE RECORD MENU LINES
		sonic.addLineGroup(0xEDC9D0, 7);	//CAPTURED TECHNIQUE DIALOGUE LINES
		sonic.addLineGroup(0xEDD5D0, 8);	//STORY MODE RESUME EPISODE MENU LINES
		
		//SPANISH
		sonic.addLineGroup(0xEE6A50, 2299);	//STORY MODE
		sonic.addLineGroup(0xEDAAA8, 309);	//EMERL CARD DESCRPTIONS
		sonic.addLineGroup(0xEDCE48, 17);	//OPTIONS MENU LINES
		sonic.addLineGroup(0xEDBA2C, 8);	//BATTLE MENU LINES
		sonic.addLineGroup(0xEDC190, 35);	//BATTLE RULES LINES
		sonic.addLineGroup(0xEDD470, 16);	//TRAINING MODE MENU LINES
		sonic.addLineGroup(0xEDD1B0, 39);	//BATTLE RECORD MENU LINES
		sonic.addLineGroup(0xEDCA44, 2);	//CAPTURED TECHNIQUE DIALOGUE LINES
		sonic.addLineGroup(0xEDD630, 8);	//STORY MODE RESUME EPISODE MENU LINES
		
		return sonic;
	}
}