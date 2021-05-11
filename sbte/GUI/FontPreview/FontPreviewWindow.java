package sbte.GUI.FontPreview;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
		try {
			setContent(parent.sbtp.parseHexBinary("Fight! Why don't you\nfight?!"));
		} catch (ParseException e) {
			try {
				setContent(parent.sbtp.parseHexBinary("Error"));
			} catch (ParseException e2) {}
		}
	}
	private void setProperties() {
		setTitle("json:textPreview");
		//setTitle(parent.localization.get("textPreview"));
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setLayout(new GridLayout(1,1,0,0));
	}
	private BufferedImage getTextFrame() {
		return getResource("frame.png");
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
	
	private void setContent(byte[] content) throws ParseException {
		resetFrame();
		
		String hex = ByteTools.toHexString(content);
		List<Object> msg = null;
		try {
			msg = parseMessage(hex);
		}
		catch (ParseException e) {
			throw e;
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
	private class FontArg {
		public static final int LINE_BREAK = 0;
	}
	private Object getFirstOccurrence(String hex) {
		StringBuilder target = new StringBuilder(hex);
		for (int i = target.length(); i > 0; i = removeLastLetter(target).length() ) {
			String focus = target.toString();
			SpecialChar sc = new SpecialChar(focus);
			if (!sc.isNull()) {
				return sc;
			}
			BufferedImage bimg = getResource("SonicBattleFont/" + focus + ".png");
			if (bimg == null) continue;
			return new NamedImage(focus, bimg);
		}
		return null;
	}
	private class SpecialChar {
		public final int arg;
		public final String name;
		
		private static final String LINE_BREAK = "FDFF";
		public SpecialChar(String substring) {
			String name = null;
			int arg = -1;
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
		public boolean isNull() {
			return arg == -1 && name == null;
		}
	}
	private StringBuilder removeLastLetter(StringBuilder sb) {
		sb.setLength(sb.length()-1);
		return sb;
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
	private void setTextFrame(BufferedImage buffImg) {
		ImageIcon imgIco = new ImageIcon(buffImg);
		JLabel label = new JLabel(imgIco);
		super.add(label);
		super.pack();
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
