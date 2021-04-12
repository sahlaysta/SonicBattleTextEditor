package sbte;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import li.flor.nativejfilechooser.NativeJFileChooser;


public class Main {
	private static 	SonicBattleTextParser sbtp = null;
	private static List<LineGroup> sblines = new ArrayList<>();
	public static void d(Object o) {
		System.out.println(o.toString());
	}
	public static JSONObject lang = null;
	public static class ObjectExt{
		private Object obj;
		private String k;
		public ObjectExt(Object object, String key) {
			obj = object;
			k = key;
		}
		public Object toObject() {
			return obj;
		}
		public JFrame toJFrame() { return (JFrame) obj; }
		public JButton toJButton() { return (JButton) obj; }
		public JMenuBar toJMenuBar() { return (JMenuBar) obj; }
		public JMenu toJMenu() { return (JMenu) obj; }
		public JMenuItem toJMenuItem() { return (JMenuItem) obj; }
		public JTextArea toJTextArea() { return (JTextArea) obj; }
		public JList toJList() { return (JList) obj; }
		public JSplitPane toJSplitPane() { return (JSplitPane) obj; }
		public TitledBorder toTitledBorder() { return (TitledBorder) obj; }
		public JPanel toJPanel() { return (JPanel) obj; }
		public JCheckBoxMenuItem toJCheckBoxMenuItem() { return (JCheckBoxMenuItem) obj; }
		public int toInt() { return (int) obj; }
		public void setLang() {
			if (k==null) return;
			String s = (String) lang.get(k);
			try { this.toJFrame().setTitle(s); return; } catch (Exception e) {}
			try { this.toJButton().setText(s); return; } catch (Exception e) {}
			try { this.toJMenu().setText(s); return; } catch (Exception e) {}
			try { this.toJMenuItem().setText(s); return; } catch (Exception e) {}
			try { this.toJCheckBoxMenuItem().setText(s); return; } catch (Exception e) {}
		}
	}
	public static class SC extends ArrayList<ObjectExt>{ //subscribe control to String
		private List<String> names = new ArrayList<>();
		public void addControl(Object o, String s, String n) {
			this.add(new ObjectExt(o, n));
			names.add(s);
		}
		public ObjectExt getByName(String s) {
			int i = 0;
			for (String a: names) {
				if (a.equals(s)) return this.get(i);
				i++;
			}
			return null;
		}
	}
	public static DefaultListModel<String> dlm = new DefaultListModel<>();
	public static SC sc = new SC();
	public static PrefManager prefs = new PrefManager(new File(System.getProperty("user.dir"), "prefs.json"));
	
	public static void main(String[] args) {
		parseLib();
		
		{//gui
			try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
			
			sc.addControl(new JFrame(), "main", "appName");
			if (prefs.isNull("windowSize")) sc.getByName("main").toJFrame().setSize(500, 300);
			else { long arr[] = prefs.getLongArray("windowSize"); sc.getByName("main").toJFrame().setSize((int) arr[0], (int) arr[1]); }
			sc.getByName("main").toJFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			sc.getByName("main").toJFrame().addWindowListener(new WindowAdapter(){ public void windowClosing(WindowEvent e){
				//save preferences on close
				boolean maximized = sc.getByName("main").toJFrame().getExtendedState() == Frame.MAXIMIZED_BOTH;
				prefs.put("windowMaximized", maximized);
				if (!maximized) {
					prefs.putArray("windowLocation", new Integer[] { sc.getByName("main").toJFrame().getX(), sc.getByName("main").toJFrame().getY() });
					{ Dimension s = sc.getByName("main").toJFrame().getSize(); prefs.putArray("windowSize", new Integer[] { s.width, s.height }); }
				}
				prefs.put("dividerLocation", sc.getByName("splitPane").toJSplitPane().getDividerLocation());
				prefs.save();
	        }});
			if (prefs.isNull("windowLocation")) sc.getByName("main").toJFrame().setLocationRelativeTo(null);
			else { long arr[] = prefs.getLongArray("windowLocation"); sc.getByName("main").toJFrame().setLocation((int) arr[0], (int) arr[1]); }
			
			{//gui
				sc.addControl(new JPanel(new GridLayout(0, 1, 1, 1)), "lpanel", null);
				sc.getByName("lpanel").toJPanel().setBorder(new TitledBorder(""));
				{//list
					sc.addControl(new JList<>(dlm), "list", null);
					sc.getByName("list").toJList().setEnabled(false);
					sc.getByName("list").toJList().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					sc.getByName("list").toJList().setCellRenderer(new DefaultListCellRenderer() {
					//background color of list items
	                     @Override
	                     public Component getListCellRendererComponent(JList list, Object value, int index,
	                               boolean isSelected, boolean cellHasFocus) {
	                          Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	                    	  if (!sc.getByName("list").toJList().isEnabled()) return c;
	                          if (hasLongLine(sblines.get(indexes.get(index).getGroup()).get(indexes.get(index).getMember()).getMessage().getContent(), 40)) {
	                        	  if (isSelected) {
	                        		  setForeground(Color.YELLOW);
	                        	  }
	                        	  else {
		                        	  setBackground(Color.YELLOW);
		                        	  if (sc.getByName("list").toJList().getBackground().equals(Color.BLACK)) {
		                        		  setForeground(Color.BLACK);
		                        	  }
	                        	  }
	                          }
	                          if (sblines.get(indexes.get(index).getGroup()).get(indexes.get(index).getMember()).getMessage().isProblematic()) {
	                        	  if (isSelected) {
	                        		  setForeground(Color.RED);
	                        	  }
	                        	  else {
		                        	  setBackground(Color.RED);
		                        	  if (sc.getByName("list").toJList().getBackground().equals(Color.BLACK)) {
		                        		  setForeground(Color.BLACK);
		                        	  }
	                        	  }
	                          }
	                          
	                          return c;
	                     }

	                });
					sc.getByName("list").toJList().addMouseListener(new MouseAdapter() { public void mouseReleased(MouseEvent e) { if (e.isPopupTrigger()) {
					//right click popup menu
						if (!((JList)e.getSource()).isEnabled()) return;
						((JList)e.getSource()).setSelectedIndex(((JList)e.getSource()).locationToIndex(e.getPoint()));
						class D extends JMenuItem{
							public D(String s) {
								this.setEnabled(false);
								this.setText(s);
							}
						}
						int sel = ((JList)e.getSource()).getSelectedIndex();
						JPopupMenu rc = new JPopupMenu();
						rc.setFocusable(false);
						JMenuItem props = new JMenuItem(lang.get("properties").toString());
						props.addActionListener(new ActionListener(){ public void actionPerformed(ActionEvent e){  
							propertiesGui(indexes.get(sel));
					    }});
						//Index index = indexes.get(sel);
						//rc.add(props);
						//rc.addSeparator();
						rc.add(new D(indexes.get(sel).toString()));
						rc.add(new D(stringLimit(sblines.get(indexes.get(sel).getGroup()).get(indexes.get(sel).getMember()).getMessage().toString(), 30)));
						
						rc.show(sc.getByName("list").toJList(), e.getX(), e.getY());
					}}});
					sc.getByName("list").toJList().addListSelectionListener(new ListSelectionListener() { public void valueChanged(ListSelectionEvent e) {
					//on index change
						if (!sc.getByName("list").toJList().isEnabled()) return;
						if (((JList) e.getSource()).getSelectedIndex() < 0) return;
						sc.getByName("ta").toJTextArea().setEnabled(false);
						Index index = indexes.get(((JList) e.getSource()).getSelectedIndex());
						sc.getByName("lpanel").toJPanel().setBorder(new TitledBorder(lang.get("selectedLine").toString().replace("[v]", "" + (((JList) e.getSource()).getSelectedIndex() + 1))));
						String content = sblines.get(index.getGroup()).get(index.getMember()).getMessage().getContent();
						boolean prob = sblines.get(index.getGroup()).get(index.getMember()).getMessage().isProblematic();
						if (!prob) {
							sc.getByName("ta").toJTextArea().setText(content);
							blackTA();
						}
						else {
							String s = dlm.get(((JList) e.getSource()).getSelectedIndex());
							JSONObject j = null;
							try { j = (JSONObject) new JSONParser().parse("{\"t\":\"" + s + "\"}"); } catch (ParseException e1) { e1.printStackTrace(); }
							sc.getByName("ta").toJTextArea().setText(j.get("t").toString());
							redTA();
						}
						sc.getByName("ta").toJTextArea().setEnabled(true);
					}});
					sc.getByName("lpanel").toJPanel().add(new JScrollPane(sc.getByName("list").toJList()));
				}
				
				JPanel tapanel = new JPanel(new GridLayout(1, 0, 1, 1));
				tapanel.setBorder(new TitledBorder(""));
				{// text box
					sc.addControl(new JTextArea(), "ta", null);
					sc.getByName("ta").toJTextArea().setEnabled(false);
					sc.getByName("ta").toJTextArea().getDocument().addDocumentListener(new DocumentListener() {
							  public void changedUpdate(DocumentEvent e) {
							  	  changed(e);
							  }
							  public void removeUpdate(DocumentEvent e) {
								  changed(e);
							  }
							  public void insertUpdate(DocumentEvent e) {
								  changed(e);
							  }
							  public void changed(DocumentEvent e) {
								  if (!sc.getByName("ta").toJTextArea().isEnabled()) return;
								  
								  int sel = sc.getByName("list").toJList().getSelectedIndex();
								  String s = sc.getByName("ta").toJTextArea().getText();
								  Index index = indexes.get(sel);
								  
								  ByteSequence bs = null;
								  String output = "";
								  try {
									  bs = new ByteSequence(sbtp.parseSB(s));
									  sblines.get(index.getGroup()).get(index.getMember()).getMessage().set(bs.getByteArray());
									  output = sblines.get(index.getGroup()).get(index.getMember()).getMessage().getDisplay();
									  sblines.get(index.getGroup()).get(index.getMember()).getMessage().setProblematic(false);
									  blackTA();
								  }
								  catch (Error ee) { //if line is problematic
									  JSONObject j = new JSONObject();
									  j.put("h", s);
									  output = j.toString().substring(6).replaceFirst(".$","").replaceFirst(".$","");
									  sblines.get(index.getGroup()).get(index.getMember()).getMessage().setProblematic(true);
									  errors[sel] = ee.toString();
									  redTA();
								  }
								  
								  if (output.equals("")) {
									  output = " ";
								  }
								  dlm.set(sel, output);
							  }
							});

					JScrollPane scrollPane = new JScrollPane(sc.getByName("ta").toJTextArea());
					
					tapanel.add(scrollPane);
				}
				//split pane
				sc.addControl(new JSplitPane(), "splitPane", null);
				int borderp = 2;
				
				int splitter = sc.getByName("main").toJFrame().getSize().height - 132;
				if (!prefs.isNull("dividerLocation")) splitter = (int) prefs.getLong("dividerLocation");
				sc.getByName("splitPane").toJSplitPane().setBorder(BorderFactory.createEmptyBorder(borderp,borderp,borderp,borderp));
				sc.getByName("splitPane").toJSplitPane().setDividerLocation(splitter);
				sc.getByName("splitPane").toJSplitPane().setOrientation(JSplitPane.VERTICAL_SPLIT);
				sc.getByName("splitPane").toJSplitPane().setTopComponent(sc.getByName("lpanel").toJPanel());
				sc.getByName("splitPane").toJSplitPane().setBottomComponent(tapanel);
				sc.getByName("main").toJFrame().add(sc.getByName("splitPane").toJSplitPane());
			}
			
			{//menu bar
				sc.addControl(new JMenuBar(), "bar", null);
				
				{//file tab
					sc.addControl(new JMenu(), "file", "file");
					sc.addControl(new JMenu(), "recent", "fileRecent");
					sc.addControl(new JMenuItem(), "open", "open");
					sc.getByName("open").toJMenuItem().setAccelerator(KeyStroke.getKeyStroke('O', Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
					sc.getByName("open").toJMenuItem().addActionListener(new ActionListener(){ public void actionPerformed(ActionEvent e){  
						openRom(null);
				    }});
					setRecent(sc.getByName("recent").toJMenu());
					sc.addControl(new JMenuItem(), "save", "save");
					sc.getByName("save").toJMenuItem().setAccelerator(KeyStroke.getKeyStroke('S', Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
					sc.getByName("save").toJMenuItem().addActionListener(new ActionListener(){ public void actionPerformed(ActionEvent e){  
						save(rompath);
				    }});
					sc.getByName("save").toJMenuItem().setEnabled(false);
					sc.addControl(new JMenuItem(), "saveAs", "saveAs");
					sc.getByName("saveAs").toJMenuItem().setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,java.awt.Event.CTRL_MASK | java.awt.Event.SHIFT_MASK));
					sc.getByName("saveAs").toJMenuItem().addActionListener(new ActionListener(){ public void actionPerformed(ActionEvent e){  
						save(null);
				    }});
					sc.getByName("saveAs").toJMenuItem().setEnabled(false);
					sc.getByName("file").toJMenu().add(sc.getByName("open").toJMenuItem());
					sc.getByName("file").toJMenu().add(sc.getByName("recent").toJMenu());
					sc.getByName("file").toJMenu().add(sc.getByName("save").toJMenuItem());
					sc.getByName("file").toJMenu().add(sc.getByName("saveAs").toJMenuItem());
				}
				
				{//edit tab
					sc.addControl(new JMenu(), "edit", "edit");
					sc.getByName("edit").toJMenu().setEnabled(false);
					sc.addControl(new JMenuItem(), "export", "export");
					sc.addControl(new JMenuItem(), "import", "import");
					sc.getByName("export").toJMenuItem().addActionListener(new ActionListener(){ public void actionPerformed(ActionEvent e){  
						exportlines();
				    }});
					sc.getByName("import").toJMenuItem().addActionListener(new ActionListener(){ public void actionPerformed(ActionEvent e){  
						importlines();
				    }});
					
					sc.getByName("edit").toJMenu().add(sc.getByName("export").toJMenuItem());
					sc.getByName("edit").toJMenu().add(sc.getByName("import").toJMenuItem());
				}
				
				{
					//view tab
					sc.addControl(new JMenu(), "options", "options");
					sc.addControl(new JCheckBoxMenuItem(), "darkTheme", "darkTheme");
					sc.getByName("darkTheme").toJMenuItem().addItemListener(new ItemListener() {
					      public void itemStateChanged(ItemEvent e) {
					        if (((JCheckBoxMenuItem) e.getSource()).isSelected()) {
					        	setTheme(Color.LIGHT_GRAY, Color.BLACK);
					        	prefs.put("darkTheme", true);
					        	prefs.save();
					        } else {
					        	setTheme(Color.BLACK, Color.WHITE);
					        	prefs.put("darkTheme", false);
					        	prefs.save();
					        }
					      }
					    });
					
					sc.addControl(new JMenuItem(), "lang", "changeLang");
					sc.getByName("lang").toJMenuItem().addActionListener(new ActionListener(){ public void actionPerformed(ActionEvent e){  
						langGui();
				    }});
					
					sc.getByName("options").toJMenu().add(sc.getByName("lang").toJMenuItem());
					sc.getByName("options").toJMenu().add(sc.getByName("darkTheme").toJCheckBoxMenuItem());
				}
				
				{
					//help tab
					sc.addControl(new JMenu(), "help", "help");
					sc.addControl(new JMenuItem(), "about", "about");
					sc.getByName("about").toJMenuItem().setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
					sc.getByName("about").toJMenuItem().addActionListener(new ActionListener(){ public void actionPerformed(ActionEvent e){  
						JOptionPane.showMessageDialog(sc.getByName("main").toJFrame(), "V3.0.0\n" + lang.get("credits").toString().replace("[v]", "porog") + "\nhttps://github.com/sahlaysta/", lang.get("about").toString(), JOptionPane.INFORMATION_MESSAGE);
				    }});
					
					sc.getByName("help").toJMenu().add(sc.getByName("about").toJMenuItem());
				}
				
				{//search tab
					//go to line
					sc.addControl(new JMenu(), "search", "search");
					sc.addControl(new JMenuItem(), "goTo", "goTo");
					sc.getByName("goTo").toJMenuItem().setAccelerator(KeyStroke.getKeyStroke('G', Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
					sc.getByName("goTo").toJMenuItem().addActionListener(new ActionListener(){ public void actionPerformed(ActionEvent e){  
						goToGui();
				    }});
					//search
					sc.addControl(new JMenuItem(), "searchMenu", "search");
					sc.getByName("searchMenu").toJMenuItem().setAccelerator(KeyStroke.getKeyStroke('F', Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
					sc.getByName("searchMenu").toJMenuItem().addActionListener(new ActionListener(){ public void actionPerformed(ActionEvent e){  
						searchGui(dlm, false);
				    }});
					//problem lines
					sc.addControl(new JMenuItem(), "prob", "prob");
					sc.getByName("prob").toJMenuItem().addActionListener(new ActionListener(){ public void actionPerformed(ActionEvent e){  
						searchGui(dlm, true);
				    }});
					
					sc.getByName("search").toJMenu().add(sc.getByName("searchMenu").toJMenuItem());
					sc.getByName("search").toJMenu().add(sc.getByName("goTo").toJMenuItem());
					sc.getByName("search").toJMenu().add(sc.getByName("prob").toJMenuItem());
					
					sc.getByName("search").toJMenu().setEnabled(false);
				}
				
				sc.getByName("bar").toJMenuBar().add(sc.getByName("file").toJMenu());
				sc.getByName("bar").toJMenuBar().add(sc.getByName("edit").toJMenu());
				sc.getByName("bar").toJMenuBar().add(sc.getByName("search").toJMenu());
				sc.getByName("bar").toJMenuBar().add(sc.getByName("options").toJMenu());
				sc.getByName("bar").toJMenuBar().add(sc.getByName("help").toJMenu());
				
				sc.getByName("main").toJFrame().setJMenuBar(sc.getByName("bar").toJMenuBar());
			}
			
			sc.getByName("main").toJFrame().setVisible(true);
			
			if (!prefs.isNull("windowMaximized")) {
				if (prefs.getBoolean("windowMaximized")) sc.getByName("main").toJFrame().setExtendedState(sc.getByName("main").toJFrame().getExtendedState() | JFrame.MAXIMIZED_BOTH);
			}
		}
		//Locale.setDefault(new Locale("es", "UY"));
		
		{//localization
			String guess;
			if (prefs.isNull("language")) guess = Locale.getDefault().toLanguageTag();
			else guess = prefs.getString("language");
			
			setLang(guess);
		}

		{//dark theme
			if (!prefs.isNull("darkTheme")) {
				if (prefs.getBoolean("darkTheme")) sc.getByName("darkTheme").toJMenuItem().setSelected(true);
			}
		}
	}
	
	public static Color themeFore = Color.BLACK;
	public static Color themeBack = Color.WHITE;
	
	public static void setTheme(Color foreground, Color background) {
		themeFore = foreground;
		themeBack = background;
		sc.getByName("list").toJList().setForeground(themeFore);
		sc.getByName("list").toJList().setBackground(themeBack);
		sc.getByName("ta").toJTextArea().setBackground(themeBack);
		if (!sc.getByName("ta").toJTextArea().getForeground().equals(Color.RED)) sc.getByName("ta").toJTextArea().setForeground(themeFore);
		sc.getByName("ta").toJTextArea().setCaretColor(foreground);
		sc.getByName("lpanel").toJPanel().setForeground(themeFore);
		sc.getByName("lpanel").toJPanel().setBackground(themeBack);
//		sc.getByName("bar").toJMenuBar().setForeground(themeFore);
//		sc.getByName("bar").toJMenuBar().setBackground(themeBack);
		
		//set titledborder text color
		TitledBorder tb = ((TitledBorder) sc.getByName("lpanel").toJPanel().getBorder());
		if (!tb.getTitleColor().equals(Color.RED)) {
			tb.setTitleColor(themeFore);
			sc.getByName("lpanel").toJPanel().setBorder(tb);
		}
	}
	
	public static void blackTA() {
		sc.getByName("ta").toJTextArea().setForeground(themeFore);
		TitledBorder tb = new TitledBorder(lang.get("selectedLine").toString().replace("[v]", "" + (sc.getByName("list").toJList().getSelectedIndex() + 1)));
		tb.setTitleColor(themeFore);
		sc.getByName("lpanel").toJPanel().setBorder(tb);
	}
	public static void redTA() {
		sc.getByName("ta").toJTextArea().setForeground(Color.RED);
		String s = errors[sc.getByName("list").toJList().getSelectedIndex()].replace("java.lang.Error: Unknown char: ", "");
		TitledBorder tb = new TitledBorder(lang.get("invalidChar").toString().replace("[v]", s));
		tb.setTitleColor(Color.RED);
		sc.getByName("lpanel").toJPanel().setBorder(tb);
	}
	public static JPopupMenu copyMenu(boolean editable) {
		class SelectAll extends TextAction
	    {
	        public SelectAll()
	        {
	            super(lang.get("selectAll").toString());
	            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control A"));
	        }

	        public void actionPerformed(ActionEvent e)
	        {
	            JTextComponent component = getFocusedComponent();
	            component.selectAll();
	            component.requestFocusInWindow();
	        }
	    }
		JPopupMenu menu = new JPopupMenu();
		menu.setFocusable(false);
		
		Action cut = new DefaultEditorKit.CutAction();
        cut.putValue(Action.NAME, lang.get("cut").toString());
        cut.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control X"));
        menu.add( cut );
        if (!editable) cut.setEnabled(false);
		
		Action copy = new DefaultEditorKit.CopyAction();
		copy.putValue(Action.NAME, lang.get("copy").toString());
		copy.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control C"));
		menu.add(copy);
		
		Action paste = new DefaultEditorKit.PasteAction();
        paste.putValue(Action.NAME, lang.get("paste").toString());
        paste.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control V"));
        menu.add( paste );
        if (!editable) paste.setEnabled(false);
        
        Action selectAll = new SelectAll();
        menu.add( selectAll );
        
		return menu;
	}
	public static void searchGui(DefaultListModel<String> si, boolean problematic) {
		class A extends JLabel{
			public String hits = lang.get("hits").toString();
			public String oneHit = lang.get("oneHit").toString();
			public void setFound(int i) {
				if (i == 1) A.this.setText(oneHit.replace("[v]", i + ""));
				else A.this.setText(hits.replace("[v]", i + ""));
			}
		}
		JDialog d = new JDialog(sc.getByName("main").toJFrame());
		d.setModal(true);
		d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		if (problematic) d.setTitle(lang.get("prob").toString());
		else d.setTitle(lang.get("search").toString());
		d.setLayout(new BorderLayout(5, 5));
		
		JPanel tfp = new JPanel();
		JTextField tf = new JTextField();
		tf.setComponentPopupMenu(copyMenu(true));
		tfp.setBorder(new EmptyBorder(5,5,5,5));
		tfp.setLayout(new GridLayout(1,1));
		tfp.add(tf);
		d.add(tfp, BorderLayout.PAGE_START);
		
		JPanel lp = new JPanel();
		DefaultListModel m = new DefaultListModel<String>();
		JList list = new JList(m);
		List<Integer> index = new ArrayList<>();
		for (int i = 0; i < si.getSize(); i++) {
			if (problematic && !sblines.get(indexes.get(i).getGroup()).get(indexes.get(i).getMember()).getMessage().isProblematic()) continue;
			m.addElement(si.get(i));
			index.add(i);
		}
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane sp = new JScrollPane(list);
		//sp.setPreferredSize(new Dimension(200, 150));
		lp.setBorder(new EmptyBorder(5,5,5,5));
		lp.setLayout(new GridLayout(1,1));
		lp.add(sp);
		//list coloring
		list.setCellRenderer(new DefaultListCellRenderer() {
			//background color of list items
                 @Override
                 public Component getListCellRendererComponent(JList jlist, Object value, int jindex,
                           boolean isSelected, boolean cellHasFocus) {
                      Component c = super.getListCellRendererComponent(jlist, value, jindex, isSelected, cellHasFocus);
                      if (hasLongLine(sblines.get(indexes.get(index.get(jindex)).getGroup()).get(indexes.get(index.get(jindex)).getMember()).getMessage().getContent(), 40)) {
                    	  if (isSelected) {
                    		  setForeground(Color.YELLOW);
                    	  }
                    	  else {
                        	  setBackground(Color.YELLOW);
                        	  if (list.getBackground().equals(Color.BLACK)) {
                        		  setForeground(Color.BLACK);
                        	  }
                    	  }
                      }
                      if (sblines.get(indexes.get(index.get(jindex)).getGroup()).get(indexes.get(index.get(jindex)).getMember()).getMessage().isProblematic()) {
                    	  if (isSelected) {
                    		  setForeground(Color.RED);
                    	  }
                    	  else {
                        	  setBackground(Color.RED);
                        	  if (list.getBackground().equals(Color.BLACK)) {
                        		  setForeground(Color.BLACK);
                        	  }
                    	  }
                      }
                      
                      return c;
                 }

            });
		d.add(lp, BorderLayout.CENTER);
		

		JPanel bottom = new JPanel();
		bottom.setLayout(new GridLayout(1,2));
		
		JPanel buttonpanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton b = new JButton(lang.get("go").toString());
		buttonpanel.add(b);
		
		JPanel labelpanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		A l = new A();
		l.setFound(m.size());
		labelpanel.add(l, BorderLayout.SOUTH);

		bottom.add(labelpanel);
		bottom.add(buttonpanel);
		d.add(bottom, BorderLayout.SOUTH);
		
		//update list on search
		tf.getDocument().addDocumentListener(new DocumentListener() {
			  public void changedUpdate(DocumentEvent e) {
			  	  changed(e);
			  }
			  public void removeUpdate(DocumentEvent e) {
				  changed(e);
			  }
			  public void insertUpdate(DocumentEvent e) {
				  changed(e);
			  }
			  public void changed(DocumentEvent e) {
				  if (tf.getText().length()==0) {
					  m.clear();
					  index.clear();
					  for (int i = 0; i < si.getSize(); i++) {
						  if (problematic && !sblines.get(indexes.get(i).getGroup()).get(indexes.get(i).getMember()).getMessage().isProblematic()) continue;
						  m.addElement(si.get(i));
						  index.add(i);
					  }
					  l.setFound(m.size());
					  return;
				  }
				  
				  m.clear();
				  index.clear();
				  for (int i = 0; i < si.getSize(); i++) {
					  if (problematic && !sblines.get(indexes.get(i).getGroup()).get(indexes.get(i).getMember()).getMessage().isProblematic()) continue;
					  if (containsAll(si.get(i).toLowerCase(), tf.getText().toLowerCase().split(" "))) {
						  m.addElement(si.get(i));
						  index.add(i);
					  }
				  }
				  l.setFound(m.getSize());
			  }
		});
		
		//go to action
		b.addActionListener(new ActionListener(){ public void actionPerformed(ActionEvent e){
			if (list.getSelectedIndex() < 0) return;
			sc.getByName("list").toJList().ensureIndexIsVisible(index.get(list.getSelectedIndex()));
			sc.getByName("list").toJList().setSelectedIndex(index.get(list.getSelectedIndex()));
			d.dispose();
		}});
		
		//double click option action
		list.addMouseListener(new MouseAdapter() {
		    public void mouseClicked(MouseEvent evt) {
		        if (evt.getClickCount() == 2) {
		            b.doClick();
		        }
		    }
		});
		
		//on enter key
		list.addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent e){
			    if (e.getKeyCode() == KeyEvent.VK_ENTER){
			   b.doClick();
			}
			}
			});
		
		d.pack();
		d.setSize(200, d.getSize().height);
		d.setLocationRelativeTo(sc.getByName("main").toJFrame());
		d.setVisible(true);
	}
	public static boolean containsAll(String st, String[] sa) {
		for (String s:sa) {
			if (!st.contains(s)) return false;
		}
		return true;
	}
	public static void exportlines() {
		File defaultDir = new File(System.getProperty("user.dir"));
		if (!prefs.isNull("lastOpenedJSON")) {
			File dir = new File(prefs.getString("lastOpenedJSON"));
			if (dir.exists()) defaultDir = dir;
		}
		
		JFileChooser fileChooser = new NativeJFileChooser(defaultDir);
		FileNameExtensionFilter fnefgba = new FileNameExtensionFilter(lang.get("fileType").toString().replace("[v]", "JSON"), "json");
		FileNameExtensionFilter fnefall = new FileNameExtensionFilter(lang.get("fileTypeAll").toString(), "*.*");
		fileChooser.addChoosableFileFilter(fnefgba);
		fileChooser.addChoosableFileFilter(fnefall);
		
		fileChooser.setFileFilter(fnefgba);
		fileChooser.setDialogTitle(lang.get("export").toString());
		int returnValue = fileChooser.showSaveDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			prefs.put("lastOpenedJSON", fileChooser.getSelectedFile().toString());
			StringBuilder sb = new StringBuilder("{\r\n");
			for (int i = 0; i < dlm.getSize(); i++) {
				sb.append("	\"" + indexes.get(i).getGroup() + "," + indexes.get(i).getMember() + "\": \"" + dlm.get(i) + "\",\n");
			}
			sb.setLength(sb.length()-2);
			sb.append("\n}");
			try {
				if (!fileChooser.getSelectedFile().exists()) {
					fileChooser.getSelectedFile().createNewFile();
				}
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileChooser.getSelectedFile(), false), StandardCharsets.UTF_8));
				bw.write(sb.toString());
				bw.close();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(sc.getByName("main").toJFrame(), e.toString(), lang.get("error").toString(), JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			JOptionPane.showMessageDialog(sc.getByName("main").toJFrame(), lang.get("exported").toString(), lang.get("export").toString(), JOptionPane.INFORMATION_MESSAGE);
        } else return;
	}
	public static void importlines() {
		File defaultDir = new File(System.getProperty("user.dir"));
		if (!prefs.isNull("lastOpenedJSON")) {
			File dir = new File(prefs.getString("lastOpenedJSON"));
			if (dir.exists()) defaultDir = dir;
		}
		
		JFileChooser fileChooser = new NativeJFileChooser(defaultDir);
		FileNameExtensionFilter fnefgba = new FileNameExtensionFilter(lang.get("fileType").toString().replace("[v]", "JSON"), "json");
		FileNameExtensionFilter fnefall = new FileNameExtensionFilter(lang.get("fileTypeAll").toString(), "*.*");
		fileChooser.addChoosableFileFilter(fnefgba);
		fileChooser.addChoosableFileFilter(fnefall);
		
		fileChooser.setFileFilter(fnefgba);
		fileChooser.setDialogTitle(lang.get("import").toString());
		int returnValue = fileChooser.showOpenDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			prefs.put("lastOpenedJSON", fileChooser.getSelectedFile().toString());
			JSONObject j = null;
			String contents = "";
			try { 
				List<String> join = Files.readAllLines(fileChooser.getSelectedFile().toPath());
				contents = String.join("", join.toArray(new String[0]));
			} catch (IOException e) { JOptionPane.showMessageDialog(sc.getByName("main").toJFrame(), e.toString(), lang.get("error").toString(), JOptionPane.ERROR_MESSAGE); return; }
			try { j = (JSONObject) new JSONParser().parse(contents); } catch (ParseException e) { JOptionPane.showMessageDialog(sc.getByName("main").toJFrame(), e.toString(), lang.get("error").toString(), JOptionPane.ERROR_MESSAGE); return; }
			
			StringBuilder sb = new StringBuilder();
			int listi = 0;
			boolean error = false;
			for (int i = 0; i < sblines.size(); i++) {
				for (int ii = 0; ii < sblines.get(i).size(); ii++) {
					String key = i + "," + ii;
					String s = "";
					try {
						s = j.get(key).toString();
					} catch (NullPointerException e) {
						sb.append(key + "\n");
						continue;
					}
					try{
						sblines.get(i).get(ii).getMessage().set(sbtp.parseSB(s));
					} catch (Error e) {
						errors[listi] = e.toString();
						sblines.get(i).get(ii).getMessage().setProblematic(true);
						error = true;
					}
					JSONObject jo = new JSONObject();
					jo.put("h", s);
					String set = jo.toString().substring(6).replaceFirst(".$","").replaceFirst(".$","");
					dlm.set(listi, set);
					listi++;
				}
			}
			int sel = sc.getByName("list").toJList().getSelectedIndex();
			sc.getByName("list").toJList().clearSelection();
			sc.getByName("list").toJList().setSelectedIndex(sel);
			
			JOptionPane.showMessageDialog(sc.getByName("main").toJFrame(), lang.get("imported").toString(), lang.get("import").toString(), JOptionPane.INFORMATION_MESSAGE);
			if (sb.length() > 0) {
				sb.setLength(sb.length()-1);
				scrollMessage(lang.get("missingLines").toString(), sb.toString());
			}
			if (error) searchGui(dlm, true);
        } else return;
	}
	public static void importjson() {
		int sel = sc.getByName("list").toJList().getSelectedIndex();
		for (int i = 0; i < 2450; i++) {
			String s = "h{" + i;
			try{
				sblines.get(indexes.get(i).getGroup()).get(indexes.get(i).getMember()).getMessage().set(sbtp.parseSB(s));
			} catch (Error e) {
				errors[i] = e.toString();
				sblines.get(indexes.get(i).getGroup()).get(indexes.get(i).getMember()).getMessage().setProblematic(true);
			}
			dlm.set(i, s);
		}
		sc.getByName("list").toJList().clearSelection();
		sc.getByName("list").toJList().setSelectedIndex(sel);
	}
	public static void langGui() {
		JDialog d = new JDialog(sc.getByName("main").toJFrame());
		d.setModal(true);
		d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		d.setTitle(lang.get("changeLang").toString());
		JSONObject j = null;
		try { j = (JSONObject) new JSONParser().parse(getLocalization()); } catch (ParseException e) { e.printStackTrace(); }
		{//list
			JPanel p = new JPanel();
			DefaultListModel<String> l = new DefaultListModel<>();  
			List<String> langCode = new ArrayList<>();
			for (Object o: j.keySet()) {
				String s = o.toString();
				langCode.add(s);
				l.addElement(((JSONObject) j.get(s)).get("thisLang").toString());
			}
			JList<String> list = new JList<>(l);
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			p.add(list);
			
			d.add(p, BorderLayout.LINE_START);
			
			//button
				JPanel pp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
				JButton b = new JButton(lang.get("sel").toString());
				b.addActionListener(new ActionListener(){ public void actionPerformed(ActionEvent e){
					setLang(langCode.get(list.getSelectedIndex()));
					b.setText(lang.get("sel").toString());
					d.setTitle(lang.get("changeLang").toString());
				}});  
				pp.add(b);
				d.add(pp, BorderLayout.SOUTH);
			
			
			//list double click
			list.addMouseListener(new MouseAdapter() {
			    public void mouseClicked(MouseEvent evt) {
			        if (evt.getClickCount() == 2) {
			            b.doClick();
			        }
			    }
			});
			
			//list enter key
			list.addKeyListener(new KeyAdapter(){
				public void keyPressed(KeyEvent e){
				    if (e.getKeyCode() == KeyEvent.VK_ENTER){
				   b.doClick();
				}
				}
				});
		}
		
		d.pack();
		d.setMinimumSize(new Dimension(d.getSize().width, d.getSize().height));
		d.setSize(d.getSize().width + 50, d.getSize().height);
		d.setLocationRelativeTo(sc.getByName("main").toJFrame());
		d.setVisible(true);
	}
	public static void goToGui() {
		class IntOnlyTF extends JTextField{
			boolean editing = false;
			public IntOnlyTF() {
				this.setComponentPopupMenu(copyMenu(true));
				//cannot enter anything that isnt a number
				this.getDocument().addDocumentListener(new DocumentListener() {
					  public void changedUpdate(DocumentEvent e) {
					  	  changed(e);
					  }
					  public void removeUpdate(DocumentEvent e) {
						  changed(e);
					  }
					  public void insertUpdate(DocumentEvent e) {
						  changed(e);
					  }
					  public void changed(DocumentEvent e) {
						  if (editing) return;
						  Runnable undo = new Runnable() {
						        @Override
						        public void run() {
						        	editing = true;
						        	StringBuilder sb = new StringBuilder();
						        	for (char c: getText().toCharArray()) {
						        		if (c == '0') sb.append(c);
						        		else if (c == '1') sb.append(c);
						        		else if (c == '2') sb.append(c);
						        		else if (c == '3') sb.append(c);
						        		else if (c == '4') sb.append(c);
						        		else if (c == '5') sb.append(c);
						        		else if (c == '6') sb.append(c);
						        		else if (c == '7') sb.append(c);
						        		else if (c == '8') sb.append(c);
						        		else if (c == '9') sb.append(c);
						        	}
						        	setText(sb.toString());
						        	editing = false;
						        }
						    };       
						    SwingUtilities.invokeLater(undo);
					  }
				});
			}
		}
		JDialog d = new JDialog(sc.getByName("main").toJFrame());
		d.setModal(true);
		d.setResizable(false);
		d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		d.setTitle(lang.get("goTo").toString());
		JPanel p = new JPanel();
		p.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		{
			p.add(new JLabel(lang.get("line").toString()));
			IntOnlyTF tf = new IntOnlyTF();
			p.add(tf);
			
			p.add(new JLabel(""));
			JButton go = new JButton(lang.get("go").toString());
			go.addActionListener(new ActionListener(){ public void actionPerformed(ActionEvent e){
				if (tf.getText().length() <= 0) return;
				int sel = -1 + Integer.parseInt(tf.getText());
				try { dlm.get(sel); } catch (Exception ee) {
					JOptionPane.showMessageDialog(d, ee.toString(), lang.get("error").toString(), JOptionPane.ERROR_MESSAGE);
					return;
				}
				sc.getByName("list").toJList().ensureIndexIsVisible(sel);
				sc.getByName("list").toJList().setSelectedIndex(sel);
				d.dispose();
			}});  
			p.add(go);
			
			// on enter key press
			Action action = new AbstractAction()
			{
			    @Override
			    public void actionPerformed(ActionEvent e)
			    {
			        go.doClick();
			    }
			};
			tf.addActionListener( action );
		}
		
		p.setLayout(new GridLayout(2, 2, 5, 5));
		d.add(p);
		d.pack();
		d.setSize(d.getSize().width, d.getSize().height - 10);
		
		d.setLocationRelativeTo(sc.getByName("main").toJFrame());
		d.setVisible(true);
	}
	public static void scrollMessage(String title, String message) {
		JDialog d = new JDialog(sc.getByName("main").toJFrame());
		d.setTitle(title);
		d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		d.setModal(true);
		d.setSize(200, 200);
		
		JPanel p = new JPanel();
		NoEditTA tf = new NoEditTA(message);
		p.add(new JScrollPane(tf));
		p.setLayout(new GridLayout(1,1,5,5));
		p.setBorder(new EmptyBorder(5,5,5,5));
		d.add(p, BorderLayout.CENTER);
		
		JPanel buttonp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton b = new JButton(lang.get("ok").toString());
		b.addActionListener(new ActionListener(){ public void actionPerformed(ActionEvent e){ d.dispose(); }});  
		buttonp.add(b, BorderLayout.SOUTH);
		d.add(buttonp, BorderLayout.SOUTH);
		
		d.setLocationRelativeTo(sc.getByName("main").toJFrame());
		d.setVisible(true);
		tf.setCaretPosition(0);
	}
	public static class NoEditTA extends JTextArea{
		boolean editing = false;
		int caretPos = -1;
		public NoEditTA(String s) {
			setText(s);
			this.setComponentPopupMenu(copyMenu(false));
			//uneditable
			this.getDocument().addDocumentListener(new DocumentListener() {
				  public void changedUpdate(DocumentEvent e) {
				  	  changed(e);
				  }
				  public void removeUpdate(DocumentEvent e) {
					  changed(e);
				  }
				  public void insertUpdate(DocumentEvent e) {
					  changed(e);
				  }
				  public void changed(DocumentEvent e) {
					  if (editing) return;
					  caretPos = NoEditTA.this.getCaretPosition();
					  Runnable undo = new Runnable() {
					        @Override
					        public void run() {
					        	editing = true;
					            NoEditTA.this.setText(s);
					            editing = false;
					            NoEditTA.this.setCaretPosition(caretPos);
					        }
					    };       
					    SwingUtilities.invokeLater(undo);
				  }
			});
		}
	}
	public static class NoEditTF extends JTextField{
		boolean editing = false;
		int caretPos = -1;
		public NoEditTF(String s) {
			setText(s);
			this.setComponentPopupMenu(copyMenu(false));
			//select all on focus
			addFocusListener(new FocusListener() {

	            @Override
	            public void focusGained(FocusEvent e) {
	                NoEditTF.this.select(0, getText().length());
	            }

	            @Override
	            public void focusLost(FocusEvent e) {
	                //NoEditTF.this.select(0, 0);
	            }
	        });
			//uneditable
			this.getDocument().addDocumentListener(new DocumentListener() {
				  public void changedUpdate(DocumentEvent e) {
				  	  changed(e);
				  }
				  public void removeUpdate(DocumentEvent e) {
					  changed(e);
				  }
				  public void insertUpdate(DocumentEvent e) {
					  changed(e);
				  }
				  public void changed(DocumentEvent e) {
					  if (editing) return;
					  caretPos = NoEditTF.this.getCaretPosition();
					  Runnable undo = new Runnable() {
					        @Override
					        public void run() {
					        	editing = true;
					            NoEditTF.this.setText(s);
					            editing = false;
					            NoEditTF.this.setCaretPosition(caretPos);
					        }
					    };       
					    SwingUtilities.invokeLater(undo);
				  }
			});
		}
	}
	
	public static void propertiesGui(Index index) {
		Line l = sblines.get(index.getGroup()).get(index.getMember());
		JDialog dialog = new JDialog(sc.getByName("main").toJFrame());
		dialog.setTitle(lang.get("propertiesOf").toString().replace("[v]", "\"" + l.getMessage().getDisplay() + "\""));
		JPanel pointerpanel = new JPanel();
		pointerpanel.setBorder(new TitledBorder(lang.get("pointer").toString()));
		pointerpanel.add(new JLabel(lang.get("beforeSave").toString()));
		pointerpanel.add(new NoEditTF(l.getMessagePointer().getHex()));
		pointerpanel.add(new NoEditTF(l.getPointerPointer().getHex()));
		pointerpanel.add(new JLabel(lang.get("afterSave").toString()));
		pointerpanel.add(new NoEditTF(""));
		pointerpanel.add(new NoEditTF(""));
		pointerpanel.setLayout(new GridLayout(2,3,3,3));
		JPanel contentpanel = new JPanel();
		contentpanel.setBorder(new TitledBorder("HEX"));
		{
			NoEditTF textField = new NoEditTF((new ByteSequence(l.getMessage().getByteArray()).getHex()) + new ByteSequence(l.getMessage().getEndline()).getHex());

		    JScrollBar scrollBar = new JScrollBar(JScrollBar.HORIZONTAL);

		    JPanel panel = new JPanel();
		    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		    BoundedRangeModel brm = textField.getHorizontalVisibility();
		    scrollBar.setModel(brm);
		    panel.add(textField);
		    textField.setCaretPosition(0);
		    panel.add(scrollBar);
		    
		    contentpanel.add(panel, BorderLayout.NORTH);
		    contentpanel.setLayout(new GridLayout(1, 1));
		}
		//contentpanel.add(new NoEditTF(new ByteSequence(l.getMessage().getByteArray()).getHex()));
		dialog.add(pointerpanel);
		dialog.add(contentpanel);
		dialog.setLayout(new GridLayout(3,3));  
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setModal(true);
		//dialog.setResizable(false);
		dialog.setSize(300,220);
		dialog.setLocationRelativeTo(sc.getByName("main").toJFrame());
		
		dialog.setVisible(true);
	}
	public static String stringLimit(String string, int limit) {
		if (string.length() <= limit) return string;
		StringBuilder sb = new StringBuilder(string);
		sb.setLength(limit);
		sb.append("...");
		return sb.toString();
	}
	public static boolean hasLongLine(String string, int limit) {
		String si = string
		.replace("<RED>", "")
		.replace("<BLUE>", "")
		.replace("<BLACK>", "")
		.replace("<GREEN>", "")
		.replace("<PURPLE>", "")
		.replace("<WHITE>", "")
		;
		for (String s: si.split("\n")) {
			if (s.length() > limit) return true;
		}
		return false;
	}
	public static void setRecent(JMenu jm) {
		jm.removeAll();
		if (prefs.isNull("recentFiles")) { jm.setEnabled(false); return; }
		jm.setEnabled(true);
		String[] dirs = prefs.getStringArray("recentFiles");
		for (int i = 0; i < dirs.length; i++) {
			String prefix = String.format("%02d", (i + 1));
			String s = dirs[i];
			if (s==null) {
				 JMenuItem jmi = new JMenuItem(prefix + ": --");
				 jmi.setEnabled(false);
				 jm.add(jmi);
				 continue;
			}
			JMenuItem jmi = new JMenuItem();
			jmi.setText(prefix + ": " + s);
			jmi.addActionListener(new ActionListener(){ public void actionPerformed(ActionEvent e){ openRom(new File(s)); }});  
			jm.add(jmi);
		}
		prefs.save();
	}
	public static void openRom(File f) {
		if (f == null) {
			File defaultDir = new File(System.getProperty("user.dir"));
			if (!prefs.isNull("lastOpened")) {
				File dir = new File(prefs.getString("lastOpened"));
				if (dir.exists()) defaultDir = dir;
			}
			
			JFileChooser fileChooser = new NativeJFileChooser(defaultDir);
			FileNameExtensionFilter fnefgba = new FileNameExtensionFilter(lang.get("fileType").toString().replace("[v]", "GBA"), "gba");
			FileNameExtensionFilter fnefall = new FileNameExtensionFilter(lang.get("fileTypeAll").toString(), "*.*");
			fileChooser.addChoosableFileFilter(fnefgba);
			fileChooser.addChoosableFileFilter(fnefall);
			
			fileChooser.setFileFilter(fnefgba);
			fileChooser.setDialogTitle(lang.get("open").toString());
			int returnValue = fileChooser.showOpenDialog(null);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
	            openRomAction(fileChooser.getSelectedFile(), true);
	        } else return;
		}
		else {
			openRomAction(f, false);
		}
	}
	public static class Index{
		private int groupvar, membervar;
		public Index(int group, int member) {
			groupvar = group;
			membervar = member;
		}
		public int getGroup() {
			return groupvar;
		}
		public int getMember() {
			return membervar;
		}
		public String toString() {
			return lang.get("indexToString").toString().replace("[v1]", "" + (groupvar + 1)).replace("[v2]", "" + (membervar + 1));
		}
		public boolean equals(Index index) {
			if (index.getGroup() != this.getGroup()) return false;
			if (index.getMember() != this.getMember()) return false;
			return true;
		}
	}
	
	public static List<Index> indexes = new ArrayList<>();
	
	public static String[] errors = new String[0];
	
	public static void openRomAction(File selectedFile, boolean pref) {
		if (pref) prefs.put("lastOpened", selectedFile.getParent());
        prefs.save();
        String error = setRom(selectedFile.toURI());
        if (error != null) {
        	JOptionPane.showMessageDialog(sc.getByName("main").toJFrame(), error, lang.get("error").toString(), JOptionPane.ERROR_MESSAGE);
        	return;
        }
        addToRecent(selectedFile);
        
        sc.getByName("list").toJList().setEnabled(false);
        sc.getByName("ta").toJTextArea().setEnabled(false);
        sc.getByName("ta").toJTextArea().setText("");
        sc.getByName("list").toJList().setSelectedIndex(2);
        dlm.clear();
        for (int i = 0; i < sblines.size(); i++) {
            for (int ii = 0; ii < sblines.get(i).size(); ii++) {
            	dlm.addElement(sblines.get(i).get(ii).getMessage().getDisplay());
            	indexes.add(new Index(i, ii));
            }
        }
        errors = new String[dlm.size()];

        sc.getByName("list").toJList().setEnabled(true);
        sc.getByName("list").toJList().setSelectedIndex(0);
        sc.getByName("list").toJList().requestFocus();
        sc.getByName("save").toJMenuItem().setEnabled(true);
        sc.getByName("saveAs").toJMenuItem().setEnabled(true);
        sc.getByName("search").toJMenuItem().setEnabled(true);
        sc.getByName("edit").toJMenu().setEnabled(true);
	}
	public static void addToRecent(File f) {
		int limit = 10;
		List<String> sl = new ArrayList<>();
		if (!prefs.isNull("recentFiles")) {
			for (String s: prefs.getStringArray("recentFiles")) {
				sl.add(s);
				if (sl.size() >= limit) break;
			}
		}
		{//remove dupe
			int queue=-1;
			for (int i = 0; i < sl.size(); i++) {
				String s = sl.get(i);
				if (s==null) continue;
				if (s.equals(f.toString())) {
					queue=i;
					break;
				}
			}
			if (queue!=-1) sl.remove(queue);
		}
		sl.add(0, f.toString());
		
		String[] op = new String[limit];
		for (int i = 0; i < op.length; i++) {
			if (i >= sl.size()) {
				op[i] = null;
				continue;
			}
			op[i] = sl.get(i);
		}
		prefs.putArray("recentFiles", op);
		setRecent(sc.getByName("recent").toJMenu());
	}
	public static String getLocalization() {
		Scanner scanner = new Scanner(Main.class.getResourceAsStream("localization.json"), "UTF-8");
		scanner.useDelimiter("\\A");
		String s = scanner.next();
		scanner.close();
		return s;
	}
	public static void setLang(String si) {
		String guess = si;
		JSONObject j = null;
		try { j = (JSONObject) new JSONParser().parse(getLocalization()); } catch (Exception e){ }
		String[] langkeys = new String[j.size()];
		{ int i = 0; for (Object o: j.keySet()) { langkeys[i] = o.toString(); i++; } }
		String setKey = null;
		for (String s:langkeys) {
			if (guess.equals(s)) {
				setKey = s;
				break;
			}
		}
		if (setKey == null) {
			if (guess.contains("-")) guess = guess.substring(0, guess.indexOf("-")+1) + "*";
			else guess = guess + "-*";
			for (String s:langkeys) {
				if (guess.equals(s)) {
					setKey = s;
					break;
				}
			}
		}
		if (setKey == null) setKey = "en-*";
		
		lang = (JSONObject) j.get(setKey);
		for (int i = 0; i < sc.size(); i++) {
			sc.get(i).setLang();
		}
		prefs.put("language", setKey);
		prefs.save();
		
		taCopyMenu();
		int sel = sc.getByName("list").toJList().getSelectedIndex();
		sc.getByName("list").toJList().clearSelection();
		sc.getByName("list").toJList().setSelectedIndex(sel);
	}
	
	public static void taCopyMenu() {
		sc.getByName("ta").toJTextArea().addMouseListener(new MouseAdapter() { public void mouseReleased(MouseEvent e) { if (e.isPopupTrigger()) {
			if (!((JTextArea) e.getSource()).isEnabled()) return;
			copyMenu(true).show((JTextArea) e.getSource(), e.getX(), e.getY());
		}}});
		
		
		//.setComponentPopupMenu(copyMenu(true));
	}
	
	public static File rompath = null;
	
	public static String setRom(URI u) {
	 //read rom and parse lines
		try { rom = Files.readAllBytes(Paths.get(u)); } catch (IOException e) { return e.toString(); }
		
		sblines.clear();
		addLines(sblines, "edfe8c", 2299); //story mode
		addLines(sblines, "ed9c2c", 309); //Emerl card descriptions
		addLines(sblines, "EDCD7C", 17); //options menu lines
		addLines(sblines, "EDB9CC", 8); //battle menu lines
		addLines(sblines, "EDBF60", 35); //battle rules lines
		addLines(sblines, "EDD3B0", 16); //training mode menu lines
		addLines(sblines, "EDC3FC", 9); //minigame name lines
		addLines(sblines, "EDCFD0", 40); //battle record menu lines
		addLines(sblines, "EDC9D0", 7); //captured technique dialog lines
		addLines(sblines, "EDD5D0", 8); //story mode episode select menu lines
		rompath = new File(u);
		return null;
	}
	
	public static void save(File f) {
		//check problematic lines
		for (Index i: indexes) {
			if (sblines.get(i.getGroup()).get(i.getMember()).getMessage().isProblematic()) {
				searchGui(dlm, true);
				return;
			}
		}
		
		String line = "";
		File savepath = null;
		if (f == null) {
			line = lang.get("saveAs").toString();
			File defaultDir = new File(System.getProperty("user.dir"));
			if (!prefs.isNull("lastOpened")) {
				File dir = new File(prefs.getString("lastOpened"));
				if (dir.exists()) defaultDir = dir;
			}
			
			JFileChooser fileChooser = new NativeJFileChooser(defaultDir);
			FileNameExtensionFilter fnefgba = new FileNameExtensionFilter(lang.get("fileType").toString().replace("[v]", "GBA"), "gba");
			FileNameExtensionFilter fnefall = new FileNameExtensionFilter(lang.get("fileTypeAll").toString(), "*.*");
			fileChooser.addChoosableFileFilter(fnefgba);
			fileChooser.addChoosableFileFilter(fnefall);
			
			fileChooser.setFileFilter(fnefgba);
			fileChooser.setDialogTitle(line);
			int returnValue = fileChooser.showSaveDialog(null);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
	            savepath = fileChooser.getSelectedFile();
	        } else return;
		} else {
			line = lang.get("save").toString();
			if (JOptionPane.showConfirmDialog(null, lang.get("overwrite").toString().replace("[v]", f.toString()), line,
			        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			    // yes option
				savepath = f;
			} else {
			    // no option
				return;
			}
		}
		
		prefs.put("lastOpened", savepath.toString());
		
		for (int i = 0; i < sblines.size(); i++){
			List<Byte> l = new ArrayList<>();
			List<Pointer> p = new ArrayList<>();
			List<Pointer> pp = new ArrayList<>();
			int position = sblines.get(i).get(0).getMessagePointer().getDec();
			for (int ii = 0; ii < sblines.get(i).size(); ii++) {
				p.add(new Pointer(position + l.size()));
				pp.add(sblines.get(i).get(ii).getPointerPointer());
				for (byte b:sblines.get(i).get(ii).getMessage().getByteArray()) l.add(b);
				for (byte b:sblines.get(i).get(ii).getMessage().getEndline()) l.add(b);
			}
			
			for (int iii = 0; iii < l.size(); iii++) {
				rom[position + iii] = l.get(iii);
			}
			for (int iii = 0; iii < p.size(); iii++) {
				int iiii = 0;
				for (byte b: new ByteSequence(p.get(iii).reverse().getHex()).getByteArray()) {
					rom[pp.get(iii).getDec() + iiii] = b;
					iiii++;
				}
			}
		}
		
		try (FileOutputStream stream = new FileOutputStream(savepath, false)) {
		    stream.write(rom);
		} catch (IOException e) { 
			JOptionPane.showMessageDialog(sc.getByName("main").toJFrame(), e.toString(), lang.get("error").toString(), JOptionPane.ERROR_MESSAGE);
		}
		
		rompath = savepath;
		
		JOptionPane.showMessageDialog(sc.getByName("main").toJFrame(), lang.get("saved").toString(), line, JOptionPane.INFORMATION_MESSAGE);
	}
	
	public static void parseLib() {
		//parse SonicBattleTextHEXtable.json for text conversion
		try{
			SonicBattleTextLib sbtl = new SonicBattleTextLib();
			JSONObject json = (JSONObject) new JSONParser().parse((new Scanner(Main.class.getResourceAsStream("SonicBattleTextHEXtable.json"), "UTF-8").useDelimiter("\\A").next()));
			for (Object o: json.keySet()) {
				String key = o.toString();
				String value = (String) json.get(key);
				sbtl.add(new ByteSequence(key).getByteArray(), value);
			}
			sbtp = new SonicBattleTextParser(sbtl);
		} catch (ParseException e) { e.printStackTrace(); }
	}
//	public static String linesToJSONString(List<LineGroup> sblines) {
//		StringBuilder sb = new StringBuilder("{\r\n");
//		for (LineGroup lg:sblines) {
//			sb.append(lg.toJSONStringWithoutBrackets() + ",\n");
//		}
//		sb.setLength(sb.length()-2);
//		sb.append("\r\n}");
//		return sb.toString();
//	}
	public static void addLines(List<LineGroup> l, String p, int a) {
		l.add(new LineGroup());
		for (int i = 0; i < a; i++) l.get(l.size()-1).add(new Line(new Pointer(p).add(i * 4)));
	}
	public static class LineGroup extends ArrayList<Line>{
//		private static int idcount = 0;
//		private int id = idcount++;
//		public int getID() {
//			return id;
//		}
		public LineGroup() {
//			new Line(null).resetID();
		}
//		public String toJSONStringWithoutBrackets() {
//			StringBuilder sb = new StringBuilder("");
//			//sb.append("{\r\n");
//			int i = 0;
//			int count = this.size();
//			int group = this.getID();
//			for (Line l:this) {
//				JSONObject j = new JSONObject();
//				String key = group + "," + l.getID();
//				j.put(key, l.getMessage().getContent());
//				String s = j.toString();
//				j = (JSONObject) new JSONObject();
//				j.put("", key);
//				String jsonkey = j.toString().substring(4).replaceFirst(".$","") + ":";
//				s = new StringBuilder(s).insert(jsonkey.length() + 1, " ").toString();
//				sb.append("	" + s.substring(1, s.length()-1));
//				if (i!=count-1) {
//					sb.append(",");
//					sb.append("\n");
//				}
//				i++;
//			}
//			//sb.append("}");
//			return sb.toString();
//		}
	}
	public static byte[] rom = null;
	
	public static class Line{
//		private static int idcount = 0;
		private static byte[] endline = new byte[] { (byte) 0xFE, (byte) 0xFF }; //FEFF is end of line
		private Pointer pointerPointer = null;
		private Pointer messagePointer = null;
		private Message message = new Message();
		private boolean problematicvar = false;
		public class Message{
			private byte[] array;
			private String content;
			private String display;
			public void set(byte[] b) {
				array = b;
				content = sbtp.parseString(b);
				
				JSONObject j = new JSONObject();
				j.put("", content);
				display = j.toString().substring(5).replaceFirst(".$","").replaceFirst(".$","");
			}
			public String getDisplay() {
				return display;
			}
			public String getContent() {
				return content;
			}
			public byte[] getByteArray() {
				return array;
			}
			public boolean isProblematic() {
				return problematicvar;
			}
			public void setProblematic(boolean problematic) {
				problematicvar = problematic;
			}
			public String toString() {
				if (problematicvar) return lang.get("error").toString();
				return "\"" + display + "\"";
			}
			public byte[] getEndline() {
				return endline;
			}
		}
		public Line(Pointer pointer) {
			if (pointer==null) {
//				idcount--;
				return;
			}
			pointerPointer = pointer;
			{//pointer of message
				int pos = pointerPointer.getDec();
				byte[] mb = new byte[] { rom[pos], rom[pos + 1], rom[pos + 2] };
				ByteSequence bs = new ByteSequence(mb);
				Pointer mp = new Pointer(bs.getHex()).reverse();
				messagePointer = mp;
			}
			{//set message string
				int pos = messagePointer.getDec();
				List<Byte> bl = new ArrayList<>();
				while (true) {
					byte[] focus = new byte[] { rom[pos], rom[pos+1] };
					// if byte array is equal to endline
					if (endline.length == focus.length) {
						boolean match = true;
						for (int i = 0; i < focus.length; i++) {
							if (endline[i] != focus[i]) match = false;
						}
						if (match) break;
					}
					pos+=2;
					for (byte b: focus) bl.add(b);
				}
				
				byte[] arr = new byte[bl.size()];
				for (int i = 0; i < arr.length; i++) arr[i] = bl.get(i);
				
				message.set(arr);
			}
		}
//		public void resetID() {
//			idcount=0;
//		}
		public Message getMessage() {
			return message;
		}
		public Pointer getMessagePointer() {
			return messagePointer;
		}
		public Pointer getPointerPointer() {
			return pointerPointer;
		}
	}
}
