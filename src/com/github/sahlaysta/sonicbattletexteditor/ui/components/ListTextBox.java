package com.github.sahlaysta.sonicbattletexteditor.ui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/** The ListTextBox component represents a text-editor GUI for the editing of an array of Strings.
 * It is a JSplitPane where the top component is the JList and the bottom component is the JTextArea.
 * Call the <code>populate</code> method to display an array of Strings to the JList, then the user
 * can click on an item in the JList to edit the selected String using the JTextArea text box. Then
 * retrieve the Strings with <code>getStrings()</code> */
public class ListTextBox extends JSplitPane {
	private static final long serialVersionUID = 1L;
	/** Display Strings: The collection of Strings shown on the JList, and not the Strings returned by <code>getStrings()</code>.
	 * This is so that Strings with escaped line breaks are shown to the JList, rather than the Strings themselves */
	protected DefaultListModel<String> strings_display;
	/** Content Strings: The Strings returned by <code>getStrings()</code> */
	protected String[] strings_content;
	/** JList: The top component of JSplitPane */
	protected JList<String> jList;
	/** The default foreground color of a JList. Used by JListColorRenderer */
	protected Color jListDefaultColor;
	/** The default selection foreground color of a JList. Used by JListColorRenderer */
	protected Color jListDefaultSelectionColor;
	/** The JTextArea text box to edit the Strings: The bottom component of JSplitPane */
	protected JTextArea jTextArea;
	/** The default foreground color of JTextArea. Used by the <code>refreshTextBoxColor</code> method */
	protected Color jTextAreaDefaultColor;
	/** The ListTextBoxUndoManager for undo and redo */
	protected ListTextBoxUndoManager ltbum = new ListTextBoxUndoManager();

	/** JSplitPane layout construction */
	public ListTextBox() {
		setOrientation(VERTICAL_SPLIT);
		strings_display = new DefaultListModel<>();
		
		//Top component JList
		JPanel listPanel = new JPanel();
		jList = new JList<>(strings_display);
		jListDefaultColor = jList.getForeground();
		jListDefaultSelectionColor = jList.getSelectionForeground();
		jList.setLayoutOrientation(JList.VERTICAL);
		jList.setPreferredSize(null);
		jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jList.addMouseListener(new OnJListRightClick());
		jList.addMouseMotionListener(new OnJListRightClick());
		jList.addListSelectionListener(new OnJListSelection());
		jList.addFocusListener(new OnJListFocus());
		jList.setCellRenderer(new ColorMapListCellRenderer());
		for (KeyListener kl: jList.getKeyListeners())
			jList.removeKeyListener(kl);
		listPanel.setLayout(new GridLayout(1,1,0,0));
		listPanel.add(new JScrollPane(jList));
		
		//Bottom component JtextArea
		JPanel textBoxPanel = new JPanel();
		textBoxPanel.setLayout(new GridLayout(1,1,0,0));
		jTextArea = new JTextArea();
		jTextAreaDefaultColor = jTextArea.getForeground();
		jTextArea.setEnabled(false);
		jTextArea.getDocument().addDocumentListener(new OnTextBoxChanged());
		((AbstractDocument)jTextArea.getDocument()).setDocumentFilter(ltbum);
		JScrollPane jTextAreaJScrollPane = new JScrollPane(jTextArea);
		jTextAreaJScrollPane.setBorder(new EmptyBorder(0,0,0,0));
		textBoxPanel.add(jTextAreaJScrollPane);
		
		//Set components
		setTopComponent(listPanel);
		setBottomComponent(textBoxPanel);
		setResizeWeight(1);
		setDividerLocation(90);
		setDividerSize(7);
	}
	
	/** Overrides the JSplitPane <code>repaint</code> method to also call the <code>refreshTextboxColor</code> method */
	@Override
	public void repaint() {
		refreshTextboxColor();
		super.repaint();
	}
	
	/** Solve JList event recursion */
	protected boolean doJListSelectionEvents = true;
	/** A ListSelectionListener added to the JList. When a JList index is selected, change the textbox to the String of the selected index */
	protected class OnJListSelection implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (!doJListSelectionEvents)
				return;
			final int index = jList.getSelectedIndex();
			
			for (ListTextBoxListener l: listTextBoxListeners) //call ListTextBoxListeners
				l.indexChanged(index);
			
			if (index < 0) {
				setTextBox("");
				jTextArea.setEnabled(false);
			} else {
				setTextBox(strings_content[jList.getSelectedIndex()]);
				jTextArea.setEnabled(true);
			}
		}
	}
	/** Solve JTextArea event recursion */
	protected boolean doTextBoxChangedEvents = true;
	/** A DocumentListener added to the JTextArea. When the text box is edited, update strings_content and set
	 * the selected index's String on the JList to the text box's text */
	protected class OnTextBoxChanged implements DocumentListener {
		@Override public void changedUpdate(DocumentEvent e) {textBoxChanged(e, jTextArea.getText(), jList.getSelectedIndex());}
		@Override public void insertUpdate(DocumentEvent e) {textBoxChanged(e, jTextArea.getText(), jList.getSelectedIndex());}
		@Override public void removeUpdate(DocumentEvent e) {textBoxChanged(e, jTextArea.getText(), jList.getSelectedIndex());}
		/** When the text box is edited, set its text to the String of the selected JList index */
		public void textBoxChanged(DocumentEvent e, String str, int index) {
			if (!doTextBoxChangedEvents)
				return;
			for (ListTextBoxListener l: listTextBoxListeners) //call ListTextBoxListeners
				l.textBoxChanged(index, str);
			setString(str, index);
		}
	}
	/** A FocusListener added to the JList. Transfer focus to the JTextArea every time the JList is clicked */
	protected class OnJListFocus implements FocusListener {
		@Override
		public void focusGained(FocusEvent e) {
			jTextArea.requestFocus();
		}
		@Override
		public void focusLost(FocusEvent e) {
			
		}
	}
	/** JPopupMenu that shows up on right clicking an item on the JList */
	protected JPopupMenu listContextMenu;
	/** MouseAdapter that makes a right click on the JList do exactly what left click does,
	 * and also shows listContextMenu on mouse release */
	protected class OnJListRightClick extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			if (SwingUtilities.isRightMouseButton(e))
				setSelectedIndex(jList.locationToIndex(e.getPoint()));
		}
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger() && jList.locationToIndex(e.getPoint()) != -1)
				if (listContextMenu != null)
					listContextMenu.show(jList, e.getX(), e.getY());
		}
		public void mouseDragged(MouseEvent e) {
			if (SwingUtilities.isRightMouseButton(e))
				setSelectedIndex(jList.locationToIndex(e.getPoint()));
		}
	}
	
	
	/** Set the String of the text box. (Called every time the JList index is changed) */
	protected void setTextBox(String text) {
		doTextBoxChangedEvents = false;
		jTextArea.setText(text);
		doTextBoxChangedEvents = true;
	}
	/** Set a String in the JList at the selected index. (Called every time the textbox is changed) */
	protected void setString(String str, int index) {
		strings_content[index] = str;
		strings_display.setElementAt(
				str.length() <= 0 ? " " // fixes JList empty String display glitch
						: escapeString(str),
				index);
	}
	/** Escape line breaks etc. in a String for the JList */
	protected String escapeString(String str) {
		StringBuilder sb = new StringBuilder();
		char ch;
		for (int i = 0; i < str.length(); i++) {
			switch (ch = str.charAt(i)) {
			case '\r': sb.append("\\r"); break;
			case '\n': sb.append("\\n"); break;
			case '\t': sb.append("\\t"); break;
			case '\\': sb.append("\\\\"); break;
			case '\"': sb.append("\\\""); break;
			default: sb.append(ch); break;
			}
		}
		return sb.toString();
	}
	
	//Population methods
	/** Populate the ListTextBox with an array of Strings */
	public void populate(String[] strings) {
		populate(new Iterator<String>() {
			int i = 0;
			@Override
			public boolean hasNext() {
				return i < strings.length;
			}
			@Override
			public String next() {
				return strings[i++];
			}
		}, strings.length);
	}
	/** Populate the ListTextBox with a Collection of Strings */
	public void populate(Collection<String> strings) {
		populate(strings.iterator(), strings.size());
	}
	/** Populate the ListTextBox with a String iterator, and the amount of String
	 * objects the Iterator holds: iteratorSize */
	protected void populate(Iterator<String> iterator, int iteratorSize) {
		clear();
		strings_content = new String[iteratorSize];
		int index;
		String str;
		while (iterator.hasNext()) {
			index = strings_display.getSize();
			str = iterator.next();
			strings_display.addElement(null);
			setString(str, index);
			for (ListTextBoxListener l: listTextBoxListeners) //Call listTextBoxListeners
				l.textBoxChanged(index, str);
		}
		if (iteratorSize > 0)
			setSelectedIndex(0);
	}
	/** Clears the ListTextBox */
	public void clear() {
		doJListSelectionEvents = false;
		doTextBoxChangedEvents = false;
		
		/////////////////////////////// This code prevents JList's freeze glitch
		strings_display.addElement("");
		strings_display.addElement("");
		jList.setSelectedIndex(1);
		///////////////////////////////
		
		jList.clearSelection();
		strings_display.clear();
		strings_content = null;
		jTextArea.setText("");
		jTextArea.setEnabled(false);
		for (ListTextBoxListener l: listTextBoxListeners) //call ListTextBoxListeners -1
			l.indexChanged(-1);
		ltbum.clear();
		
		doJListSelectionEvents = true;
		doTextBoxChangedEvents = true;
	}
	/** Returns the String array of the ListTextBox's Strings */
	public String[] getStrings() {
		return strings_content;
	}
	
	//ListTextBoxListener handler
	/** The LinkedHashSet collection of ListTextBoxListeners */
	protected Set<ListTextBoxListener> listTextBoxListeners = new LinkedHashSet<>();
	/** Register a listener to this ListTextBox */
	public void addListTextBoxListener(ListTextBoxListener l) {
		listTextBoxListeners.add(l);
	}
	/** Remove a listener from the registered ListTextBoxListeners */
	public void removeListTextBoxListener(ListTextBoxListener l) {
		listTextBoxListeners.remove(l);
	}
	/** Return the array of the registered ListTextBoxListeners */
	public ListTextBoxListener[] getListTextBoxListeners() {
		ListTextBoxListener[] output = new ListTextBoxListener[listTextBoxListeners.size()];
		int i = 0;
		for (ListTextBoxListener l: listTextBoxListeners)
			output[i++] = l;
		return output;
	}
	
	//Other public methods
	/** Get the selected index of the JList */
	public int getSelectedIndex() {
		return jList.getSelectedIndex();
	}
	/** Set the selected index of the JList */
	public void setSelectedIndex(int index) {
		jList.setSelectedIndex(index);
		jList.ensureIndexIsVisible(index);
	}
	/** Undo an edit made on the ListTextBox, using the UndoManager */
	public void undo() {
		ltbum.undo();
		for (ListTextBoxListener l: listTextBoxListeners) //call ListTextBoxListeners
			l.textBoxChanged(jList.getSelectedIndex(), jTextArea.getText());
	}
	/** Redo an edit made on the ListTextBox, using the UndoManager */
	public void redo() {
		ltbum.redo();
		for (ListTextBoxListener l: listTextBoxListeners) //call ListTextBoxListeners
			l.textBoxChanged(jList.getSelectedIndex(), jTextArea.getText());
	}
	
	//ListTextBox Undo Manager
	/** ListTextBoxUndoManager: a DocumentFilter that keeps tracks of all edits and provides undo and redo operations */
	protected class ListTextBoxUndoManager extends DocumentFilter {
		
		//Collection methods
		/** The ArrayList collection of ListTextBoxUndoManagerItems used to keep track of edits */
		protected final List<ListTextBoxUndoManagerItem> edits = new ArrayList<>();
		/** <b>index</b>: the index at which the ListTextBoxUndoManager is focused on the <b>edits</b> List.
		 * Used for redo to work, by keeping track of the index */
		protected int index = 0;
		/** Add a ListTextBoxUndoManagerItem to ListTextBoxUndoManager */
		public void addItem(ListTextBoxUndoManagerItem item) {
			//Remove all leading edits when a new item is added
			for (int i = edits.size() - 1; i >= index; i--)
				edits.remove(i);
			edits.add(item);
			index++;
		}
		/** Reset this UndoManager */
		public void clear() {
			edits.clear();
			index = 0;
		}
		
		//DocumentFilter methods, listen to every change to the text box, and add ListTextBoxUndoManagerItems
		@Override
		public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
			if (doTextBoxChangedEvents) {
				addItem(new ListTextBoxUndoManagerItem(
						ListTextBoxUndoManagerItem.TEXT_REPLACED,
						jList.getSelectedIndex(),
						offset,
						string,
						""
					));
			}
			super.insertString(fb, offset, string, attr);
		}
		@Override
		public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
			if (doTextBoxChangedEvents) {
				addItem(new ListTextBoxUndoManagerItem(
						ListTextBoxUndoManagerItem.TEXT_REPLACED,
						jList.getSelectedIndex(),
						offset,
						text,
						length == 0 ? "" : jTextArea.getText(offset, length)
					));
			}
			super.replace(fb, offset, length, text, attrs);
		}
		@Override
		public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
			if (doTextBoxChangedEvents) {
				addItem(new ListTextBoxUndoManagerItem(
						ListTextBoxUndoManagerItem.TEXT_REMOVED,
						jList.getSelectedIndex(),
						offset,
						null,
						length == 0 ? "" : jTextArea.getText(offset, length)
					));
			}
			super.remove(fb, offset, length);
		}
		
		//Undo methods
		/** Returns true if it is possible to undo (avoiding ArrayIndexOutOfBoundsException) */
		public boolean canUndo() {
			return index > 0;
		}
		/** Undo an edit, and update the undo to the text box */
		public void undo() {
			if (canUndo())
				edits.get(--index).undo();
		}
		/** Returns true if it is possible to redo (avoiding ArrayIndexOutOfBoundsException) */
		public boolean canRedo() {
			return index < edits.size();
		}
		/** Redo an undo, and update the redo to Document */
		public void redo() {
			if (canRedo())
				edits.get(index++).redo();
			
		}
	}
	/** Item class of the ListTextBoxUndoManager. Has the data of an edit on the text box */
	protected class ListTextBoxUndoManagerItem {
		/** The type of edit made to the text box, e.g. TEXT_REPLACED */
		public final int type;
		/** This edit type means that text was replaced/inserted */
		public static final int TEXT_REPLACED = 0;
		/** This edit type means that text was removed */
		public static final int TEXT_REMOVED = 1;
		
		/** The ListTextBox list index of the edited item */
		public final int index;
		/** The text position where the edit was made */
		public final int offset;
		/** The text substring that was inserted by the edit */
		public final String text;
		/** The text substring that was replaced/removed by the edit */
		public final String replacedText;
		/** Construct ListTextBoxUndoManagerItem defining all fields */
		public ListTextBoxUndoManagerItem(int type, int index, int offset, String text, String replacedText) {
			this.type = type;
			this.index = index;
			this.offset = offset;
			this.text = text;
			this.replacedText = replacedText;
		}
		
		/** Undo this edit to ListTextBox's text box */
		public void undo() {
			setSelectedIndex(index);
			doTextBoxChangedEvents = false;
			if (type == TEXT_REPLACED)
				jTextArea.replaceRange(replacedText, offset, offset + text.length());
			else
				jTextArea.insert(replacedText, offset);
			doTextBoxChangedEvents = true;
			setString(jTextArea.getText(), index);
		}
		/** Redo this edit to ListTextBox's text box */
		public void redo() {
			setSelectedIndex(index);
			doTextBoxChangedEvents = false;
			if (type == TEXT_REPLACED)
				jTextArea.replaceRange(text, offset, offset + replacedText.length());
			else
				jTextArea.replaceRange("", offset, offset + replacedText.length());
			doTextBoxChangedEvents = true;
			setString(jTextArea.getText(), index);
		}
	}
	
	
	//JList Color Renderer
	/** HashMap of Integers to Colors, used by ColorMapListCellRenderer to set the color of the font of JList items.
	 * Integer is the index of the JList item, and Color is the color to set the item of the index.<br>
	 * Ex: Set the index of 3 to Color.RED, and the 3rd item will be red in the JList */
	protected Map<Integer, Color> colorMap = new HashMap<>();
	/** Set the color of an index in the list, and that item will be colored to the value <b>color</b> */
	public void setColor(int index, Color color) {
		colorMap.put(index, color);
	}
	/** Remove a registered color in the list, and that item will no longer be colored */
	public void removeColor(int index) {
		colorMap.remove(index);
	}
	/** Return the registered colors in the list */
	public Map<Integer, Color> getColors() {
		return new HashMap<>(colorMap);
	}
	/** Set the colors of with an Integer-Color Map */
	public void setColors(Map<Integer, Color> colorMap) {
		this.colorMap = new HashMap<>(colorMap);
	}
	/** A DefaultListCellRenderer added to the list. Display the color changes on the JList, using <b>colorMap</b> */
	protected class ColorMapListCellRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 1L;
		@Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			component.setForeground(colorMap.getOrDefault(index, isSelected ? jListDefaultSelectionColor : jListDefaultColor));
			return component;
		}
	}
	/** Refreshes the font color of the text box according to <b>colorMap</b> */
	protected void refreshTextboxColor() {
		if (jTextAreaDefaultColor == null)
			return;
		jTextArea.setForeground(colorMap.getOrDefault(jList.getSelectedIndex(), jTextAreaDefaultColor));
	}
	
	//Other public methods
	/** Return the top component JList of the ListTextBox */
	public JList<String> getJList() { return jList; }
	/** Return the bottom component JTextArea of the ListTextBox */
	public JTextArea getJTextArea() { return jTextArea; }
	/** Return the length of the set String array */
	public int getLength() { return strings_content == null ? 0 : strings_content.length; }
	/** Return the escaped display strings of ListTextBox */
	public String[] getEscapedStrings() {
		String[] strings = new String[strings_display.getSize()];
		for (int i = 0; i < strings.length; i++)
			strings[i] = new String(strings_display.get(i));
		return strings;
	}
	/** Return a String of the ListTextBox at the passed index */
	public String getString(int index) {
		return strings_content[index];
	}
	/** Return the selected String in ListTextBox */
	public String getSelectedString() {
		int index = jList.getSelectedIndex();
		return index < 0 ? null : strings_content[index];
	}
	/** Set the JPopupMenu that shows up when an item is right-clicked on the list */
	public void setListContextMenu(JPopupMenu listContextMenu) {
		this.listContextMenu = listContextMenu;
	}
	/** Get the JPopupMenu that shows up when an item is right-clicked on the list */
	public JPopupMenu getListContextMenu() {
		return listContextMenu;
	}
}