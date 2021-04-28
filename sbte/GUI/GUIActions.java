package sbte.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GUIActions {
	public OpenROMEvent openROMListener = new OpenROMEvent();
	
	private GUI parent;
	public GUIActions(GUI caller) {
		parent = caller;
	}

	public ActionListener open = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	open(null);
        }
    };
    public void open(File file) {
    	File selection = null;
		if (file == null) {
			GUIFileChooser gfc = new GUIFileChooser(GUIFileChooser.OPEN_FILE_PROMPT, "GBA");
			gfc.setParent(parent);
			gfc.show();
	        if (gfc.HasCanceled()) return;
	        selection = gfc.getSelectedFile();
		}
		else selection = file;
		
        parent.menuBar.recentOpened.add(selection);
        openROMListener.raiseOpenROM(selection);
    }
    
    //custom listener ROM handler
    public interface ROMListener {
	    void ROMopened(File rom);
	    void ROMsaved(File rom);
	}
	public static class OpenROMEvent {
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
