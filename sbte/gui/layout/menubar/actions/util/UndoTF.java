package sbte.gui.layout.menubar.actions.util;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.undo.UndoManager;

import sbte.gui.GUI;
import sbte.gui.popupmenus.CopyContextMenu;

public class UndoTF extends JTextField {
	/*
	 * Custom JTextField with undo right-click menu
	 * and shortcut functionality
	 */
	public UndoTF(GUI parent) {
		UndoManager um = new UndoManager();
		getDocument().addUndoableEditListener(um);
		CopyContextMenu ccm = new CopyContextMenu(parent);
		//Control Z to undo
		JMenuItem undo = new JMenuItem(parent.localization.get("undo"));
		Action undoAction = new AbstractAction()
		{
		    @Override
		    public void actionPerformed(ActionEvent e)
		    {
		    	if (um.canUndo()) um.undo();
		    }
		};
		KeyStroke ctrlZ = KeyStroke.getKeyStroke("control Z");
		undo.addActionListener(undoAction);
		undo.setAccelerator(ctrlZ);
		getInputMap().put(ctrlZ, undoAction);
		
		//Control Y to redo
		JMenuItem redo = new JMenuItem(parent.localization.get("redo"));
		Action redoAction = new AbstractAction()
		{
		    @Override
		    public void actionPerformed(ActionEvent e)
		    {
		    	if (um.canRedo()) um.redo();
		    }
		};
		KeyStroke ctrlY = KeyStroke.getKeyStroke("control Y");
		redo.addActionListener(redoAction);
		redo.setAccelerator(ctrlY);
		getInputMap().put(ctrlY, redoAction);
    	
    	ccm.add(undo);
    	ccm.add(redo);
    	ccm.addSeparator();
		ccm.putItems();
		setComponentPopupMenu(ccm);
	}
}
