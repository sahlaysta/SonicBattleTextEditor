package sbte.gui.layout.menubar.actions.windowmenus;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import sbte.gui.GUI;
import sbte.gui.layout.menubar.actions.util.UndoTF;

public final class GUIGoTo {
	public static void goToGUI(GUI caller) {
		new GoToGUI(caller).setVisible(true);
	}
	public static String history = ""; //save entry
	public static class GoToGUI extends JDialog {
		private static final long serialVersionUID = -836969490378692183L;

		public GoToGUI(GUI parent) {
			super(parent);
			setModal(true);
			setResizable(false);
			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			setTitle(parent.localization.get("goTo"));
			JPanel p = new JPanel();
			p.setBorder(new EmptyBorder(5, 5, 5, 5));
			
			{
				p.add(new JLabel(parent.localization.get("line")));
				UndoTF tf = new UndoTF(parent);
				tf.setText(history);
				p.add(tf);
				class CustomDocumentFilter extends DocumentFilter {

			        private Pattern regexCheck = Pattern.compile("[0-9]+");

			        @Override
			        public void insertString(FilterBypass fb, int offs, String str, AttributeSet a) throws BadLocationException {
			            if (str == null) {
			                return;
			            }

			            if (regexCheck.matcher(str).matches()) {
			            	if (!valid(str)) return;
			                super.insertString(fb, offs, str, a);
			            }
			        }

			        @Override
			        public void replace(FilterBypass fb, int offset, int length, String str, AttributeSet attrs)
			                throws BadLocationException {
			            if (str == null) {
			                return;
			            }

			            if (regexCheck.matcher(str).matches()) {
			            	if (!valid(str)) return;
			                fb.replace(offset, length, str, attrs);
			            }
			        }
			        
			        boolean valid(String str) {
			        	if ((tf.getText().length() > ("" + parent.listModel.getSize()).length())) return false;
			        	return true;
			        }
			    }
				((AbstractDocument) tf.getDocument()).setDocumentFilter(new CustomDocumentFilter());
				tf.getDocument().addDocumentListener(new DocumentListener() {
					@Override
					public void changedUpdate(DocumentEvent arg0) {
						changed(arg0);
					}
					@Override
					public void insertUpdate(DocumentEvent arg0) {
						changed(arg0);
					}

					@Override
					public void removeUpdate(DocumentEvent arg0) {
						changed(arg0);
					}
					
					void changed(DocumentEvent arg0) {
						history = tf.getText();
					}
				});
				
				p.add(new JLabel(""));
				JButton go = new JButton(parent.localization.get("go"));
				go.addActionListener(new ActionListener(){ public void actionPerformed(ActionEvent e){
					if (tf.getText().length() <= 0) return;
					try {
						int sel = -1 + Integer.parseInt(tf.getText());
						try { parent.listModel.get(sel); } catch (Exception ee) {
							JOptionPane.showMessageDialog(GoToGUI.this, ee.toString(), parent.localization.get("error"), JOptionPane.ERROR_MESSAGE);
							return;
						}
						parent.list.ensureIndexIsVisible(sel);
						parent.list.setSelection(sel);
					} catch (NumberFormatException ee) {
						JOptionPane.showMessageDialog(GoToGUI.this, parent.localization.get("invNum") + ": " + tf.getText(), parent.localization.get("error"), JOptionPane.ERROR_MESSAGE);
						return;
					}
					GoToGUI.this.dispose();
				}});  
				p.add(go);
				
				// on enter key press
				Action action = new AbstractAction()
				{
					private static final long serialVersionUID = 8207604209813199163L;

					@Override
				    public void actionPerformed(ActionEvent e)
				    {
				        go.doClick();
				    }
				};
				tf.addActionListener( action );
			}
			
			p.setLayout(new GridLayout(2, 2, 5, 5));
			add(p);
			pack();
			setSize(getSize().width, getSize().height - 10);
			
			setLocationRelativeTo(parent);
		}
	}
}
