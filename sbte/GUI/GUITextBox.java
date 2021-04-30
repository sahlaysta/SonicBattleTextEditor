package sbte.GUI;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class GUITextBox extends JPanel {
	private final GUI parent;
	private TextArea ta;
	public GUITextBox(GUI caller) {
		parent = caller;
		
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
	public void setRed() {
		ta.setForeground(Color.RED);
	}
	public void unred() {
		ta.setForeground(Color.BLACK);
	}
	public void clear() {
		unred();
		ta.programmaticEditing = true;
		ta.setText("");
		ta.programmaticEditing = false;
	}
	private class TextArea extends JTextArea{
		public boolean programmaticEditing = false;
		public TextArea() {
			setName("disabledBeforeOpen:true");
			getDocument().addDocumentListener(textListener);
		}
		private DocumentListener textListener = new DocumentListener() {

	        @Override
	        public void removeUpdate(DocumentEvent e) {
	        	changed(e);
	        }

	        @Override
	        public void insertUpdate(DocumentEvent e) {
	        	changed(e);
	        }

	        @Override
	        public void changedUpdate(DocumentEvent e) {
	        	changed(e);
	        }
	        
	        void changed(DocumentEvent e) {
	        	if (programmaticEditing) return;

	        	parent.list.setText(GUITextBox.this.ta.getText());
	        	if (parent.isSaved) parent.isSaved = false;
	        }
	    };
	}
}
