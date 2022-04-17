package sahlaysta.sonicbattletexteditor.ui.components;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

/** A JMenu component of Recently Opened Files. Adds Files with the <code>addFile</code> method */
public class RecentlyOpenedFilesJMenu extends JMenu {
	private static final long serialVersionUID = 1L;

	//Constructor
	/** The array of Files used to show on the JMenu */
	protected final File[] files;
	/** Construct RecentlyOpenedFilesJMenu with the passed File array */
	public RecentlyOpenedFilesJMenu(File[] files) {
		if (files.length > 99)
			throw new IllegalArgumentException("RecentlyOpenedFilesJMenu: Count cannot be greater than 99");
		if (files.length < 1)
			throw new IllegalArgumentException("RecentlyOpenedFilesJMenu: Count cannot be less than 1");
		this.files = files;
		refreshFileMenu();
	}
	/** Construct RecentlyOpenedFilesJMenu with the number of max recent items */
	public RecentlyOpenedFilesJMenu(int maxRecentItems) {
		this(new File[maxRecentItems]);
	}
	/** Construct RecentlyOpenedFilesJMenu with a default amount of 10 max recent items */
	public RecentlyOpenedFilesJMenu() {
		this(10);
	}
	
	//Public Methods
	/** Returns the array of Files of this RecentlyOpenedFilesJMenu */
	public File[] getFiles() {
		return files;
	}
	
	/** Adds a file to the JMenu and refreshes the JMenu */
	public void addFile(File file) {
		removeFile(file); //remove file if exists; prevents duplicate files
		for (int i = files.length - 2; i >= 0; i--) //move all files down on the list
			files[i + 1] = files[i];
		files[0] = file; //set the file to the first option
		refreshFileMenu();
	}
	
	/** Removes a file from the JMenu and refreshes the JMenu */
	public void removeFile(int index) {
		for (int i = 1 + index; i < files.length; i++) //move files up starting at the index
			files[i - 1] = files[i];
		refreshFileMenu();
	}
	
	/** Removes a file from the JMenu and refreshes the JMenu */
	public void removeFile(File file) {
		for (int i = 0; i < files.length; i++) //find the index of the file in the 'files' array
			if (file.equals(files[i])) {
				removeFile(i);
				break;
			}
	}
	
	/** Refresh the RecentlyOpenedFilesJMenu and populate JMenuItems */
	public void refreshFileMenu() {
		for (Component c: super.getMenuComponents()) //clear all JMenuItems
			super.remove(c);
		for (int i = 0; i < files.length; i++) {
			String num = String.format("%02d", 1 + i); // format number to have 2 digits always: 01, 02
			if (files[i] == null) { //blank option: --
				JMenuItem jmi = new JMenuItem(num + ": --");
				jmi.setEnabled(false);
				add(jmi);
			} else {
				final File file = files[i];
				JMenuItem jmi = new JMenuItem(num + ": " + file); //file example: "03: C:\file.txt"
				jmi.addActionListener(new ActionListener() { //Action when this file is clicked
					@Override
					public void actionPerformed(ActionEvent e) { //Call the RecentlyOpenedFilesJMenuListeners
						for (RecentlyOpenedFilesJMenuListener l: recentlyOpenedFilesJMenuListeners)
							l.actionPerformed(e, file);
					}
				});
				add(jmi);
			}
		}
	}
	
	// RecentlyOpenedFilesJMenuListener handler
	/** The LinkedHashSet collection of RecentlyOpenedFilesJMenuListeners */
	protected Set<RecentlyOpenedFilesJMenuListener> recentlyOpenedFilesJMenuListeners = new LinkedHashSet<>();
	/** Add a listener of the RecentlyOpenedFile JMenuItems */
	public void addRecentlyOpenedFilesJMenuListener(RecentlyOpenedFilesJMenuListener l) {
		recentlyOpenedFilesJMenuListeners.add(l);
	}
	/** Remove a listener from the registered RecentlyOpenedFilesJMenuListeners */
	public void removeRecentlyOpenedFilesJMenuListener(RecentlyOpenedFilesJMenuListener l) {
		recentlyOpenedFilesJMenuListeners.remove(l);
	}
	/** Return the array of the registered RecentlyOpenedFilesJMenuListeners */
	public RecentlyOpenedFilesJMenuListener[] getRecentlyOpenedFilesJMenuListeners() {
		RecentlyOpenedFilesJMenuListener[] output = new RecentlyOpenedFilesJMenuListener[recentlyOpenedFilesJMenuListeners.size()];
		int i = 0;
		for (RecentlyOpenedFilesJMenuListener l: recentlyOpenedFilesJMenuListeners)
			output[i++] = l;
		return output;
	}
}