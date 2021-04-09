package sbte;

//import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SonicBattleTextLib {
	public class Item{
		private byte[] hex;
		private String letter;
		public Item(byte[] b, String s) {
			hex = b;
			letter = s;
		}
		public String getLetter() {
			return letter;
		}
		public byte[] getBytes() {
			return hex;
		}
		public String getBytesAsHex() {
		    StringBuilder sb = new StringBuilder();
		    for (byte b : hex) {
		        sb.append(String.format("%02X", b));
		    }
			return sb.toString();
		}
		public String toString() {
			return getBytesAsHex() + " | " + letter;
		}
	}
	private List<Item> array = new ArrayList<>();
	public void add(byte[] b, String s) {
		array.add(new Item(b, s));
	}
	public void sortByHexLength() {
		Collections.sort(array, new Comparator<Item>() {
		    public int compare(Item i1, Item i2) {
		        return i1.getBytes().length - i2.getBytes().length;
		    }
		}.reversed());
	}
	public void sortByLetterLength() {
		Collections.sort(array, new Comparator<Item>() {
		    public int compare(Item i1, Item i2) {
		        return i1.getLetter().length() - i2.getLetter().length();
		    }
		}.reversed());
	}
	public Item get(int index) {
		return array.get(index);
	}
	public Item getByString(String s) {
		for(Item i: array) {
			if (i.letter.equals(s)) return i;
		}
		return null;
	}
	public Item getByBytes(byte[] b) {
		for(Item i: array) {
			if (b.length != i.getBytes().length) continue;
			boolean match = true;
			for (int ii = 0; ii < b.length; ii++) {
				if (b[ii] != i.getBytes()[ii]) { match = false; break; }
			}
			if (match) return i;
		}
		return null;
	}
	public int getItemCount() {
		return array.size();
	}
	public Item[] getItems() {
		Item[] it = new Item[array.size()];
		for (int i = 0; i < array.size(); i++) it[i] = array.get(i);
		return it;
	}
	public Boolean exists(byte[] b) {
		return null;
	}
	public Boolean exists(String s) {
		for(Item i: array) {
			if (i.letter.equals(s)) return true;
		}
		return false;
	}
	public String joined(String delimiter) {
		String[] s = new String[array.size()];
		for (int i = 0; i < array.size(); i++) s[i] = array.get(i).toString();
		return String.join(delimiter, s);
	}
}
