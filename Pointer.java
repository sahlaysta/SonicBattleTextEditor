package sbte;

public class Pointer {
	private int decValue;
	private String hexValue;
	private void format() {
		int len = hexValue.length();
		int lenPref = 6;
		if (len > lenPref) throw new Error("Value is too big");
		if (len < lenPref) {
			int it = lenPref - len;
			for (int i = 0; i < it; i++ ) {
				hexValue = "0" + hexValue;
			}
		}
		hexValue = hexValue.toUpperCase();
	}
	public Pointer reverse() {
		hexValue = hexValue.substring(4, 6) + hexValue.substring(2, 4) + hexValue.substring(0, 2);
		decValue = Integer.parseInt(hexValue, 16);
		format();
		return this;
	}
	public Pointer(int decimal) {
		decValue = decimal;
		hexValue = Integer.toHexString(decimal);
		format();
	}
	public Pointer(String hex) {
		hexValue = hex;
		decValue = Integer.parseInt(hex, 16);
		format();
	}
	public Pointer add(int a) {
		decValue = decValue + a;
		hexValue = Integer.toHexString(decValue);
		format();
		return this;
	}
	public int getDec() {
		return decValue;
	}
	public String getHex() {
		return hexValue;
	}
}
