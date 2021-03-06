package sbte.gui.layout.splitpane.list;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import sbte.gui.GUI;
import sbte.gui.popupmenus.PropertiesContextMenu;

public final class GUIList extends JPanel {
	private static final long serialVersionUID = -2190873094177847552L;
	
	private final TitledBorder title;
	private final JScrollPane scroll;
	private final GUI parent;
	private final List list;
	public GUIList(GUI caller, DefaultListModel<String> e) {
		parent = caller;
		
		setLayout(new GridLayout(1, 1, 0, 0));
		
		title = new TitledBorder("");
		setBorder(title);
		
		list = new List(e);
		scroll = new JScrollPane(list);
		
		add(scroll);
	}
	public void refreshTitle() {
		int index = list.getSelectedIndex();
		if (index < 0) {
			 title.setTitle(parent.localization.get("textEdit"));
			 repaint();
			 return;
		}
		
		String s = null;
		if (!parent.listModel.isProblematic(index)) { //normal subtitle
			title.setTitleColor(Color.BLACK);
			s = parent.localization.get("selectedLine").replace("[v]", (1 + index) + "");
		}
		else { //error subtitle
			title.setTitleColor(Color.RED);
			s = parent.localization.get("invalidChar").replace("[v]", parent.listModel.errors.get(index) + "");
		}
		title.setTitle(s);
		repaint();
	}
	public void setText(String e, boolean undo) {
		int index = list.getSelectedIndex();
		if (!undo) {
			parent.listModel.undo.add(e, index, parent.listModel.textBoxDisplay.get(index));
		}
		parent.listModel.setContent(index, e);
		refreshTitle();
	}
	public void setSelection(int index) {
		if (index == -1) {
			list.clearSelection();
			return;
		}
		list.setSelectedIndex(index);
	}
	public int getSelection() {
		return list.getSelectedIndex();
	}
	public void ensureIndexIsVisible(int index) {
		list.ensureIndexIsVisible(index);
	}
	public void upOne() {
		int index = list.getSelectedIndex();
		if (index <= 0) return;
		setSelection(index - 1);
		ensureIndexIsVisible(index - 1);
	}
	public void downOne() {
		int index = list.getSelectedIndex();
		if (index > parent.listModel.getSize() - 2) return;
		setSelection(index + 1);
		ensureIndexIsVisible(index + 1);
	}
	private class List extends JList<String> {
		private static final long serialVersionUID = 1224580479384959661L;

		public List(DefaultListModel<String> e) {
			super(e);
			
			setPreferredSize(null);
			setLayoutOrientation(JList.VERTICAL);
			setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			ListHandler lh = new ListHandler();
			addListSelectionListener(lh);
			setCellRenderer(new ColorList(parent.listModel));
			addFocusListener(new FocusHandler());
			addMouseListener(new PropertiesMenu());
			
			//remove all the 'key click to change position' listeners
			KeyListener[] lsnrs = getKeyListeners();
			for (int i = 0; i < lsnrs.length; i++)
			    removeKeyListener(lsnrs[i]);
		}
	}
	private class PropertiesMenu extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent arg0) {
			popupMenu(arg0);
		}
		@Override
		public void mouseReleased(MouseEvent arg0) {
			popupMenu(arg0);
		}
		void popupMenu(MouseEvent arg0){ //properties right-click
			if (!arg0.isPopupTrigger()) return;
			if (getSelection() < 0) return;
			if (!((Component)arg0.getSource()).isEnabled()) return;
			final int index = getSelection();
			final int mousePos = list.locationToIndex(arg0.getPoint());
			if (index != mousePos) return; //mouse must be on selected line to show
			
			new PropertiesContextMenu(parent).show((Component)arg0.getSource(), arg0.getX(), arg0.getY());
		}
	}
	private class FocusHandler implements FocusListener { //on focus changed
		@Override
		public void focusGained(FocusEvent arg0) {
			parent.textBox.requestFocus();
		}
		@Override
		public void focusLost(FocusEvent arg0) {
			
		}
	}
	private class ListHandler implements ListSelectionListener { //on list index changed
		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			int index = ((JList<?>)arg0.getSource()).getSelectedIndex();
			refreshTitle();
			if (index < 0) {
				parent.textBox.clear();
				parent.textBox.setEnabled(false);
				parent.menuBar.editMenu.properties.setEnabled(false);
				return;
			}
			parent.textBox.setEnabled(true);
			parent.menuBar.editMenu.properties.setEnabled(true);
			
			parent.textBox.setText(parent.listModel.textBoxDisplay.get(index).toString());
			parent.setTextPreview(index);
			if (parent.listModel.isProblematic(index)) {
				parent.textBox.setRed();
			}
			else {
				parent.textBox.unred();
			}
		}
	}
	private class ColorList extends DefaultListCellRenderer {
		private static final long serialVersionUID = -6895089364903065431L;
		
		private final ListModel lm;
		public ColorList(ListModel e) {
			lm = e;
		}
		
		//background color of list items
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        	final Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        	
            if (lm.isProblematic(index)) {
            	setForeground(Color.RED);
            }
                  
            return c;
        }

    };
}
