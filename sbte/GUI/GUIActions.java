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
	}

	//menubar actions
	public ActionListener open = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	open(null);
        }
    };
    public ActionListener save = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	save(parent.openedRom);
        }
    };
    public ActionListener saveAs = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	save(null);
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
        ROMListener.raiseOpenROM(selection);
    }
    
    public void save(File file) {
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
    	
    	ROMListener.raiseSaveROM(selection);
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
	    void ROMopened(File rom);
	    void ROMsaved(File rom);
	}
	public static class ROMEvent {
	    private List<ROMListener> listeners = new ArrayList<ROMListener>();

	    public void addListener(ROMListener toAdd) {
	        listeners.add(toAdd);
	    }

	    public void raiseOpenROM(File rom) {
	        for (ROMListener orl: listeners)
	            orl.ROMopened(rom);
	    }
	    public void raiseSaveROM(File rom) {
	        for (ROMListener orl: listeners)
	            orl.ROMsaved(rom);
	    }
	}
}
