package sbte.utilities;

public class ByteTools { //Global byte utilities
	private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
	public static String toHexString(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
	    for (int j = 0; j < bytes.length; j++) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = HEX_ARRAY[v >>> 4];
	        hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	public static byte[] toByteArray(String e) {
		int len = e.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(e.charAt(i), 16) << 4)
	                             + Character.digit(e.charAt(i+1), 16));
	    }
	    return data;
	}
	public static int hexToInt(String hex) {
		return Integer.parseInt(hex, 16);
	}
	public static String intToHex(int e) {
		return Integer.toHexString(e).toUpperCase();
	}
	public static String reversePointer(String arg0) {
		String pointer = new String(arg0);
		if (pointer.length() == 5) pointer = "0" + pointer;
		return pointer.substring(4, 6) + pointer.substring(2, 4) + pointer.substring(0, 2);
	}
}
