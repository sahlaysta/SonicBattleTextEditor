package sbte.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GUIActions {
	public OpenROMEvent openRomListener = new OpenROMEvent();
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
        openRomListener.raiseROM(selection);
    }
    
    //custom listener to open a ROM
    public interface OpenROMListener {
	    void ROMopened(File rom);
	}
	public static class OpenROMEvent {
	    private List<OpenROMListener> listeners = new ArrayList<OpenROMListener>();

	    public void addListener(OpenROMListener toAdd) {
	        listeners.add(toAdd);
	    }

	    public void raiseROM(File rom) {
	        for (OpenROMListener orl: listeners)
	            orl.ROMopened(rom);
	    }
	}
}
