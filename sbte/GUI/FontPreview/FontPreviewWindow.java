package sbte.GUI.FontPreview;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import sbte.ByteTools;
import sbte.SonicBattleROMReader;
import sbte.GUI.GUI;
import sbte.GUI.GUITools;

public class FontPreviewWindow extends JDialog {
	private final GUI parent;
	public FontPreviewWindow(GUI caller) {
		super(caller);
		parent = caller;
		setProperties();
		setContent(parent.sbtp.parseHexBinary(""));
	}
	private void setProperties() {
		super.setName("json:textPreview");
		super.setLocationRelativeTo(null);
		super.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		super.addWindowListener(new OnClose());
		super.setLayout(new GridLayout(1,1,0,0));
		super.setResizable(false);
		this.setMenuBar();
		this.setMagnification(0);
	}
	private class OnClose extends WindowAdapter { //close event
		@Override
	    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
	        parent.closeTextPreview();
	    }
	};
	private JMenuBar menuBar;
	private JMenu file, view;
	private JMenuItem saveImage, copyImage;
	private static final int MAXIMUM_MAGNIFICATION = 4;
	private static final String MAGNIFICATION_MENU_ITEM_NAME = "mag";
	private void setMenuBar() { //menubar
		menuBar = new JMenuBar();
		
		file = new JMenu();
		file.setName("json:file");
		saveImage = new JMenuItem();
		saveImage.setName("json:saveImage");
		file.add(saveImage);
		copyImage = new JMenuItem();
		copyImage.setName("json:copyImage");
		file.add(copyImage);

		menuBar.add(file);
		
		view = new JMenu();
		view.setName("json:options");
		for (int i = 0; i < MAXIMUM_MAGNIFICATION; i++) {
			JCheckBoxMenuItem jcbmi = new JCheckBoxMenuItem((1 + i) + "X");
			jcbmi.setName(MAGNIFICATION_MENU_ITEM_NAME);
			jcbmi.addActionListener(getMagnificationMenuItemActionListener(i));
			view.add(jcbmi);
		}
		
		menuBar.add(view);
		setJMenuBar(menuBar);
	}
	private ActionListener getMagnificationMenuItemActionListener(int arg0) {
		final int magnification = arg0;
		ActionListener action = new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	        	setMagnification(magnification);
	        	resetDisplayText();
	        }
	    };
	    return action;
	}
	private void setMagnification(int arg0) {
		this.magnification = arg0;
		setMagnificationOption(arg0);
	}
	private void setMagnificationOption(int arg0) {
		JMenuItem[] options = getMagnificationMenuItems(view);
		for (int i = 0; i < options.length; i++) {
			JMenuItem jmi = options[i];
			if (i == arg0) {
				jmi.setSelected(true);
				continue;
			}
			jmi.setSelected(false);
		}
	}
	private JMenuItem[] getMagnificationMenuItems(JMenu jm) {
		List<JMenuItem> array = new ArrayList<>();
		for (Component comp: jm.getMenuComponents()) {
			if (!(comp instanceof JMenuItem)) continue;
			JMenuItem jmi = (JMenuItem)comp;
			if (!(jmi.getName().equals(MAGNIFICATION_MENU_ITEM_NAME))) continue;
			array.add(jmi);
		}
		
		JMenuItem[] output = new JMenuItem[array.size()];
		for (int i = 0; i < output.length; i++)
			output[i] = array.get(i);
		return output;
	}
	private BufferedImage textFrame = getResource("frame.png");
	private BufferedImage getTextFrame() {
		return copyImage(textFrame);
	}
	private BufferedImage copyImage(BufferedImage buffImg){ //deep copy image
	    BufferedImage output = new BufferedImage(buffImg.getWidth(), buffImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
	    for (int i = 0; i < buffImg.getWidth(); i++) {
			for (int ii = 0; ii < buffImg.getHeight(); ii++) {
				final int pixel = buffImg.getRGB(i, ii);
				output.setRGB(i, ii, pixel);
			}
		}
	    return output;
	}
	private BufferedImage getResource(String resourceName) {
		BufferedImage output = null;
		setResource: try {
			InputStream is = FontPreviewWindow.class.getResourceAsStream(resourceName);
			if (is == null) break setResource;
			output = ImageIO.read(is);
		} catch (IOException e) {}
		return output;
	}
	
	private static final int BLACK = -16777216; //new Color(0, 0, 0, 255).getRGB();
	private static final Point defaultFontPos = new Point(15, 5); //default font draw position
	
	private List<byte[]> content;
	private byte[] display;
	public void setContent(byte[] arg0) {
		content = splitLineBreaks(arg0);
		this.display = arg0;
		resetDisplayText();
	}
	private static final byte[] LINE_BREAK_DELIMITER = new byte[] { (byte) 0xFD, (byte) 0xFF }; //FDFF is new line
	private List<byte[]> splitLineBreaks(byte[] message){
		List<byte[]> output = new ArrayList<>();
		List<Byte> addition = new ArrayList<>();
		for (int i = 0; i < message.length; i++) {
			boolean isDelimiter = true;
			for (int ii = 0; ii < LINE_BREAK_DELIMITER.length; ii++) {
				if (LINE_BREAK_DELIMITER[ii] != message[i + ii]) {
					isDelimiter = false;
					break;
				}
			}
			if (isDelimiter) {
				byte[] array = new byte[addition.size()];
				for (int ii = 0; ii < array.length; ii++)
					array[ii] = addition.get(ii);
				output.add(array);
				addition.clear();
			}
			
			addition.add(message[i]);
		}
		for (byte[] b: output)
			System.out.println(ByteTools.toHexString(b));
		
		return null;
	}
	private List<PaintWorker> workers = new ArrayList<>();
	private void resetDisplayText() {
		Thread newThread = new Thread(() -> {
			while (workers.size() > 0) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			PaintWorker pw = new PaintWorker();
			pw.execute();
		});
		newThread.start();
	}
	private class PaintWorker extends SwingWorker {
		BufferedImage buffImg;
		
		public PaintWorker() {
			super();
			workers.add(this);
		}
		
		@Override
		protected Object doInBackground() throws Exception {
			buffImg = paintFrame();
			return null;
		}
		
		@Override
		protected void done() {
			setTextFrame(buffImg);
			workers.remove(this);
		}
		
	}
	private BufferedImage paintFrame() {
		BufferedImage textFrame = getTextFrame();
		String hex = ByteTools.toHexString(display);
		List<Object> msg = null;
		try {
			msg = parseMessage(hex);
		}
		catch (ParseException e) {
			try {
				msg = parseMessage(ByteTools.toHexString(parent.sbtp.parseHexBinary("Error")));
			} catch (ParseException e2) {}
		}
		int drawPosX = defaultFontPos.x, drawPosY = defaultFontPos.y; //default position
		for (Object obj: msg) {
			if (obj instanceof NamedImage) { //Letter
				NamedImage nimg = (NamedImage)obj;
				BufferedImage bimg = nimg.image;
				
				drawLetter(textFrame, drawPosX, drawPosY, bimg);
				drawPosX += 1 + bimg.getWidth();
			}
			else if (obj instanceof SpecialChar) { //special character
				SpecialChar sc = (SpecialChar)obj;
				
				if (sc.arg == FontArg.LINE_BREAK) {
					drawPosX = defaultFontPos.x;
					drawPosY += 16;
				}
			}
		}
		
		return textFrame;
	}
	private void removeAllJLabels() {
		for (Component comp: GUITools.getAllComponents(this)) {
			if (comp instanceof JLabel) {
				super.remove(comp);
			}
		}
	}
	private List<Object> parseMessage(String string) throws ParseException{
		List<Object> output = new ArrayList<>();
		
		String hex = new String(string);
		while (hex.length() > 0) {
			Object obj = getFirstOccurrence(hex);
			if (obj == null) throw new ParseException(string);
			output.add(obj);
			hex = hex.substring(obj.toString().length());
		}
		
		return output;
	}
	private class ParseException extends Exception {
		public ParseException(String arg0) {
			super(arg0);
		}
	}
	private Object getFirstOccurrence(String hex) {
		StringBuilder target = new StringBuilder(hex);
		for (int i = target.length(); i > 0; i = removeLastLetter(target).length() ) {
			String focus = target.toString();
			SpecialChar sc = SpecialChar.fromString(focus);
			if (!sc.isUnnamed()) {
				return sc;
			}
			BufferedImage bimg = getResource("SonicBattleFont/" + focus + ".png");
			if (bimg == null) continue;
			return new NamedImage(focus, bimg);
		}
		return null;
	}
	private StringBuilder removeLastLetter(StringBuilder sb) {
		sb.setLength(sb.length()-1);
		return sb;
	}
	private static class SpecialChar {
		public final int arg;
		public final String name;
		private static final int NULL_ARG = 0;
		private static final String NULL_NAME = null;
		
		public static SpecialChar fromString(String substring) {
			return new SpecialChar(substring);
		}
		private static final String LINE_BREAK = "FDFF";
		private SpecialChar(String substring) {
			int arg = NULL_ARG;
			String name = NULL_NAME;
			if (substring.equals(LINE_BREAK)) {
				name = LINE_BREAK;
				arg = FontArg.LINE_BREAK;
			}
			
			this.name = name;
			this.arg = arg;
		}
		public String toString() {
			return name;
		}
		public boolean isUnnamed() {
			return name == NULL_NAME;
		}
	}
	private class FontArg {
		public static final int LINE_BREAK = 0;
	}
	private class NamedImage { //bufferedimage + string tuple
		public final BufferedImage image;
		public final String name;
		public NamedImage(String name, BufferedImage bimg) {
			this.name = name;
			this.image = bimg;
		}
		public String toString() {
			return name;
		}
	}
	private void drawLetter(BufferedImage frame, int x, int y, BufferedImage letter) {
		for (int i = 0; i <= letter.getWidth(); i++) {
			for (int ii = 0; ii <= letter.getHeight(); ii++) {
				final int xx = x + i;
				final int yy = y + ii;
				int pixel = getRGBSafely(letter, i, ii);
				if (pixel != BLACK) continue;
				
				setRGBSafely(frame, xx, yy, BLACK);
			}
		}
	}
	private int getRGBSafely(BufferedImage buffImg, int x, int y) {
		int output = 0;
		try {
			output = buffImg.getRGB(x, y);
		} catch (ArrayIndexOutOfBoundsException e) {}
		return output;
	}
	private void setRGBSafely(BufferedImage buffImg, int x, int y, int rgb) {
		try {
			buffImg.setRGB(x, y, rgb);
		} catch (ArrayIndexOutOfBoundsException e) {}
	}
	private int magnification;
	private void setTextFrame(BufferedImage buffImg) {
		BufferedImage img = enlargeImage(buffImg, magnification);
		ImageIcon imgIco = new ImageIcon(img);
		JLabel label = new JLabel(imgIco);
		removeAllJLabels();
		super.add(label);
		super.pack();
	}
	private BufferedImage enlargeImage(BufferedImage buffImg, int magnification) {
		final int magVal = 1 + magnification;
		final int newW = buffImg.getWidth() * magVal;
		final int newH = buffImg.getHeight() * magVal;
		
		Image tmp = buffImg.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
	    BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

	    Graphics2D g2d = dimg.createGraphics();
	    g2d.drawImage(tmp, 0, 0, null);
	    g2d.dispose();

	    return dimg;
	}
	
	
	//stolen
	private class CopyImagetoClipBoard implements ClipboardOwner {
	    public CopyImagetoClipBoard(BufferedImage i) {
            TransferableImage trans = new TransferableImage( i );
            Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
            c.setContents( trans, this );
	    }

	    public void lostOwnership( Clipboard clip, Transferable trans ) {
	        System.out.println( "Lost Clipboard Ownership" );
	    }

	    private class TransferableImage implements Transferable {

	        Image i;

	        public TransferableImage( Image i ) {
	            this.i = i;
	        }

	        public Object getTransferData( DataFlavor flavor )
	        throws UnsupportedFlavorException, IOException {
	            if ( flavor.equals( DataFlavor.imageFlavor ) && i != null ) {
	                return i;
	            }
	            else {
	                throw new UnsupportedFlavorException( flavor );
	            }
	        }

	        public DataFlavor[] getTransferDataFlavors() {
	            DataFlavor[] flavors = new DataFlavor[ 1 ];
	            flavors[ 0 ] = DataFlavor.imageFlavor;
	            return flavors;
	        }

	        public boolean isDataFlavorSupported( DataFlavor flavor ) {
	            DataFlavor[] flavors = getTransferDataFlavors();
	            for ( int i = 0; i < flavors.length; i++ ) {
	                if ( flavor.equals( flavors[ i ] ) ) {
	                    return true;
	                }
	            }

	            return false;
	        }
	    }
	}
}
