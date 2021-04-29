package sbte.GUI;

import java.awt.GridLayout;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import sbte.JSONTools;

public class GUIList extends JPanel {
	private final TitledBorder title;
	private final GUI parent;
	public GUIList(GUI caller, DefaultListModel e) {
		parent = caller;
		
		setLayout(new GridLayout(1, 1, 0, 0));
		
		title = new TitledBorder("g");
		setBorder(title);
		
		List list = new List(e);
		JScrollPane scroll = new JScrollPane(list);
		
		add(scroll);
		
		ListHandler lh = new ListHandler();
		list.addListSelectionListener(lh);
	}
	public void setTitle(int arg0) {
		title.setTitle(parent.localization.get("selectedLine").replace("[v]", (1 + arg0) + ""));
		repaint();
	}
	private class List extends JList {
		public List(DefaultListModel e) {
			super(e);
			
			setPreferredSize(null);
			setLayoutOrientation(JList.VERTICAL);
			setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
	}
	private class ListHandler implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			int index = ((JList)arg0.getSource()).getSelectedIndex();
			setTitle(index);
			
			String string = parent.listModel.get(index).toString();
			parent.textBox.setText(JSONTools.valueToString(string));
		}
	}
}
