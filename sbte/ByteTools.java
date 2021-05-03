package sbte;

import javax.xml.bind.DatatypeConverter;

public class ByteTools {
	public static String toHexString(byte[] array) {
	    return DatatypeConverter.printHexBinary(array);
	}

	public static byte[] toByteArray(String s) {
	    return DatatypeConverter.parseHexBinary(s);
	}
	public static int hexToInt(String hex) {
		return Integer.parseInt(hex, 16);
	}
	public static String intToHex(int e) {
		return Integer.toHexString(e).toUpperCase();
	}
	public static String reversePointer(String pointer) {
		if (pointer.length() == 5) pointer = "0" + pointer;
		return pointer.substring(4, 6) + pointer.substring(2, 4) + pointer.substring(0, 2);
	}
}
