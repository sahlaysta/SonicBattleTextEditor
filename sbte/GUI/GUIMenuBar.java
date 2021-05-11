package sbte.GUI;

import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class GUIMenuBar extends JMenuBar {
	private final GUI parent;
	
	public RecentlyOpenedMenu recentOpened;
	private FileMenu fileMenu;
	private EditMenu editMenu;
	private SearchMenu searchMenu;
	private ViewMenu viewMenu;
	private HelpMenu helpMenu;
	
	public GUIMenuBar(GUI caller) {
		parent = caller;
		
		recentOpened = new RecentlyOpenedMenu(parent, "json:fileRecent");

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
			
			MenuItem importion, exportion, undo, redo, upOne, downOne; //cant name a variable import smh
			importion = new MenuItem("json:import", null, parent.actions.importion);
			exportion = new MenuItem("json:export", null, parent.actions.exportion);
			undo = new MenuItem("json:undo", "control Z", parent.actions.undo);
			redo = new MenuItem("json:redo", "control Y", parent.actions.redo);
			upOne = new MenuItem("json:upOne", null, parent.actions.upOne);
			upOne.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_MASK));
			downOne = new MenuItem("json:downOne", null, parent.actions.downOne);
			downOne.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_MASK));
			
			add(importion);
			add(exportion);
			addSeparator();
			add(undo);
			add(redo);
			add(upOne);
			add(downOne);
		}
	}
	private class SearchMenu extends JMenu{
		public SearchMenu() {
			setName("json:search,disabledBeforeOpen:true");
			
			MenuItem goTo, search, problematicLines;
			goTo = new MenuItem("json:goTo", "control G", parent.actions.goTo);
			search = new MenuItem("json:search", "control F", parent.actions.search);
			problematicLines = new MenuItem("json:prob", null, parent.actions.problematicLines);
			
			add(goTo);
			add(search);
			add(problematicLines);
		}
	}
	private class ViewMenu extends JMenu{
		public ViewMenu() {
			setName("json:options");
			
			MenuItem changeLang, textPreview;
			changeLang = new MenuItem("json:changeLang", null, parent.actions.changeLanguage);
			textPreview = new MenuItem("json:textPreview", null, parent.actions.textPreview);
			
			add(changeLang);
			add(textPreview);
		}
	}
	private class HelpMenu extends JMenu{
		public HelpMenu() {
			setName("json:help");
			
			MenuItem about;
			about = new MenuItem("json:about", "F1", parent.actions.about);
			
			add(about);
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
	private class CheckBoxMenuItem extends JCheckBoxMenuItem{
		public CheckBoxMenuItem(String name, String shortcut, ActionListener action) {
			setName(name);
			if (shortcut != null) setAccelerator(KeyStroke.getKeyStroke(shortcut));
			if (action != null) addActionListener(action);
		}
	}
}