package sbte.gui.layout.splitpane.textbox;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import sbte.gui.GUI;
import sbte.gui.popupmenus.CopyContextMenu;

public final class GUITextBox extends JPanel {
	private static final long serialVersionUID = -2096201573553170972L;
	
	private final GUI parent;
	private final TextArea ta;
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
	public void setUndo(String text) {
		ta.undoOperation = true;
		ta.setText(text);
		ta.undoOperation = false;
	}
	public void setRed() {
		ta.setForeground(Color.RED);
	}
	public void unred() {
		ta.setForeground(Color.BLACK);
	}
	public void requestFocus() {
		ta.requestFocus();
	}
	public void setEnabled(boolean enabled) {
		ta.setEnabled(enabled);
		super.setEnabled(enabled);
	}
	public void clear() {
		unred();
		ta.programmaticEditing = true;
		ta.setText("");
		ta.programmaticEditing = false;
	}
	private class TextArea extends JTextArea {
		private static final long serialVersionUID = 4464584664871435557L;
		
		public boolean programmaticEditing = false;
		public boolean undoOperation = false;
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

	        	parent.list.setText(GUITextBox.this.ta.getText(), undoOperation);
	        	if (parent.isSaved) parent.isSaved = false;
	        }
	    };
	    private class PopupMenu extends MouseAdapter { //right click copy menu with undo and redo
	    	class PopupDemo extends CopyContextMenu {
				private static final long serialVersionUID = -4207580203458913254L;

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
	    	    	putItems(CopyContextMenu.EDITABLE_FIELD);
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
