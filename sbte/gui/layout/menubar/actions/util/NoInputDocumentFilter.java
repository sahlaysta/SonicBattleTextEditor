package sbte.gui.layout.menubar.actions.util;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public final class NoInputDocumentFilter extends DocumentFilter {
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
