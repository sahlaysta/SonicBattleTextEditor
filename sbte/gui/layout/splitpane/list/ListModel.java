package sbte.gui.layout.splitpane.list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.DefaultListModel;

import sbte.gui.GUI;
import sbte.gui.util.SBUndoManager;
import sbte.parser.SonicBattleROMReader.SonicBattleLine;
import sbte.parser.SonicBattleTextParser.SonicBattleParseException;
import sbte.util.JSONTools;

public final class ListModel extends DefaultListModel<String> {
	private static final long serialVersionUID = 1057801624301988574L;
	
	public List<byte[]> content = new ArrayList<>();
	public List<SonicBattleLine> baseLines = new ArrayList<>();
	public List<String> textBoxDisplay = new ArrayList<>();
	public HashMap<Integer, String> errors = new HashMap<>();
	public SBUndoManager undo = null;
	
	private final GUI parent;
	public ListModel(GUI caller) {
		parent = caller;
		resetUndoManager();
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
	public void clear() {
		content = new ArrayList<>();
		baseLines = new ArrayList<>();
		textBoxDisplay = new ArrayList<>();
		errors = new HashMap<>();
		resetUndoManager();
		super.clear();
	}
	public void undo() {
		undo.undo();
	}
	public void redo() {
		undo.redo();
	}
	public void resetUndoManager() {
		undo = new SBUndoManager(parent);
	}
	public void setContent(int index, String string) {
		try { //successfully set
			errors.remove(index);
			parent.textBox.unred();
			
			byte[] message = parent.sbtp.parseHexBinary(string);
			content.set(index, message);
		} //error
		catch (SonicBattleParseException e) {
			char errorChar = e.source.charAt(e.index);
			errors.put(index, errorChar + "");
			parent.textBox.setRed();
		}

		parent.setTextPreview(index);
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
	public List<SonicBattleLine> getSonicBattleLines() {
		List<SonicBattleLine> output = new ArrayList<>();
		for (int i = 0; i < content.size(); i++) {
			SonicBattleLine sbl = new SonicBattleLine(content.get(i), baseLines.get(i).pointer, -1, baseLines.get(i).group, baseLines.get(i).member);
			output.add(sbl);
		}
		
		return output;
	}
	
	@Override
	public void setElementAt(String element, int index) {
		if (element.length() == 0) element = " "; //JList empty line fix
		super.setElementAt(element, index);
	}
}
