package com.github.sahlaysta.sonicbattletexteditor.romparser;

import java.util.ArrayList;
import java.util.List;

import com.github.sahlaysta.sonicbattletexteditor.romparser.SonicBattleTextParserDictionary.Entry;

/** Utility class to parse Sonic Battle ROM bytes
 * to text and vice versa */
public final class SonicBattleTextParser {
	
	//Invisible constructor
	private SonicBattleTextParser() {}
	
	// Parse bytes to String
	/** Parse a byte List of a Sonic Battle dialogue line to a String */
	public static final String parse(List<Byte> bytes) {
		/*
		 * The Sonic Battle text parse method. Convert a List of
		 * bytes to a String using the SonicBattleTextParserDictionary.
		 * 
		 * Iterate through each entry in the BYTE_TO_STRING map of
		 * SonicBattleTextParserDictionary. If it matches, append
		 * it to the StringBuilder.
		 */
		final int size = bytes.size();
		int pos = 0;
		StringBuilder sb = new StringBuilder();
		byteLoop: while (pos < size) {
			dictionaryLoop: for (Entry entry: SonicBattleTextParserDictionary.BYTE_TO_STRING) {
				if (entry.bytes.length > (size - pos))
					continue;
				for (int i = 0; i < entry.bytes.length; i++)
					if (entry.bytes[i] != bytes.get(i + pos))
						continue dictionaryLoop;
				sb.append(entry.string);
				pos += entry.bytes.length;
				continue byteLoop;
			}
		
			//parse fail, throw exception
			StringBuilder error = new StringBuilder("Unknown byte sequence in parse attempt");
			String hexString = HexUtils.bytesToHexString(bytes);
			if (hexString.length() > 48)
				throw new SonicBattleTextParseException(error.toString(), pos);
			error.append(":\r\n");
			error.append(hexString);
			error.append('\n');
			for (int i = 0; i < (pos * 2); i++)
				error.append(' ');
			error.append('^');
			throw new SonicBattleTextParseException(error.toString(), pos);
		}
		return sb.toString();
	}
	
	/** Parse a byte array of a Sonic Battle dialogue line to a String */
	public static final String parse(byte[] bytes) {
		/*
		 * See the parseStringFromBytes(List<Byte> bytes) method
		 */
		int pos = 0;
		StringBuilder sb = new StringBuilder();
		byteLoop: while (pos < bytes.length) {
			dictionaryLoop: for (Entry entry: SonicBattleTextParserDictionary.BYTE_TO_STRING) {
				if (entry.bytes.length > (bytes.length - pos))
					continue;
				for (int i = 0; i < entry.bytes.length; i++)
					if (entry.bytes[i] != bytes[i + pos])
						continue dictionaryLoop;
				sb.append(entry.string);
				pos += entry.bytes.length;
				continue byteLoop;
			}

			//parse fail, throw exception
			StringBuilder error = new StringBuilder("Unknown byte sequence in parse attempt");
			String hexString = HexUtils.bytesToHexString(bytes);
			if (hexString.length() > 48)
				throw new SonicBattleTextParseException(error.toString(), pos);
			error.append(":\r\n");
			error.append(hexString);
			error.append('\n');
			for (int i = 0; i < (pos * 2); i++)
				error.append(' ');
			error.append('^');
			throw new SonicBattleTextParseException(error.toString(), pos);
		}
		return sb.toString();
	}
	
	//Parse a String of text into bytes in Sonic Battle
	/** Parse a String into the bytes of a Sonic Battle dialogue line */
	public static final List<Byte> parse(String str) {
		/*
		 * The Sonic Battle text parse method. Convert a String to a
		 * bytes list using the SonicBattleTextParserDictionary.
		 * 
		 * Iterate through each entry in the STRING_TO_BYTE map of
		 * SonicBattleTextParserDictionary. If it matches, append
		 * it to the byte list.
		 */
		final int strLength = str.length();
		int pos = 0;
		List<Byte> bytes = new ArrayList<>(25);
		byteLoop: while (pos < strLength) {
			dictionaryLoop: for (Entry entry: SonicBattleTextParserDictionary.STRING_TO_BYTE) {
				final int entryStrLength = entry.string.length();
				if (entryStrLength > (strLength - pos))
					continue;
				for (int i = 0; i < entryStrLength; i++)
					if (entry.string.charAt(i) != str.charAt(i + pos))
						continue dictionaryLoop;
				for (byte b: entry.bytes)
					bytes.add(b);
				pos += entryStrLength;
				continue byteLoop;
			}
		
			//parse fail, throw exception
			StringBuilder error = new StringBuilder("Unknown byte sequence in parse attempt starting at char '" + (char)str.charAt(pos) + "'");
			if (str.length() > 48)
				throw new SonicBattleTextParseException(error.toString(), pos);
			error.append(" :\r\n");
			error.append(str);
			error.append('\n');
			for (int i = 0; i < pos; i++)
				error.append(' ');
			error.append('^');
			throw new SonicBattleTextParseException(error.toString(), pos);
		}
		return bytes;
	}
	
	//Validators
	/** Test if the passed String is valid to be parsed into Sonic Battle bytes. 
	 * Returns the int position of the error char in the parse attempt. Returns -1 if there is no error and the String is valid. */
	public static final int validate(String str) {
		/*
		 * See the parseBytesFromString(String str) method
		 */
		final int strLength = str.length();
		int pos = 0;
		byteLoop: while (pos < strLength) {
			dictionaryLoop: for (Entry entry: SonicBattleTextParserDictionary.STRING_TO_BYTE) {
				final int entryStrLength = entry.string.length();
				if (entryStrLength > (strLength - pos))
					continue;
				for (int i = 0; i < entryStrLength; i++)
					if (entry.string.charAt(i) != str.charAt(i + pos))
						continue dictionaryLoop;
				pos += entryStrLength;
				continue byteLoop;
			}
			return pos;
		}
		return -1;
	}
	/** Test if the passed bytes is valid to be parsed into a Sonic Battle String.
	* Returns the int position of the error in the parse attempt. Returns -1 if there is no error and the bytes are valid. */
	public static final int validate(List<Byte> bytes) {
		/*
		 * See the parseStringFromBytes(List<Byte> bytes) method
		 */
		final int size = bytes.size();
		int pos = 0;
		byteLoop: while (pos < size) {
			dictionaryLoop: for (Entry entry: SonicBattleTextParserDictionary.BYTE_TO_STRING) {
				if (entry.bytes.length > (size - pos))
					continue;
				for (int i = 0; i < entry.bytes.length; i++)
					if (entry.bytes[i] != bytes.get(i + pos))
						continue dictionaryLoop;
				pos += entry.bytes.length;
				continue byteLoop;
			}
			return pos;
		}
		return -1;
	}
	/** Test if the passed bytes is valid to be parsed into a Sonic Battle String.
	* Returns the int position of the error in the parse attempt. Returns -1 if there is no error and the bytes are valid. */
	public static final int validate(byte[] bytes) {
		/*
		 * See the parseStringFromBytes(byte[] bytes) method
		 */
		int pos = 0;
		byteLoop: while (pos < bytes.length) {
			dictionaryLoop: for (Entry entry: SonicBattleTextParserDictionary.BYTE_TO_STRING) {
				if (entry.bytes.length > (bytes.length - pos))
					continue;
				for (int i = 0; i < entry.bytes.length; i++)
					if (entry.bytes[i] != bytes[i + pos])
						continue dictionaryLoop;
				pos += entry.bytes.length;
				continue byteLoop;
			}
			return pos;
		}
		return -1;
	}
}