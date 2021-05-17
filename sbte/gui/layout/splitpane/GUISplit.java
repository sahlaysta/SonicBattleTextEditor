package sbte.gui.layout.splitpane;

import java.awt.Component;

import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;

import sbte.gui.utilities.Preferences;

public final class GUISplit extends JSplitPane {
	public GUISplit(Component arg0, Component arg1, Preferences preferences) {
		super(JSplitPane.VERTICAL_SPLIT, arg0, arg1);
		
		setBorder(new EmptyBorder(0, 5, 5, 5));
		setResizeWeight(1);
		
		setDividerLocation(preferences.getDividerLocation());
		addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, preferences.dividerListener);
	}
}
