package sbte.GUI;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.UIManager;

import sbte.JSONTools;

public class GUI extends JFrame {
	public Preferences preferences;
	public HashMap<String, String> localization;
	public GUIMenuBar menuBar;
	public GUIActions actions;
	
	public GUI() {
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		preferences = new Preferences(JSONTools.getPrefsJson());
		preferences.applyWindowProperties(this);
		addComponentListener(preferences.windowListener);
		
		setName("json:appName");
		try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
		
		actions = new GUIActions(this);
		
		menuBar = new GUIMenuBar(this);
		setJMenuBar(menuBar);
		
		String language = "en-*";
		setLocalization(language);
		
		disabledBeforeOpen(); //disables the components with this tag
	}

	public void setLocalization(String language) {
		localization = Localization.getMap(language);
		refreshGUIText();
	}
	public void refreshGUIText() {
		for (Object element: getElements()) {
			if (!(element instanceof Component)) continue;
			try {
				String value = getComponentValue((Component)element, "json");
				GUITools.setSwingObjectText(element, localization.get(value));
			} catch (GUIException e) { continue; }
		}
	}
	public void disabledBeforeOpen() {
		for (Object element: getElements()) {
			if (!(element instanceof Component)) continue;
			try {
				String value = getComponentValue((Component)element, "disabledBeforeOpen");
				boolean disable = Boolean.parseBoolean(value);
				if (disable) ((Component) element).setEnabled(false);
			} catch (GUIException e) { continue; }
		}
	}
	public List<Object> getElements(){
		List<Object> elements = new ArrayList<>();
		elements.addAll(GUITools.getAllComponents(this));
		elements.add(this);
		
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
}
