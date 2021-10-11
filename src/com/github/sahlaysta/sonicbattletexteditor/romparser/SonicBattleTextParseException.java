package com.github.sahlaysta.sonicbattletexteditor.romparser;

/** Runtime exception thrown by the SonicBattleTextParser */
public class SonicBattleTextParseException extends IllegalArgumentException {
	private static final long serialVersionUID = 1L;
	/** The index at which the parse failed */
	protected int index = -1;
	/** Throw parse failure exception in parsing Sonic Battle Text,
	 * with the String runtime error messange and the int index where the parse failed */
	public SonicBattleTextParseException(String message, int index) {
		super(message);
		this.index = index;
	}
	/** Throw parse failure exception in parsing Sonic Battle Text,
	 * with the String runtime error messange */
	public SonicBattleTextParseException(String message) {
		super(message);
	}
	/** Throw parse failure exception in parsing Sonic Battle Text */
	public SonicBattleTextParseException() {
		super();
	}
	
	/** Returns the index where the parse failed */
	public int getIndex() {
		return index;
	}
}