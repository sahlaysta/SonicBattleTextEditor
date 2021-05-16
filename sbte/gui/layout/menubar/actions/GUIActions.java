package sbte.gui.layout.menubar.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JOptionPane;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import sbte.gui.GUI;
import sbte.gui.GUIHandler;
import sbte.gui.layout.menubar.actions.utilities.GUIFileChooser;
import sbte.gui.layout.menubar.actions.windowmenus.GUIChangeLanguage;
import sbte.gui.layout.menubar.actions.windowmenus.GUIGoTo;
import sbte.gui.layout.menubar.actions.windowmenus.GUISearch;
import sbte.gui.layout.menubar.actions.windowmenus.ScrollMessage;
import sbte.utilities.FileTools;
import sbte.utilities.JSONTools;

public class GUIActions {
	public ROMEvent romHandler = new ROMEvent();
	
	private final GUI parent;
	public GUIActions(GUI caller) {
		parent = caller;
		romHandler.addListener(new GUIHandler());
	}

	//menubar actionlisteners
	public ActionListener open = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	open(null);
        }
    };
    public ActionListener save = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	save(parent.rom.romPath);
        }
    };
    public ActionListener saveAs = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	save(null);
        }
    };
    public ActionListener close = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	if (parent.isOpen) {
        		if (!parent.isSaved) {
        			if (yesNoDialog("close") != JOptionPane.YES_OPTION) return;
        		}
        		parent.close();
        		return;
        	}
        	System.exit(0);
        }
    };
    public ActionListener goTo = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	GUIGoTo.goToGUI(parent);
        }
    };
    public ActionListener search = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	GUISearch.searchGUI(parent);
        }
    };
    public ActionListener problematicLines = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	GUISearch.problematicGUI(parent);
        }
    };
    public ActionListener changeLanguage = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	GUIChangeLanguage.languageGUI(parent);
        }
    };
    public ActionListener about = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	parent.showMsgBox(
        			parent.localization.get("about"), 
        			"V3.3.0\n" + parent.localization.get("credits").replace("[v]", "porog") + "\nhttps://github.com/sahlaysta/SonicBattleTextEditor"
        			);
        }
    };
    public ActionListener importion = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	importion();
        }
    };
    public ActionListener exportion = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	exportion();
        }
    };
    public ActionListener undo = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	parent.listModel.undo();
        }
    };
    public ActionListener redo = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	parent.listModel.redo();
        }
    };
    public ActionListener upOne = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	parent.list.upOne();
        }
    };
    public ActionListener downOne = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	parent.list.downOne();
        }
    };
    public ActionListener textPreview = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	boolean ticked = ((JCheckBoxMenuItem)e.getSource()).isSelected();
        	if (ticked) {
        		parent.openTextPreview();
        	} else {
        		parent.closeTextPreview();
        	}
        }
    };
    
    public void open(File file) { //open a ROM
    	if (parent.isOpen) {
    		if (!parent.isSaved) {
    			if (yesNoDialog("open") != JOptionPane.YES_OPTION) return;
    		}
    	}
    		
    	File selection = null;
		if (file == null) {
			GUIFileChooser gfc = new GUIFileChooser(parent, GUIFileChooser.OPEN_FILE_PROMPT, "GBA", GUIFileChooser.ROM_PATH_PREFERENCE);
	        if (gfc.hasCanceled()) return;
	        selection = gfc.getSelectedFile();
		}
		else selection = file;
		
        parent.menuBar.fileMenu.recentOpened.add(selection);
        romHandler.raiseOpenROM(new ROMArgs(selection, parent));
    }
    
    public void save(File file) { //save ROM
    	if (parent.listModel.errors.size() > 0) {
    		GUISearch.problematicGUI(parent);
    		return;
    	}
    	
    	File selection = null;
    	if (file == null) {
			GUIFileChooser gfc = new GUIFileChooser(parent, GUIFileChooser.SAVE_FILE_PROMPT, "GBA", GUIFileChooser.ROM_PATH_PREFERENCE);
	        if (gfc.hasCanceled()) return;
	        selection = gfc.getSelectedFile();
		}
    	else {
    		selection = file;
    	}
    	
    	if (selection.exists()) { //overwrite prompt
    		Object[] options = { parent.localization.get("yes"), parent.localization.get("no") };
    		if (JOptionPane.showOptionDialog(parent, parent.localization.get("overwrite").replace("[v]", selection.toString()), parent.localization.get("save"),
            	    JOptionPane.DEFAULT_OPTION, 3, null, 
            	    options, options[0]) != JOptionPane.YES_OPTION) return;
    	}
    	
    	romHandler.raiseSaveROM(new ROMArgs(selection, parent));
    }
    
    public void exportion() { //export lines
    	GUIFileChooser gfc = new GUIFileChooser(parent, GUIFileChooser.SAVE_FILE_PROMPT, "JSON", GUIFileChooser.LINES_FILE_PREFERENCE);
        if (gfc.hasCanceled()) return;
        File selection = gfc.getSelectedFile();
        if (selection.exists()) { //overwrite prompt
        	Object[] options = { parent.localization.get("yes"), parent.localization.get("no") };
    		if (JOptionPane.showOptionDialog(parent, parent.localization.get("overwrite").replace("[v]", selection.toString()), parent.localization.get("export"),
            	    JOptionPane.DEFAULT_OPTION, 3, null, 
            	    options, options[0]) != JOptionPane.YES_OPTION) return;
    	}
        
    	StringBuilder sb = new StringBuilder("{\r\n");
    	for (int i = 0; i < parent.listModel.getSize(); i++) {
    		String key = parent.listModel.baseLines.get(i).group + "," + parent.listModel.baseLines.get(i).member;
    		String value = JSONTools.toJSONValue(parent.listModel.textBoxDisplay.get(i));
    		String append = "	\"" + key + "\": \"" + value + "\",\n";
    		sb.append(append);
    	}
    	sb.setLength(sb.length()-2);
    	sb.append("\n}");
    	
    	try {
			FileTools.writeStringToFile(selection, sb.toString());
		} catch (IOException e) {
			parent.showErrorMsgBox(parent.localization.get("error") + ": " + selection.toString(), e.toString());
			return;
		}
    	
    	parent.showMsgBox(parent.localization.get("export"), parent.localization.get("exported"));
    }
    
    public void importion() { //import lines
    	GUIFileChooser gfc = new GUIFileChooser(parent, GUIFileChooser.OPEN_FILE_PROMPT, "JSON", GUIFileChooser.LINES_FILE_PREFERENCE);
        if (gfc.hasCanceled()) return;
        File selection = gfc.getSelectedFile();
        
    	JSONObject json = null;
        try {
			String jsonFile = FileTools.readFileToString(selection);
			json = (JSONObject) JSONTools.parser.parse(jsonFile);
		} catch (FileNotFoundException | ParseException e) {
			parent.showErrorMsgBox(parent.localization.get("error") + ": " + selection.toString(), e.toString());
			return;
		}
        
        StringBuilder missed = new StringBuilder();
        for (int i = 0; i < parent.listModel.getSize(); i++) {
        	String key = parent.listModel.baseLines.get(i).group + "," + parent.listModel.baseLines.get(i).member;
        	if (json.containsKey(key)) {
            	String value = json.get(key).toString();
        		parent.listModel.setContent(i, value);
        	}
        	else {
        		missed.append(key + "\r\n");
        	}
        }
        parent.closeTextPreview();
        parent.isSaved = false;
        parent.list.setSelection(-1);
        parent.list.setSelection(0);
        
        parent.listModel.resetUndoManager();
        
        parent.showMsgBox(parent.localization.get("import"), parent.localization.get("imported"));
        if (parent.listModel.errors.size() > 0)
        	GUISearch.problematicGUI(parent);
        if (missed.length() > 0) {
        	missed.setLength(missed.length() - 2);
        	ScrollMessage.show(parent, parent.localization.get("missingLines"), missed.toString());
        }
    }
    
    private int yesNoDialog(String jsonTitle) {
    	Object[] options = { parent.localization.get("yes"), parent.localization.get("no") };
    	int choice = JOptionPane.showOptionDialog(parent, parent.localization.get("closePrompt"), parent.localization.get(jsonTitle),
        	    JOptionPane.DEFAULT_OPTION, 3, null, 
        	    options, options[0]);
    	return choice;
    }
    
    //custom listener ROM handler
    public interface ROMListener {
	    void romOpened(ROMArgs args);
	    void romSaved(ROMArgs args);
	}
	public static class ROMEvent {
	    private List<ROMListener> listeners = new ArrayList<ROMListener>();

	    public void addListener(ROMListener toAdd) {
	        listeners.add(toAdd);
	    }

	    public void raiseOpenROM(ROMArgs args) {
	        for (ROMListener orl: listeners)
	            orl.romOpened(args);
	    }
	    public void raiseSaveROM(ROMArgs args) {
	        for (ROMListener orl: listeners)
	            orl.romSaved(args);
	    }
	}
	public static class ROMArgs {
		public final File selectedPath;
		public final GUI source;
		public ROMArgs(File selectedPath, GUI source) {
			this.selectedPath = selectedPath;
			this.source = source;
		}
	}
}
