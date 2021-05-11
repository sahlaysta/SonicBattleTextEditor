package sbte.GUI.FontPreview;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;

import sbte.ByteTools;
import sbte.GUI.GUI;
import sbte.GUI.GUITools;

public class FontPreviewWindow extends JDialog {
	private final GUI parent;
	private BufferedImage textFrame = getTextFrame();
	public FontPreviewWindow(GUI caller) {
		super(caller);
		parent = caller;
		setProperties();
		setContent(parent.sbtp.parseHexBinary("AB"));
	}
	private void setProperties() {
		setTitle(parent.localization.get("textPreview"));
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setLayout(new GridLayout(1,1,0,0));
	}
	private BufferedImage getTextFrame() {
		return getResource("frame.png");
	}
	private BufferedImage getResource(String resourceName) {
		BufferedImage output = null;
		try {
			output = ImageIO.read(FontPreviewWindow.class.getResourceAsStream(resourceName));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return output;
	}
	
	private static final int BLACK = new Color(0, 0, 0, 255).getRGB();
	private void setContent(byte[] content) {
		resetFrame();
		
		String hex = ByteTools.toHexString(content);
		
		textFrame.setRGB(30, 30, BLACK);
		setTextFrame(textFrame);
	}
	private void resetFrame() {
		removeAllJLabels();
		textFrame = getTextFrame();
	}
	private void removeAllJLabels() {
		for (Component comp: GUITools.getAllComponents(this)) {
			if (comp instanceof JLabel) {
				super.remove(comp);
			}
		}
	}
	private void setTextFrame(BufferedImage buffImg) {
		ImageIcon imgIco = new ImageIcon(buffImg);
		JLabel label = new JLabel(imgIco);
		super.add(label);
		super.pack();
	}
}
