package sbte.gui.layout.splitpane.list;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.KeyListener;

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

public final class GUIList extends JPanel {
	private final TitledBorder title;
	private final JScrollPane scroll;
	private final GUI parent;
	private final List list;
	public GUIList(GUI caller, DefaultListModel e) {
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
	private class List extends JList {
		public List(DefaultListModel e) {
			super(e);
			
			setPreferredSize(null);
			setLayoutOrientation(JList.VERTICAL);
			setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			ListHandler lh = new ListHandler();
			addListSelectionListener(lh);
			setCellRenderer(new ColorList(parent.listModel));
			
			//remove all the 'key click to change position' listeners
			KeyListener[] lsnrs = getKeyListeners();
			for (int i = 0; i < lsnrs.length; i++)
			    removeKeyListener(lsnrs[i]);
		}
	}
	private class ListHandler implements ListSelectionListener { //on list index changed
		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			int index = ((JList)arg0.getSource()).getSelectedIndex();
			refreshTitle();
			if (index < 0) return;
			
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
	public static class ColorList extends DefaultListCellRenderer {
		private final ListModel lm;
		public ColorList(ListModel e) {
			lm = e;
		}
		
		//background color of list items
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        	final Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        	
            if (lm.isProblematic(index)) {
            	setForeground(Color.RED);
            }
                  
            return c;
        }

    };
}
