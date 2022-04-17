package sahlaysta.sonicbattletexteditor.ui.components;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.EventListener;

/** Interface listener handling the action event of RecentlyOpenedFilesJMenu */
public interface RecentlyOpenedFilesJMenuListener extends EventListener {
	/** Called when a RecentlyOpenedFile JMenuItem option is performed, passing its File */
	void actionPerformed(ActionEvent e, File file);
}