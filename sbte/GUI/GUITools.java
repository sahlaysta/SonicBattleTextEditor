package sbte.GUI;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;

public class GUITools {
	public static List<Component> getAllComponents(Container container) {
		Component[] iterate = container.getComponents();
		if (container instanceof JMenu)
			iterate = ((JMenu) container).getMenuComponents();
		
		List<Component> components = new ArrayList<>();
		for (Component c: iterate) {
			components.add(c);
			if (c instanceof Container) {
				components.addAll(getAllComponents((Container) c));
			}
		}
		
		return components;
	}
	
	public static void setSwingObjectText(Object object, String text) {
		//generated
		if (object instanceof javax.swing.AbstractButton)
			((javax.swing.AbstractButton) object).setText(text);
		else if (object instanceof javax.swing.DefaultListCellRenderer)
			((javax.swing.DefaultListCellRenderer) object).setText(text);
		else if (object instanceof javax.swing.JButton)
			((javax.swing.JButton) object).setText(text);
		else if (object instanceof javax.swing.JCheckBox)
			((javax.swing.JCheckBox) object).setText(text);
		else if (object instanceof javax.swing.JCheckBoxMenuItem)
			((javax.swing.JCheckBoxMenuItem) object).setText(text);
		else if (object instanceof javax.swing.JEditorPane)
			((javax.swing.JEditorPane) object).setText(text);
		else if (object instanceof javax.swing.JFormattedTextField)
			((javax.swing.JFormattedTextField) object).setText(text);
		else if (object instanceof javax.swing.JLabel)
			((javax.swing.JLabel) object).setText(text);
		else if (object instanceof javax.swing.JMenu)
			((javax.swing.JMenu) object).setText(text);
		else if (object instanceof javax.swing.JMenuItem)
			((javax.swing.JMenuItem) object).setText(text);
		else if (object instanceof javax.swing.JPasswordField)
			((javax.swing.JPasswordField) object).setText(text);
		else if (object instanceof javax.swing.JRadioButton)
			((javax.swing.JRadioButton) object).setText(text);
		else if (object instanceof javax.swing.JRadioButtonMenuItem)
			((javax.swing.JRadioButtonMenuItem) object).setText(text);
		else if (object instanceof javax.swing.JTextArea)
			((javax.swing.JTextArea) object).setText(text);
		else if (object instanceof javax.swing.JTextField)
			((javax.swing.JTextField) object).setText(text);
		else if (object instanceof javax.swing.JTextPane)
			((javax.swing.JTextPane) object).setText(text);
		else if (object instanceof javax.swing.JToggleButton)
			((javax.swing.JToggleButton) object).setText(text);
		else if (object instanceof javax.swing.plaf.basic.BasicArrowButton)
			((javax.swing.plaf.basic.BasicArrowButton) object).setText(text);
		else if (object instanceof javax.swing.plaf.basic.BasicComboBoxRenderer)
			((javax.swing.plaf.basic.BasicComboBoxRenderer) object).setText(text);
		else if (object instanceof javax.swing.plaf.metal.MetalComboBoxButton)
			((javax.swing.plaf.metal.MetalComboBoxButton) object).setText(text);
		else if (object instanceof javax.swing.plaf.metal.MetalScrollButton)
			((javax.swing.plaf.metal.MetalScrollButton) object).setText(text);
		else if (object instanceof javax.swing.table.DefaultTableCellRenderer)
			((javax.swing.table.DefaultTableCellRenderer) object).setText(text);
		else if (object instanceof javax.swing.text.JTextComponent)
			((javax.swing.text.JTextComponent) object).setText(text);
		else if (object instanceof javax.swing.tree.DefaultTreeCellRenderer)
			((javax.swing.tree.DefaultTreeCellRenderer) object).setText(text);
		else if (object instanceof javax.swing.JDialog)
			((javax.swing.JDialog) object).setTitle(text);
		else if (object instanceof javax.swing.JFrame)
			((javax.swing.JFrame) object).setTitle(text);
		else if (object instanceof javax.swing.JInternalFrame)
			((javax.swing.JInternalFrame) object).setTitle(text);
		else if (object instanceof javax.swing.border.TitledBorder)
			((javax.swing.border.TitledBorder) object).setTitle(text);
	}
	public static String getSwingObjectText(Object object) {
		//generated
		if (object instanceof javax.swing.AbstractButton)
			return ((javax.swing.AbstractButton) object).getText();
		if (object instanceof javax.swing.DefaultListCellRenderer)
			return ((javax.swing.DefaultListCellRenderer) object).getText();
		if (object instanceof javax.swing.JButton)
			return ((javax.swing.JButton) object).getText();
		if (object instanceof javax.swing.JCheckBox)
			return ((javax.swing.JCheckBox) object).getText();
		if (object instanceof javax.swing.JCheckBoxMenuItem)
			return ((javax.swing.JCheckBoxMenuItem) object).getText();
		if (object instanceof javax.swing.JEditorPane)
			return ((javax.swing.JEditorPane) object).getText();
		if (object instanceof javax.swing.JFormattedTextField)
			return ((javax.swing.JFormattedTextField) object).getText();
		if (object instanceof javax.swing.JLabel)
			return ((javax.swing.JLabel) object).getText();
		if (object instanceof javax.swing.JMenu)
			return ((javax.swing.JMenu) object).getText();
		if (object instanceof javax.swing.JMenuItem)
			return ((javax.swing.JMenuItem) object).getText();
		if (object instanceof javax.swing.JPasswordField)
			return ((javax.swing.JPasswordField) object).getText();
		if (object instanceof javax.swing.JRadioButton)
			return ((javax.swing.JRadioButton) object).getText();
		if (object instanceof javax.swing.JRadioButtonMenuItem)
			return ((javax.swing.JRadioButtonMenuItem) object).getText();
		if (object instanceof javax.swing.JTextArea)
			return ((javax.swing.JTextArea) object).getText();
		if (object instanceof javax.swing.JTextField)
			return ((javax.swing.JTextField) object).getText();
		if (object instanceof javax.swing.JTextPane)
			return ((javax.swing.JTextPane) object).getText();
		if (object instanceof javax.swing.JToggleButton)
			return ((javax.swing.JToggleButton) object).getText();
		if (object instanceof javax.swing.plaf.basic.BasicArrowButton)
			return ((javax.swing.plaf.basic.BasicArrowButton) object).getText();
		if (object instanceof javax.swing.plaf.basic.BasicComboBoxRenderer)
			return ((javax.swing.plaf.basic.BasicComboBoxRenderer) object).getText();
		if (object instanceof javax.swing.plaf.metal.MetalComboBoxButton)
			return ((javax.swing.plaf.metal.MetalComboBoxButton) object).getText();
		if (object instanceof javax.swing.plaf.metal.MetalScrollButton)
			return ((javax.swing.plaf.metal.MetalScrollButton) object).getText();
		if (object instanceof javax.swing.table.DefaultTableCellRenderer)
			return ((javax.swing.table.DefaultTableCellRenderer) object).getText();
		if (object instanceof javax.swing.text.JTextComponent)
			return ((javax.swing.text.JTextComponent) object).getText();
		if (object instanceof javax.swing.tree.DefaultTreeCellRenderer)
			return ((javax.swing.tree.DefaultTreeCellRenderer) object).getText();
		if (object instanceof javax.swing.JDialog)
			return ((javax.swing.JDialog) object).getTitle();
		if (object instanceof javax.swing.JFrame)
			return ((javax.swing.JFrame) object).getTitle();
		if (object instanceof javax.swing.JInternalFrame)
			return ((javax.swing.JInternalFrame) object).getTitle();
		if (object instanceof javax.swing.border.TitledBorder)
			return ((javax.swing.border.TitledBorder) object).getTitle();

		return null; //no matches
	}
}
