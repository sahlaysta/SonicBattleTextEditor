package sbte.gui.layout.menubar.actions.windowmenus;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import sbte.gui.GUI;
import sbte.gui.utilities.Localization;
import sbte.utilities.JSONTools;

public class GUIChangeLanguage {
	public static void languageGUI(GUI caller) {
		new ChangeLangGUI(caller).setVisible(true);
	}
	public static class ChangeLangGUI extends JDialog {
		public ChangeLangGUI(GUI parent) {
			super(parent);
			setModal(true);
			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			setTitle(parent.localization.get("changeLang"));
			JSONObject j = null;
			try { j = (JSONObject) JSONTools.parser.parse(Localization.getLocalizationFile()); } catch (ParseException e) { e.printStackTrace(); }
			{//list
				JPanel p = new JPanel();
				DefaultListModel<String> l = new DefaultListModel<>();  
				List<String> langCode = new ArrayList<>();
				for (Object o: j.keySet()) {
					String s = o.toString();
					try {
						l.addElement(((JSONObject) j.get(s)).get("thisLang").toString());
						langCode.add(s);
					} catch (ClassCastException e) { // json comment error fix
						continue;
					}
				}
				JList<String> list = new JList<>(l);
				list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				p.add(list);
				
				add(p, BorderLayout.LINE_START);
				
				//button
					JPanel pp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
					JButton b = new JButton(parent.localization.get("sel"));
					b.addActionListener(new ActionListener(){ public void actionPerformed(ActionEvent e){
						parent.setLocalization(langCode.get(list.getSelectedIndex()));
						b.setText(parent.localization.get("sel"));
						setTitle(parent.localization.get("changeLang"));
					}});  
					pp.add(b);
					add(pp, BorderLayout.SOUTH);
				
				
				//list double click
				list.addMouseListener(new MouseAdapter() {
				    public void mouseClicked(MouseEvent evt) {
				        if (evt.getClickCount() == 2) {
				            b.doClick();
				        }
				    }
				});
				
				//list enter key
				list.addKeyListener(new KeyAdapter(){
					public void keyPressed(KeyEvent e){
					    if (e.getKeyCode() == KeyEvent.VK_ENTER){
					   b.doClick();
					}
					}
					});
			}
			
			pack();
			setMinimumSize(new Dimension(getSize().width, getSize().height));
			setSize(getSize().width + 50, getSize().height);
			setLocationRelativeTo(parent);
		}
	}
}
