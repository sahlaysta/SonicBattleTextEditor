package sbte.GUI;

import java.awt.Container;
import java.util.HashMap;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import sbte.Main;

public class GUIFileChooser extends JFileChooser {
	public static final int OPEN_FILE_PROMPT = 0;
	public static final int SAVE_FILE_PROMPT = 1;
	public static final int SAVE_AS_FILE_PROMPT = 2;
	private int argsVal;
	private boolean approve;
	private GUI parentVal;
	private String filterVal;
	
	public GUIFileChooser(int args, String filter) {
		argsVal = args;
		filterVal = filter;
	}
	public void setParent(GUI caller) {
		parentVal = caller;
		setDialogTitle();
		setFilter(parentVal.localization.get("fileType"), filterVal);
	}
	private void setDialogTitle() {
		String dialogTitle = null;
		if (argsVal == OPEN_FILE_PROMPT) dialogTitle = parentVal.localization.get("open");
		else if (argsVal == SAVE_FILE_PROMPT) dialogTitle = parentVal.localization.get("save");
		else if (argsVal == SAVE_AS_FILE_PROMPT) dialogTitle = parentVal.localization.get("saveAs");
		if (dialogTitle != null) setDialogTitle(dialogTitle);
	}
	public void setFilter(String text, String filter) {
		FileNameExtensionFilter fef = null; 
		if (text != null) fef = new FileNameExtensionFilter(text.replace("[v]", filter), filter);
		else fef = new FileNameExtensionFilter(filter, filter, filter);
		addChoosableFileFilter(fef);
		setFileFilter(fef);
	}
	public void show() {
		if (argsVal == OPEN_FILE_PROMPT) {
			approve = showOpenDialog(parentVal) == APPROVE_OPTION;
		}
		else if (argsVal == SAVE_FILE_PROMPT || argsVal == SAVE_AS_FILE_PROMPT) {
			approve = showSaveDialog(parentVal) == APPROVE_OPTION;
		}
	}
	public boolean HasCanceled() {
		return !approve;
	}
}
