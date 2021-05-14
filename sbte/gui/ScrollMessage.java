package sbte.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.DocumentFilter.FilterBypass;

public class ScrollMessage {
	public static void show(GUI caller, String title, String message) {
		new ScrollMsg(caller, title, message).setVisible(true);
	}
	public static class ScrollMsg extends JDialog {
		public ScrollMsg(GUI parent, String title, String message) {
			super(parent);
			setTitle(title);
			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			setModal(true);
			setSize(200, 200);
			
			JPanel p = new JPanel();
			JTextArea tf = new JTextArea(message);
			((AbstractDocument) tf.getDocument()).setDocumentFilter(new CustomDocumentFilter());
			p.add(new JScrollPane(tf));
			p.setLayout(new GridLayout(1,1,5,5));
			p.setBorder(new EmptyBorder(5,5,5,5));
			add(p, BorderLayout.CENTER);
			
			JPanel buttonp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			JButton b = new JButton(parent.localization.get("ok"));
			b.addActionListener(new ActionListener(){ public void actionPerformed(ActionEvent e){ dispose(); }});  
			buttonp.add(b, BorderLayout.SOUTH);
			add(buttonp, BorderLayout.SOUTH);
			
			setLocationRelativeTo(parent);
			tf.setCaretPosition(0);
		}
		private static class CustomDocumentFilter extends DocumentFilter {

	        private Pattern regexCheck = Pattern.compile("[0-9]+");

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
	}
}
