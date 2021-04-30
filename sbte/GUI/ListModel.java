package sbte.GUI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.DefaultListModel;

import sbte.JSONTools;
import sbte.SonicBattleROMReader.SonicBattleLine;
import sbte.SonicBattleTextParser.SonicBattleParseException;

public class ListModel extends DefaultListModel{
	public List<byte[]> content = new ArrayList<>();
	public List<SonicBattleLine> baseLines = new ArrayList<>();
	
	public List<String> textBoxDisplay = new ArrayList<>();
	public HashMap<Integer, String> errors = new HashMap<>();
	
	private final GUI parent;
	public ListModel(GUI caller) {
		parent = caller;
	}
	public void add(SonicBattleLine line) {
		addContent(line.content);
		baseLines.add(line);
	}
	private void addContent(byte[] arg0) {
		addEmpty();
		setContent(super.getSize() - 1, arg0);
	}
	private void addEmpty() {
		addElement(null);
		content.add(null);
		textBoxDisplay.add(null);
	}
	public void setContent(int index, String string) {
		try { //successfully set
			errors.remove(index);
			parent.textBox.unred();
			
			content.set(index, parent.sbtp.parseHexBinary(string));
		} //error
		catch (SonicBattleParseException e) {
			char errorChar = e.source.charAt(e.index);
			errors.put(index, parent.localization.get("invalidChar").replace("[v]", errorChar + ""));
			parent.textBox.setRed();
		}
		
		textBoxDisplay.set(index, string);
		this.setElementAt(JSONTools.toJSONValue(string), index);
	}
	public void setContent(int arg0, byte[] arg1) {
		content.set(arg0, arg1);
		
		String s = parent.sbtp.parseString(arg1);
		textBoxDisplay.set(arg0, s);
		this.setElementAt(JSONTools.toJSONValue(s), arg0);
	}
	public boolean isProblematic(int index) {
		for (int i: errors.keySet()) {
			if (i == index)
				return true;
		}
		
		return false;
	}
	
	@Override
	public void setElementAt(Object object, int index) {
		String value = object.toString();
		if (value.length() == 0) value = " "; //JList empty line fix
		super.setElementAt(value, index);
	}
}
