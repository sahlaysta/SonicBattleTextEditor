package sbte.gui.util;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JTextArea;
import javax.swing.undo.UndoManager;

import sbte.gui.GUI;

public final class SBUndoManager {
	private Set<Integer> firstEdit = new HashSet<>();
	private final UndoWrapper uw = new UndoWrapper();
	private final GUI parent;
	public SBUndoManager(GUI caller) {
		parent = caller;
	}
	public void add(String string, int index, String previous) {
		if (!firstEdit.contains(index)) {
			uw.add(new UndoItem(previous, index).toString());
		}
		firstEdit.add(index);
		
		UndoItem ui = new UndoItem(string, index);
		uw.add(ui.toString());
	}
	public void undo() {
		if (!uw.canUndo()) return;
		UndoItem ui = new UndoItem(uw.getUndo());
		setUndoItem(ui);
	}
	public void redo() {
		if (!uw.canRedo()) return;
		UndoItem ui = new UndoItem(uw.getRedo());
		setUndoItem(ui);
	}
	private void setUndoItem(UndoItem ui) {
		parent.list.setSelection(ui.index);
		parent.list.ensureIndexIsVisible(ui.index);
		parent.textBox.setUndo(ui.string);
	}
	private class UndoItem {
		public final String string;
		public final int index;
		public UndoItem(String string, int index) {
			this.string = string;
			this.index = index;
		}
		public UndoItem(String fromString) {
			this.index = Integer.parseInt(fromString.substring(0, fromString.indexOf(':')));
			this.string = fromString.substring(fromString.indexOf(':') + 1);
		}
		public String toString() {
			return index + ":" + string;
		}
	}
	private class UndoWrapper {
		private final JTextArea jta;
		private final UndoManager um;
		private int history = 0;
		public UndoWrapper() {
			jta = new JTextArea();
			um = new UndoManager();
			jta.getDocument().addUndoableEditListener(um);
		}
		public String getUndo() {
			um.undo();
			um.undo(); //need to undo twice for some reason unsure why
			history--;
			return jta.getText();
		}
		public String getRedo() {
			um.redo();
			um.redo();
			history++;
			return jta.getText();
		}
		public boolean canUndo() {
			return history > 1;
		}
		public boolean canRedo() {
			return um.canRedo();
		}
		public void add(String e) {
			jta.setText(e);
			history++;
		}
	}
}
