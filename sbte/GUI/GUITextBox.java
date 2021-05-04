package sbte.GUI;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultEditorKit;

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
	public void setTextUnprogrammatically(String text) {
		ta.unprogrammaticEditing = true;
		ta.setText(text);
		ta.unprogrammaticEditing = false;
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
	private class TextArea extends JTextArea {
		public boolean programmaticEditing = false;
		public boolean unprogrammaticEditing = false;
		public TextArea() {
			setName("disabledBeforeOpen:true");
			getDocument().addDocumentListener(textListener);
			addMouseListener(new PopupMenu());
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

	        	parent.list.setText(GUITextBox.this.ta.getText(), unprogrammaticEditing);
	        	if (parent.isSaved) parent.isSaved = false;
	        }
	    };
	    private class PopupMenu extends MouseAdapter {
	    	class PopupDemo extends CopyContextMenu {
	    	    public PopupDemo(GUI caller) {
	    	    	super(caller);
	    	    	JMenuItem undo = new JMenuItem(parent.localization.get("undo"));
	    	    	undo.addActionListener(new ActionListener() {
	    	    		public void actionPerformed(ActionEvent e) {
	    	    			parent.listModel.undo();
	    	    		}
	    	    	});
	    	    	undo.setAccelerator(KeyStroke.getKeyStroke("control Z"));
	    	    	JMenuItem redo = new JMenuItem(parent.localization.get("redo"));
	    	    	redo.addActionListener(new ActionListener() {
	    	    		public void actionPerformed(ActionEvent e) {
	    	    			parent.listModel.redo();
	    	    		}
	    	    	});
	    	    	redo.setAccelerator(KeyStroke.getKeyStroke("control Y"));
	    	    	
	    	    	add(undo);
	    	    	add(redo);
	    	    	addSeparator();
	    	    	putItems();
	    	    }
	    	}
	    	
	    	public void mousePressed(MouseEvent e) {
	            if (e.isPopupTrigger())
	                doPop(e);
	        }

	        public void mouseReleased(MouseEvent e) {
	            if (e.isPopupTrigger())
	                doPop(e);
	        }

	        private void doPop(MouseEvent e) {
	        	if (!((JTextArea)e.getSource()).isEnabled()) return;
	            PopupDemo menu = new PopupDemo(parent);
	            menu.show(e.getComponent(), e.getX(), e.getY());
	        }
	    }
	}
}
