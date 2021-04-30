package sbte.GUI;

import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class GUIMenuBar extends JMenuBar {
	private final GUI parent;
	
	public RecentlyOpenedMenu recentOpened;
	private FileMenu fileMenu;
	private EditMenu editMenu;
	private ViewMenu viewMenu;
	
	public GUIMenuBar(GUI caller) {
		parent = caller;
		
		recentOpened = new RecentlyOpenedMenu(parent, "json:fileRecent");

		fileMenu = new FileMenu();
		editMenu = new EditMenu();
		viewMenu = new ViewMenu();
		
		add(fileMenu);
		add(editMenu);
		add(viewMenu);
	}
	
	//Menus
	private class FileMenu extends JMenu{
		public FileMenu() {
			setName("json:file");
			
			MenuItem open, save, saveAs, close;
			open = new MenuItem("json:open", "control O", parent.actions.open);
			save = new MenuItem("json:save,disabledBeforeOpen:true", "control S", parent.actions.save);
			saveAs = new MenuItem("json:saveAs,disabledBeforeOpen:true", "control shift S", parent.actions.saveAs);
			close = new MenuItem("json:close", null, parent.actions.close);
			
			add(open);
			add(recentOpened);
			add(save);
			add(saveAs);
			addSeparator();
			add(close);
		}
	}
	private class EditMenu extends JMenu{
		public EditMenu() {
			setName("json:edit,disabledBeforeOpen:true");
		}
	}
	private class ViewMenu extends JMenu{
		public ViewMenu() {
			setName("json:options");
			
			MenuItem changeLang;
			changeLang = new MenuItem("json:changeLang", null, null);
			
			add(changeLang);
		}
	}
	
	//custom menuitem
	private class MenuItem extends JMenuItem{
		public MenuItem(String name, String shortcut, ActionListener action) {
			setName(name);
			if (shortcut != null) setAccelerator(KeyStroke.getKeyStroke(shortcut));
			if (action != null) addActionListener(action);
		}
	}
}