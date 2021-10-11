package com.github.sahlaysta.sonicbattletexteditor.romparser;

import java.util.Collection;

/** Utility class centered around Hex Strings */
public final class HexUtils {
	
	//Invisible constructors
	private HexUtils() {}
	
	// Bytes to Hex String
	// https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
	private static final char[] HEX_ARRAY = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
	/** Convert a Collection of bytes to a Hex String */
	public static String bytesToHexString(Collection<Byte> bytes) {
	    char[] hexChars = new char[bytes.size() * 2];
	    int i = 0;
	    for (byte b: bytes) {
	    	int v = b & 0xFF;
	        hexChars[i * 2] = HEX_ARRAY[v >>> 4];
	        hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
	        i++;
	    }
	    return new String(hexChars);
	}
	/** Convert an array of bytes to a Hex String */
	public static String bytesToHexString(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for (int i = 0; i < bytes.length; i++) {
	        int v = bytes[i] & 0xFF;
	        hexChars[i * 2] = HEX_ARRAY[v >>> 4];
	        hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	//Hex String to bytes
	// https://stackoverflow.com/questions/140131/convert-a-string-representation-of-a-hex-dump-to-a-byte-array-using-java
	/** Convert a HEX String to a byte array */
	public static byte[] hexStringToBytes(String str) {
	    int len = str.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4)
	                             + Character.digit(str.charAt(i+1), 16));
	    }
	    return data;
	}
}