package sbte.gui.layout.menubar;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import sbte.gui.layout.menubar.actions.GUIActions;
import sbte.gui.utilities.Preferences;

public class RecentlyOpenedMenu extends JMenu {
	public static final int RECENT_FILES_LIMIT = 10;
	
	private final GUIActions actions;
	private final Preferences preferences;
	private final RecentFiles recentFiles;
	public RecentlyOpenedMenu(GUIActions actions, Preferences preferences, String name) {
		this.actions = actions;
		this.preferences = preferences;
		setName(name);
		
		recentFiles = new RecentFiles(RECENT_FILES_LIMIT);
	}
	public void add(File file) {
		recentFiles.addElement(file);
	}
	public void clear() {
		for (Component c: this.getMenuComponents()) {
			remove(c);
		}
	}
	private class RecentFiles extends ArrayList<File>{
		private final int limit;
		public RecentFiles(int limit) {
			this.limit = limit;
			final List<File> recentsPreference = preferences.getRecentlyOpened();
			
			for (int i = 0; i < limit; i++)
				addElement(null);
			
			if (recentsPreference != null) {
				for (int i = recentsPreference.size()-1; i >= 0; i--) {
					addElement(recentsPreference.get(i));
				}
				refresh();
			}
		}
		public void addElement(File arg0) {
			int duplicateIndex = indexOf(arg0);
			if (arg0 == null) duplicateIndex = -1;
			if (duplicateIndex != -1) remove(duplicateIndex);
			
			add(0, arg0);
			refresh();
		}
		public void removeElement(int index) {
			remove(index);
			refresh();
		}
		public void refresh() {
			RecentlyOpenedMenu.this.clear();
			int index = 0;
			for (File file: this) {
				index++;
				JMenuItem jmi = new JMenuItem();
				if (file == null) {
					jmi.setText("--");
					jmi.setEnabled(false);
				}
				else {
					jmi.setText(file.toString());
					jmi.addActionListener(new ActionListener() {
					      public void actionPerformed(ActionEvent e) {
					          actions.open(file);
					      }
					});
				}
				
				jmi.setText(String.format("%02d: " + jmi.getText(), index));
				RecentlyOpenedMenu.this.add(jmi);
			}
			
			while (size() < limit) {
				addElement(null);
			}
			
			while (size() > limit) {
				removeElement(size()-1);
			}
			
			preferences.setRecentlyOpened(this);
		}
	}
}
