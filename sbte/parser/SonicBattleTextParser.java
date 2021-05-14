package sbte.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import sbte.utilities.ByteTools;
import sbte.utilities.FileTools;
import sbte.utilities.JSONTools;

public class SonicBattleTextParser {
	private LinkedHashMap<byte[], String> hexToString = new LinkedHashMap<>();
	private LinkedHashMap<String, byte[]> stringToHex = new LinkedHashMap<>();
	public SonicBattleTextParser() {
		String jsonFile = FileTools.readResourceToString("parser/SonicBattleTextHEXtable.json");
		JSONObject json = null;
		try {
			json = (JSONObject)JSONTools.parser.parse(jsonFile);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		List<Tuple> dictionary = parseDictionary(json);
		this.populateHashMaps(dictionary);
	}
	public class SonicBattleParseException extends IllegalArgumentException{
		public final String source;
		public final int index;
		public SonicBattleParseException(String arg0, String arg1, int arg2) {
			super(arg0);
			source = arg1;
			index = arg2;
		}
	}
	public byte[] parseHexBinary(String e) throws SonicBattleParseException{
		final List<Byte> array = new ArrayList<>();
		final StringBuilder sb = new StringBuilder(e);
		while (sb.length() > 0) {
			boolean found = false;
			for (String s: stringToHex.keySet()) {
				if (sb.indexOf(s) == 0) {
					for (byte b: stringToHex.get(s))
						array.add(b);
					sb.replace(0, s.length(), "");
					found = true;
					break;
				}
			}
			if (!found) { //throw a styled error message
				final int errorIndex = e.length() - sb.length();
				final StringBuilder errorMsg = new StringBuilder(String.format("Bad sequence at index %s", errorIndex));
				errorMsg.append("\n" + e.replace("\n", " ") + "\n");
				for (int i = 0; i < errorIndex; i++) errorMsg.append(" ");
				errorMsg.append("^");
				throw new SonicBattleParseException(errorMsg.toString(), e, errorIndex);
			}
		}
		
		byte[] output = new byte[array.size()];
		for (int i = 0; i < output.length; i++)
			output[i] = array.get(i);
		return output;
	}
	public String parseString(byte[] e) throws IllegalArgumentException{
		final StringBuilder output = new StringBuilder();
		final List<Byte> bytes = new ArrayList<>();
		for (byte b: e) bytes.add(b);
		
		while (bytes.size() > 0) {
			boolean found = false;
			for (byte[] b: hexToString.keySet()) {
				if (b.length > bytes.size()) continue;
				boolean matched = true;
				for (int i = 0; i < b.length; i++) {
					if (b[i] != bytes.get(i)) {
						matched = false;
						break;
					}
				}
				if (!matched) continue;
				found = true;
				output.append(hexToString.get(b));
				for (int i = 0; i < b.length; i++) 
					bytes.remove(0);
			}
			
			if (!found) { //throw a styled error message
				final int errorIndex = e.length - bytes.size();
				final StringBuilder errorMsg = new StringBuilder(String.format("Bad sequence at index %s", errorIndex));
				errorMsg.append("\n" + ByteTools.toHexString(e) + "\n");
				for (int i = 0; i < (errorIndex * 2); i++) errorMsg.append(" ");
				errorMsg.append("^");
				throw new IllegalArgumentException(errorMsg.toString());
			}
		}
		
		return output.toString();
	}
	public List<byte[]> splitBytes(byte[] bytes) throws SonicBattleParseException{
		List<byte[]> output = new ArrayList<>();
		
		List<Byte> array = new ArrayList<>();
		for (byte b: bytes) array.add(b);

		while (array.size() > 0) {
			for (byte[] b: hexToString.keySet()) {
				if (b.length > array.size()) continue;
				boolean matched = true;
				for (int i = 0; i < b.length; i++) {
					if (b[i] != array.get(i)) {
						matched = false;
						break;
					}
				}
				if (!matched) continue;
				output.add(b);
				for (int i = 0; i < b.length; i++) 
					array.remove(0);
			}
		}
		
		return output;
	}
	
	private List<Tuple> parseDictionary(JSONObject json){
		List<Tuple> output = new ArrayList<>();
		for (Object o: json.keySet()) {
			String key = o.toString();
			byte[] hex = ByteTools.toByteArray(key);
			String string = json.get(key).toString();
			
			Tuple tuple = new Tuple(hex, string);
			output.add(tuple);
		}
		return output;
	}
	private void populateHashMaps(List<Tuple> dictionary) {
		sortByStringLength(dictionary);
		for (Tuple t: dictionary) stringToHex.put(t.string, t.hex);
		
		sortByHexLength(dictionary);
		for (Tuple t: dictionary) hexToString.put(t.hex, t.string);
	}
	private void sortByStringLength(List<Tuple> arg0) {
	    Comparator<Tuple> c = new Comparator<Tuple>()
	    {
			public int compare(Tuple arg0, Tuple arg1) {
				return Integer.compare(arg1.string.length(), arg0.string.length());
			}
	    };
	    Collections.sort(arg0, c);
	}
	private void sortByHexLength(List<Tuple> arg0) {
	    Comparator<Tuple> c = new Comparator<Tuple>()
	    {
			public int compare(Tuple arg0, Tuple arg1) {
				return Integer.compare(arg1.hex.length, arg0.hex.length);
			}
	    };
	    Collections.sort(arg0, c);
	}
	private class Tuple{
		public final byte[] hex;
		public final String string;
		public Tuple(byte[] hex, String string) {
			this.hex = hex;
			this.string = string;
		}
	}
}
