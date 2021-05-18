package sbte.gui.layout.menubar.actions.util;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import sbte.gui.GUI;

public final class GUIFileChooser extends JFileChooser {
	private static final long serialVersionUID = 2422590987110006512L;
	
	public static final int OPEN_FILE_PROMPT = 0;
	public static final int SAVE_FILE_PROMPT = 1;
	public static final int SAVE_AS_FILE_PROMPT = 2;
	public static final String ROM_PATH_PREFERENCE = "romPath";
	public static final String LINES_FILE_PREFERENCE = "jsonPath";
	private boolean approve;
	private GUI parent;
	private int chooseType;
	private String filter;
	public GUIFileChooser(GUI caller, int chooseType, String filter) {
		construct(caller, chooseType, filter);
		start();
	}
	public GUIFileChooser(GUI caller, int chooseType, String filter, String pathPreference) {
		construct(caller, chooseType, filter);
		setPreference(pathPreference);
		start();
	}
	private void construct(GUI caller, int chooseType, String filter) {
		parent = caller;
		this.chooseType = chooseType;
		this.filter = filter;
		
		setDialogTitle();
		setFilter(parent.localization.get("fileType"), filter);
	}
	
	
	private void setDialogTitle() {
		String dialogTitle = null;
		if (chooseType == OPEN_FILE_PROMPT) dialogTitle = parent.localization.get("open");
		else if (chooseType == SAVE_FILE_PROMPT) dialogTitle = parent.localization.get("save");
		else if (chooseType == SAVE_AS_FILE_PROMPT) dialogTitle = parent.localization.get("saveAs");
		if (dialogTitle != null) setDialogTitle(dialogTitle);
	}
	private void setFilter(String text, String filter) {
		FileNameExtensionFilter fef = null; 
		if (text != null) fef = new FileNameExtensionFilter(text.replace("[v]", filter), filter);
		else fef = new FileNameExtensionFilter(filter, filter, filter);
		addChoosableFileFilter(fef);
		setFileFilter(fef);
	}
	private String key = null;
	private void setPreference(String key) {
		this.key = key;
		if (parent.preferences.containsFileKey(key)) {
			setCurrentDirectory(new File(parent.preferences.getFile(key)));
		}
	}
	private void putPreference() {
		if (key == null) return;
		parent.preferences.setFile(key, getSelectedFile().getParentFile().toString());
	}
	
	@Override
	public File getSelectedFile() { //smart-add extension
		File output = super.getSelectedFile();
		if (output == null || this.chooseType == OPEN_FILE_PROMPT || output.exists() || getFileFilter().toString().contains("AcceptAllFileFilter")) return output;
		if (!output.toString().contains(".")) {
			File extension = new File(output.toString() + "." + filter.toLowerCase());
			if (!extension.exists()) return extension;
		}
		return output;
	}
	private void start() {
		if (chooseType == OPEN_FILE_PROMPT) {
			approve = showOpenDialog(parent) == APPROVE_OPTION;
		}
		else if (chooseType == SAVE_FILE_PROMPT || chooseType == SAVE_AS_FILE_PROMPT) {
			approve = showSaveDialog(parent) == APPROVE_OPTION;
		}
		
		if (approve) putPreference();
	}
	public boolean hasCanceled() {
		return !approve;
	}
}
