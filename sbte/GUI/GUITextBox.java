package sbte.GUI;

import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

public class GUITextBox extends JPanel {
	public GUITextBox() {
		setLayout(new GridLayout(1, 1, 0, 0));
		
		TextArea ta = new TextArea();
		JScrollPane scroll = new JScrollPane(ta);
		scroll.setBorder(new EmptyBorder(0,0,0,0));
		add(scroll);
	}
	public class TextArea extends JTextArea{
		public TextArea() {
			setName("disabledBeforeOpen:true");
		}
	}
}
