package sahlaysta.sonicbattletexteditor.ui.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/** ChoicePromptWindow is a simple modal GUI that shows a list of options for
 * user selection. The selected option is gettable with <code>getSelectedChoice()</code> */
public class ChoicePromptWindow<T> extends JDialog {
	private static final long serialVersionUID = 1L;

	//Constructor
	/** Construct ChoicePromptWindow with no modality owner */
	public ChoicePromptWindow() {
		this(null);
	}
	/** Construct ChoicePromptWindow with the passed Component as its modality owner */
	public ChoicePromptWindow(Component component) {
		super((Frame) SwingUtilities.getAncestorOfClass(Frame.class, component), true);
		initializeChoicePromptWindow();
		setLocationRelativeTo(component);
	}
	
	//Choice
	/** Choice: a choice in ChoicePromptWindow, holding its <code>String</code> label and its <code>T</code> object */
	protected class Choice {
		/** The String label of the choice in ChoicePromptWindow that will be shown to the JList as a user selectable choice */
		public final String label;
		/** The object of the Choice that will be returned by <code>getSelectedChoice()</code> if this choice is selected by the user */
		public final T obj;
		/** Construct a Choice with its <code>String</code> label and its <code>T</code> object */
		public Choice(String label, T obj) {
			this.label = label;
			this.obj = obj;
		}
		@Override
		public String toString() {
			return label;
		}
	}
	
	//Components
	/** The JList where the user the user chooses a selection */
	protected JList<Choice> jList;
	/** The list model of the JList */
	protected DefaultListModel<Choice> dlm;
	/** The List collection of choices added to ChoicePromptWindow */
	protected List<Choice> choices;
	/** The default foreground color of the JList, used to reset the color */
	protected Color jListDefaultColor;
	/** The default selection color of the JList, used to reset the color */
	protected Color jListDefaultSelectionColor;
	/** The search bar text field */
	protected JTextField jTextField;
	/** If false, the JTextField DocumentListener will not fire */
	protected boolean jTextFieldListener = true;
	/** The button that confirms a selection */
	protected JButton confirmButton;
	/** The button that cancels ChoicePromptWindow */
	protected JButton cancelButton;
	/** Initialize the components and layout of ChoicePromptWindow */
	protected void initializeChoicePromptWindow() {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setSize(200, 300);
		
		//Initialize components
		choices = new ArrayList<>();
		dlm = new DefaultListModel<>();
		jList = new JList<>(dlm);
		jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		for (KeyListener l: jList.getKeyListeners())
			jList.removeKeyListener(l);
		jListDefaultColor = jList.getForeground();
		jListDefaultSelectionColor = jList.getSelectionForeground();
		jList.setCellRenderer(new ColorMapListCellRenderer());
		confirmButton = new JButton("OK");
		confirmButton.setToolTipText("Confirm the selected option");
		confirmButton.setEnabled(false);
		cancelButton = new JButton("Cancel");
		cancelButton.setToolTipText("Abort this window");
		jTextField = new JTextField() {
			private static final long serialVersionUID = 1L;
			@Override
			protected void paintComponent(Graphics pG) { //JTextField placeholder text ("Search...")
				super.paintComponent(pG);
				if (getText().length() > 0)
					return;
				Graphics2D g = (Graphics2D)pG;
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setColor(getDisabledTextColor());
				g.drawString(
						searchPlaceHolder == null ? "" : searchPlaceHolder,
						getInsets().left,
						pG.getFontMetrics().getMaxAscent() + getInsets().top
					);
				getCaret().paint(g);
			}
		};
		
		//Set layout
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		JPanel listPanel = new JPanel();
		listPanel.setLayout(new GridLayout(1,1));
		listPanel.setBorder(new EmptyBorder(5,5,0,5));
		listPanel.add(new JScrollPane(jList));
		mainPanel.add(listPanel, BorderLayout.CENTER);
		JPanel textFieldPanel = new JPanel();
		textFieldPanel.setBorder(new EmptyBorder(5,5,0,5));
		textFieldPanel.setLayout(new GridLayout(1,1));
		textFieldPanel.add(jTextField);
		mainPanel.add(textFieldPanel, BorderLayout.PAGE_START);
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(confirmButton);
		buttonPanel.add(cancelButton);
		mainPanel.add(buttonPanel, BorderLayout.PAGE_END);
		setLayout(new GridLayout(1,1));
		add(mainPanel);
		
		//Action listeners
		confirmButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				confirmSelection();
			}
		});
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		jList.addKeyListener(new KeyListener() { //Confirm selection on Enter key press
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case 10: //Confirm selection on Enter key press
					confirmSelection();
					break;
				case 27: //Close ChoicePromptWindow on escape key press
					dispose();
					break;
				}
			}
			@Override public void keyReleased(KeyEvent e) {}
			@Override public void keyTyped(KeyEvent e) {}
		});
		jList.addMouseListener(new MouseAdapter() { //Confirm selection on double mouse click
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getClickCount() == 2)
					confirmSelection();
			}
		});
		jList.addListSelectionListener(new ListSelectionListener() { //Disable the confirm button if there is no selection
			@Override
			public void valueChanged(ListSelectionEvent e) {
				confirmButton.setEnabled(jList.getSelectedIndex() > -1);
			}
		});
		jTextField.getDocument().addDocumentListener(new DocumentListener() { //Update the search when the text box is changed
			@Override public void changedUpdate(DocumentEvent e) { textFieldChanged(); }
			@Override public void insertUpdate(DocumentEvent e) { textFieldChanged(); }
			@Override public void removeUpdate(DocumentEvent e) { textFieldChanged(); }
			private final void textFieldChanged() {
				if (jTextFieldListener) {
					searchQuery = jTextField.getText();
					refreshChoices();
				}
			}
		});
		jTextField.addKeyListener(new KeyListener() { //JTextField key press actions
			@Override
			public void keyPressed(KeyEvent e) {
				int index = jList.getSelectedIndex();
				switch (e.getKeyCode()) {
				case 38: //Go up in JList on up arrow key
					if (index > -1) {
						jList.setSelectedIndex(index - 1);
						jList.ensureIndexIsVisible(index - 1);
					}
					break;
				case 40: //Go down in JList on down arrow key
					jList.setSelectedIndex(index + 1);
					jList.ensureIndexIsVisible(index + 1);
					break;
				case 10: //Confirm selection on Enter key press
					confirmSelection();
					break;
				case 27: //Close ChoicePromptWindow on escape key press
					dispose();
					break;
				}
			}
			@Override public void keyReleased(KeyEvent e) {}
			@Override public void keyTyped(KeyEvent e) {}
		});
	}
	
	//Selection state
	/** True if the user has selected an option in ChoicePromptWindow <b>and</b> clicked the confirm button / pressed the Enter key */
	protected boolean hasSelected = false;
	/** True if the user has selected an option in ChoicePromptWindow <b>and</b> clicked the confirm button / pressed the Enter key */
	public boolean hasSelected() {
		return hasSelected;
	}
	/** Confirm the selection of ChoicePromptWindow */
	protected void confirmSelection() {
		if (!confirmButton.isEnabled())
			return;
		hasSelected = true;
		dispose();
	}
	
	//Search
	/** The search query to filter searches. It is set every time the text field is edited */
	protected String searchQuery;
	/** Repopulate the JList with the <code>choices</code> List */
	protected void refreshChoices() {
		dlm.clear();
		for (Choice c: choices)
			if (_searchMatches(c.label))
				dlm.addElement(c);
	}
	private final boolean _searchMatches(String str) {
		if (searchQuery == null || searchQuery.isEmpty())
			return true;
		if (str == null)
			return false;
		return searchMatches(str, searchQuery);
	}
	/** True if the String <b>str</b> matches the String <b>searchQuery</b> */
	protected boolean searchMatches(String str, String searchQuery) {
		return str.toLowerCase().contains(searchQuery.toLowerCase());
	}
	
	//Public methods
	/** Add a user selectable choice to ChoicePromptWindow */
	protected void addChoice(Choice choice) {
		choices.add(choice);
		if (_searchMatches(choice.label))
			dlm.addElement(choice);
	}
	/** Add a user selectable choice to ChoicePromptWindow with its list label */
	public void addChoice(T choice, String label) {
		addChoice(new Choice(label, choice));
	}
	/** Add a user selectable choice to ChoicePromptWindow with a default label */
	public void addChoice(T choice) {
		addChoice(choice, choice.toString());
	}
	/** Remove an added user selectable choice from ChoicePromptWindow */
	public void removeChoice(T choice) {
		for (Choice c: choices)
			if (c.obj == choice) {
				choices.remove(c);
				if (dlm.contains(c))
					dlm.removeElement(c);
			}
	}
	/** Return the added choices to ChoicePromptWindow */
	public List<T> getChoices() {
		List<T> list = new ArrayList<>(choices.size());
		for (Choice c: choices)
			list.add(c.obj);
		return list;
	}
	/** Return the amount of added choices to ChoicePromptWindow */
	public int getChoicesSize() {
		return choices.size();
	}
	/** Remove all added user selectable choice from ChoicePromptWindow */
	public void clearChoices() {
		choices.clear();
		dlm.clear();
	}
	/** Returns true if the added choices contain the choice */
	public boolean choicesContains(T choice) {
		for (Choice c: choices)
			if (c.obj == choice)
				return true;
		return false;
	}
	/** Get the currently selected choice in ChoicePromptWindow. Returns null if nothing is selected */
	public T getSelectedChoice() {
		int index = jList.getSelectedIndex();
		return index > -1 ? dlm.get(index).obj : null;
	}
	/** Set the currently selected choice in ChoicePromptWindow */
	public void setSelectedChoice(T choice) {
		for (int i = 0; i < dlm.getSize(); i++)
			if (dlm.get(i).obj == choice) {
				jList.setSelectedIndex(i);
				break;
			}
	}
	/** Clear the currently selected choice in ChoicePromptWindow */
	public void clearSelectedChoice() {
		jList.clearSelection();
	}
	/** Get the collection index of the selected choice in ChoicePromptWindow */
	public int getSelectedIndex() {
		T t = getSelectedChoice();
		for (int i = 0; i < choices.size(); i++)
			if (choices.get(i).obj == t)
				return i;
		return -1;
	}
	
	//Component methods
	/** Set the search of ChoicePromptWindow */
	public void setSearchQuery(String searchQuery) {
		this.searchQuery = searchQuery;
		jTextFieldListener = false;
		jTextField.setText(searchQuery);
		jTextField.selectAll();
		jTextFieldListener = true;
	}
	/** Get the search query of ChoicePromptWindow */
	public String getSearchQuery() {
		return searchQuery;
	}
	/** Enable or disable the search box of ChoicePromptWindow */
	public void setSearchVisible(boolean visible) {
		jTextField.getParent().setVisible(visible);
	}
	/** Show or do not show the cancel button in ChoicePromptWindow */
	public void setCancelButtonVisible(boolean visible) {
		cancelButton.setVisible(visible);
	}
	/** Set the text of the confirm button of ChoicePromptWindow */
	public void setConfirmButtonText(String text) {
		confirmButton.setText(text);
	}
	/** Get the text of the confirm button of ChoicePromptWindow */
	public String getConfirmButtonText() {
		return confirmButton.getText();
	}
	/** Set the tooltip text of the confirm button in ChoicePromptWindow */
	public void setConfirmButtonToolTipText(String text) {
		confirmButton.setToolTipText(text);
	}
	/** Get the tooltip text of the confirm button in ChoicePromptWindow */
	public String getConfirmButtonToolTipText() {
		return confirmButton.getToolTipText();
	}
	/** Set the text of the cancel button of ChoicePromptWindow */
	public void setCancelButtonText(String text) {
		cancelButton.setText(text);
	}
	/** Get the text of the cancel button of ChoicePromptWindow */
	public String getCancelButtonText() {
		return cancelButton.getText();
	}
	/** Set the tooltip text of the cancel button in ChoicePromptWindow */
	public void setCancelButtonToolTipText(String text) {
		cancelButton.setToolTipText(text);
	}
	/** Get the tooltip text of the cancel button in ChoicePromptWindow */
	public String getCancelButtonToolTipText() {
		return cancelButton.getToolTipText();
	}
	/** The placeholder text of the search field shown when no text is entered into it */
	protected String searchPlaceHolder = "Search...";
	/** Set the placeholder text of the search field shown when no text is entered into it */
	public void setSearchPlaceholder(String text) {
		searchPlaceHolder = text;
		jTextField.repaint();
	}
	/** Get the placeholder text of the search field shown when no text is entered into it */
	public String getSearchPlaceholder() {
		return searchPlaceHolder;
	}
	
	//JList Color Renderer
	/** HashMap of T to Colors, used by ColorMapListCellRenderer to set the color of the font of JList items.
	 * Integer is the index of the JList item, and Color is the color to set the item of the index.<br>
	 * Ex: Set the index of 3 to Color.RED, and the 3rd item will be red in the JList */
	protected Map<T, Color> colorMap = new HashMap<>();
	/** Set the color of a choice in the list, and that item will be colored to the value <b>color</b> */
	public void setColor(T choice, Color color) {
		colorMap.put(choice, color);
	}
	/** Remove a registered color in the list, and that item will no longer be colored */
	public void removeColor(T choice) {
		colorMap.remove(choice);
	}
	/** Return the registered colors in the list */
	public Map<T, Color> getColors() {
		return new HashMap<>(colorMap);
	}
	/** Set the colors of the list with a Map object */
	public void setColors(Map<T, Color> colorMap) {
		this.colorMap = new HashMap<>(colorMap);
	}
	/** A DefaultListCellRenderer added to the list. Handle the color changes of the JList, using the HashMap <b>colorMap</b> */
	protected class ColorMapListCellRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 1L;
		@Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			component.setForeground(colorMap.getOrDefault(dlm.get(index).obj, isSelected ? jListDefaultSelectionColor : jListDefaultColor));
			return component;
		}
	}
}