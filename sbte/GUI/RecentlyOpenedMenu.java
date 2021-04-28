package sbte.GUI;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import sbte.Main;

public class RecentlyOpenedMenu extends JMenu {
	public static final int RECENT_FILES_LIMIT = 10;
	private RecentFiles rf = null;
	private GUI parent;
	public RecentlyOpenedMenu(GUI caller, String name) {
		parent = caller;
		setName(name);
		
		rf = new RecentFiles(RECENT_FILES_LIMIT);
	}
	public void add(File file) {
		rf.addElement(file);
	}
	public void clear() {
		for (Component c: this.getMenuComponents()) {
			remove(c);
		}
	}
	private class RecentFiles extends ArrayList<File>{
		private int limitVal;
		public RecentFiles(int limit) {
			limitVal = limit;
			
			List<File> recentsPreference = parent.preferences.getRecentlyOpened();
			
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
			int duplicate = indexOf(arg0);
			if (arg0 == null) duplicate = -1;
			if (duplicate != -1) {
				remove(duplicate);
			}
			
			add(0, arg0);
			refresh();
		}
		public void refresh() {
			RecentlyOpenedMenu.this.clear();
			int i = 0;
			for (File f: this) {
				i++;
				JMenuItem jmi = new JMenuItem();
				if (f == null) {
					jmi.setText("--");
					jmi.setEnabled(false);
				}
				else {
					jmi.setText(f.toString());
					jmi.addActionListener(new ActionListener() {
					      public void actionPerformed(ActionEvent e) {
					          parent.actions.open(f);
					      }
					});
				}
				
				jmi.setText(String.format("%02d: " + jmi.getText(), i));
				RecentlyOpenedMenu.this.add(jmi);
			}
			
			while (size() < limitVal) {
				addElement(null);
			}
			
			while (size() > limitVal) {
				remove(size()-1);
			}
			
			parent.preferences.setRecentlyOpened(this);
		}
	}
	
	public void setRecentlyOpened(List<File> files, GUIMenuBar gmb) {
		if (files == null) return;
		
		int i = 0;
		for (File f: files) {
			if (i > RECENT_FILES_LIMIT) return;
			add(f);
		}
	}
}
