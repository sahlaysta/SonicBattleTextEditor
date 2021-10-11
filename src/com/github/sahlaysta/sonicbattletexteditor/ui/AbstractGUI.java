package com.github.sahlaysta.sonicbattletexteditor.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;

import com.github.sahlaysta.sonicbattletexteditor.textpreview.SonicBattleTextPreview;
import com.github.sahlaysta.sonicbattletexteditor.ui.components.ListTextBox;
import com.github.sahlaysta.sonicbattletexteditor.ui.components.RecentlyOpenedFilesJMenu;
import com.github.sahlaysta.sonicbattletexteditor.ui.components.RecentlyOpenedFilesJMenuListener;
import com.github.sahlaysta.sonicbattletexteditor.ui.localization.Localization;
import com.github.sahlaysta.sonicbattletexteditor.ui.util.WindowProperties;

/** Abstract GUI class with Sonic Battle Text Editor GUI layout and localization */
public abstract class AbstractGUI extends JFrame {
	private static final long serialVersionUID = 1L;
	
	//Constructor
	/** True if the UIManager successfully themed JFrame */
	public final boolean isThemed;
	/** Construct the GUI JFrame setting the default size and the default close operation, etc. */
	public AbstractGUI() {
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setLocationRelativeTo(null);
		setSize(350, 250);
		setMinimumSize(new Dimension(250, 200));
		setIconImage(new ImageIcon(getClass().getResource("icon.png")).getImage());
		addComponentListener(windowPropertiesRefresher);
		boolean themed = true;
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (
			ClassNotFoundException
			| InstantiationException
			| IllegalAccessException
			| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
			themed = false;
		}
		this.isThemed = themed;
	}
	
	// GUI Localization
	/** A HashMap of <b>String</b>s (keys) to <b>SetTextRunnable</b>s. Used by
	 * the <code>setLanguage</code> method to set the language */
	protected Map<String, SetTextRunnable> localizableComponents = new HashMap<>();
	/** The SetTextRunnable interface, used to set the text of a GUI component */
	protected static interface SetTextRunnable {
		/** This method takes the <b>value</b> String parameter and set the text of the registered Component */
		abstract void setTextAction(String value);
	}
	/** A String-String localization map parsed from Localization.json */
	protected Map<String, String> localization;
	/** Returns a String from the localization */
	public String getString(String key) { return localization.get(key); }
	/** Sets the localization of the GUI to the passed key <b>language</b> */
	public void setLanguage(String language) {
		this.localization = Localization.getLanguageMap(language);
		for (Entry<String, SetTextRunnable> localizableComponent: localizableComponents.entrySet())
			localizableComponent.getValue().setTextAction(localization.get(localizableComponent.getKey()));
		languageChanged();
	}
	/** Called when the language is changed in Sonic Battle Text Editor GUI */
	abstract void languageChanged();
	
	// GUI Component
	/** The ListTextBox component instance of the GUI */
	protected ListTextBox listTextBox;
	/** <i>(Menu bar)</i> The JMenuBar instance of the GUI */
	protected JMenuBar menuBar;
		/** <i>(Menu bar)</i> <b>file</b>: JMenu instance under the JMenuBar */
		protected JMenu file;
			/** <i>(Menu bar)</i> <b>open</b>: JMenuItem instance under the <b>file</b> JMenu */
			protected JMenuItem open; 
				/** <i>(Menu bar)</i> Called when the <b>open</b> JMenuItem is clicked */
				protected abstract void open_Click();
			/** <i>(Menu bar)</i> <b>openRecent</b>: JMenu instance under the <b>file</b> JMenu */
			protected RecentlyOpenedFilesJMenu openRecent;
				/** <i>(Menu bar)</i> Called when an <b>openRecent</b> JMenuItem is clicked, passing the File */
				protected abstract void openRecent_Click(File file);
			/** <i>(Menu bar)</i> <b>save</b>: JMenuItem instance under the <b>file</b> JMenu */
			protected JMenuItem save;
				/** <i>(Menu bar)</i> Called when the <b>save</b> JMenuItem is clicked */
				protected abstract void save_Click();
			/** <i>(Menu bar)</i> <b>saveAs</b>: JMenuItem instance under the <b>file</b> JMenu */
			protected JMenuItem saveAs;
				/** <i>(Menu bar)</i> Called when the <b>saveAs</b> JMenuItem is clicked */
				protected abstract void saveAs_Click();
			/** <i>(Menu bar)</i> <b>close</b>: JMenuItem instance under the <b>file</b> JMenu */
			protected JMenuItem close;
				/** <i>(Menu bar)</i> Called when the <b>close</b> JMenuItem is clicked */
				protected abstract void close_Click();
		/** <i>(Menu bar)</i> <b>edit</b>: JMenu instance under the JMenuBar */
		protected JMenu edit;
			/** <i>(Menu bar)</i> <b>undo</b>: JMenuItem instance under the <b>edit</b> JMenu */
			protected JMenuItem undo;
			/** <i>(Menu bar)</i> <b>redo</b>: JMenuItem instance under the <b>edit</b> JMenu */
			protected JMenuItem redo;
			/** <i>(Menu bar)</i> <b>upOne</b>: JMenuItem instance under the <b>edit</b> JMenu */
			protected JMenuItem upOne;
			/** <i>(Menu bar)</i> <b>downOne</b>: JMenuItem instance under the <b>edit</b> JMenu */
			protected JMenuItem downOne;
			/** <i>(Menu bar)</i> <b>importLines</b>: JMenuItem instance under the <b>edit</b> JMenu */
			protected JMenuItem importLines;
				/** <i>(Menu bar)</i> Called when the <b>importLines</b> JMenuItem is clicked */
				protected abstract void importLines_Click();
			/** <i>(Menu bar)</i> <b>exportLines</b>: JMenuItem instance under the <b>edit</b> JMenu */
			protected JMenuItem exportLines;
				/** <i>(Menu bar)</i> Called when the <b>exportLines</b> JMenuItem is clicked */
				protected abstract void exportLines_Click();
		/** <i>(Menu bar)</i> <b>search</b>: JMenu instance under the JMenuBar */
		protected JMenu search;
			/** <i>(Menu bar)</i> <b>goTo</b>: JMenuItem instance under the <b>search</b> JMenu */
			protected JMenuItem goTo;
				/** <i>(Menu bar)</i> Called when the <b>goTo</b> JMenuItem is clicked */
				protected abstract void goTo_Click();
			/** <i>(Menu bar)</i> <b>find</b>: JMenuItem instance under the <b>search</b> JMenu */
			protected JMenuItem find;
				/** <i>(Menu bar)</i> Called when the <b>find</b> JMenuItem is clicked */
				protected abstract void find_Click();
			/** <i>(Menu bar)</i> <b>errorLines</b>: JMenuItem instance under the <b>search</b> JMenu */
			protected JMenuItem errorLines;
				/** <i>(Menu bar)</i> Called when the <b>errorLines</b> JMenuItem is clicked */
				protected abstract void errorLines_Click();
		/** <i>(Menu bar)</i> <b>view</b>: JMenu instance under the JMenuBar */
		protected JMenu view;
			/** <i>(Menu bar)</i> <b>changeLang</b>: JMenuItem instance under the <b>view</b> JMenu */
			protected JMenuItem changeLang;
				/** <i>(Menu bar)</i> Called when the <b>changeLang</b> JMenuItem is clicked */
				protected abstract void changeLang_Click();
			/** <i>(Menu bar)</i> <b>textPreview</b>: JCheckBoxMenuItem instance under the <b>view</b> JMenu */
			protected JCheckBoxMenuItem textPreview;
		/** <i>(Menu bar)</i> <b>help</b>: JMenu instance under the JMenuBar */
		protected JMenu help;
			/** <i>(Menu bar)</i> <b>about</b>: JMenuItem instance under the <b>help</b> JMenu */
			protected JMenuItem about;
				/** <i>(Menu bar)</i> Called when the <b>about</b> JMenuItem is clicked */
				protected abstract void about_Click();
	/** The Sonic Battle Text Preview dialog window */
	protected TextPreviewWindow textPreviewWindow;
		/** Method to save an Image to a file. Called by the Sonic Battle Text Preview window */
		protected abstract void saveImage(RenderedImage im);
	
	/** Initialize the components of the GUI */
	public void initComponents() {
		localizableComponents = new HashMap<>();
		localizableComponents.put("title",new SetTextRunnable(){public void setTextAction(String s){setTitle(s);}});
		
		//ListTextBox
		listTextBox = new ListTextBox();
		
		//Text preview window
		textPreviewWindow = new TextPreviewWindow(this);
		
		//Menubar
		menuBar = new JMenuBar();
		
			//File JMenu
			file = new JMenu();
			open = new JMenuItem();
			openRecent = new RecentlyOpenedFilesJMenu();
			save = new JMenuItem();
			saveAs = new JMenuItem();
			close = new JMenuItem();
			file.add(open);
			file.add(openRecent);
			file.add(save);
			file.add(saveAs);
			file.addSeparator();
			file.add(close);
			
			//Edit JMenu
			edit = new JMenu();
			undo = new JMenuItem();
			redo = new JMenuItem();
			upOne = new JMenuItem();
			downOne = new JMenuItem();
			importLines = new JMenuItem();
			exportLines = new JMenuItem();
			edit.add(undo);
			edit.add(redo);
			edit.add(upOne);
			edit.add(downOne);
			edit.addSeparator();
			edit.add(importLines);
			edit.add(exportLines);
			
			//Search JMenu
			search = new JMenu();
			goTo = new JMenuItem();
			find = new JMenuItem();
			errorLines = new JMenuItem();
			search.add(goTo);
			search.add(find);
			search.add(errorLines);
			
			//View JMenu
			view = new JMenu();
			changeLang = new JMenuItem();
			textPreview = new JCheckBoxMenuItem();
			view.add(changeLang);
			view.add(textPreview);
			
			//Help JMenu
			help = new JMenu();
			about = new JMenuItem();
			help.add(about);
			
		menuBar.add(file);
		menuBar.add(edit);
		menuBar.add(search);
		menuBar.add(view);
		menuBar.add(help);
		
		//Setup shortcuts
		int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		open.setAccelerator(KeyStroke.getKeyStroke('O', ctrl));
		save.setAccelerator(KeyStroke.getKeyStroke('S', ctrl));
		saveAs.setAccelerator(KeyStroke.getKeyStroke('S', ctrl | InputEvent.ALT_DOWN_MASK));
		close.setAccelerator(KeyStroke.getKeyStroke('W', ctrl));
		undo.setAccelerator(KeyStroke.getKeyStroke('Z', ctrl));
		redo.setAccelerator(KeyStroke.getKeyStroke('Y', ctrl));
		upOne.setAccelerator(KeyStroke.getKeyStroke(38, ctrl)); //38 is up
		downOne.setAccelerator(KeyStroke.getKeyStroke(40, ctrl)); //40 is down
		goTo.setAccelerator(KeyStroke.getKeyStroke('G', ctrl));
		find.setAccelerator(KeyStroke.getKeyStroke('F', ctrl));
		about.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		
		//Setup action listeners
		open.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){open_Click();}});
		openRecent.addRecentlyOpenedFilesJMenuListener(new RecentlyOpenedFilesJMenuListener(){public void actionPerformed(ActionEvent e,File f){openRecent_Click(f);}});
		save.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){save_Click();}});
		saveAs.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){saveAs_Click();}});
		close.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){close_Click();}});
		undo.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){listTextBox.undo();}});
		redo.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){listTextBox.redo();}});
		upOne.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){listTextBox.setSelectedIndex(listTextBox.getSelectedIndex() - 1);}});
		downOne.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){listTextBox.setSelectedIndex(listTextBox.getSelectedIndex() + 1);}});
		importLines.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){importLines_Click();}});
		exportLines.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){exportLines_Click();}});
		goTo.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){goTo_Click();}});
		find.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){find_Click();}});
		errorLines.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){errorLines_Click();}});
		changeLang.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){changeLang_Click();}});
		about.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){about_Click();}});
		textPreview.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (textPreview.isSelected()) {
					if (listTextBox.getLength() > 0)
						textPreviewWindow.setText(listTextBox.getSelectedString(), listTextBox.getSelectedIndex());
					textPreviewWindow.setVisible(true);
				} else {
					textPreviewWindow.setVisible(false);
				}
			}
		});
		
		//Set Localizable Components
		localizableComponents.put("file",new SetTextRunnable(){public void setTextAction(String s){file.setText(s);textPreviewWindow.file.setText(s);}});
		localizableComponents.put("open",new SetTextRunnable(){public void setTextAction(String s){open.setText(s);}});
		localizableComponents.put("openRecent",new SetTextRunnable(){public void setTextAction(String s){openRecent.setText(s);}});
		localizableComponents.put("save",new SetTextRunnable(){public void setTextAction(String s){save.setText(s);}});
		localizableComponents.put("saveAs",new SetTextRunnable(){public void setTextAction(String s){saveAs.setText(s);}});
		localizableComponents.put("close",new SetTextRunnable(){public void setTextAction(String s){close.setText(s);}});
		localizableComponents.put("edit",new SetTextRunnable(){public void setTextAction(String s){edit.setText(s);}});
		localizableComponents.put("undo",new SetTextRunnable(){public void setTextAction(String s){undo.setText(s);}});
		localizableComponents.put("redo",new SetTextRunnable(){public void setTextAction(String s){redo.setText(s);}});
		localizableComponents.put("upOne",new SetTextRunnable(){public void setTextAction(String s){upOne.setText(s);}});
		localizableComponents.put("downOne",new SetTextRunnable(){public void setTextAction(String s){downOne.setText(s);}});
		localizableComponents.put("copy",new SetTextRunnable(){public void setTextAction(String s){textPreviewWindow.copy.setText(s);}});
		localizableComponents.put("importLines",new SetTextRunnable(){public void setTextAction(String s){importLines.setText(s);}});
		localizableComponents.put("exportLines",new SetTextRunnable(){public void setTextAction(String s){exportLines.setText(s);}});
		localizableComponents.put("search",new SetTextRunnable(){public void setTextAction(String s){search.setText(s);}});
		localizableComponents.put("goTo",new SetTextRunnable(){public void setTextAction(String s){goTo.setText(s);}});
		localizableComponents.put("find",new SetTextRunnable(){public void setTextAction(String s){find.setText(s);}});
		localizableComponents.put("errorLines",new SetTextRunnable(){public void setTextAction(String s){errorLines.setText(s);}});
		localizableComponents.put("view",new SetTextRunnable(){public void setTextAction(String s){view.setText(s);textPreviewWindow.view.setText(s);}});
		localizableComponents.put("changeLang",new SetTextRunnable(){public void setTextAction(String s){changeLang.setText(s);}});
		localizableComponents.put("textPreview",new SetTextRunnable(){public void setTextAction(String s){textPreview.setText(s);textPreviewWindow.setTitle(s);}});
		localizableComponents.put("saveImg",new SetTextRunnable(){public void setTextAction(String s){textPreviewWindow.save.setText(s);}});
		localizableComponents.put("copyImg",new SetTextRunnable(){public void setTextAction(String s){textPreviewWindow.copy.setText(s);}});
		localizableComponents.put("showArrow",new SetTextRunnable(){public void setTextAction(String s){textPreviewWindow.showArrow.setText(s);}});
		localizableComponents.put("help",new SetTextRunnable(){public void setTextAction(String s){help.setText(s);}});
		localizableComponents.put("about",new SetTextRunnable(){public void setTextAction(String s){about.setText(s);}});

		//Register components
		add(listTextBox);
		setJMenuBar(menuBar);
	}
	
	//Window properties
	/** The WindowProperties of the Sonic Battle Text Editor GUI window. Used when saving window preference such as size, location etc. */
	private WindowProperties windowProperties;
	/** Return the Window Properties of the JFrame window */
	public WindowProperties getWindowProperties() { return windowProperties; }
	/** A ComponentListener added to this JFrame.
	 * Its task is to refresh WindowProperties when the window is resized or moved.
	 * Get the WindowProperties with <code>getWindowProperties()</code> */
	protected ComponentListener windowPropertiesRefresher = new ComponentListener() {
		public final void componentHidden(ComponentEvent e){refreshWindowProperties();}
		public final void componentMoved(ComponentEvent e){refreshWindowProperties();}
		public final void componentResized(ComponentEvent e){refreshWindowProperties();}
		public final void componentShown(ComponentEvent e){refreshWindowProperties();}
		void refreshWindowProperties() {
			windowProperties = WindowProperties.fromFrame(AbstractGUI.this, windowProperties);
		}
	};
	
	
	//Text box right click menu
	/** Create the right-click context menu of text box, with "Copy", "Cut", "Paste", "Select all", "Undo", "Redo" */
	protected JPopupMenu createTextBoxRightClickMenu(JTextComponent jtc, ActionListener undoAction, ActionListener redoAction) {
		JPopupMenu menu = new JPopupMenu();
		
		int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		
		JMenuItem cut = new JMenuItem(getString("cut"));
		cut.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){jtc.cut();}});
		cut.setAccelerator(KeyStroke.getKeyStroke('X', ctrl));
		
		JMenuItem copy = new JMenuItem(getString("copy"));
		copy.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){jtc.copy();}});
		copy.setAccelerator(KeyStroke.getKeyStroke('C', ctrl));
		
		JMenuItem paste = new JMenuItem(getString("paste"));
		paste.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){jtc.paste();}});
		paste.setAccelerator(KeyStroke.getKeyStroke('V', ctrl));
		
		JMenuItem selectAll = new JMenuItem(getString("selectAll"));
		selectAll.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e){jtc.selectAll();}});
		selectAll.setAccelerator(KeyStroke.getKeyStroke('A', ctrl));
		
		JMenuItem undo = new JMenuItem(getString("undo"));
		undo.addActionListener(undoAction);
		undo.setAccelerator(KeyStroke.getKeyStroke('Z', ctrl));
		
		JMenuItem redo = new JMenuItem(getString("redo"));
		redo.addActionListener(redoAction);
		redo.setAccelerator(KeyStroke.getKeyStroke('Y', ctrl));
		
		menu.add(cut);
		menu.add(copy);
		menu.add(paste);
		menu.add(selectAll);
		menu.addSeparator();
		menu.add(undo);
		menu.add(redo);
		return menu;
	}
	/** Find JTextComponent in a Container, and set its right-click popup menu of text box with "Cut" and "Copy" etc. */
	protected void setupRightClickMenu(Container container) {
		for (Component c: getAllComponents(container)) {
			if (c instanceof JTextComponent) {
				//add UndoManager and right-click popup menu to JTextComponent
				
				JTextComponent jtc = (JTextComponent)c;
				UndoManager um = new UndoManager();
				jtc.getDocument().addUndoableEditListener(um);
				jtc.setComponentPopupMenu(
						createTextBoxRightClickMenu(
								jtc,
								new ActionListener(){@Override public void actionPerformed(ActionEvent e){if(um.canUndo())um.undo();}},
								new ActionListener(){@Override public void actionPerformed(ActionEvent e){if(um.canRedo())um.redo();}}
							));
				
				//Ctrl Z and Ctrl Y inputs
				jtc.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "undo");
				jtc.getActionMap().put("undo", new AbstractAction() {
					private static final long serialVersionUID = 1L;
					@Override
					public void actionPerformed(ActionEvent e) {
						if (um.canUndo())
							um.undo();
					}
				});
				jtc.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "redo");
				jtc.getActionMap().put("redo", new AbstractAction() {
					private static final long serialVersionUID = 1L;
					@Override
					public void actionPerformed(ActionEvent e) {
						if (um.canRedo())
							um.redo();
					}
				});
			}
		}
	}
	/** Get all Components and sub Components of a Container */
	protected Collection<Component> getAllComponents(Container container) {
		return addAllSubComponents(container, new ArrayList<>());
	}
	private final Collection<Component> addAllSubComponents(Container container, Collection<Component> components) {
		components.add(container);
		for (Component c: container.getComponents()) {
			components.add(c);
			if (c instanceof Container)
				addAllSubComponents((Container)c, components);
		}
		return components;
	}
	
	
	//Text preview window
	/** The Sonic Battle Text preview window class that has the SonicBattleTextPreview JComponent
	 * and also has buttons and page indicator */
	protected class TextPreviewWindow extends JDialog {
		private static final long serialVersionUID = 1L;
		
		//Constructor
		/** SonicBattleTextPreview JComponent that displays Sonic Battle text */
		protected SonicBattleTextPreview sbtp = new SonicBattleTextPreview();
		/** Construct TextPreviewWindow with the passed JFrame as its modality owner */
		public TextPreviewWindow(JFrame jFrame) {
			super(jFrame);
			setResizable(false);
			setLayout(new BorderLayout());
			add(sbtp, BorderLayout.CENTER);
			add(bottomPanel(), BorderLayout.PAGE_END);
			downButton.setEnabled(false);
			upButton.setEnabled(false);
			setJMenuBar(menuBar());
			pack();
			setLocationRelativeTo(jFrame);
			addWindowListener(new WindowAdapter() {
			    @Override
			    public void windowClosing(WindowEvent e) {
			    	textPreview.setSelected(false);
			    }
			});
		}
		/** Construct TextPreviewWindow with no modality owner */
		public TextPreviewWindow() {
			this(null);
		}
		
		//Layout
		/** The ListTextBox index of the currently displayed Sonic Battle line (used to save page number) */
		protected int index;
		/** JLabel that shows the line scroll page number in the Sonic Battle text preview window */
		protected JLabel pageLabel;
		/** Create the bottom JPanel with label and buttons */
		protected JPanel bottomPanel() {
			JPanel masterPanel = new JPanel();
			masterPanel.setLayout(new GridLayout(1,2));
			masterPanel.add(labelPanel());
			masterPanel.add(buttonPanel());
			return masterPanel;
		}
		/** Create the label JPanel with the page number indicator */
		protected JPanel labelPanel() {
			JPanel labelPanel = new JPanel();
			labelPanel.setLayout(new BorderLayout());
			pageLabel = new JLabel("");
			labelPanel.add(pageLabel, BorderLayout.PAGE_END);
			labelPanel.setBorder(new EmptyBorder(0,5,3,0));
			return labelPanel;
		}
		/** JButton that moves the page down in SonicBattleTextPreview */
		protected JButton downButton;
		/** JButton that moves the page up in SonicBattleTextPreview */
		protected JButton upButton;
		/** Create the JPanel of the up and down buttons */
		protected JPanel buttonPanel() {
			JPanel masterPanel = new JPanel();
			masterPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new GridLayout(1,2,5,0));
			buttonPanel.setPreferredSize(new Dimension(100, 20));
			downButton = new JButton("▼");
			downButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					pageDown();
				}
			});
			upButton = new JButton("▲");
			upButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					pageUp();
				}
			});
			buttonPanel.add(upButton);
			buttonPanel.add(downButton);
			masterPanel.add(buttonPanel);
			return masterPanel;
		}
		//JMenuBar
		/** The File JMenu with copy and save options */
		protected JMenu file;
		/** The save JMenuItem to save the text preview as an image file */
		protected JMenuItem save;
		/** The copy JMenuItem to copy the text preview image to the clipboard */
		protected JMenuItem copy;
		/** The View JMenu with window size options */
		protected JMenu view;
		/** The JCheckBoxMenuItem to show the blue arrow of SonicBattleTextPreview */
		protected JCheckBoxMenuItem showArrow;
		/** JMenuBar with the save and view options */
		protected JMenuBar menuBar() {
			JMenuBar jmb = new JMenuBar();
			file = new JMenu();
			save = new JMenuItem();
			save.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) { //save SonicBattleTextPreview to image file
					Dimension d = sbtp.getSize();
					BufferedImage bimg = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
					sbtp.paintAll(bimg.createGraphics());
					saveImage(bimg);
				}
			});
			copy = new JMenuItem();
			copy.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) { //copy SonicBattleTextPreview image to clipboard
					Dimension d = sbtp.getSize();
					BufferedImage bimg = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
					sbtp.paintAll(bimg.createGraphics());
					Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new Transferable() {
						@Override
						public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
							return bimg;
						}
						@Override
						public DataFlavor[] getTransferDataFlavors() {
							return new DataFlavor[] { DataFlavor.imageFlavor };
						}
						@Override
						public boolean isDataFlavorSupported(DataFlavor flavor) {
							return true;
						}
					}, null);
				}
			});
			file.add(save);
			file.add(copy);
			view = new JMenu();
			showArrow = new JCheckBoxMenuItem();
			showArrow.setSelected(true);
			showArrow.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					sbtp.setArrowVisible(showArrow.isSelected());
				}
			});
			view.add(showArrow);
			view.addSeparator();
			for (int i = 1; i < 5; i++) { //Add size options under the 'view' JMenu (1X, 2X, 3X etc...)
				final int magnification = i;
				JCheckBoxMenuItem sizeItem = new JCheckBoxMenuItem(i + "X");
				sizeItem.setName("sizeItem");
				sizeItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						for (Component c: view.getMenuComponents()) //set all the other size options to unchecked when one size option is checked
							if ("sizeItem".equals(c.getName()))
								((JCheckBoxMenuItem)c).setSelected(false);
						sizeItem.setSelected(true);
						if (sbtp.getMagnification() == magnification)
							return;
						sbtp.setMagnification(magnification);
						pack();
					}
				});
				view.add(sizeItem);
				if (i == 1)
					sizeItem.setSelected(true);
			}
			jmb.add(file);
			jmb.add(view);
			return jmb;
		}
		
		//
		/** Set the text of the Sonic Battle Text preview, and use the index in ListTextBox to save the page number */
		public void setText(String text, int index) {
			if (this.index != index)
				sbtp.setPage(0);
			this.index = index;
			sbtp.setText(text.isEmpty() ? " " : text);
			refreshPageLabel();
			upButton.setEnabled(true);
			downButton.setEnabled(true);
		}
		/** Move up one line scroll page in the Sonic Battle text preview */
		public void pageUp() {
			sbtp.setPage(sbtp.getPage() - 1);
			refreshPageLabel();
		}
		/** Move down one line scroll page in the Sonic Battle text preview */
		public void pageDown() {
			sbtp.setPage(sbtp.getPage() + 1);
			refreshPageLabel();
		}
		/** Refresh the page label that shows the line scroll page number in the Sonic Battle text preview, example: "page 1/3" */
		protected void refreshPageLabel() {
			pageLabel.setText((1 + sbtp.getPage()) + "/" + sbtp.getPages());
		}
	}
}