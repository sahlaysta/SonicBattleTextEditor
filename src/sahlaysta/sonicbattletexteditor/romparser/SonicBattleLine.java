package sahlaysta.sonicbattletexteditor.romparser;

import java.util.List;

/** A SonicBattleLine object represents a singular dialogue line
 * from a Sonic Battle ROM. Example: The very first line spoken by
 * Dr. Eggman, "Fight! Why don't you fight?!" It contains the
 * String of the line, retrievable by <code>getContent()</code> and
 * settable by the <code>setContent</code> method */
public class SonicBattleLine {
	
	/** The parsed String of this dialogue line from the ROM. */
	protected String str;
	
	/** Get the String value of this Sonic Battle dialogue line */
	public String getContent() {
		return str;
	}
	public String toString() {
		return getContent();
	}
	
	/** Set the String value of this Sonic Battle dialogue line */
	public void setContent(String str) {
		this.str = str;
	}
	
	//Constructors
	/** Construct a SonicBattleLine object with a null String value */
	protected SonicBattleLine() { }
	/** Construct a SonicBattleLine object from a byte List.
	 * Meant to be used by the parser */
	protected SonicBattleLine(List<Byte> bytes) {
		this.str = SonicBattleTextParser.parse(bytes);
	}
	/** Construct a SonicBattleLine object with the dialogue line <b>str</b> */
	protected SonicBattleLine(String str) {
		this.str = str;
	}
}