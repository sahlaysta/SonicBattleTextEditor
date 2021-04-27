package sbte.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class GUIActions {
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
    }
}
