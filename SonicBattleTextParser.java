package sbte;

import java.util.ArrayList;
import java.util.List;

public class SonicBattleTextParser {
	private SonicBattleTextLib lib;
	public SonicBattleTextParser(SonicBattleTextLib sbtl) {
		lib = sbtl;
	}
	public SonicBattleTextLib getLib() {
		return lib;
	}
	public String parseString(byte[] b) {
		lib.sortByHexLength();
		StringBuilder sb = new StringBuilder();
		List<Byte> bl = new ArrayList<>();
		for (byte i: b) bl.add(i);
		
		while (bl.size() > 0) {
			boolean match = true;
			for (int i = 0; i < lib.getItemCount(); i++) {
				byte[] focus = lib.get(i).getBytes();
				if (focus.length > bl.size()) continue;
				match = true;
				for (int ii = 0; ii < focus.length; ii++) {
					if (focus[ii] != bl.get(ii)) { match = false; break; }
				}
				if (match) {
					sb.append(lib.get(i).getLetter());
					for (byte ii: focus) bl.remove(0);
					break;
				}
			}
			if (!match) throw new java.lang.Error("Unknown HEX sequence: \"" + new ByteSequence(new byte[] { bl.get(0), bl.get(1) }).getHex() + "\"");
		}
		return sb.toString();
	}
	public byte[] parseSB(String si) {
		List<Byte> op = new ArrayList<>();
		String s = si;
		lib.sortByLetterLength();
		while (s.length() > 0) {
			boolean match = true;
			for (int i = 0; i < lib.getItemCount(); i++) {
				String focus = lib.get(i).getLetter();
				if (focus.length() > s.length()) continue;
				match = true;
				if (s.indexOf(focus) != 0) { match = false; continue; }
				if (match) {
					for (byte b: lib.get(i).getBytes()) op.add(b);
					s = s.substring(focus.length());
					break;
				}
			}
			if (!match) throw new java.lang.Error("Unknown char: '" + s.toCharArray()[0] + "'");
		}
		
		byte[] output = new byte[op.size()];
		for (int i = 0; i < output.length; i++) output[i] = op.get(i);
		return output;
	}
}
