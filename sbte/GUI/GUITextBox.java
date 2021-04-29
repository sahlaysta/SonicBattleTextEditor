package sbte.GUI;

import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

public class GUITextBox extends JPanel {
	private final TextArea ta;
	public GUITextBox() {
		setLayout(new GridLayout(1, 1, 0, 0));
		
		ta = new TextArea();
		JScrollPane scroll = new JScrollPane(ta);
		scroll.setBorder(new EmptyBorder(0,0,0,0));
		add(scroll);
	}
	public void setText(String text) {
		ta.programmaticEditing = true;
		ta.setText(text);
		ta.programmaticEditing = false;
	}
	private class TextArea extends JTextArea{
		public boolean programmaticEditing = false;
		public TextArea() {
			setName("disabledBeforeOpen:true");
		}
	}
}
