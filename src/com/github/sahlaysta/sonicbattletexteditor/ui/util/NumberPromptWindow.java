package com.github.sahlaysta.sonicbattletexteditor.ui.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/** NumberPromptWindow is a small GUI with a text field that gets a number
 * by user entry. The entered number gettable with <code>getNumber()</code> */
public class NumberPromptWindow extends JDialog {
	private static final long serialVersionUID = 1L;
	
	/** Construct NumberPromptWindow with no modality owner */
	public NumberPromptWindow() {
		this(null);
	}
	
	/** Construct NumberPromptWindow with the passed Component as its modality owner */
	public NumberPromptWindow(Component component) {
		super((Frame) SwingUtilities.getAncestorOfClass(Frame.class, component), true);
		initializeNumberPromptWindow();
		setLocationRelativeTo(component);
	}
	
	//NumberPromptWindow fields
	/** The number entered into the text field. It is set every time the text field is changed */
	protected int number = -1;
	/** Return the currently entered number of NumberPromptWindow */
	public int getNumber() {
		return number;
	}
	/** The minimum value number that NumberPromptWindow will accept */
	protected int minimumValue = Integer.MIN_VALUE;
	/** The maximum value number that NumberPromptWindow will accept */
	protected int maximumValue = Integer.MAX_VALUE;
	/** Set the minimum value that NumberPromptWindow will accept */
	public void setMinimumValue(int minimumValue) {
		this.minimumValue = minimumValue;
	}
	/** Set the maximum value that NumberPromptWindow will accept */
	public void setMaximumValue(int maximumValue) {
		this.maximumValue = maximumValue;
	}
	/** Return the minimum value number that NumberPromptWindow will accept */
	public int getMinimumValue() {
		return minimumValue;
	}
	/** Return the maximum value number that NumberPromptWindow will accept */
	public int getMaximumValue() {
		return maximumValue;
	}
	
	//Components
	/** The JTextField component where the user enters a number into NumberPromptWindow */
	protected JTextField jTextField;
	/** The JButton that the user presses to confirm the entered number */
	protected JButton jButton;
	/** The JLabel component that sits at the left of the NumberPromptWindow */
	protected JLabel jLabel;
	
	/** The default foreground color of jTextField, used to set back the jTextField's color after it has been set to red */
	protected Color jTextFieldDefaultColor;

	/** Initialize the components and layout of NumberPromptWindow */
	protected void initializeNumberPromptWindow() {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);
		setSize(160, 90);
		
		//Initialize components
		jTextField = new JTextField();
		jTextFieldDefaultColor = jTextField.getForeground();
		jButton = new JButton("OK");
		jButton.setToolTipText("Confirm the entered number");
		jLabel = new JLabel("Enter number:");
		
		//Initialize layout
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(2,2,5,5));
		mainPanel.setBorder(new EmptyBorder(5,5,5,5));
		mainPanel.add(jLabel);
		mainPanel.add(jTextField);
		mainPanel.add(new JPanel());
		mainPanel.add(jButton);
		setLayout(new GridLayout(1,1));
		add(mainPanel);
		
		//Setup jTextField
		/* DocumentFilter that only allows numbers to be entered to jTextField */
		((AbstractDocument)jTextField.getDocument()).setDocumentFilter(new DocumentFilter() {
			@Override
			public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
				if ((offset != 0 || jTextField.getText().indexOf('-') == -1) && ((string.length() > 0 && string.charAt(0) == '-' && offset == 0) || numCheck(string)))
					super.insertString(fb, offset, string, attr);
			}
			@Override
			public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
				if ((offset != 0 || jTextField.getText().indexOf('-') == -1) && ((text.length() > 0 && text.charAt(0) == '-' && offset == 0) || numCheck(text)))
					super.replace(fb, offset, length, text, attrs);
			}
			private final boolean numCheck(CharSequence cs) {
				for (int i = 0; i < cs.length(); i++) {
					switch (cs.charAt(i)) {
					case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9': continue;
					default: return false;
					}
				}
				return true;
			}
		});
		/* DocumentListener that disables the confirm button if the entered number does not coincide minValue / maxValue,
		 * and sets the font color of jTextField to red */
		DocumentListener documentListener = new DocumentListener() {
			@Override public void changedUpdate(DocumentEvent arg0) { numCheck(); }
			@Override public void insertUpdate(DocumentEvent arg0) { numCheck(); }
			@Override public void removeUpdate(DocumentEvent arg0) { numCheck(); }
			private final void numCheck() {
				String text = jTextField.getText();
				if (text.equals("-")) {
					setThingsEnabled(true);
					jButton.setEnabled(false);
				}
				else {
					try {
						number = Integer.parseInt(text);
						setThingsEnabled(number >= minimumValue && number <= maximumValue);
					} catch (NumberFormatException e) {
						setThingsEnabled(false);
					}
				}
			}
			private final void setThingsEnabled(boolean enabled) {
				if (enabled) {
					jButton.setEnabled(true);
					jTextField.setForeground(jTextFieldDefaultColor);
				} else {
					jButton.setEnabled(false);
					jTextField.setForeground(Color.RED);
				}
			}
		};
		documentListener.changedUpdate(null);
		jTextField.getDocument().addDocumentListener(documentListener);
		jTextField.addKeyListener(new KeyListener() { //Close the NumberPromptWindow on esc key press
			@Override
			public void keyPressed(KeyEvent arg0) {
				if (arg0.getKeyCode() == 27) //27 is ESC
					NumberPromptWindow.this.dispose();
			}
			@Override public void keyReleased(KeyEvent arg0) {}
			@Override public void keyTyped(KeyEvent arg0) {}
		});
		addComponentListener(new ComponentListener() { //Select all text in the text field once NumberPromptWindow is shown
			@Override public void componentHidden(ComponentEvent e) {}
			@Override public void componentMoved(ComponentEvent e) {}
			@Override public void componentResized(ComponentEvent e) {}
			@Override
			public void componentShown(ComponentEvent e) {
				jTextField.selectAll();
				removeComponentListener(this);
			}
		});
		
		// jButton and jTextField ActionListener
		ActionListener actionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (jButton.isEnabled())
					confirmNumber();
			}
		};
		jButton.addActionListener(actionListener);
		jTextField.addActionListener(actionListener);
	}
	
	//Selection state
	/** True if the user entered a number in NumberPromptWindow <b>and</b> clicked the confirm button / pressed Enter */
	protected boolean hasSelected = false;
	/** True if the user entered a number in NumberPromptWindow <b>and</b> clicked the confirm button / pressed Enter */
	public boolean hasSelected() {
		return hasSelected;
	}
	protected void confirmNumber() {
		hasSelected = true;
		dispose();
	}
	
	//Public methods
	/** Set the text on the left of the NumberPromptWindow */
	public void setLabelText(String text) {
		jLabel.setText(text);
	}
	/** Set the text of the confirm button of the NumberPromptWindow */
	public void setButtonText(String text) {
		jButton.setText(text);
	}
	/** Get the text of the left label of the NumberPromptWindow */
	public String getLabelText() {
		return jLabel.getText();
	}
	/** Get the text of the confirm button of the NumberPromptWindow */
	public String getButtonText() {
		return jButton.getText();
	}
	/** Set the number of the text field text */
	public void setNumber(int number) {
		jTextField.setText(Integer.toString(number));
		jTextField.selectAll();
	}
	/** Set the tooltip text of the confirm button of the NumberPromptWindow */
	public void setButtonToolTipText(String text) {
		jButton.setToolTipText(text);
	}
	/** Get the tooltip text of the confirm button of the NumberPromptWindow */
	public String getButtonToolTipText() {
		return jButton.getToolTipText();
	}
}