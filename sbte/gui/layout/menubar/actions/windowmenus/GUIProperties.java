package sbte.gui.layout.menubar.actions.windowmenus;

import javax.swing.JDialog;

import sbte.gui.GUI;

public final class GUIProperties {
	public static void propertiesGUI(GUI caller) {
		new PropertiesGUI(caller).setVisible(true);
	}
	public static class PropertiesGUI extends JDialog {
		private static final long serialVersionUID = -2414038955855809883L;

		public PropertiesGUI(GUI parent) {
			
		}
	}
}
