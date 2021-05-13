package sbte.GUI;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;

import org.json.simple.parser.ParseException;

import sbte.JSONTools;
import sbte.SonicBattleROMReader.ROM;
import sbte.SonicBattleROMReader.SonicBattleLine;
import sbte.SonicBattleTextParser;
import sbte.GUI.FontPreview.FontPreviewWindow;

public class GUI extends JFrame {
	public Preferences preferences;
	public HashMap<String, String> localization;
	public GUIMenuBar menuBar;
	public GUIActions actions;
	public ListModel listModel;
	
	public GUIList list;
	public GUITextBox textBox;
	public GUISplit splitPane;
	
	public FontPreviewWindow textPreview;
	
	public final SonicBattleTextParser sbtp;
	
	public GUI() {
		setLocationRelativeTo(null);
		setMinimumSize(new Dimension(50, 120));
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		try {
			preferences = new Preferences(JSONTools.getPrefsJson());
		} catch (ParseException e) {
			showMsg(JSONTools.prefsJson.getName(), e.toString(), Msg.ERROR_MESSAGE);
			System.exit(0);
		}
		preferences.applyWindowProperties(this);
		addComponentListener(preferences.windowListener);
		
		setName("json:appName");
		try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
		
		actions = new GUIActions(this);
		
		menuBar = new GUIMenuBar(this);
		setJMenuBar(menuBar);
		
		sbtp = new SonicBattleTextParser();
		
		listModel = new ListModel(this);
		list = new GUIList(this, listModel);
		textBox = new GUITextBox(this);
		splitPane = new GUISplit(JSplitPane.VERTICAL_SPLIT, list, textBox);
		splitPane.setDividerLocation(preferences.getDividerLocation());
		splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, preferences.dividerListener);
		add(splitPane);
		
		String language = preferences.getLanguage();
		setLocalization(language);
		
		disabledBeforeOpen(); //disables the components with this tag
		
		addWindowListener(new OnClose()); //save before close warning
	}
	public ROM rom = null;
	public boolean isOpen = false;
	public boolean isSaved = true;
	public void open(ROM rom, List<SonicBattleLine> arg0) {
		this.rom = rom;
		for (SonicBattleLine b: arg0)
			listModel.add(b);
		enableBeforeOpen();
		
		list.setSelection(0);
		isOpen = true;
	}
	public void close() {
		list.setSelection(-1);
		listModel.clear();
		textBox.clear();
		disabledBeforeOpen();
		isOpen = false;
		isSaved = true;
		rom = null;
	}
	private class OnClose extends WindowAdapter { //close event
		@Override
	    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
	        if (!isSaved) {
	        	Object[] options = { localization.get("yes"), localization.get("no") };
    			if (JOptionPane.showOptionDialog(GUI.this, localization.get("closePrompt"), localization.get("close"),
            	    JOptionPane.DEFAULT_OPTION, 3, null, 
            	    options, options[0]) == JOptionPane.NO_OPTION) return;
	        }
	        
	        System.exit(0);
	    }
	};
	public void openTextPreview() {
		textPreview = new FontPreviewWindow(this);
		textPreview.setVisible(true);
		refreshGUIText(textPreview);
		menuBar.viewMenu.textPreview.setSelected(true);
	}
	public void closeTextPreview() {
		textPreview.dispose();
		textPreview = null;
		menuBar.viewMenu.textPreview.setSelected(false);
	}
	public void setTextPreview(byte[] message) {
		if (textPreview != null)
			textPreview.setContent(message);
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
	public void refreshGUIText(Container c) {
		if (c == null) return;
		for (Object element: getElements(c)) {
			if (!(element instanceof Component)) { continue; }
			try {
				String value = getComponentValue((Component)element, "json");
				GUITools.setSwingObjectText(element, localization.get(value));
			} catch (GUIException e) { continue; }
		}

		list.refreshTitle();
	}
	public void disabledBeforeOpen() {
		for (Object element: getElements(this)) {
			if (!(element instanceof Component)) continue;
			try {
				String value = getComponentValue((Component)element, "disabledBeforeOpen");
				boolean disable = Boolean.parseBoolean(value);
				if (disable) ((Component) element).setEnabled(false);
			} catch (GUIException e) { continue; }
		}
	}
	public void enableBeforeOpen() {
		for (Object element: getElements(this)) {
			if (!(element instanceof Component)) continue;
			try {
				String value = getComponentValue((Component)element, "disabledBeforeOpen");
				boolean disable = Boolean.parseBoolean(value);
				if (disable) ((Component) element).setEnabled(true);
			} catch (GUIException e) { continue; }
		}
	}
	public List<Object> getElements(Container c){
		List<Object> elements = new ArrayList<>();
		elements.addAll(GUITools.getAllComponents(c));
		elements.add(c);

		return elements;
	}
	public String getComponentValue(Component component, String key) throws GUIException {
		String keyVal = key + ":";
		
		String compName = component.getName();
		if (compName == null) throw new GUIException("Null component");
		String[] parts = compName.split(",");
		for (String part: parts) {
			if (part.length() < keyVal.length()) continue;
			if (!part.substring(0, keyVal.length()).contains(keyVal)) continue;
			part = part.replace(keyVal, "");
			return part;
		}
		
		throw new GUIException("Missing value");
	}
	private class GUIException extends Exception{
		public GUIException(String arg0) {
			super(arg0);
		}
	}
	
	//misc
	public boolean isMaximized() {
		return getExtendedState() == JFrame.MAXIMIZED_BOTH;
	}
	public void maximizeWindow() {
		setExtendedState( getExtendedState()|JFrame.MAXIMIZED_BOTH );
	}
	public void showMsg(String title, String content, int arg) {
		Msg.showMessageDialog(this, content, title, arg);
	}
	public void showMsg(String title, String content) {
		this.showMsg(title, content, Msg.PLAIN_MESSAGE);
	}
	public static final class Msg extends JOptionPane {}
}
