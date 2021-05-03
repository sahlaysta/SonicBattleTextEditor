package sbte.GUI;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class GUIGoTo {
	public static void goToGUI(GUI caller) {
		new GoToGUI(caller).setVisible(true);
	}
	public static class GoToGUI extends JDialog {
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
				JTextField tf = new JTextField();
				p.add(tf);
				
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