package sbte.gui.utilities;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.JSplitPane;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import sbte.gui.GUI;
import sbte.utilities.JSONTools;

public class Preferences extends JSONObject {
	private final GUI parent;
	public Preferences(GUI caller, JSONObject jsonObject) {
		parent = caller;
		for (Object o: jsonObject.keySet()) {
			String key = o.toString();
			Object value = jsonObject.get(key);
			super.put(key, value);
		}
		applyWindowProperties(parent);
		parent.addComponentListener(windowListener);
	}
	
	private static final String WINDOW_PROPERTIES_KEY = "windowProperties";
	private static final String WINDOW_MAXIMIZED_KEY = "windowMaximized";
	private static final String WINDOW_SIZE_KEY = "windowSize";
	private static final String WINDOW_LOCATION_KEY = "windowLocation";
	private static final String RECENT_FILES_KEY = "recentFiles";
	private static final String FILE_PREFERENCE_KEY = "fileChooserPreference";
	private static final String DIVIDER_LOCATION_KEY = "dividerLocation";
	private static final String LANGUAGE_KEY = "language";

	public void applyWindowProperties(GUI gui) {
		WindowProperties wp = getWindowProperties();
		
		gui.setSize(wp.windowSizeX, wp.windowSizeY);
		gui.setLocation(wp.windowLocationX, wp.windowLocationY);
		if (wp.windowMaximized) gui.maximizeWindow();
	}
	public void saveWindowProperties(GUI gui) {
		boolean maxed = gui.isMaximized();
		WindowProperties wp = null;
		if (!maxed) {
			Point p = gui.getLocation();
			Dimension d = gui.getSize();
			wp = new WindowProperties(maxed, p.x, p.y, d.width, d.height);
		} else {
			WindowProperties cwp = getWindowProperties();
			wp = new WindowProperties(maxed, cwp.windowLocationX, cwp.windowLocationY, cwp.windowSizeX, cwp.windowSizeY);
		}

		put(WINDOW_PROPERTIES_KEY, wp.toJSONObject());
	}
	public WindowProperties getWindowProperties() {
		if (!super.containsKey(WINDOW_PROPERTIES_KEY)) return new WindowProperties();
		
		JSONObject json = (JSONObject) super.get(WINDOW_PROPERTIES_KEY);
		boolean windowMaximized = (boolean) json.get(WINDOW_MAXIMIZED_KEY);
		int windowSizeX = -1, windowSizeY = -1, windowLocationX = -1, windowLocationY = -1;
		{// get size
			JSONObject size = (JSONObject) json.get(WINDOW_SIZE_KEY);
			windowSizeX = objToInt(size.get("X"));
			windowSizeY = objToInt(size.get("Y"));
		}
		{// get location
			JSONObject location = (JSONObject) json.get(WINDOW_LOCATION_KEY);
			windowLocationX = objToInt(location.get("X"));
			windowLocationY = objToInt(location.get("Y"));
		}
		
		return new WindowProperties(windowMaximized, windowLocationX, windowLocationY, windowSizeX, windowSizeY);
	}
	
	private ComponentListener windowListener = new ComponentListener() {
		@Override
		public void componentHidden(ComponentEvent arg0) {
			
		}

		@Override
		public void componentMoved(ComponentEvent arg0) {
			changed((GUI)arg0.getSource());
		}

		@Override
		public void componentResized(ComponentEvent arg0) {
			changed((GUI)arg0.getSource());
		}

		@Override
		public void componentShown(ComponentEvent arg0) {
			
		}
		
		public void changed(GUI gui) {
			saveWindowProperties(gui);
		}
    };
	private int objToInt(Object arg0) {
		if (arg0 instanceof Long) return Math.toIntExact((long)arg0);
		if (arg0 instanceof Integer) return (int)arg0;
		return (Integer)null;
	}
	private class WindowProperties{
		public final boolean windowMaximized;
		public final int windowLocationX;
		public final int windowLocationY;
		public final int windowSizeX;
		public final int windowSizeY;
		public WindowProperties() {
			//default values
			windowMaximized = false;
			windowLocationX = 200;
			windowLocationY = 200;
			windowSizeX = 350;
			windowSizeY = 260;
		}
		public WindowProperties(boolean maximized, int locationX, int locationY, int sizeX, int sizeY) {
			windowMaximized = maximized;
			windowLocationX = locationX;
			windowLocationY = locationY;
			windowSizeX = sizeX;
			windowSizeY = sizeY;
		}
		public JSONObject toJSONObject() {
			JSONObject json = new JSONObject();
			json.put(WINDOW_MAXIMIZED_KEY, windowMaximized);
			{//windowSize key
				JSONObject size = new JSONObject();
				size.put("X", windowSizeX);
				size.put("Y", windowSizeY);
				json.put(WINDOW_SIZE_KEY, size);
			}
			
			{//windowLocation key
				JSONObject location = new JSONObject();
				location.put("X", windowLocationX);
				location.put("Y", windowLocationY);
				json.put(WINDOW_LOCATION_KEY, location);
			}
			
			return json;
		}
	}
	public List<File> getRecentlyOpened(){
		if (super.containsKey(RECENT_FILES_KEY)) {
			List<File> output = new ArrayList<>();
			JSONArray jsonArray = (JSONArray) super.get(RECENT_FILES_KEY);
			
			for (int i = 0; i < jsonArray.size(); i++) {
				if (jsonArray.get(i) == null) break;
				File f = new File(jsonArray.get(i).toString());
				output.add(f);
			}
			
			return output;
		}
		
		return null;
	}
	public void setRecentlyOpened(List<File> files) {
		JSONArray recentFiles = new JSONArray();
		for (File f: files) {
			if (f == null) {
				recentFiles.add(null);
				continue;
			}
			recentFiles.add(f.toString());
		}
		
		put(RECENT_FILES_KEY, recentFiles);
	}
	public void saveDividerLocation(int e) {
		put(DIVIDER_LOCATION_KEY, e);
	}
	public int getDividerLocation() {
		if (super.containsKey(DIVIDER_LOCATION_KEY))
			return objToInt(super.get(DIVIDER_LOCATION_KEY));
		
		return 130; //default divider location
	}
	public String getLanguage() {
		if (super.containsKey(LANGUAGE_KEY))
			return super.get(LANGUAGE_KEY).toString();
		
		return Locale.getDefault().toLanguageTag(); //default language
	}
	public void setLanguage(String e) {
		put(LANGUAGE_KEY, e);
	}
	public PropertyChangeListener dividerListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
        	saveDividerLocation(((JSplitPane)e.getSource()).getDividerLocation());
        }
	};
	public void setFile(String key, String file) {
		JSONObject files = null;
		getObj: {
			if (super.containsKey(FILE_PREFERENCE_KEY)) {
				files = (JSONObject)super.get(FILE_PREFERENCE_KEY);
				break getObj;
			}
			files = new JSONObject();
		}
		files.put(key, file);
		put(FILE_PREFERENCE_KEY, files);
	}
	public String getFile(String key) {
		JSONObject files = (JSONObject)super.get(FILE_PREFERENCE_KEY);
		return files.get(key).toString();
	}
	public boolean containsFileKey(String key) {
		if (!super.containsKey(FILE_PREFERENCE_KEY)) return false;
		JSONObject files = (JSONObject)super.get(FILE_PREFERENCE_KEY);
		return files.containsKey(key);
	}
	
	@Override
	public Object put(Object key, Object value) {
		Object output = super.put(key, value);
		try {
			JSONTools.savePrefsJson(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return output;
	}
}
