package sahlaysta.sonicbattletexteditor;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;

import sahlaysta.sonicbattletexteditor.ui.GUI;

/** Main code block of Sonic Battle Text Editor */
public class Main {
	/** Load the Sonic Battle Text Editor GUI, opening the
	 * Sonic Battle ROM argument if exists */
	public static void main(String[] args) {
		GUI gui = new GUI();
		if (args.length == 1) {
			gui.addComponentListener(new ComponentListener() {
				public void componentHidden(ComponentEvent e) {}
				public void componentMoved(ComponentEvent e) {}
				public void componentResized(ComponentEvent e) {}
				public void componentShown(ComponentEvent e) {
					File file = new File(args[0]);
					if (file.exists() && !file.isDirectory())
						gui.openRom(file);
					gui.removeComponentListener(this);
				}
			});
		}
		gui.setVisible(true);
	}
}