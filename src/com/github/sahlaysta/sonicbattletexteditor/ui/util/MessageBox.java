package com.github.sahlaysta.sonicbattletexteditor.ui.util;

import java.awt.Component;
import java.awt.Frame;

import javax.swing.JOptionPane;

/** Utility class for JFrame display of message boxes */
public class MessageBox {
	
	//Constructor
	/** The parent component that message boxes are shown over using the <code>show</code> methods */
	protected Component parentComponent;
	/** Construct MessageBox passing a parent Component for non-static methods */
	public MessageBox(Frame parentComponent) {
		this.parentComponent = parentComponent;
	}
	/** Construct MessageBox with a null parent Component */
	public MessageBox() {
		this(null);
	}
	
	//Parent component
	/** Set the parent component that message boxes are shown over using the <code>show</code> methods */
	public void setParentComponent(Component parentComponent) {
		this.parentComponent = parentComponent;
	}
	/** Return the parent component that message boxes are shown over using the <code>show</code> methods */
	public Component getParentComponent() {
		return parentComponent;
	}
	
	//Non-static methods
	/** Show a MessageBox with the message and title and the OK button text */
	public void show(String message, String title, String okButtonText) {
		show(parentComponent, message, title, okButtonText);
	}
	/** Show an error MessageBox with the message and title and the OK button text */
	public void showError(String message, String title, String okButtonText) {
		showError(parentComponent, message, title, okButtonText);
	}
	/** Show a Yes-No prompt with the message and title and the Yes and No buttons' text.
	 * Returns true if the 'Yes' option is clicked */
	public boolean showYesNoPrompt(String message, String title, String yes, String no) {
		return showYesNoPrompt(parentComponent, message, title, yes, no);
	}
	
	//Static methods
	/** Show a MessageBox over the passed parent Component, with the message and title and buttons */
	public static final int show(Component parentComponent, String message, String title, String... buttons) {
		return show(parentComponent,message,title,JOptionPane.INFORMATION_MESSAGE,buttons);
	}
	/** Show an error MessageBox over the passed parent Component, with the message and title and the OK button text */
	public static final int showError(Component parentComponent, String message, String title, String okButtonText) {
		return show(parentComponent,message,title,JOptionPane.ERROR_MESSAGE,okButtonText);
	}
	/** Show a Yes-No prompt MessageBox over the passed parent Component, with the message and title and the Yes and No buttons' text.
	 * Returns true if the 'Yes' option is clicked */
	public boolean showYesNoPrompt(Component parentComponent, String message, String title, String yes, String no) {
		return show(parentComponent,message,title,JOptionPane.QUESTION_MESSAGE,yes,no) == 0;
	}
	/** Show a MessageBox over the passed parent Component, with the message and title and message type and button */
	public static final int show(Component parentComponent, String message, String title, int messageType, String... buttons) {
		return JOptionPane.showOptionDialog(parentComponent,message,title,0,messageType,null,buttons,null);
	}
}