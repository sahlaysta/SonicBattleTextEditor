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
	private final GUI parent;
	private final List list;
	public GUIList(GUI caller, DefaultListModel e) {
		parent = caller;
		
		setLayout(new GridLayout(1, 1, 0, 0));
		
		title = new TitledBorder("g");
		setBorder(title);
		
		list = new List(e);
		JScrollPane scroll = new JScrollPane(list);
		
		add(scroll);
	}
	public void refreshTitle() {
		int index = list.getSelectedIndex();
		
		String s = null;
		if (!parent.listModel.isProblematic(index)) {
			title.setTitleColor(Color.BLACK);
			s = parent.localization.get("selectedLine").replace("[v]", (1 + index) + "");
		}
		else {
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
		list.setSelectedIndex(index);
	}
	public class List extends JList {
		public List(DefaultListModel e) {
			super(e);
			
			setPreferredSize(null);
			setLayoutOrientation(JList.VERTICAL);
			setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			ListHandler lh = new ListHandler();
			addListSelectionListener(lh);
			setCellRenderer(colorList);
		}
	}
	private class ListHandler implements ListSelectionListener { //on list index changed
		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			int index = ((JList)arg0.getSource()).getSelectedIndex();
			refreshTitle();
			
			parent.textBox.setText(parent.listModel.textBoxDisplay.get(index).toString());
			if (parent.listModel.isProblematic(index)) {
				parent.textBox.setRed();
			}
			else {
				parent.textBox.unred();
			}
		}
	}
	public DefaultListCellRenderer colorList = new DefaultListCellRenderer() {
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
