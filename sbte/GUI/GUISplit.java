package sbte.GUI;

import java.awt.Component;

import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;

public class GUISplit extends JSplitPane {
	public GUISplit(int arg0, Component arg1, Component arg2) {
		super(arg0, arg1, arg2);
		
		setBorder(new EmptyBorder(0, 5, 5, 5));
		setResizeWeight(1);
	}
}
