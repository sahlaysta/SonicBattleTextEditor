package sbte.GUI;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class GUIList extends JPanel {
	private final TitledBorder title;
	private final JScrollPane scroll;
	private final GUI parent;
	private List list;
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
			if (parent.isDarkTheme) DarkTheme.setDarkTitledBorder(title);
			s = parent.localization.get("selectedLine").replace("[v]", (1 + index) + "");
		}
		else { //error subtitle
			title.setTitleColor(Color.RED);
			s = parent.listModel.errors.get(index);
		}
		title.setTitle(s);
		repaint();
	}
	public void setText(String e) {
		int index = list.getSelectedIndex();
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
	public void setDarkTheme() {
		DarkTheme.setDarkScroll(scroll);
		DarkTheme.setDarkList(list);
		DarkTheme.setDarkPanel(this);
		DarkTheme.setDarkTitledBorder(title);
	}
	public class List extends JList {
		public List(DefaultListModel e) {
			super(e);
			
			setPreferredSize(null);
			setLayoutOrientation(JList.VERTICAL);
			setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			ListHandler lh = new ListHandler();
			addListSelectionListener(lh);
			setCellRenderer(new ColorList());
		}
	}
	private class ListHandler implements ListSelectionListener { //on list index changed
		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			int index = ((JList)arg0.getSource()).getSelectedIndex();
			refreshTitle();
			if (index < 0) return;
			
			parent.textBox.setText(parent.listModel.textBoxDisplay.get(index).toString());
			if (parent.listModel.isProblematic(index)) {
				parent.textBox.setRed();
			}
			else {
				parent.textBox.unred();
			}
		}
	}
	public class ColorList extends DefaultListCellRenderer {
		//background color of list items
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        	Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (parent.listModel.isProblematic(index)){
            	setForeground(Color.RED);
            }
                  
            return c;
        }

    };
}
