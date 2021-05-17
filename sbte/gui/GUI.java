package sbte.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.json.simple.parser.ParseException;

import sbte.gui.layout.menubar.GUIMenuBar;
import sbte.gui.layout.menubar.actions.GUIActions;
import sbte.gui.layout.menubar.actions.windowmenus.textpreview.TextPreviewWindow;
import sbte.gui.layout.splitpane.GUISplit;
import sbte.gui.layout.splitpane.list.GUIList;
import sbte.gui.layout.splitpane.list.ListModel;
import sbte.gui.layout.splitpane.textbox.GUITextBox;
import sbte.gui.utilities.GUITools;
import sbte.gui.utilities.Localization;
import sbte.gui.utilities.Preferences;
import sbte.parser.SonicBattleTextParser;
import sbte.parser.SonicBattleROMReader.ROM;
import sbte.parser.SonicBattleROMReader.SonicBattleLine;
import sbte.util.JSONTools;

public final class GUI extends JFrame {
	public final SonicBattleTextParser sbtp;
	public final Preferences preferences;
	public HashMap<String, String> localization;
	public GUIMenuBar menuBar;
	public GUIActions actions;
	public ListModel listModel;
	public GUIList list;
	public GUITextBox textBox;
	public GUISplit splitPane;
	public TextPreviewWindow textPreview;
	
	public GUI() {
		setProperties();
		sbtp = new SonicBattleTextParser();
		preferences = initializePreferences();
		initializeComponents(preferences);
	}
	private void setProperties() {
		setLocationRelativeTo(null);
		setMinimumSize(new Dimension(50, 120));
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		class OnClose extends WindowAdapter { //save before close warning
			@Override
		    public void windowClosing(WindowEvent windowEvent) {
		        if (!isSaved) {
		        	Object[] options = { localization.get("yes"), localization.get("no") };
	    			if (JOptionPane.showOptionDialog(GUI.this, localization.get("closePrompt"), localization.get("close"),
	            	    JOptionPane.DEFAULT_OPTION, 3, null, 
	            	    options, options[0]) != JOptionPane.YES_OPTION) return;
		        }
		        
		        System.exit(0);
		    }
		}
		addWindowListener(new OnClose());
		setName("json:appName");
		try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
	}
	private Preferences initializePreferences() {
		Preferences result = null;
		try {
			result = new Preferences(this, JSONTools.getPrefsJson());
		} catch (ParseException e) {
			showErrorMsgBox(JSONTools.prefsJson.getName(), e.toString());
			System.exit(0);
		}
		result.applyWindowProperties(this);
		return result;
	}
	private void initializeComponents(Preferences prefs) {
		actions = new GUIActions(this);
		menuBar = new GUIMenuBar(actions, prefs);
		listModel = new ListModel(this);
		list = new GUIList(this, listModel);
		textBox = new GUITextBox(this);
		splitPane = new GUISplit(list, textBox, prefs);
		setJMenuBar(menuBar);
		add(splitPane);
		setLocalization(prefs.getLanguage());
		setDisabledBeforeOpen(false);
	}
	
	
	public ROM rom = null;
	public boolean isOpen = false;
	public boolean isSaved = true;
	public void open(ROM rom, List<SonicBattleLine> arg0) {
		this.rom = rom;
		for (SonicBattleLine b: arg0)
			listModel.add(b);
		setDisabledBeforeOpen(true);
		
		list.setSelection(0);
		isOpen = true;
	}
	public void close() {
		closeTextPreview();
		list.setSelection(-1);
		listModel.clear();
		textBox.clear();
		setDisabledBeforeOpen(false);
		isOpen = false;
		isSaved = true;
		rom = null;
	}
	
	public void openTextPreview() {
		textPreview = new TextPreviewWindow(this);
		textPreview.setVisible(true);
		refreshGUIText(textPreview);
		menuBar.viewMenu.textPreview.setSelected(true);
		textPreview.setContent(list.getSelection());
	}
	public void closeTextPreview() {
		if (textPreview == null) return;
		textPreview.dispose();
		textPreview = null;
		menuBar.viewMenu.textPreview.setSelected(false);
	}
	public void setTextPreview(int index) {
		if (textPreview != null)
			textPreview.setContent(index);
	}
	
	public List<SonicBattleLine> getSonicBattleLines() {
		return listModel.getSonicBattleLines();
	}
	
	public void setLocalization(String language) {
		localization = Localization.getMap(language);
		preferences.setLanguage(localization.get("thisKey"));
		refreshGUIText(this);
		refreshGUIText(textPreview);
	}
	private void refreshGUIText(Container c) {
		if (c == null) return;
		HashMap<Object, String> elements = GUITools.getAllElements(c, "json");
		for (Object element: elements.keySet()) {
			final String value = elements.get(element);
			final String text = localization.get(value);
			GUITools.setSwingObjectText(element, text);
		}

		list.refreshTitle();
	}
	private void setDisabledBeforeOpen(boolean enabled) {
		for (Object element: GUITools.getAllElements(this, "disabledBeforeOpen").keySet()) {
			final Component comp = (Component)element;
			comp.setEnabled(enabled);
		}
	}
	
	//misc
	public boolean isMaximized() {
		return getExtendedState() == JFrame.MAXIMIZED_BOTH;
	}
	public void maximizeWindow() {
		setExtendedState( getExtendedState()|JFrame.MAXIMIZED_BOTH );
	}
	public void showMsgBox(String title, String content) {
		msgBox(title, content, JOptionPane.INFORMATION_MESSAGE);
	}
	public void showErrorMsgBox(String title, String content) {
		msgBox(title, content, JOptionPane.ERROR_MESSAGE);
	}
	private void msgBox(String title, String content, int messageType) {
		JOptionPane.showOptionDialog(this, content, title, JOptionPane.DEFAULT_OPTION, messageType, null, new Object[]{ localization.get("ok") }, null);
	}
}
