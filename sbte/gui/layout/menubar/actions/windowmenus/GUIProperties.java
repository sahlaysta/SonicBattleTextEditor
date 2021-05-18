package sbte.gui.layout.menubar.actions.windowmenus;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import sbte.gui.GUI;
import sbte.gui.layout.menubar.actions.util.NoInputDocumentFilter;
import sbte.parser.SonicBattleROMReader.SonicBattleLine;
import sbte.util.ByteTools;

public final class GUIProperties {
	public static void propertiesGUI(GUI caller) {
		new PropertiesGUI(caller).setVisible(true);
		System.out.println(3);
	}
	public static class PropertiesGUI extends JDialog {

		private final GUI parent;
		public PropertiesGUI(GUI caller) {
			parent = caller;
			
			super.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			super.setModal(true);
			super.setResizable(false);
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
			
			ScrollJTextArea messagePanel = new ScrollJTextArea(parent.localization.get("line"), message);
			add(messagePanel, BorderLayout.CENTER);
			ScrollJTextArea hexPanel = new ScrollJTextArea("HEX", hex);
			hexPanel.setPreferredSize(new Dimension(0, 64));
			add(hexPanel, BorderLayout.PAGE_END);

			setSize(new Dimension(170,180));
			super.setLocationRelativeTo(parent);
		}
		private class ScrollJTextArea extends JPanel {
			public ScrollJTextArea(String title, String content) {
				super.setLayout(new GridLayout(1,1,0,0));
				super.setBorder(new TitledBorder(title));
				
				CustomTextArea cta = new CustomTextArea(content);
				JScrollPane jsp = new JScrollPane(cta);
				super.add(jsp);
			}
			class CustomTextArea extends JTextArea {
				public CustomTextArea(String text) {
					super(text);
					NoInputDocumentFilter.set(this);
				}
			}
		}
		private String getMessage(int index) {
			return parent.listModel.isProblematic(index)
					? null
					: parent.listModel.textBoxDisplay.get(index).toString()
					;
		}
		private String getHex(int index) {
			return parent.listModel.isProblematic(index)
					? null
					: ByteTools.toHexString(parent.listModel.content.get(index))
					;
		}
	}
}
