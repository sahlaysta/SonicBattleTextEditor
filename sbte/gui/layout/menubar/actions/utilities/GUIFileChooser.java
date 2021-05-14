package sbte.gui.layout.menubar.actions.utilities;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import sbte.gui.GUI;

public class GUIFileChooser extends JFileChooser {
	public static final int OPEN_FILE_PROMPT = 0;
	public static final int SAVE_FILE_PROMPT = 1;
	public static final int SAVE_AS_FILE_PROMPT = 2;
	public static final String ROM_PATH_PREFERENCE = "romPath";
	public static final String LINES_FILE_PREFERENCE = "jsonPath";
	private boolean approve;
	private GUI parent;
	private final int args;
	private final String filter;
	public GUIFileChooser(int args, String filter) {
		this.args = args;
		this.filter = filter;
	}
	public void setParent(GUI caller) {
		parent = caller;
		setDialogTitle();
		setFilter(parent.localization.get("fileType"), filter);
	}
	private void setDialogTitle() {
		String dialogTitle = null;
		if (args == OPEN_FILE_PROMPT) dialogTitle = parent.localization.get("open");
		else if (args == SAVE_FILE_PROMPT) dialogTitle = parent.localization.get("save");
		else if (args == SAVE_AS_FILE_PROMPT) dialogTitle = parent.localization.get("saveAs");
		if (dialogTitle != null) setDialogTitle(dialogTitle);
	}
	public void setFilter(String text, String filter) {
		FileNameExtensionFilter fef = null; 
		if (text != null) fef = new FileNameExtensionFilter(text.replace("[v]", filter), filter);
		else fef = new FileNameExtensionFilter(filter, filter, filter);
		addChoosableFileFilter(fef);
		setFileFilter(fef);
	}
	public String key;
	public void setPreference(String key) {
		this.key = key;
		if (parent.preferences.containsKey(key)) {
			setCurrentDirectory(new File(parent.preferences.get(key).toString()));
		}
	}
	private void putPreference() {
		if (key == null) return;
		parent.preferences.put(key, getSelectedFile().toString());
	}
	
	@Override
	public File getSelectedFile() { //smart-add extension
		File output = super.getSelectedFile();
		if (output == null || this.args == OPEN_FILE_PROMPT || output.exists() || getFileFilter().toString().contains("AcceptAllFileFilter")) return output;
		if (!output.toString().contains(".")) {
			File extension = new File(output.toString() + "." + filter.toLowerCase());
			if (!extension.exists()) return extension;
		}
		return output;
	}
	public void show() {
		if (args == OPEN_FILE_PROMPT) {
			approve = showOpenDialog(parent) == APPROVE_OPTION;
		}
		else if (args == SAVE_FILE_PROMPT || args == SAVE_AS_FILE_PROMPT) {
			approve = showSaveDialog(parent) == APPROVE_OPTION;
		}
		
		if (approve) putPreference();
	}
	public boolean hasCanceled() {
		return !approve;
	}
}
