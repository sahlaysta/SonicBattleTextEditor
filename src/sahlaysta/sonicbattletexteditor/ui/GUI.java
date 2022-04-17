package sahlaysta.sonicbattletexteditor.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.github.sahlaysta.jsonevent.JsonEventArgs;
import com.github.sahlaysta.jsonevent.JsonEventReader;
import com.github.sahlaysta.jsonevent.JsonEventType;
import com.github.sahlaysta.jsonevent.JsonEventWriter;
import sahlaysta.sonicbattletexteditor.romparser.HexUtils;
import sahlaysta.sonicbattletexteditor.romparser.SonicBattleLine;
import sahlaysta.sonicbattletexteditor.romparser.SonicBattleLineGroup;
import sahlaysta.sonicbattletexteditor.romparser.SonicBattleLineGroupCollection;
import sahlaysta.sonicbattletexteditor.romparser.SonicBattleROMDialogueReader;
import sahlaysta.sonicbattletexteditor.romparser.SonicBattleTextParser;
import sahlaysta.sonicbattletexteditor.ui.components.ListTextBoxListener;
import sahlaysta.sonicbattletexteditor.ui.localization.Localization;
import sahlaysta.sonicbattletexteditor.ui.util.FileChooser;
import sahlaysta.sonicbattletexteditor.ui.util.ChoicePromptWindow;
import sahlaysta.sonicbattletexteditor.ui.util.MessageBox;
import sahlaysta.sonicbattletexteditor.ui.util.NumberPromptWindow;
import sahlaysta.sonicbattletexteditor.ui.util.WindowProperties;

/** The GUI of Sonic Battle Text Editor */
public final class GUI extends AbstractGUI {
	private static final long serialVersionUID = 1L;
	
	//Construction
	private final MessageBox msgBox = new MessageBox();
	public GUI() {
		initComponents();
		
		//Set MessageBox
		msgBox.setParentComponent(this);
		
		//Load preferences
		try {
			loadPreferences();
		} catch (Exception e) {
			System.out.println("Error reading preferences:");
			e.printStackTrace();
		}
		
		//Set language
		if (language_Pref == null || !Localization.LANGUAGE_MAPS.containsKey(language_Pref))
			language_Pref = Locale.getDefault().toLanguageTag();//Try set localization to system language
		setLanguage(language_Pref);
		
		//Set ListTextBox layout
		setupListTextBox();
		
		//Save before closing prompt + save preferences on close
		addWindowListener(new WindowAdapter() {
		    @Override
		    public void windowClosing(WindowEvent e) {
		    	if (listTextBox.hasUnsavedChanges() &&
		    		!msgBox.showYesNoPrompt(
		    				getString("unsaved"),
		    				getString("close"),
		    				getString("yes"),
		    				getString("no"))) {
		    		return;
		    	}
		    	trySavePreferences();
		    	System.exit(0);
		    }
		});
		
		//Disabled, until a ROM is opened
		save.setEnabled(false);
		saveAs.setEnabled(false);
		edit.setEnabled(false);
		search.setEnabled(false);
	}
	
	//ListTextBox layout
	/* Set up the ListTextBox layout */
	private ListTextBoxListener listTextBoxListener;
	private boolean ignoreListTextBoxListener = false;
	private void setupListTextBox() {
		listTextBox.setBorder(new EmptyBorder(0, 5, 5, 5));
		TitledBorder tb = new TitledBorder("");
		((JComponent)listTextBox.getTopComponent()).setBorder(tb);
		Color titledBorderDefaultColor = tb.getTitleColor();
		
		//ListTextBoxListener
		listTextBoxListener = new ListTextBoxListener() {
			
			/* listTextBoxListener: 
			 * Every time text is entered/edited in the text box, validate the text to see if it
			 * is valid for Sonic Battle. If it is invalid, set the font red, and display the error
			 * message at the top */
			
			/** The errorMap: HashMap of Integers to Characters where Integer is the 
			 * JList index of the error, and Character is the invalid char causing the error */
			final Map<Integer, Character> errorMap = new HashMap<>();
			
			/** Update errorMap and sets the color of the font to red
			 * if an invalid String is entered */
			@Override
			public void textBoxChanged(final int index, final String str) {
				if (ignoreListTextBoxListener)
					return;
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						int errorPos = SonicBattleTextParser.validate(str);
						if (errorPos == -1) {
							listTextBox.removeColor(index);
							errorMap.remove(index);
							if (textPreviewWindow.isVisible())
								textPreviewWindow.setText(str, index);
						}
						else {
							listTextBox.setColor(index, Color.RED);
							errorMap.put(index, str.charAt(errorPos));
						}
						listTextBox.repaint();
						setTitle(listTextBox.hasUnsavedChanges() ? getString("title") + "*" : getString("title")); //'edited' asterisk
						indexChanged(listTextBox.getSelectedIndex());
					}
				});
			}
			
			//Update the "Line #" text shown at the top
			@Override
			public void indexChanged(int index) {
				if (errorMap.containsKey(index)) { // Invalid character entered, show error message
					String errorChar = Character.toString(errorMap.get(index));
					switch (errorChar) { //escape
					case "\t": errorChar = "\\t"; break;
					case "\n": errorChar = "\\n"; break;
					case "\r": errorChar = "\\r"; break;
					}
					tb.setTitle(String.format(getString("invalidChar"), errorChar));
					tb.setTitleColor(Color.RED);
					copyHex.setEnabled(false);
				}
				else if (index < 0) { // No selected index
					tb.setTitle(getString("textEdit"));
					tb.setTitleColor(titledBorderDefaultColor);
				}
				else { //Normal, "Line X"
					tb.setTitle(String.format(getString("lineNum"), 1 + index));
					tb.setTitleColor(titledBorderDefaultColor);
					if (textPreviewWindow.isVisible())
						textPreviewWindow.setText(listTextBox.getString(index), index);
					copyHex.setEnabled(true);
				}
				listTextBox.repaint();
			}
		};
		listTextBoxListener.indexChanged(-1);
		listTextBox.addListTextBoxListener(listTextBoxListener);
		
		//set ListTextBox text box right-click popup menu
		JTextArea jta = listTextBox.getJTextArea();
		jta.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (jta.isEnabled() && e.isPopupTrigger())
					textBoxRightClickMenu.show(jta, e.getX(), e.getY());
			}
		});
	}
	private JPopupMenu textBoxRightClickMenu;
	private JMenuItem copyHex;
	@Override
	protected final void languageChanged() { //update right-click menus when the language is set in Sonic Battle Text Editor GUI
		//set the ListTextBox text box right-click popup menu with "Cut", "Copy" etc.
		textBoxRightClickMenu = createTextBoxRightClickMenu(
						listTextBox.getJTextArea(),
						new ActionListener(){@Override public void actionPerformed(ActionEvent e){listTextBox.undo();}},
						new ActionListener(){@Override public void actionPerformed(ActionEvent e){listTextBox.redo();}}
					);
		
		//Set the right-click popup menu for right-clicking an item on the list, with "Copy HEX to clipboard" JMenuItem
		JPopupMenu listRightClickMenu = new JPopupMenu();
		copyHex = new JMenuItem(getString("copyHex"));
		copyHex.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//Copy hex action: Set the text of the clipboard to Sonic Battle HEX of the selected String in ListTextBox
				Toolkit.getDefaultToolkit()
					.getSystemClipboard().setContents(
							new StringSelection(HexUtils.bytesToHexString(SonicBattleTextParser.parse(listTextBox.getSelectedString())) + "FEFF"),
							null);
			}
		});
		listRightClickMenu.add(copyHex);
		listTextBox.setListContextMenu(listRightClickMenu);
		
		//Window title edited asterisk
		setTitle(listTextBox.hasUnsavedChanges() ? getString("title") + "*" : getString("title"));
	}
	
	
	//Open ROM
	/** The currently opened Sonic Battle ROM */
	private File openedRom;
	/** The SonicBattleLineGroupCollection parsed from the currently opened Sonic Battle ROM */
	private SonicBattleLineGroupCollection sonicBattleLineGroupCollection;
	/** Return Sonic Battle Text Editor GUI's currently opened Sonic Battle ROM. Returns null if there is none */
	public File getCurrentlyOpenedRom() { return openedRom; }
	/** Returns true if a ROM is currently opened in Sonic Battle Text Editor GUI. */
	public boolean hasRomOpen() { return openedRom != null; }
	/** Main method to open a ROM in Sonic Battle Text Editor GUI */
	public final void openRom(File file) {
		openRecent.addFile(file);
		try {
			//Read and parse all dialogue lines in the Sonic Battle ROM
			List<String> lines = new ArrayList<>(5481);
			for (SonicBattleLineGroup sblg: sonicBattleLineGroupCollection = SonicBattleROMDialogueReader.parseUSARom(file))
				for (SonicBattleLine sbl: sblg)
					lines.add(sbl.getContent());
			ignoreListTextBoxListener = true;
			listTextBox.populate(lines);
			ignoreListTextBoxListener = false;
			openedRom = file;
			GUI.this.file.setEnabled(false);
			new SwingWorker<Object, Object>() {
				@Override
				protected Object doInBackground() throws Exception {
					//Set the ROM of the SonicBattleLineGroupCollection in the background
					try {
						sonicBattleLineGroupCollection.setRom(file);
					} catch (IOException e) {
						close_Click();
						e.printStackTrace();
						msgBox.showError(
								file + "\n\n" + getString("openErr") + " " + e.getMessage(),
								getString("open"),
								getString("ok"));
					}
					GUI.this.file.setEnabled(true);
					return null;
				}
			}.execute();
			save.setEnabled(true);
			saveAs.setEnabled(true);
			edit.setEnabled(true);
			search.setEnabled(true);
		} catch (Exception e) {
			e.printStackTrace();
			msgBox.showError(
					file + "\n\n" + getString("openErr") + " " + e.getMessage(),
					getString("open"),
					getString("ok"));
		}
	}
	//Save ROM
	/** Save a ROM in Sonic Battle Text Editor GUI */
	public final void saveRom(File file) {
		openRecent.addFile(file);
		String[] strs = listTextBox.getStrings();
		int i = 0;
		for (SonicBattleLineGroup sblg: sonicBattleLineGroupCollection) //put all Strings in the SonicBattleLineGroupCollection
			for (SonicBattleLine sbl: sblg)
				sbl.setContent(strs[i++]);
		sonicBattleLineGroupCollection.save(); //save the byte data of SonicBattleLineGroupCollection
		try {
			FileOutputStream fos = new FileOutputStream(file); //write Sonic Battle ROM to file
			fos.write(sonicBattleLineGroupCollection.getRom());
			fos.close();
			msgBox.show(getString("saved") + "\n" + file.toString(), getString("save"), getString("ok"));
			listTextBox.save();
			setTitle(getString("title"));
		} catch (IOException e) {
			e.printStackTrace();
			msgBox.showError(
					file + "\n\n" + getString("saveErr") + " " + e.getMessage(),
					getString("save"),
					getString("ok"));
		}
	}
	

	// GUI menubar actions
	
	/** Create a FileChooser window for the open_Click method */
	private final FileChooser getFileChooser(String fileExtension, String fileDescription, int fileFilterIndex, String title, File defaultDirectory) {
		FileChooser fc = new FileChooser();
		fc.setParentComponent(this);
		fc.addChoosableFileFilter(fileDescription, fileExtension);
		fc.setFileFilterIndex(fileFilterIndex);
		fc.setDialogTitle(title);
		fc.setCurrentDirectory(defaultDirectory);
		fc.setFileErrorText(getString("fileErr"));
		fc.setFileErrorOkText(getString("ok"));
		fc.setFileOverwriteText(getString("overwrite"));
		fc.setFileOverwriteYesText(getString("yes"));
		fc.setFileOverwriteNoText(getString("no"));
		setupRightClickMenu(fc);
		return fc;
	}

	//Open and save ROM
	@Override
	protected final void open_Click() {
		FileChooser fc = getFileChooser("gba", getString("gbaFiles"), romFileFilter_Pref, getString("open"), openRecent.getFiles()[0]);
		if (fc.showOpenDialog() == FileChooser.APPROVE_OPTION)
			openRom(fc.getSelectedFile());
		romFileFilter_Pref = fc.getFileFilterIndex();
	}
	@Override
	protected final void openRecent_Click(File file) {
		openRom(file);
	}
	@Override
	protected final void save_Click() {
		if (!checkErrorLines())
			return;
		if (openedRom.exists()
			&& !msgBox.showYesNoPrompt(
					getString("overwrite") + "\n" + openedRom,
					getString("save"),
					getString("yes"),
					getString("no")))
			return;
		saveRom(openedRom);
	}
	@Override
	protected final void saveAs_Click() {
		if (!checkErrorLines()) return;
		FileChooser fc = getFileChooser("gba", getString("gbaFiles"), romFileFilter_Pref, getString("saveAs"), openRecent.getFiles()[0]);
		if (fc.showSaveDialog() == FileChooser.APPROVE_OPTION) {
			openedRom = fc.getSelectedFile();
			saveRom(openedRom);
		}
		romFileFilter_Pref = fc.getFileFilterIndex();
	}
	private final boolean checkErrorLines() {
		if (listTextBox.getColors().size() > 0) {
			errorLines_Click();
			return false;
		}
		return true;
	}
	
	//Close
	@Override
	protected final void close_Click() {
		if (openedRom == null) {
			trySavePreferences();
			System.exit(0);
		}
		else {
			if (listTextBox.hasUnsavedChanges() &&
				!msgBox.showYesNoPrompt(
    				getString("unsaved"),
    				getString("close"),
    				getString("yes"),
    				getString("no")))
				return;
			listTextBox.clear();
			openedRom = null;
			sonicBattleLineGroupCollection = null;
			setTitle(getString("title"));
			save.setEnabled(false);
			saveAs.setEnabled(false);
			edit.setEnabled(false);
			search.setEnabled(false);
		}
	}
	
	//Import and export methods
	/** Create a FileChooser for the importLines and exportLines methods */
	private final FileChooser getJsonFileChooser() {
		return getFileChooser(
				"json",
				getString("jsonFiles"),
				jsonFileFilter_Pref,
				getString("importLines"),
				jsonFile_Pref == null ? openRecent.getFiles()[0] : jsonFile_Pref
			);
	}
	@Override
	protected final void importLines_Click() {
		FileChooser fc = getJsonFileChooser();
		fc.setDialogTitle(getString("importLines"));
		if (fc.showOpenDialog() == FileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			importLines(file);
			jsonFile_Pref = file.getParentFile();
		}
		jsonFileFilter_Pref = fc.getFileFilterIndex();
	}
	@Override
	protected final void exportLines_Click() {
		FileChooser fc = getJsonFileChooser();
		fc.setDialogTitle(getString("exportLines"));
		if (fc.showSaveDialog() == FileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			exportLines(file);
			jsonFile_Pref = file.getParentFile();
		}
		jsonFileFilter_Pref = fc.getFileFilterIndex();
	}
	/** Export all the lines of the currently opened Sonic Battle ROM to a JSON file */
	public final void exportLines(File file) {
		try {
			String[] lines = listTextBox.getStrings();
			int i = 0;
			JsonEventWriter jew = new JsonEventWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)));
			jew.startJsonObject();
			for (int ii = 0; ii < sonicBattleLineGroupCollection.size; ii++)
				for (int iii = 0; iii < sonicBattleLineGroupCollection.group(ii).size; iii++) {
					jew.writeString(ii + "," + iii);
					jew.writeString(lines[i++]);
				}
			jew.endJsonObject();
			jew.getWriter().close();
			msgBox.show(getString("exported") + "\n" + file.toString(), getString("exportLines"), getString("ok"));
		} catch (Exception e) {
			msgBox.showError(
					getString("exportErr") + "\r\n" + e.getMessage(),
					getString("exportLines"),
					getString("ok"));
		}
	}
	/** Import Sonic Battle lines into the Sonic Battle Text Editor GUI from a JSON file */
	public final void importLines(File file) {
		try {
			JsonEventReader jer = new JsonEventReader(new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)));
			Map<String, Integer> indexMap = new HashMap<>();
			int i = 0;
			for (int ii = 0; ii < sonicBattleLineGroupCollection.size; ii++)
				for (int iii = 0; iii < sonicBattleLineGroupCollection.group(ii).size; iii++)
					indexMap.put(ii + "," + iii, i++);
			String[] lines = listTextBox.getStrings();
			JsonEventArgs jea;
			while (jer.hasNext()) {
				jea = jer.next();
				if (jea.type == JsonEventType.JSON_STRING) {
					if (indexMap.containsKey(jea.elmntStr)) {
						i = indexMap.get(jea.elmntStr);
						lines[i] = jer.next().elmntStr;
					}
				}
			}
			jer.getReader().close();
			listTextBox.populate(lines);
			msgBox.show(getString("imported") + "\n" + file.toString(), getString("importLines"), getString("ok"));
		} catch (Exception e) {
			msgBox.showError(
					getString("importErr") + "\r\n" + e.getMessage(),
					getString("importLines"),
					getString("ok"));
		}
		
	}
	
	//Go to line
	@Override
	protected final void goTo_Click() {
		NumberPromptWindow npw = new NumberPromptWindow();
		npw.setLocationRelativeTo(this);
		npw.setTitle(getString("goTo"));
		npw.setLabelText(getString("line"));
		npw.setButtonText(getString("go"));
		npw.setButtonToolTipText(null);
		npw.setMinimumValue(1);
		npw.setMaximumValue(listTextBox.getLength());
		if (goTo_Pref != null)
			npw.setNumber(goTo_Pref.intValue());
		setupRightClickMenu(npw);
		
		npw.setVisible(true);
		int number = npw.getNumber();
		if (npw.hasSelected())
			listTextBox.setSelectedIndex(number - 1);
		goTo_Pref = Integer.valueOf(number);
	}
	
	//Find + Error lines
	/** Create a ChoicePromptWindow for find_Click and errorLines_Click */
	private final ChoicePromptWindow<String> getChoicePromptWindow(String title, String confirmButtonText) {
		ChoicePromptWindow<String> cpw = new ChoicePromptWindow<>();
		cpw.setLocationRelativeTo(this);
		cpw.setMinimumSize(new Dimension(130, 170));
		cpw.setTitle(title);
		cpw.setConfirmButtonText(confirmButtonText);
		cpw.setConfirmButtonToolTipText(null);
		cpw.setCancelButtonText(getString("cancel"));
		cpw.setCancelButtonToolTipText(null);
		cpw.setSearchPlaceholder(getString("searchPh"));
		cpw.setIconImage(getIconImage());
		setupRightClickMenu(cpw);
		return cpw;
	}
	@Override
	protected final void find_Click() {
		ChoicePromptWindow<String> cpw = getChoicePromptWindow(getString("find"), getString("go"));
		cpw.setSearchQuery(search_Pref);
		String[] strings = listTextBox.getEscapedStrings();
		for (String str: strings)
			cpw.addChoice(str);
		
		//Copy ListTextBox's colorMap to ChoicePromptWindow
		for (Entry<Integer, Color> entry: listTextBox.getColors().entrySet())
			cpw.setColor(strings[entry.getKey()], entry.getValue());
		
		cpw.setVisible(true);
		if (cpw.hasSelected())
			listTextBox.setSelectedIndex(cpw.getSelectedIndex());
		search_Pref = cpw.getSearchQuery();
	}
	@Override
	protected final void errorLines_Click() {
		ChoicePromptWindow<String> cpw = getChoicePromptWindow(getString("errorLines"), getString("go"));
		cpw.setSearchQuery(errorLines_Pref);
		String[] strings = listTextBox.getEscapedStrings();
		List<Integer> stringIndexes = new ArrayList<>();
		
		//Only add lines that are colored red
		Map<Integer, Color> colorMap = listTextBox.getColors();
		Set<Integer> colorSet = colorMap.keySet();
		for (int i = 0; i < strings.length; i++) {
			if (colorSet.contains(i)) {
				cpw.setColor(strings[i], colorMap.get(i));
				cpw.addChoice(strings[i]);
				stringIndexes.add(i);
			}
		}
		
		cpw.setVisible(true);
		if (cpw.hasSelected())
			listTextBox.setSelectedIndex(stringIndexes.get(cpw.getSelectedIndex()));
		errorLines_Pref = cpw.getSearchQuery();
	}
	
	//Change language
	@Override
	protected final void changeLang_Click() {
		ChoicePromptWindow<String> cpw = getChoicePromptWindow(getString("changeLang"), getString("ok"));
		cpw.setSearchVisible(false);
		cpw.setResizable(false);
		cpw.setSize(200, 120);
		cpw.setLocationRelativeTo(this);
		for (Entry<String, Map<String, String>> entry: Localization.LANGUAGE_MAPS.entrySet())
			cpw.addChoice(entry.getKey(), entry.getValue().get("lang"));
		cpw.setSelectedChoice(language_Pref);
		
		cpw.setVisible(true);
		if (cpw.hasSelected()) {
			String language = cpw.getSelectedChoice();
			setLanguage(language);
			language_Pref = language;
			listTextBoxListener.indexChanged(listTextBox.getSelectedIndex());
		}
	}
	
	//Save image
	@Override
	protected final void saveImage(RenderedImage im) {
		FileChooser fc = getFileChooser(
				"png",
				getString("pngFiles"),
				pngFileFilter_Pref,
				getString("saveImg"),
				pngFile_Pref == null ? openRecent.getFiles()[0] : pngFile_Pref
			);
		if (fc.showSaveDialog() == FileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			pngFile_Pref = file.getParentFile();
			try {
				ImageIO.write(im, "png", file);
				msgBox.show(getString("saved") + "\n" + file.toString(), getString("saveImg"), getString("ok"));
			} catch (IOException e) {
				msgBox.showError(
						getString("saveErr") + "\r\n" + e.getMessage(),
						getString("saveImg"),
						getString("ok"));
			}
		}
		pngFileFilter_Pref = fc.getFileFilterIndex();
	}
	
	//About
	@Override
	protected final void about_Click() {
		msgBox.show(
				"Sonic Battle Text Editor v3.4.4\n"
					+ String.format(getString("aboutMsg"), "sahlaysta") + "\n"
					+ "https://github.com/sahlaysta/SonicBattleTextEditor",
				getString("about"),
				getString("ok"));
	}
	
	
	
	
	//Preferences
	
	/////////////////////////////////////////////////////////////////////////
	private static final File PREFS_FILE;
	static {
		/* Static block to set PREFS_FILE to the .prefs file, which is the path of
		 * the running .jar file with ".prefs" added to the end
		 * Ex: "C:\sonicbattletexteditor.jar.prefs" */
		File prefsFile = null;
		try {
			String runningJarPath = new File(
					GUI.class
					.getProtectionDomain()
					.getCodeSource()
					.getLocation()
					.toURI()
				).toString();
			prefsFile = new File(runningJarPath + ".prefs");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		PREFS_FILE = prefsFile;
	}
	/////////////////////////////////////////////////////////////////////////
	
	//Preference variable fields
	/** A preference variable that saves the language of Sonic Battle Text Editor GUI */
	private String language_Pref;
	/** A preference variable that saves the text of the Search window */
	private String search_Pref;
	/** A preference variable that saves the text of the Error Lines window */
	private String errorLines_Pref;
	/** A preference variable that saves the number of the "Go to lines" window prompt */
	private Integer goTo_Pref;
	/** A preference variable that saves the FileFilter index of the <code>open_Click</code> method */
	private int romFileFilter_Pref = 1;
	/** A preference variable that saves the FileFilter index of the <code>importLines_Click</code> and <code>exportLines_Click</code> method */
	private int jsonFileFilter_Pref = 1;
	/** A preference variable that saves the folder of the last opened file by <code>importLines_Click</code> and <code>exportLines_Click</code> */
	private File jsonFile_Pref;
	/** A preference variable that saves the FileFilter index of the <code>saveImage</code> method */
	private int pngFileFilter_Pref = 1;
	/** A preference variable that saves the folder of the last saved file by the <code>saveImage</code> method */
	private File pngFile_Pref;
	
	
	//Method to load and write preferences
	
	public void loadPreferences() throws IOException {
		// Preference variables
		WindowProperties windowProperties = null;
		int dividerLocation = -1;
		File[] openRecent = null;
		
		// Read JSON
		JsonEventReader jer = new JsonEventReader(new BufferedReader(new InputStreamReader(new FileInputStream(PREFS_FILE), StandardCharsets.UTF_8)));
		JsonEventArgs jea;
		while (jer.hasNext()) {
			jea = jer.next();
			if (jea.type == JsonEventType.JSON_STRING) {
				switch (jea.elmntStr) {
				
				//Read windowProperties
				case "windowProperties": {
					boolean maximized = false;
					int x = -1, y = -1, width = -1, height = -1;
					while (jea.type != JsonEventType.JSON_OBJECT_END) {
						jea = jer.next();
						if (jea.type == JsonEventType.JSON_STRING) {
							switch (jea.elmntStr) {
							case "maximized": {
								maximized = jer.next().type == JsonEventType.JSON_BOOLEAN_TRUE;
								break;
							}
							case "x": {
								x = Integer.parseInt(jer.next().elmntStr);
								break;
							}
							case "y": {
								y = Integer.parseInt(jer.next().elmntStr);
								break;
							}
							case "width": {
								width = Integer.parseInt(jer.next().elmntStr);
								break;
							}
							case "height": {
								height = Integer.parseInt(jer.next().elmntStr);
								break;
							}
							}
						}
					}
					windowProperties = new WindowProperties(maximized, x, y, width, height);
					break;
				}
				
				//Read dividerLocation
				case "dividerLocation": {
					dividerLocation = Integer.parseInt(jer.next().elmntStr);
					break;
				}
				
				//Read openRecent
				case "openRecent": {
					List<String> recentFiles = new ArrayList<>();
					jer.next();
					while ((jea = jer.next()).type != JsonEventType.JSON_ARRAY_END) {
						if (jea.type == JsonEventType.JSON_STRING)
							recentFiles.add(jea.elmntStr);
						else
							recentFiles.add(null);
					}
					openRecent = new File[recentFiles.size()];
					for (int i = 0; i < openRecent.length; i++) {
						String s = recentFiles.get(i);
						if (s == null)
							continue;
						openRecent[i] = new File(s);
					}
					break;
				}
				
				//Read language
				case "language": {
					if ((jea = jer.next()).type == JsonEventType.JSON_STRING)
						language_Pref = jea.elmntStr;
					break;
				}
				
				//Read goTo
				case "goTo": {
					if ((jea = jer.next()).type == JsonEventType.JSON_NUMBER)
						goTo_Pref = Integer.valueOf(Integer.parseInt(jea.elmntStr));
					break;
				}
				
				//Read search
				case "search": {
					if ((jea = jer.next()).type == JsonEventType.JSON_STRING)
						search_Pref = jea.elmntStr;
					break;
				}
				
				//Read errorLines
				case "errorLines": {
					if ((jea = jer.next()).type == JsonEventType.JSON_STRING)
						errorLines_Pref = jea.elmntStr;
					break;
				}
				
				//Read romFileFilter
				case "romFileFilter": {
					romFileFilter_Pref = Integer.parseInt(jer.next().elmntStr);
					break;
				}
				
				//Read jsonFileFilter
				case "jsonFileFilter": {
					jsonFileFilter_Pref = Integer.parseInt(jer.next().elmntStr);
					break;
				}
				
				//Read jsonFile
				case "jsonFile": {
					if ((jea = jer.next()).type == JsonEventType.JSON_STRING)
						jsonFile_Pref = new File(jea.elmntStr);
					break;
				}
				
				//Read pngFileFilter
				case "pngFileFilter": {
					pngFileFilter_Pref = Integer.parseInt(jer.next().elmntStr);
					break;
				}
				
				//Read pngFile
				case "pngFile": {
					if ((jea = jer.next()).type == JsonEventType.JSON_STRING)
						pngFile_Pref = new File(jea.elmntStr);
					break;
				}
				
				}
			}
		}
		jer.getReader().close();
		
		//Set preference variables
		windowProperties.apply(this);
		listTextBox.setDividerLocation(dividerLocation);
		for (int i = openRecent.length - 1; i >= 0; i--)
			if (openRecent[i] != null)
				super.openRecent.addFile(openRecent[i]);
		
	}
	public void savePreferences() throws IOException {
		WindowProperties wp = getWindowProperties();
		JsonEventWriter jew = new JsonEventWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(PREFS_FILE), StandardCharsets.UTF_8)));
		jew.startJsonObject();
		jew.writeString("windowProperties");
		jew.startJsonObject();
		jew.writeString("maximized");
		jew.writeBoolean(wp.maximized);
		jew.writeString("x");
		jew.writeNumber(wp.x);
		jew.writeString("y");
		jew.writeNumber(wp.y);
		jew.writeString("width");
		jew.writeNumber(wp.width);
		jew.writeString("height");
		jew.writeNumber(wp.height);
		jew.endJsonObject();
		jew.writeString("dividerLocation");
		jew.writeNumber(listTextBox.getDividerLocation());
		jew.writeString("openRecent");
		jew.startJsonArray();
		for (File file: openRecent.getFiles())
			if (file == null) jew.writeNull(); else jew.writeString(file.toString());
		jew.endJsonArray();
		jew.writeString("language");
		if (language_Pref == null) jew.writeNull(); else jew.writeString(language_Pref);
		jew.writeString("goTo");
		if (goTo_Pref == null) jew.writeNull(); else jew.writeNumber(goTo_Pref.intValue());
		jew.writeString("search");
		if (search_Pref == null) jew.writeNull(); else jew.writeString(search_Pref);
		jew.writeString("errorLines");
		if (errorLines_Pref == null) jew.writeNull(); else jew.writeString(errorLines_Pref);
		jew.writeString("romFileFilter");
		jew.writeNumber(romFileFilter_Pref);
		jew.writeString("jsonFileFilter");
		jew.writeNumber(jsonFileFilter_Pref);
		jew.writeString("jsonFile");
		if (jsonFile_Pref == null) jew.writeNull(); else jew.writeString(jsonFile_Pref.toString());
		jew.writeString("pngFileFilter");
		jew.writeNumber(pngFileFilter_Pref);
		jew.writeString("pngFile");
		if (pngFile_Pref == null) jew.writeNull(); else jew.writeString(pngFile_Pref.toString());
		jew.endJsonObject();
		jew.getWriter().close();
	}
	/** Try to save preferences, and show error message if it fails */
	private void trySavePreferences() {
		try {
			savePreferences();
		} catch (Exception e) {
			msgBox.showError(
					e.getMessage(),
					getString("saveErr") + " " + e.getClass().getSimpleName(),
					getString("ok"));
		}
	}
}