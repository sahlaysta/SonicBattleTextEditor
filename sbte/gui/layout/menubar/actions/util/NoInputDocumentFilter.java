package sbte.gui.layout.menubar.actions.util;

import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

public final class NoInputDocumentFilter extends DocumentFilter {
	/*
	 * Document filter for textfields
	 * to ignore all inputs. Become uneditable
	 */
	public static void set(JTextField arg0) {
		set(arg0.getDocument());
	}
	public static void set(JTextArea arg0) {
		set(arg0.getDocument());
	}
	public static void set(Document arg0) {
		((AbstractDocument)arg0).setDocumentFilter(new NoInputDocumentFilter());;
	}
	
	
    @Override
    public void insertString(FilterBypass fb, int offs, String str, AttributeSet a) throws BadLocationException {
        return;
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String str, AttributeSet a) throws BadLocationException {
       return;
    }
    
    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
       return;
    }
}
