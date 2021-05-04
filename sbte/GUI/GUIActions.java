package sbte.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import sbte.FileTools;
import sbte.JSONTools;
import sbte.GUI.GUI.Msg;

public class GUIActions {
	public ROMEvent ROMListener = new ROMEvent();
	
	private final GUI parent;
	public GUIActions(GUI caller) {
		parent = caller;
		ROMListener.addListener(new GUIHandler());
	}

	//menubar actions
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
        			if (yesNoDialog("close") == JOptionPane.NO_OPTION) return;
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
        	JOptionPane.showMessageDialog(parent, "V3.2.0\n" + parent.localization.get("credits").replace("[v]", "porog") + "\nhttps://github.com/sahlaysta/SonicBattleTextEditor", parent.localization.get("about"), JOptionPane.INFORMATION_MESSAGE);
        }
    };
    public ActionListener importion = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	importion();
        }
    };
    public ActionListener exportion = new ActionListener() {
        public void actionPerformed(ActionEvent e) { //export all lines to json file
        	exportion();
        }
    };
    public ActionListener undo = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	parent.listModel.undo();
        }
    };
    public ActionListener redo = new ActionListener() {
        public void actionPerformed(ActionEvent e) { //export all lines to json file
        	parent.listModel.redo();
        }
    };
    public ActionListener upOne = new ActionListener() {
        public void actionPerformed(ActionEvent e) { //export all lines to json file
        	parent.list.upOne();
        }
    };
    public ActionListener downOne = new ActionListener() {
        public void actionPerformed(ActionEvent e) { //export all lines to json file
        	parent.list.downOne();
        }
    };
    
    public void open(File file) {
    	if (parent.isOpen) {
    		if (!parent.isSaved) {
    			if (yesNoDialog("open") == JOptionPane.NO_OPTION) return;
    		}
    	}
    		
    	File selection = null;
		if (file == null) {
			GUIFileChooser gfc = new GUIFileChooser(GUIFileChooser.OPEN_FILE_PROMPT, "GBA");
			gfc.setParent(parent);
			gfc.setPreference(GUIFileChooser.ROM_PATH_PREFERENCE);
			gfc.show();
	        if (gfc.HasCanceled()) return;
	        selection = gfc.getSelectedFile();
		}
		else selection = file;
		
        parent.menuBar.recentOpened.add(selection);
        ROMListener.raiseOpenROM(new ROMArgs(selection, parent));
    }
    
    public void save(File file) {
    	if (parent.listModel.errors.size() > 0) {
    		GUISearch.problematicGUI(parent);
    		return;
    	}
    	
    	File selection = null;
    	if (file == null) {
			GUIFileChooser gfc = new GUIFileChooser(GUIFileChooser.SAVE_FILE_PROMPT, "GBA");
			gfc.setParent(parent);
			gfc.setPreference(GUIFileChooser.ROM_PATH_PREFERENCE);
			gfc.show();
	        if (gfc.HasCanceled()) return;
	        selection = gfc.getSelectedFile();
		}
    	else {
    		selection = file;
    	}
    	
    	if (selection.exists()) {
    		if (JOptionPane.showConfirmDialog(parent, parent.localization.get("overwrite").replace("[v]", selection.toString()), parent.localization.get("save"), JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) return;
    	}
    	
    	ROMListener.raiseSaveROM(new ROMArgs(selection, parent));
    }
    
    public void exportion() {
    	GUIFileChooser gfc = new GUIFileChooser(GUIFileChooser.SAVE_FILE_PROMPT, "JSON");
		gfc.setParent(parent);
		gfc.setPreference(GUIFileChooser.LINES_FILE_PREFERENCE);
		gfc.show();
        if (gfc.HasCanceled()) return;
        File selection = gfc.getSelectedFile();
        if (selection.exists()) {
    		if (JOptionPane.showConfirmDialog(parent, parent.localization.get("overwrite").replace("[v]", selection.toString()), parent.localization.get("export"), JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) return;
    	}
        
    	StringBuilder sb = new StringBuilder("{\r\n");
    	for (int i = 0; i < parent.listModel.getSize(); i++) {
    		String key = parent.listModel.baseLines.get(i).group + "," + parent.listModel.baseLines.get(i).member;
    		String value = parent.listModel.get(i).toString();
    		String append = "	\"" + key + "\": \"" + value + "\",\n";
    		sb.append(append);
    	}
    	sb.setLength(sb.length()-2);
    	sb.append("\n}");
    	
    	try {
			FileTools.writeStringToFile(selection, sb.toString());
		} catch (IOException e) {
			parent.showMsg(parent.localization.get("error") + ": " + selection.toString(), e.toString(), Msg.ERROR_MESSAGE);
			return;
		}
    	
    	parent.showMsg(parent.localization.get("export"), parent.localization.get("exported"), Msg.INFORMATION_MESSAGE);
    }
    
    public void importion() {
    	GUIFileChooser gfc = new GUIFileChooser(GUIFileChooser.OPEN_FILE_PROMPT, "JSON");
		gfc.setParent(parent);
		gfc.setPreference(GUIFileChooser.LINES_FILE_PREFERENCE);
		gfc.show();
        if (gfc.HasCanceled()) return;
        File selection = gfc.getSelectedFile();
        
    	JSONObject json = null;
        try {
			String jsonFile = FileTools.readFileToString(selection);
			json = (JSONObject) JSONTools.jp.parse(jsonFile);
		} catch (FileNotFoundException e) {
			parent.showMsg(parent.localization.get("error") + ": " + selection.toString(), e.toString(), Msg.ERROR_MESSAGE);
			return;
		} catch (ParseException e) {
			parent.showMsg(parent.localization.get("error") + ": " + selection.toString(), e.toString(), Msg.ERROR_MESSAGE);
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
        
        parent.listModel.resetUndoManager();
        
        parent.showMsg(parent.localization.get("import"), parent.localization.get("imported"), Msg.INFORMATION_MESSAGE);
        if (parent.listModel.errors.size() > 0)
        	GUISearch.problematicGUI(parent);
        if (missed.length() > 0) {
        	missed.setLength(missed.length() - 2);
        	ScrollMessage.show(parent, parent.localization.get("missingLines"), missed.toString());
        }
    }
    
    private int yesNoDialog(String jsonTitle) {
    	return JOptionPane.showConfirmDialog(parent, parent.localization.get("closePrompt"), parent.localization.get(jsonTitle), JOptionPane.YES_NO_OPTION);
    }
    
    //custom listener ROM handler
    public interface ROMListener {
	    void ROMopened(ROMArgs args);
	    void ROMsaved(ROMArgs args);
	}
	public static class ROMEvent {
	    private List<ROMListener> listeners = new ArrayList<ROMListener>();

	    public void addListener(ROMListener toAdd) {
	        listeners.add(toAdd);
	    }

	    public void raiseOpenROM(ROMArgs args) {
	        for (ROMListener orl: listeners)
	            orl.ROMopened(args);
	    }
	    public void raiseSaveROM(ROMArgs args) {
	        for (ROMListener orl: listeners)
	            orl.ROMsaved(args);
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
