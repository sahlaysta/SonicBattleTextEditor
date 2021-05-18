package sbte.gui.layout.menubar.actions.windowmenus;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import sbte.gui.GUI;
import sbte.gui.layout.menubar.actions.util.NoInputDocumentFilter;
import sbte.gui.popupmenus.CopyContextMenu;
import sbte.parser.SonicBattleROMReader;
import sbte.parser.SonicBattleROMReader.SonicBattleLine;
import sbte.util.ByteTools;

public final class GUIProperties {
	public static void propertiesGUI(GUI caller) {
		new PropertiesGUI(caller).setVisible(true);
	}
	
	
	public static class PropertiesGUI extends JDialog {
		private static final long serialVersionUID = -1302051134202542036L;
		
		private final GUI parent;
		public PropertiesGUI(GUI caller) {
			parent = caller;
			
			super.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			super.setModal(true);
			//super.setResizable(false);
			super.setMinimumSize(new Dimension(50, 210));
			initializeComponents();
		}
		private String getTitle(int index) {
			SonicBattleLine sbl = parent.listModel.baseLines.get(index);
			String id = parent.localization.get("indexToString")
					.replace("[v1]", 1 + sbl.group + "")
					.replace("[v2]", 1 + sbl.member + "");
			return parent.localization.get("propertiesOf").replace("[v]", id);
		}
		
		
		private void initializeComponents() {
			final int index = parent.list.getSelection();
			final String message = getMessage(index);
			final String hex = getHex(index);
			
			super.setLayout(new BorderLayout());
			super.setTitle(getTitle(index));
			
			//line + HEX panel
			final JPanel messageHexPanel = new JPanel();
			messageHexPanel.setLayout(new BorderLayout());
			messageHexPanel.setBorder(new EmptyBorder(0,0,0,0));
			ScrollJTextArea messagePanel = new ScrollJTextArea(parent.localization.get("line"), message);
			messagePanel.textArea.setEnabled(false); //auto-focus fix
			messageHexPanel.add(messagePanel, BorderLayout.CENTER);
			ScrollJTextArea hexPanel = new ScrollJTextArea("HEX", hex);
			hexPanel.textArea.setEnabled(false);
			hexPanel.setPreferredSize(new Dimension(0, 64));
			messageHexPanel.add(hexPanel, BorderLayout.PAGE_END);
			
			//OK button
			final JPanel buttonPanel = new JPanel();
			buttonPanel.setBorder(new EmptyBorder(0,0,0,0));
			buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			JButton okButton = new JButton(parent.localization.get("ok"));
			okButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					PropertiesGUI.super.dispose();
				}
			});
			buttonPanel.add(okButton);
			
			super.add(buttonPanel, BorderLayout.SOUTH);
			super.add(messageHexPanel, BorderLayout.CENTER);

			setSize(new Dimension(180,230));
			super.setLocationRelativeTo(parent);
			
			addComponentListener(new ComponentListener() {
		        public void componentResized(ComponentEvent e) {}
		        public void componentMoved(ComponentEvent e) {}
		        public void componentHidden(ComponentEvent e) {}
		        
		        @Override
		        public void componentShown(ComponentEvent e) { //set focus to button
		            okButton.requestFocus();
		            messagePanel.textArea.setEnabled(true);
		            hexPanel.textArea.setEnabled(true);
		            PropertiesGUI.super.removeComponentListener(this); //fire only once (at startup)
		        }
		    });
		}
		private class ScrollJTextArea extends JPanel {
			private static final long serialVersionUID = 1858482259392534496L;
			
			public final CustomTextArea textArea;
			public ScrollJTextArea(String title, String content) {
				super.setLayout(new GridLayout(1,1,0,0));
				super.setBorder(new TitledBorder(title));
				
				textArea = new CustomTextArea(content);
				JScrollPane jsp = new JScrollPane(textArea);
				super.add(jsp);
			}
			class CustomTextArea extends JTextArea {
				private static final long serialVersionUID = 8595174775615583309L;

				public CustomTextArea(String text) {
					super(text);
					NoInputDocumentFilter.set(this);
					CopyContextMenu ccm = new CopyContextMenu(parent);
					ccm.putItems(CopyContextMenu.UNEDITABLE_FIELD);
					super.setComponentPopupMenu(ccm);
					addFocusListener(new FocusListener() { //select all on focus

			            @Override
			            public void focusGained(FocusEvent e) {
			            	CustomTextArea.this.select(0, getText().length());
			            }

			            @Override
			            public void focusLost(FocusEvent e) {

			            }
			        });
				}
			}
		}
		private String getMessage(int index) {
			return parent.listModel.isProblematic(index)
					? parent.localization.get("error")
					: parent.listModel.textBoxDisplay.get(index).toString()
					;
		}
		private String getHex(int index) {
			return parent.listModel.isProblematic(index)
					? parent.localization.get("error")
					: ByteTools.toHexString(parent.listModel.content.get(index)) + ByteTools.toHexString(SonicBattleROMReader.delimiter)
					;
		}
	}
}
