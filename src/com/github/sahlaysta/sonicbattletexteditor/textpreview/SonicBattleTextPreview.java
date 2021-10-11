package com.github.sahlaysta.sonicbattletexteditor.textpreview;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import com.github.sahlaysta.sonicbattletexteditor.textpreview.SonicBattleFont.Glyph;

/** SonicBattleTextPreview is a JComponent that displays scrollable Sonic Battle
 * text with the SonicBattleFont class. Has the methods <code>setText</code>
 * and <code>setPage</code> */
public class SonicBattleTextPreview extends JComponent {
	private static final long serialVersionUID = 1L;

	/** Construct SonicBattleTextPreview with a default magnification of 1 */
	public SonicBattleTextPreview() {
		this(1);
	}
	/** Construct SonicBattleTextPreview with the passed magnification value */
	public SonicBattleTextPreview(int magnification) {
		setMagnification(magnification);
	}
	
	//Paint component
	@Override
	protected void paintComponent(Graphics g) {
		if (glyphPages == null)
			return;

		//Draw the Sonic Battle text box frame
		g.drawImage(FRAME, 0, 0, FRAME.getWidth() * magnification, FRAME.getHeight() * magnification, null);
		
		//Draw the text of the current page with SonicBattleFont
		SonicBattleFont.drawText(glyphPages.get(page), g, 15 * magnification, 5 * magnification, magnification);
		
		//Draw the small blue scroll arrow in the bottom right, like in Sonic Battle's text boxes
		if (showArrow && page < getPages() - 1)
			g.drawImage(FRAME_ARROW, 218 * magnification, 37 * magnification, FRAME_ARROW.getWidth() * magnification, FRAME_ARROW.getHeight() * magnification, null);
	}
	/** When true, shows the small blue scroll arrow in the bottom right, like in Sonic Battle's text boxes */
	protected boolean showArrow = true;
	
	//Sonic Battle Font
	/** Glyph pages: List of SonicBattleFont Glyph arrays. One Glyph array is a single linescroll page */
	protected List<Glyph[]> glyphPages;
	/** The line scroll page of the Sonic Battle text */
	protected int page = 0;
	/** The size of the Sonic Battle text font and frame */
	protected int magnification = 1;
	/** Set the text of Sonic Battle Text Preview to a Sonic Battle String */
	public void setText(String text) {
		glyphPages = new ArrayList<>();
		List<Glyph> glyphs = SonicBattleFont.parse(text);
		
		/*
		 * Parse a List of Glyphs with SonicBattleFont and split it into
		 * separate arrays every two line breaks. Also preserve the color
		 * that was last set into the next array page.
		 */
		int lineBreakCount = 0; //line break count
		int lineBreakPos = 0; //keep track of the array index of the last line break
		int fontColor = Glyph.BLACK, tempFontColor = fontColor; //fontColor and tempFontColor, preserve colors
		for (int i = 0; i < glyphs.size(); i++) {
			Glyph glyph = glyphs.get(i);
			if (glyph.isSpecial()) {
				if (glyph.specialArg == Glyph.LINE_BREAK)
					lineBreakCount++;
				else
					tempFontColor = glyph.specialArg;
			}
			if (lineBreakCount >= 2) {
				/* Every time the line break count is 2, make a Glyph array of each Glyph that
				   has been iterated in the loop so far (from lineBreakPos to i), then add the Glyph array
				   to glyphPages, then set the line break count to 0 again.
				   Also, put an extra Glyph at the 0 index of the Glyph array, which is the
				   last set color (black if none) */
				Glyph[] glyphArray = new Glyph[i - lineBreakPos + 1];
				i++;
				glyphArray[0] = new Glyph(fontColor);
				for (int ii = 0; ii < glyphArray.length - 1; ii++)
					glyphArray[1 + ii] = glyphs.get(ii + lineBreakPos);
				glyphPages.add(glyphArray);
				lineBreakPos = i;
				lineBreakCount = 0;
				fontColor = tempFontColor;
			}
		}
		if (lineBreakPos < glyphs.size()) {
			/* Do the same thing as what would happen when the line break count
			 * reaches 2, but with the remaining Glyphs */
			Glyph[] glyphArray = new Glyph[glyphs.size() - lineBreakPos + 1];
			glyphArray[0] = new Glyph(fontColor);
			for (int ii = 0; ii < glyphArray.length - 1; ii++)
				glyphArray[1 + ii] = glyphs.get(ii + lineBreakPos);
			glyphPages.add(glyphArray);
		}
		
		setPage(page);
	}
	
	//Frame images
	/** The image of Sonic Battle's text box frame */
	protected static final BufferedImage FRAME;
	/** The image of Sonic Battle's text box frame arrow */
	protected static final BufferedImage FRAME_ARROW;
	static { //set the frame framearrow
		BufferedImage frameImg = null, arrowImg = null;
		try {
			frameImg = ImageIO.read(SonicBattleTextPreview.class.getResource("frame.png"));
			arrowImg = ImageIO.read(SonicBattleTextPreview.class.getResource("framearrow.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		FRAME = frameImg;
		FRAME_ARROW = arrowImg;
	}
	
	//Public methods
	/** Set the linescroll page of the Sonic Battle text */
	public void setPage(int page) {
		if (page >= getPages())
			page = getPages() - 1;
		if (page < 0)
			return;
		this.page = page;
		repaint();
	}
	/** Get the linescroll page of Sonic Battle Text Preview */
	public int getPage() {
		return page;
	}
	/** Return the amount of linescroll pages of Sonic Battle text */
	public int getPages() {
		return glyphPages == null ? 0 : glyphPages.size();
	}
	/** Set the size multiplier of the SonicBattleTextPreview */
	public void setMagnification(int magnification) {
		setPreferredSize(new Dimension(
				FRAME.getWidth() * magnification,
				FRAME.getHeight() * magnification));
		this.magnification = magnification;
		repaint();
	}
	/** Get the magnification of SonicBattleTextPreview */
	public int getMagnification() {
		return magnification;
	}
	/** If true: show the small blue scroll arrow in the bottom right of Sonic Battle's text boxes */
	public void setArrowVisible(boolean visible) {
		this.showArrow = visible;
		repaint();
	}
}