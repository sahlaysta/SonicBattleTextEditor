package sbte.gui.popupmenus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import sbte.gui.GUI;
import sbte.gui.layout.menubar.actions.windowmenus.GUIProperties;

public final class PropertiesContextMenu extends JPopupMenu {
	private static final long serialVersionUID = -1207436870108470445L;
	
	private final GUI parent;
	public PropertiesContextMenu(GUI caller) {
		parent = caller;
		
		JMenuItem jmi = new JMenuItem(parent.localization.get("properties"));
		jmi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GUIProperties.propertiesGUI(parent);
			}
		});
		add(jmi);
	}
}
