package com.github.sahlaysta.sonicbattletexteditor.textpreview;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.github.sahlaysta.sonicbattletexteditor.romparser.HexUtils;
import com.github.sahlaysta.sonicbattletexteditor.romparser.SonicBattleTextParserDictionary;
import com.github.sahlaysta.sonicbattletexteditor.romparser.SonicBattleTextParserDictionary.Entry;

/** Class that draws text in Sonic Battle's font to a <code>Graphics</code> object.
 * It is meant to be a perfect representation of Sonic Battle's font */
public class SonicBattleFont {
	
	//Invis constructor
	private SonicBattleFont() {}
	
	//Sonic Battle Font colors
	/** The Color of black text in Sonic Battle */
	public static final Color BLACK = new Color(-16777216);
	/** The Color of red text in Sonic Battle */
	public static final Color RED = new Color(-524240);
	/** The Color of blue text in Sonic Battle */
	public static final Color BLUE = new Color(-16777008);
	/** The Color of green text in Sonic Battle */
	public static final Color GREEN = new Color(-16734136);
	/** The Color of purple text in Sonic Battle */
	public static final Color PURPLE = new Color(-7327528);
	/** The Color of white text in Sonic Battle */
	public static final Color WHITE = BLACK;
	
	//Draw text overloads
	/** Draw a Sonic Battle String to a <code>Graphics</code> object */
	public static final void drawText(String text, Graphics g, int x, int y) {
		drawText(parse(text), g, x, y);
	}
	/** Draw a Sonic Battle String to a <code>Graphics</code> object with the magnification size */
	public static final void drawText(String text, Graphics g, int x, int y, int magnification) {
		drawText(parse(text), g, x, y, magnification);
	}
	/** Draw an Iterable object (List, etc.) of Sonic Battle Glyphs to a <code>Graphics</code> object */
	public static final void drawText(Iterable<Glyph> glyphs, Graphics g, int x, int y) {
		drawText(glyphs.iterator(), g, x, y, 1, 16, 1);
	}
	/** Draw an Iterable object (List, etc.) Sonic Battle Glyphs to a <code>Graphics</code> object with the magnification size */
	public static final void drawText(Iterable<Glyph> glyphs, Graphics g, int x, int y, int magnification) {
		drawText(glyphs.iterator(), g, x, y, 1, 16, magnification);
	}
	/** Draw an array of Sonic Battle Glyphs to a <code>Graphics</code> object */
	public static final void drawText(Glyph[] glyphs, Graphics g, int x, int y) {
		drawText(glyphs, g, x, y, 1);
	}
	/** Draw an array of Sonic Battle Glyphs to a <code>Graphics</code> object with magnification */
	public static final void drawText(Glyph[] glyphs, Graphics g, int x, int y, int magnification) {
		Iterator<Glyph> it = new Iterator<Glyph>() {
			private int i = 0;
			@Override
			public boolean hasNext() {
				return i < glyphs.length;
			}
			@Override
			public Glyph next() {
				return glyphs[i++];
			}
		};
		drawText(it, g, x, y, 1, 16, magnification);
	}
	/** Draw an Iterator of Sonic Battle Glyphs to a <code>Graphics</code> object<br><br>
	 * 
	 *  - <b>iterator</b>: the Iterator of Sonic Battle Glyphs<br>
	 *  - <b>g</b>: the Graphics object to draw onto<br>
	 *  - <b>horizontalGap</b>: the horizontal gap in pixels between letters<br>
	 *  - <b>verticalGap</b>: the vertical gap in pixels between line breaks<br> */
	public static final void drawText(Iterator<Glyph> iterator, Graphics g, int x, int y, int horizontalGap, int verticalGap, int magnification) {
		g.setColor(BLACK);
		int xPos = x, yPos = y;
		while (iterator.hasNext()) {
			Glyph glyph = iterator.next();
			
			//Normal Glyphs
			if (!glyph.isSpecial()) {
				glyph.draw(g, xPos, yPos, magnification);
				xPos += (glyph.width * magnification) + (horizontalGap * magnification);
			}
			
			//Special Glyphs
			else {
				switch (glyph.specialArg) {
				case Glyph.LINE_BREAK:
					xPos = x;
					yPos+= verticalGap * magnification;
					break;
				case Glyph.BLACK:
					g.setColor(BLACK);
					break;
				case Glyph.RED:
					g.setColor(RED);
					break;
				case Glyph.BLUE:
					g.setColor(BLUE);
					break;
				case Glyph.GREEN:
					g.setColor(GREEN);
					break;
				case Glyph.PURPLE:
					g.setColor(PURPLE);
					break;
				case Glyph.WHITE:
					g.setColor(WHITE);
					break;
				}
			}
		}
	}

	//Parse Glyphs for font drawing
	/** Parse a List of Glyphs from a Sonic Battle String */
	public static final List<Glyph> parse(String str) {
		/*
		 * See SonicBattleTextParser
		 */
		List<Glyph> glyphs = new ArrayList<>();
		final int strLength = str.length();
		int pos = 0;
		byteLoop: while (pos < strLength) {
			dictionaryLoop: for (String entryStr: DICTIONARY.keySet()) {
				final int entryStrLength = entryStr.length();
				if (entryStrLength > (strLength - pos))
					continue;
				for (int i = 0; i < entryStrLength; i++)
					if (entryStr.charAt(i) != str.charAt(i + pos))
						continue dictionaryLoop;
				glyphs.add(DICTIONARY.get(entryStr));
				pos += entryStrLength;
				continue byteLoop;
			}
			throw new IllegalArgumentException("SonicBattleFont parse fail");
		}
		return glyphs;
	}
	
	//Dictionary
	/** The glyph dictionary map of Strings (the letter of the glyph, for example "A")
	 * to Glyphs (the Glyph object, with the letter's bitmap to draw).
	 * Used to parse Glyph Lists to draw text */
	protected static final Map<String, Glyph> DICTIONARY;
	static {
		/*
		 * Populate the DICTIONARY map with font glyph images from the 'sonicbattlefont' resource folder (using
		 * SonicBattleTextParserDictionary to enumerate resource files)
		 */
		DICTIONARY = new LinkedHashMap<>();
		//Special glyphs
		byte[] lineBreak = new byte[] { -3, -1 }; //FDFF is line break
		byte[] black = new byte[] { -5, -1, 3, 0 }; //FBFF0300 is black
		byte[] red = new byte[] { -5, -1, 4, 0 }; //FBFF0400 is red
		byte[] blue = new byte[] { -5, -1, 5, 0 }; //FBFF0500 is blue
		byte[] green = new byte[] { -5, -1, 6, 0 }; //FBFF0600 is green
		byte[] purple = new byte[] { -5, -1, 7, 0 }; //FBFF0700 is purple
		byte[] white = new byte[] { -5, -1, 0, 0 }; //FBFF0000 is white
		try {
			ImageIO.setUseCache(false);
			for (Entry entry: SonicBattleTextParserDictionary.STRING_TO_BYTE) {
				
				//Special Glyph
				if (byteArrayEquals(entry.bytes, lineBreak))
					DICTIONARY.put(entry.string, new Glyph(Glyph.LINE_BREAK));
				else if (byteArrayEquals(entry.bytes, black))
					DICTIONARY.put(entry.string, new Glyph(Glyph.BLACK));
				else if (byteArrayEquals(entry.bytes, red))
					DICTIONARY.put(entry.string, new Glyph(Glyph.RED));
				else if (byteArrayEquals(entry.bytes, blue))
					DICTIONARY.put(entry.string, new Glyph(Glyph.BLUE));
				else if (byteArrayEquals(entry.bytes, green))
					DICTIONARY.put(entry.string, new Glyph(Glyph.GREEN));
				else if (byteArrayEquals(entry.bytes, purple))
					DICTIONARY.put(entry.string, new Glyph(Glyph.PURPLE));
				else if (byteArrayEquals(entry.bytes, white))
					DICTIONARY.put(entry.string, new Glyph(Glyph.WHITE));
				
				//Normal Glyph
				else {
					URL resource = SonicBattleFont.class.getResource("sonicbattlefont/" + HexUtils.bytesToHexString(entry.bytes) + ".png");
					if (resource != null)
						DICTIONARY.put(entry.string, new Glyph(ImageIO.read(resource)));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/** True if two byte arrays are the same */
	private static final boolean byteArrayEquals(byte[] arg0, byte[] arg1) {
		if (arg0.length != arg1.length)
			return false;
		for (int i = 0; i < arg0.length; i++)
			if (arg0[i] != arg1[i])
				return false;
		return true;
	}
	
	
	//Glyph class
	/** A Glyph object represents a single Sonic Battle font glyph (for example the letter "A") that can be drawn to a
	 * <code>Graphics</code> object with <code>draw()</code> */
	protected static class Glyph {
		/** The width in pixels of the glyph */
		public final int width;
		/** The height in pixels of the glyph */
		public final int height;
		/** The bitmap of the glyph sprite: a boolean array */
		public final boolean[] bitmap;
		/** The 'Special Glyph' arg (for example, Color Red or Line Break). -1 if not a Special Glyph */
		public final int specialArg;
			//Special Glyph args enum
			/** Line break: Special Glyph */
			public static final int LINE_BREAK = 0;
			/** Color Black: Special Glyph */
			public static final int BLACK = 1;
			/** Color Red: Special Glyph */
			public static final int RED = 2;
			/** Color Blue: Special Glyph */
			public static final int BLUE = 3;
			/** Color Green: Special Glyph */
			public static final int GREEN = 4;
			/** Color Purple: Special Glyph */
			public static final int PURPLE = 5;
			/** Color White: Special Glyph */
			public static final int WHITE = 6;
		/** True if this Glyph is a Special Glyph */
		public boolean isSpecial() {
			return specialArg != -1;
		}
		
		/** Create a Glyph from a BufferedImage by iterating every pixel */
		public Glyph(BufferedImage bimg) {
			width = bimg.getWidth();
			height = bimg.getHeight();
			bitmap = new boolean[width * height];
			specialArg = -1;
			int i = 0;
			for (int y = 0; y < height; y++) //iterate every pixel, define the bitmap
				for (int x = 0; x < width; x++)
					bitmap[i++] = ((bimg.getRGB(x, y) & 0xff000000) >>> 24) > 0; //True: the ARGB alpha value is greater than 0
		}
		/** Construct a Special Glyph with a special Glyph arg (for example, Color Red or Line Break) */
		public Glyph(int specialArg) {
			this.specialArg = specialArg;
			width = -1;
			height = -1;
			bitmap = null;
		}
		
		/** Draw this Glyph to a <code>Graphics</code> object. (Do not use with Special Glyphs) */
		public void draw(Graphics g, int x, int y, int magnification) {
			int xPos = x, yPos = y;
			for (boolean bool: bitmap) {
				if (bool)
					drawPixel(g, xPos, yPos, magnification);
				xPos += magnification;
				if (xPos >= (x + (width * magnification))) {
					xPos = x;
					yPos += magnification;
				}
			}
		}
		/** Draw a pixel to a Graphics object (with magnification)
		 * e.g. if magnification is 1, draw 1x1, and if magnification is 2, draw 2x2 */
		public static final void drawPixel(Graphics g, int x, int y, int magnification) {
			for (int i = y; i < y + magnification; i++)
				g.drawLine(x, i, x + magnification - 1, i);
		}
	}
}