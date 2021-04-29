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
}
