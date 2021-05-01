package sbte.GUI;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.ScrollBarUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

public class DarkTheme {
	public static final Color[] darkColors = new Color[] {
			Color.BLACK, //background
			Color.DARK_GRAY,
			Color.LIGHT_GRAY //foreground
	};
	public static void setDarkPanel(JPanel e) {
		e.setBackground(darkColors[0]);
		e.setForeground(darkColors[1]);
	}
	public static void setDarkTitledBorder(TitledBorder e) {
		e.setTitleColor(darkColors[2]);
	}
	public static void setDarkSplitPane(JSplitPane e) {
		e.setBackground(darkColors[0]);
		e.setForeground(darkColors[1]);
		e.setUI(new BasicSplitPaneUI() 
		{
		    @Override
		    public BasicSplitPaneDivider createDefaultDivider() 
		    {
		        return new BasicSplitPaneDivider(this) 
		        {                
		            public void setBorder(Border b) {}

		            @Override
		            public void paint(Graphics g) 
		            {
		                g.setColor(darkColors[1]);
		                g.fillRect(0, 0, getSize().width, getSize().height);
		                super.paint(g);
		            }
		        };
		    }
		});
	}
	public static void setDarkTextArea(JTextArea e) {
		e.setBackground(darkColors[0]);
		e.setForeground(darkColors[2]);
		e.setCaretColor(darkColors[2]);
	}
	public static void setDarkScroll(JScrollPane e) {
		e.setBackground(darkColors[0]);
		e.getViewport().setBackground(darkColors[0]);
		
		e.getVerticalScrollBar().setUI(new DarkScroll());
		e.getHorizontalScrollBar().setUI(new DarkScroll());
	}
	private static class DarkScroll extends BasicScrollBarUI{
		@Override
	    protected void configureScrollBarColors() {
	        this.thumbColor = darkColors[0];
	        this.trackColor = darkColors[0];
	        this.thumbHighlightColor = darkColors[2];
	    }
		@Override
	    protected JButton createDecreaseButton(int orientation) {
	        JButton button = super.createDecreaseButton(orientation);
	        button.setBackground(darkColors[0]);
	        button.setForeground(darkColors[1]);
	        return button;
	    }
		@Override
	    protected JButton createIncreaseButton(int orientation) {
	        JButton button = super.createDecreaseButton(orientation);
	        button.setBackground(darkColors[0]);
	        button.setForeground(darkColors[1]);
	        return button;
	    }
	}
	public static void setDarkList(JList e) {
		e.setBackground(darkColors[0]);
		e.setForeground(darkColors[2]);
	}
}
