package com.github.sahlaysta.sonicbattletexteditor.romparser;

import java.util.List;

/** Runtime exception thrown by the SonicBattleTextParser */
public class SonicBattleTextParseException extends IllegalArgumentException {
	private static final long serialVersionUID = 1L;
	/** The index at which the parse failed */
	protected int index = -1;
	/** The byte array where there was parse failure. Null if none */
	protected byte[] byteArray = null;
	/** The byte list where there was parse failure. Null if none */
	protected List<Byte> byteList = null;
	/** The String where there was parse failure. Null if none */
	protected String string = null;
	
	/** Throw parse failure exception in parsing Sonic Battle Text,
	 * with the String runtime error messange, the byte array that
	 * failed to parse and the int index where the parse failed */
	public SonicBattleTextParseException(String message, byte[] byteArray, int index) {
		super(message);
		this.byteArray = byteArray;
		this.index = index;
	}
	/** Throw parse failure exception in parsing Sonic Battle Text,
	 * with the String runtime error messange, the byte List that
	 * failed to parse and the int index where the parse failed */
	public SonicBattleTextParseException(String message, List<Byte> byteList, int index) {
		super(message);
		this.byteList = byteList;
		this.index = index;
	}
	/** Throw parse failure exception in parsing Sonic Battle Text,
	 * with the String runtime error messange, the String that
	 * failed to parse and the int index where the parse failed */
	public SonicBattleTextParseException(String message, String string, int index) {
		super(message);
		this.string = string;
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
	
	/** Return the String that failed to parse. Null if none */
	public String getString() {
		return string;
	}
	/** Return the byte array that failed to parse. Null if none */
	public byte[] getByteArray() {
		return byteArray;
	}
	/** Return the byte List that failed to parse. Null if none */
	public List<Byte> getByteList() {
		return byteList;
	}
	/** Returns the index where the parse failed */
	public int getIndex() {
		return index;
	}
}