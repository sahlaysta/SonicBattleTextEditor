package sbte.gui.layout.menubar.actions.windowmenus;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import sbte.gui.GUI;
import sbte.gui.layout.menubar.actions.utilities.UndoTF;

public class GUISearch {
	public static void searchGUI(GUI caller) {
		new SearchGUI(caller, 0).setVisible(true);
	}
	public static void problematicGUI(GUI caller) {
		new SearchGUI(caller, 1).setVisible(true);
	}
	public static class SearchGUI extends JDialog {
		public SearchGUI(GUI parent, int args) {
			super(parent);
			
			class HitsLabel extends JLabel {
				private final String hits = parent.localization.get("hits");
				private final String oneHit = parent.localization.get("oneHit");
				public void setFound(int i) {
					if (i == 1) setText(oneHit.replace("[v]", i + ""));
					else setText(hits.replace("[v]", i + ""));
				}
			}
			
			class LineExt {
				public final String string;
				public final boolean red;
				public LineExt(String string, boolean red) {
					this.string = string;
					this.red = red;
				}
				public String toString() {
					return string;
				}
			}
			
			boolean problematic = (args == 1);
			setModal(true);
			setMinimumSize(new Dimension(150, 180));
			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			if (problematic) setTitle(parent.localization.get("prob"));
			else setTitle(parent.localization.get("search"));
			setLayout(new BorderLayout(5, 5));
			
			JPanel tfp = new JPanel();
			UndoTF tf = new UndoTF(parent);
			tfp.setBorder(new EmptyBorder(5,5,5,5));
			tfp.setLayout(new GridLayout(1,1));
			tfp.add(tf);
			add(tfp, BorderLayout.PAGE_START);
			
			JPanel lp = new JPanel();
			DefaultListModel m = new DefaultListModel<LineExt>();
			JList list = new JList(m);
			List<Integer> index = new ArrayList<>();
			for (int i = 0; i < parent.listModel.getSize(); i++) {
				if (problematic && !parent.listModel.isProblematic(i)) continue;
				m.addElement(new LineExt(parent.listModel.get(i).toString(), parent.listModel.isProblematic(i)));
				index.add(i);
			}
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			JScrollPane sp = new JScrollPane(list);
			//sp.setPreferredSize(new Dimension(200, 150));
			lp.setBorder(new EmptyBorder(5,5,5,5));
			lp.setLayout(new GridLayout(1,1));
			lp.add(sp);
			//list coloring
			DefaultListCellRenderer colorCell = new DefaultListCellRenderer() {
				//background color of list items
                @Override
                public Component getListCellRendererComponent(JList jlist, Object value, int jindex,
                          boolean isSelected, boolean cellHasFocus) {
                     Component c = super.getListCellRendererComponent(jlist, value, jindex, isSelected, cellHasFocus);
                     if (((LineExt)value).red)
                    	 setForeground(Color.RED);
                     return c;
                }

            };
			list.setCellRenderer(colorCell);
			//remove key click to change position
			KeyListener[] lsnrs = list.getKeyListeners();
			for (int i = 0; i < lsnrs.length; i++)
				list.removeKeyListener(lsnrs[i]);
			
			add(lp, BorderLayout.CENTER);
			

			JPanel bottom = new JPanel();
			bottom.setLayout(new GridLayout(1,2));
			
			JPanel buttonpanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			JButton b = new JButton(parent.localization.get("go"));
			buttonpanel.add(b);
			
			JPanel labelpanel = new JPanel(new BorderLayout());
			labelpanel.setBorder(new EmptyBorder(3,5,5,5));
			HitsLabel l = new HitsLabel();
			l.setFound(m.size());
			labelpanel.add(l, BorderLayout.PAGE_END);

			bottom.add(labelpanel);
			bottom.add(buttonpanel);
			add(bottom, BorderLayout.SOUTH);
			
			//update list on search
			tf.getDocument().addDocumentListener(new DocumentListener() {
				  public void changedUpdate(DocumentEvent e) {
				  	  changed(e);
				  }
				  public void removeUpdate(DocumentEvent e) {
					  changed(e);
				  }
				  public void insertUpdate(DocumentEvent e) {
					  changed(e);
				  }
				  public void changed(DocumentEvent e) {
					  if (tf.getText().length()==0) {
						  m.clear();
						  index.clear();
						  for (int i = 0; i < parent.listModel.getSize(); i++) {
							  if (problematic && !parent.listModel.isProblematic(i)) continue;
							  m.addElement(new LineExt(parent.listModel.get(i).toString(), parent.listModel.isProblematic(i)));
							  index.add(i);
						  }
						  l.setFound(m.size());
						  return;
					  }
					  
					  m.clear();
					  index.clear();
					  for (int i = 0; i < parent.listModel.getSize(); i++) {
						  if (problematic && !parent.listModel.isProblematic(i)) continue;
						  if (containsAll(parent.listModel.get(i).toString().toLowerCase(), tf.getText().toLowerCase().split(" "))) {
							  m.addElement(new LineExt(parent.listModel.get(i).toString(), parent.listModel.isProblematic(i)));
							  index.add(i);
						  }
					  }
					  l.setFound(m.getSize());
				  }
			});
			
			//go to action
			b.addActionListener(new ActionListener(){ public void actionPerformed(ActionEvent e){
				if (list.getSelectedIndex() < 0) return;
				parent.list.ensureIndexIsVisible(index.get(list.getSelectedIndex()));
				parent.list.setSelection(index.get(list.getSelectedIndex()));
				dispose();
			}});
			
			//double click option action
			list.addMouseListener(new MouseAdapter() {
			    public void mouseClicked(MouseEvent evt) {
			        if (evt.getClickCount() == 2) {
			            b.doClick();
			        }
			    }
			});
			
			//on enter key
			list.addKeyListener(new KeyAdapter(){
				public void keyPressed(KeyEvent e){
				    if (e.getKeyCode() == KeyEvent.VK_ENTER){
				   b.doClick();
				}
				}
				});
			
			pack();
			setSize(200, getSize().height);
			setLocationRelativeTo(parent);
		}
		public static boolean containsAll(String st, String[] sa) {
			for (String s:sa) {
				if (!st.contains(s)) return false;
			}
			return true;
		}
	}
}
