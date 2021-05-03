package sbte.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

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
    		if (JOptionPane.showConfirmDialog(parent, parent.localization.get("overwrite").replace("[v]", selection.toString()), parent.localization.get("saveAs"), JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) return;
    	}
    	
    	ROMListener.raiseSaveROM(new ROMArgs(selection, parent));
    }
    
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
