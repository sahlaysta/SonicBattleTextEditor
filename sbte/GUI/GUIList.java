package sbte.GUI;

import java.awt.GridLayout;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;

public class GUIList extends JPanel {
	private final TitledBorder title;
	private final GUI parent;
	public GUIList(GUI caller, DefaultListModel e) {
		parent = caller;
		
		setLayout(new GridLayout(1, 1, 0, 0));
		
		title = new TitledBorder("Line 1");
		setBorder(title);
		
		List list = new List(e);
		JScrollPane scroll = new JScrollPane(list);
		
		add(scroll);
	}
	public class List extends JList {
		public List(DefaultListModel e) {
			super(e);
			
			setPreferredSize(null);
			setLayoutOrientation(JList.VERTICAL);
			setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
	}
}
