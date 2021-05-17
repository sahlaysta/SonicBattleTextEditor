package sbte.gui.layout.menubar;

import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import sbte.gui.layout.menubar.actions.GUIActions;
import sbte.gui.utilities.Preferences;

public final class GUIMenuBar extends JMenuBar {
	public FileMenu fileMenu;
	public EditMenu editMenu;
	public SearchMenu searchMenu;
	public ViewMenu viewMenu;
	public HelpMenu helpMenu;
	
	private final GUIActions actions;
	private final Preferences preferences;
	public GUIMenuBar(GUIActions actions, Preferences preferences) {
		this.actions = actions;
		this.preferences = preferences;

		fileMenu = new FileMenu();
		editMenu = new EditMenu();
		searchMenu = new SearchMenu();
		viewMenu = new ViewMenu();
		helpMenu = new HelpMenu();
		
		add(fileMenu);
		add(editMenu);
		add(searchMenu);
		add(viewMenu);
		add(helpMenu);
	}
	
	//Menus
	public class FileMenu extends JMenu{
		public final MenuItem open, save, saveAs, close;
		public final RecentlyOpenedMenu recentOpened;
		public FileMenu() {
			setName("json:file");
			
			open = new MenuItem("json:open", "control O", actions.open);
			recentOpened = new RecentlyOpenedMenu(actions, preferences, "json:fileRecent");
			save = new MenuItem("json:save,disabledBeforeOpen:true", "control S", actions.save);
			saveAs = new MenuItem("json:saveAs,disabledBeforeOpen:true", "control shift S", actions.saveAs);
			close = new MenuItem("json:close", null, actions.close);
			
			add(open);
			add(recentOpened);
			add(save);
			add(saveAs);
			addSeparator();
			add(close);
		}
	}
	public class EditMenu extends JMenu{
		public final MenuItem importion, exportion, undo, redo, upOne, downOne, properties;
		public EditMenu() {
			setName("json:edit,disabledBeforeOpen:true");
			
			importion = new MenuItem("json:import", null, actions.importion);
			exportion = new MenuItem("json:export", null, actions.exportion);
			undo = new MenuItem("json:undo", "control Z", actions.undo);
			redo = new MenuItem("json:redo", "control Y", actions.redo);
			upOne = new MenuItem("json:upOne", null, actions.upOne);
			upOne.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK));
			downOne = new MenuItem("json:downOne", null, actions.downOne);
			downOne.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK));
			properties = new MenuItem("json:properties", null, null);
			properties.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_DOWN_MASK));
			
			add(importion);
			add(exportion);
			addSeparator();
			add(undo);
			add(redo);
			add(upOne);
			add(downOne);
			addSeparator();
			add(properties);
		}
	}
	public class SearchMenu extends JMenu{
		public final MenuItem goTo, search, problematicLines;
		public SearchMenu() {
			setName("json:search,disabledBeforeOpen:true");

			goTo = new MenuItem("json:goTo", "control G", actions.goTo);
			search = new MenuItem("json:search", "control F", actions.search);
			problematicLines = new MenuItem("json:prob", null, actions.problematicLines);
			
			add(goTo);
			add(search);
			add(problematicLines);
		}
	}
	public class ViewMenu extends JMenu{
		public final MenuItem changeLang;
		public CheckBoxMenuItem textPreview;
		public ViewMenu() {
			setName("json:options");
			
			changeLang = new MenuItem("json:changeLang", null, actions.changeLanguage);
			textPreview = new CheckBoxMenuItem("json:textPreview,disabledBeforeOpen:true", null, actions.textPreview);
			
			add(changeLang);
			add(textPreview);
		}
	}
	public class HelpMenu extends JMenu{
		public final MenuItem about;
		public HelpMenu() {
			setName("json:help");
			
			about = new MenuItem("json:about", "F1", actions.about);
			
			add(about);
		}
	}
	
	
	//custom menuitem
	public class MenuItem extends JMenuItem{
		public MenuItem(String name, String shortcut, ActionListener action) {
			setMenuItemProperties(this, name, shortcut, action);
		}
	}
	public class CheckBoxMenuItem extends JCheckBoxMenuItem{
		public CheckBoxMenuItem(String name, String shortcut, ActionListener action) {
			setMenuItemProperties(this, name, shortcut, action);
		}
	}
	private void setMenuItemProperties(JMenuItem arg0, String name, String shortcut, ActionListener action) {
		arg0.setName(name);
		if (shortcut != null) arg0.setAccelerator(KeyStroke.getKeyStroke(shortcut));
		if (action != null) arg0.addActionListener(action);
	}
}