package sahlaysta.sonicbattletexteditor.romparser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.github.sahlaysta.jsonevent.JsonEventArgs;
import com.github.sahlaysta.jsonevent.JsonEventReader;
import com.github.sahlaysta.jsonevent.JsonEventType;

/** The dictionary used by the Sonic Battle Text Parser class */
public final class SonicBattleTextParserDictionary {

	//Invisible constructor
	private SonicBattleTextParserDictionary() {}
	
	//Dictionary Entry class
	/** Dictionary entry of the SonicBattleTextParserDictionary for text parsing,
	 * holding its bytes and its in-game shown character represented by those bytes */
	public static final class Entry {
		/** The bytes of the character inside of a Sonic Battle ROM.<br>
		 * Example: the bytes of the character <code>'J'</code> is <code>2A00</code>*/
		public final byte[] bytes;
		/** The represented String from the bytes of the character as shown in-game.<br>
		 * Example: the String of the bytes <code>2A00</code> is <code>'J'</code>*/
		public final String string;
		/** Construct an Entry passing the bytes of the character and the in-game String
		 * shown by the bytes.<br>
		 * Example of an Entry: bytes = <code>2A00</code>, character = <code>'J'</code> */
		public Entry(byte[] bytes, String character) {
			this.bytes = bytes;
			this.string = character;
		}
	}
	
	//Dictionary
	/** The dictionary BYTE_TO_STRING of Sonic Battle
	 * characters. This entries are sorted by length of bytes */
	public static final Entry[] BYTE_TO_STRING;
	/** The dictionary STRING_TO_BYTE of Sonic Battle
	 * characters. This entries are sorted by the length of String */
	public static final Entry[] STRING_TO_BYTE;
	
	/*
	 * Static block to set both Dictionary arrays
	 * from SonicBattleTextParserDictionary.json
	 */
	static {
		//Read entries from SonicBattleTextParserDictionary.json
		List<Entry> entries = new ArrayList<>();
		JsonEventReader jer = new JsonEventReader(
				new InputStreamReader(
						SonicBattleTextParserDictionary.class
						.getResourceAsStream("SonicBattleTextParserDictionary.json"),
				StandardCharsets.UTF_8));
		try {
			JsonEventArgs jea = jer.next();
			while ((jea = jer.next()).type != JsonEventType.JSON_OBJECT_END)
				entries.add(new Entry(HexUtils.hexStringToBytes(jea.elmntStr), jer.next().elmntStr));
			jer.getReader().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Sort entries by length of bytes and then set the BYTE_TO_STRING map
		Collections.sort(entries, new Comparator<Entry>() {
			public int compare(Entry e1, Entry e2) {
				return e2.bytes.length - e1.bytes.length;
			}
		});
		BYTE_TO_STRING = new Entry[entries.size()];
		for (int i = 0; i < BYTE_TO_STRING.length; i++)
			BYTE_TO_STRING[i] = entries.get(i);
		
		// Sort entries by length of String and then set the STRING_TO_BYTE map
		Collections.sort(entries, new Comparator<Entry>() {
			public int compare(Entry e1, Entry e2) {
				return e2.string.length() - e1.string.length();
			}
		});
		STRING_TO_BYTE = new Entry[BYTE_TO_STRING.length];
		for (int i = 0; i < STRING_TO_BYTE.length; i++)
			STRING_TO_BYTE[i] = entries.get(i);
	}
}