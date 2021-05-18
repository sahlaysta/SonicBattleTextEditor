package sbte.gui.popupmenus;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;

import sbte.gui.GUI;

public class CopyContextMenu extends JPopupMenu {
	private static final long serialVersionUID = 3321735238397981864L;
	/*
	 * Global pop up menu with cut, copy and paste
	 * menuitems
	 */
	private final GUI parent;
	public CopyContextMenu(GUI caller) {
		parent = caller;
	}
	public void putItems() {
		Action cut = new DefaultEditorKit.CutAction();
        cut.putValue(Action.NAME, parent.localization.get("cut"));
        cut.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control X"));
        add( cut );
        
        Action copy = new DefaultEditorKit.CopyAction();
		copy.putValue(Action.NAME, parent.localization.get("copy"));
		copy.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control C"));
		add(copy);
		
		Action paste = new DefaultEditorKit.PasteAction();
        paste.putValue(Action.NAME, parent.localization.get("paste"));
        paste.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control V"));
        add( paste );
        
        class SelectAll extends TextAction
	    {
			private static final long serialVersionUID = -3854872983764809018L;

			public SelectAll()
	        {
	            super(parent.localization.get("selectAll"));
	            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control A"));
	        }

	        public void actionPerformed(ActionEvent e)
	        {
	            JTextComponent component = getFocusedComponent();
	            component.selectAll();
	            component.requestFocusInWindow();
	        }
	    }
        
        Action selectAll = new SelectAll();
        add( selectAll );
	}
}
