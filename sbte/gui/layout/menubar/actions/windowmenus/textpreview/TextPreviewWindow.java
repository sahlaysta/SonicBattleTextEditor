package sbte.gui.layout.menubar.actions.windowmenus.textpreview;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import sbte.gui.GUI;
import sbte.gui.layout.menubar.actions.util.GUIFileChooser;
import sbte.gui.util.GUITools;
import sbte.parser.SonicBattleTextParser.SonicBattleParseException;
import sbte.util.ByteTools;

public final class TextPreviewWindow extends JDialog {
	private static final long serialVersionUID = 578154221665252907L;
	
	private final GUI parent;
	public TextPreviewWindow(GUI caller) {
		super(caller);
		parent = caller;
		setProperties();
		setContent(-1);
	}
	private JPanel imagePanel;
	private ButtonPanel buttonPanel;
	private class ButtonPanel extends JPanel {
		private static final long serialVersionUID = 6473957450626208780L;
		
		public JButton upButton, downButton;
		private JLabel label;
		public ButtonPanel() {
			setLayout(new GridLayout(1,1,0,0));
			setBorder(new EmptyBorder(0,0,0,0));
			upButton = new JButton("▲");
			downButton = new JButton("▼");
			upButton.addActionListener(new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		        	setDisplay(displayIndex-1);
		        }
		    });
			downButton.addActionListener(new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		        	setDisplay(displayIndex+1);
		        }
		    });
		}
		public void putButtons() {
			JPanel masterPanel = new JPanel();
			masterPanel.setLayout(new GridLayout(1,2,0,0));
			masterPanel.setBorder(new EmptyBorder(0,0,0,0));
			
			JPanel buttons = new JPanel();
			buttons.setLayout(new FlowLayout(FlowLayout.RIGHT));
			buttons.setBorder(new EmptyBorder(0,0,0,0));
			buttons.add(upButton);
			buttons.add(downButton);
			
			JPanel labelPanel = new JPanel();
			labelPanel.setLayout(new BorderLayout());
			labelPanel.setBorder(new EmptyBorder(3,5,5,5));
			this.label = new JLabel("");
			label.setVerticalAlignment(JLabel.BOTTOM);
			labelPanel.add(label, BorderLayout.PAGE_END);
			
			masterPanel.add(labelPanel);
			masterPanel.add(buttons);
			
			add(masterPanel);
		}
		public void setText(int index, int limit) {
			label.setText((1 + index) + "/" + limit);
		}
	}
	private void setProperties() {
		super.setName("json:textPreview");
		super.setLocationRelativeTo(null);
		super.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		super.addWindowListener(new OnClose());
		super.setResizable(false);
		
		this.setMenuBar();
		imagePanel = new JPanel();
		imagePanel.setLayout(new GridLayout(1,1,0,0));
		imagePanel.setBorder(new EmptyBorder(0,0,0,0));
		imagePanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				popupMenu(arg0);
			}
			@Override
			public void mouseReleased(MouseEvent arg0) {
				popupMenu(arg0);
			}
			void popupMenu(MouseEvent arg0){
				if (!arg0.isPopupTrigger()) return;
				RightClickMenu rcm = new RightClickMenu();
				rcm.show((Component)arg0.getSource(), arg0.getX(), arg0.getY());
			}
			class RightClickMenu extends JPopupMenu {
				private static final long serialVersionUID = 1905073680128109030L;
				public RightClickMenu() {
					addMenu(file);
					addSeparator();
					addMenu(view);
				}
				void addMenu(JMenu arg0) { //deep copy JMenu
					for (Component comp: arg0.getMenuComponents()) {
						if (!(comp instanceof JMenuItem)) continue;
						JMenuItem source = (JMenuItem)comp;
						JMenuItem addition = new JMenuItem();
						addition.setText(source.getText());
						addition.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent arg0) {
								source.doClick();
							}
						});
						super.add(addition);
						if (!(comp instanceof JCheckBoxMenuItem)) continue;
						super.remove(addition);
						JCheckBoxMenuItem checkSource = (JCheckBoxMenuItem)comp;
						JCheckBoxMenuItem checkAddition = new JCheckBoxMenuItem();
						checkAddition.setText(checkSource.getText());
						checkAddition.setSelected(checkSource.isSelected());
						checkAddition.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent arg0) {
								checkSource.doClick();
							}
						});
						super.add(checkAddition);
					}
				}
			}
		});
		super.add(imagePanel);
		buttonPanel = new ButtonPanel();
		buttonPanel.putButtons();
		super.add(buttonPanel, BorderLayout.PAGE_END);
		
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
	private static final int MAXIMUM_MAGNIFICATION = 4; //the number of magnification options there are e.g. 1x, 2x
	private static final String MAGNIFICATION_MENU_ITEM_NAME = "mag";
	private void setMenuBar() { //menubar
		menuBar = new JMenuBar();
		
		file = new JMenu();
		file.setName("json:file");
		saveImage = new JMenuItem();
		saveImage.setName("json:saveImage");
		saveImage.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) { //save image
	        	GUIFileChooser gfc = new GUIFileChooser(parent, GUIFileChooser.SAVE_AS_FILE_PROMPT, "PNG", "pngPath");
	        	if (gfc.hasCanceled()) return;
	        	File selection = gfc.getSelectedFile();
	        	try {
					ImageIO.write(rawImage, "png", selection);
				} catch (IOException e2) {
					JOptionPane.showMessageDialog(TextPreviewWindow.this, parent.localization.get("saveImage") + ": " + selection.toString(), e2.toString(), JOptionPane.ERROR_MESSAGE);
				}
	        }
	    });
		file.add(saveImage);
		copyImage = new JMenuItem();
		copyImage.setName("json:copyImage");
		copyImage.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	        	new CopyImagetoClipBoard(rawImage);
	        }
	    });
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
	private ActionListener getMagnificationMenuItemActionListener(int arg0) { //when clicked
		final int magnification = arg0;
		ActionListener action = new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	        	setMagnification(magnification);
	        }
	    };
	    return action;
	}
	private void setMagnification(int arg0) {
		this.magnification = arg0;
		setMagnificationOption(arg0);
    	resetDisplayText();
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
	
	
	private final BufferedImage textFrame = getResource("frame.png");
	private BufferedImage getTextFrame() {
		return copyImage(textFrame);
	}
	private BufferedImage copyImage(BufferedImage buffImg){ //deep copy a bufferedimage object
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
			InputStream is = TextPreviewWindow.class.getResourceAsStream(resourceName);
			if (is == null) break setResource;
			output = ImageIO.read(is);
		} catch (IOException e) {}//impossible
		return output;
	}
	
	
	private static final int BLACK = -16777216; //rgb colors
	private static final int GREEN = -16734136;
	private static final int BLUE = -16777008;
	private static final int PURPLE = -7327528;
	private static final int RED = -524240;
	private static final int WHITE = BLACK;
	
	private static final Point defaultFontPos = new Point(15, 5); //default font draw position
	
	private List<byte[]> content;
	private byte[] display;
	private int contentIndex;
	private int displayIndex;
	private HashMap<Integer, Integer> indexHistory = new HashMap<>();
	public void setContent(int index) {
		removeWorkers();
		
		byte[] msg = null;
		if (index >= 0)
			msg = parent.listModel.content.get(index);
		
		content = (msg == null) ? getEmpty() : splitLineBreaks(msg);
		contentIndex = index;
		
		int display = 0;
		if (indexHistory.containsKey(index)) {
			int hindex = indexHistory.get(index);
			while (hindex >= content.size())
				hindex--;
			display = hindex;
		}
		setDisplay(display);
	}
	private List<byte[]> getEmpty() {
		List<byte[]> output = new ArrayList<>();
		output.add(new byte[] { (byte) 0x00, (byte) 0x00 });
		return output;
	}
	private void setDisplay(int index) {
		if (index >= content.size() || index < 0) return;
		displayIndex = index;
		this.display = content.get(displayIndex);
		indexHistory.put(contentIndex, displayIndex);
		resetDisplayText();
	}
	private static final byte[] LINE_BREAK_DELIMITER = new byte[] { (byte) 0xFD, (byte) 0xFF }; //FDFF is new line
	private static final int LINE_BREAK_LIMIT = 2; //2 lines per scroll
	private List<byte[]> splitLineBreaks(byte[] message){
		List<byte[]> output = new ArrayList<>();
		List<Byte> addition = new ArrayList<>();
		int lineBreaks = 0;
		for (int i = 0; i < message.length; i++) {
			boolean isDelimiter = true;
			for (int ii = 0; ii < LINE_BREAK_DELIMITER.length; ii++) {
				if (LINE_BREAK_DELIMITER[ii] != message[i + ii]) {
					isDelimiter = false;
					break;
				}
			}
			delimiterAction: if (isDelimiter) {
				if (lineBreaks % LINE_BREAK_LIMIT == 0) {
					for (byte b: LINE_BREAK_DELIMITER)
						addition.add(b);
					break delimiterAction;
				}
				output.add(byteListToArray(addition));
				addition.clear();
			}
			if (isDelimiter) {
				lineBreaks++;
				i += -1 + LINE_BREAK_DELIMITER.length;
				continue;
			}
			
			addition.add(message[i]);
		}
		output.add(byteListToArray(addition));
		return output;
	}
	private byte[] byteListToArray(List<Byte> arg0) {
		byte[] output = new byte[arg0.size()];
		for (int i = 0; i < output.length; i++)
			output[i] = arg0.get(i);
		return output;
	}
	private List<PaintWorker> workers = new ArrayList<>();
	private void resetDisplayText() {
		Thread newThread = new Thread(() -> { //do in background (stops UI lag)
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
	private void removeWorkers() {
		List<PaintWorker> removeQueue = new ArrayList<>();
		for (PaintWorker pw: workers) {
			removeQueue.add(pw);
		}
		for (PaintWorker pw: removeQueue) {
			if (pw == null) continue;
			workers.remove(pw);
		}
	}
	private class PaintWorker extends SwingWorker<Object, Object> {
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
			buttonPanel.setText(displayIndex, content.size());
		}
		
	}
	private BufferedImage paintFrame() {
		final BufferedImage textFrame = getTextFrame();
		List<Object> msg = null;
		try {
			msg = parseMessage(display);
		}
		catch (ParseException e) {
			try {
				msg = parseMessage(parent.sbtp.parseHexBinary("Error"));
			} catch (ParseException e2) {}
		}
		int drawPosX = defaultFontPos.x, drawPosY = defaultFontPos.y;
		int color = getPrevColor(displayIndex);
		for (Object obj: msg) {
			if (obj instanceof NamedImage) {
				NamedImage nimg = (NamedImage)obj;
				BufferedImage bimg = nimg.image;
				
				drawLetter(textFrame, drawPosX, drawPosY, color, bimg);
				drawPosX += 1 + bimg.getWidth();
			}
			else special: if (obj instanceof SpecialChar) {
				SpecialChar sc = (SpecialChar)obj;
				
				if (sc.arg == FontArg.LINE_BREAK) {
					drawPosX = defaultFontPos.x;
					drawPosY += 16;
					break special;
				}
				if (sc.arg == FontArg.BLUE) {
					color = BLUE;
					break special;
				}
				if (sc.arg == FontArg.BLACK) {
					color = BLACK;
					break special;
				}
				if (sc.arg == FontArg.GREEN) {
					color = GREEN;
					break special;
				}
				if (sc.arg == FontArg.PURPLE) {
					color = PURPLE;
					break special;
				}
				if (sc.arg == FontArg.RED) {
					color = RED;
					break special;
				}
				if (sc.arg == FontArg.WHITE) {
					color = WHITE;
					break special;
				}
			}
		}
		
		return textFrame;
	}
	private List<Object> parseMessage(byte[] arg0) throws ParseException{
		List<Object> output = new ArrayList<>();
		
		List<byte[]> splitHex = null;
		try {
			splitHex = parent.sbtp.splitBytes(arg0);
		}
		catch (SonicBattleParseException e) {
			throw new ParseException(e.toString());
		}
		
		for (byte[] barr: splitHex) {
			Object part = getPart(barr);
			if (part == null) throw new ParseException("err");
			output.add(part);
		}

		return output;
	}
	private class ParseException extends Exception {
		private static final long serialVersionUID = 5119810980401920182L;

		public ParseException(String arg0) {
			super(arg0);
		}
	}
	private Object getPart(byte[] sequence) {
		final String hex = ByteTools.toHexString(sequence);
		
		SpecialChar sc = SpecialChar.fromString(hex);
		if (!sc.isUnnamed()) return sc;
		
		BufferedImage bimg = getSBResource(hex);
		if (bimg != null) {
			NamedImage nimg = new NamedImage(hex, bimg);
			return nimg;
		}
		
		//variable sequences
		final BufferedImage placeHolder = getSBResource("2000"); //at sign as placeholder
		if (hex.equals("FBFF0300F9FF0200FBFF0100")) {
			NamedImage nimg = new NamedImage(hex, placeHolder);
			return nimg;
		}
		if (hex.equals("F9FFED00")) {
			NamedImage nimg = new NamedImage(hex, placeHolder);
			return nimg;
		}
		if (hex.equals("F9FF0000")) {
			NamedImage nimg = new NamedImage(hex, placeHolder);
			return nimg;
		}
		if (hex.equals("F9FFCA00")) {
			NamedImage nimg = new NamedImage(hex, placeHolder);
			return nimg;
		}
		if (hex.equals("F9FF")) {
			NamedImage nimg = new NamedImage(hex, placeHolder);
			return nimg;
		}
		
		return null; //no matches
	}
	private int getPrevColor(int index) {
		int result = BLACK;
		for (int i = 0; i < index; i++) {
			List<Object> msg = null;
			try {
				msg = parseMessage(content.get(i));
			} catch (ParseException e) {
				return BLACK;
			}
			
			for (Object obj: msg) {
				if (!(obj instanceof SpecialChar)) continue;
				SpecialChar sc = (SpecialChar)obj;
				if (sc.arg == FontArg.BLUE) result = BLUE;
				if (sc.arg == FontArg.BLACK) result = BLACK;
				if (sc.arg == FontArg.GREEN) result = GREEN;
				if (sc.arg == FontArg.PURPLE) result = PURPLE;
				if (sc.arg == FontArg.RED) result = RED;
				if (sc.arg == FontArg.WHITE) result = WHITE;
			}
		}
		
		return result;
	}
	private BufferedImage getSBResource(String arg0) {
		return getResource("SonicBattleFont/" + arg0 + ".png");
	}
	private static class SpecialChar {
		public final int arg;
		public final String name;
		private static final int NULL_ARG = -1;
		private static final String NULL_NAME = null;
		
		public static SpecialChar fromString(String substring) {
			return new SpecialChar(substring);
		}
		private static final String LINE_BREAK = "FDFF";
		private static final String BLUE = "FBFF0500";
		private static final String BLACK = "FBFF0300";
		private static final String GREEN = "FBFF0600";
		private static final String PURPLE = "FBFF0700";
		private static final String RED = "FBFF0400";
		private static final String WHITE = "FBFF0000";
		private SpecialChar(String substring) {
			int arg = NULL_ARG;
			String name = NULL_NAME;
			
			findMatch: {
				if (substring.equals(LINE_BREAK)) {
					name = LINE_BREAK;
					arg = FontArg.LINE_BREAK;
					break findMatch;
				}
				if (substring.equals(BLUE)) {
					name = BLUE;
					arg = FontArg.BLUE;
					break findMatch;
				}
				if (substring.equals(BLACK)) {
					name = BLACK;
					arg = FontArg.BLACK;
					break findMatch;
				}
				if (substring.equals(GREEN)) {
					name = GREEN;
					arg = FontArg.GREEN;
					break findMatch;
				}
				if (substring.equals(PURPLE)) {
					name = PURPLE;
					arg = FontArg.PURPLE;
					break findMatch;
				}
				if (substring.equals(RED)) {
					name = RED;
					arg = FontArg.RED;
					break findMatch;
				}
				if (substring.equals(WHITE)) {
					name = WHITE;
					arg = FontArg.WHITE;
					break findMatch;
				}
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
		public static final int BLUE = 1;
		public static final int BLACK = 2;
		public static final int GREEN = 3;
		public static final int PURPLE = 4;
		public static final int WHITE = 5;
		public static final int RED = 6;
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
	private void drawLetter(BufferedImage frame, int x, int y, int color, BufferedImage letter) {
		for (int i = 0; i <= letter.getWidth(); i++) {
			for (int ii = 0; ii <= letter.getHeight(); ii++) {
				final int xx = x + i;
				final int yy = y + ii;
				int pixel = getRGBSafely(letter, i, ii);
				if (pixel != BLACK) continue;
				
				setRGBSafely(frame, xx, yy, color);
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
	private BufferedImage rawImage; //raw image
	private void setTextFrame(BufferedImage buffImg) {
		rawImage = buffImg;
		BufferedImage img = enlargeImage(buffImg, magnification);
		ImageIcon imgIco = new ImageIcon(img);
		JLabel label = new JLabel(imgIco);
		removeAllJLabels(imagePanel);
		imagePanel.add(label);
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
	private void removeAllJLabels(Container c) {
		for (Object obj: GUITools.getAllElements(c)) {
			if (!(obj instanceof Component)) continue;
			Component comp = (Component)obj;
			if (comp instanceof JLabel) {
				c.remove(comp);
			}
		}
	}
	public BufferedImage getImage() {
		return rawImage;
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
