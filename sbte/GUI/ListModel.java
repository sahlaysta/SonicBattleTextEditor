package sbte.GUI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.DefaultListModel;

import sbte.JSONTools;

public class ListModel extends DefaultListModel{
	private List<byte[]> content = new ArrayList<>();
	public List<String> textBoxDisplay = new ArrayList<>();
	HashMap<Integer, String> errors = new HashMap<>();
	
	private final GUI parent;
	public ListModel(GUI caller) {
		parent = caller;
	}
	public void add(byte[] line) {
		addEmpty();
		setContent(super.getSize() - 1, line);
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
			this.setElementAt(JSONTools.toJSONValue(string), index);
		} //error
		catch (IllegalArgumentException e) {
			errors.put(index, e.toString());
			parent.textBox.setRed();
			
			this.setElementAt(string, index);
		}
		
		textBoxDisplay.set(index, string);
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
