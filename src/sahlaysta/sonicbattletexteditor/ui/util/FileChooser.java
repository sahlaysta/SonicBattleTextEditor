package sahlaysta.sonicbattletexteditor.ui.util;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

/** FileChooser is an abstraction of JFileChooser that enhances get methods and FileFilter methods */
public class FileChooser extends JFileChooser {
	private static final long serialVersionUID = 1L;
	
	//Constructors
	/** Construct FileChooser with the default user directory */
	public FileChooser() {
		super();
	}
	/** Construct FileChooser with the passed default directory */
	public FileChooser(File currentDirectory) {
		super(currentDirectory);
	}
	/** Construct FileChooser with the passed FileSystemView */
	public FileChooser(FileSystemView fsv) {
		super(fsv);
	}
	/** Construct FileChooser with the passed String as the default directory */
	public FileChooser(String currentDirectory) {
		super(currentDirectory);
	}
	/** Construct FileChooser with the passed File as the default directory and the passed FileSystemView */
	public FileChooser(File currentDirectory, FileSystemView fsv) {
		super(currentDirectory, fsv);
	}
	/** Construct FileChooser with the passed String as the default directory and the passed FileSystemView */
	public FileChooser(String currentDirectory, FileSystemView fsv) {
		super(currentDirectory, fsv);
	}
	
	//FileFilter override
	/** FileFilter List used to index file filters */
	protected List<FileFilter> fileFilterList;
	@Override
	public void addChoosableFileFilter(FileFilter fileFilter) {
		if (fileFilterList == null) {
			fileFilterList = new ArrayList<>();
			fileFilterList.add(getAcceptAllFileFilter());
		}
		fileFilterList.add(fileFilter);
		super.addChoosableFileFilter(fileFilter);
	}
	@Override
	public boolean removeChoosableFileFilter(FileFilter fileFilter) {
		if (fileFilterList == null) {
			fileFilterList = new ArrayList<>();
			fileFilterList.add(getAcceptAllFileFilter());
		}
		fileFilterList.remove(fileFilter);
		return super.removeChoosableFileFilter(fileFilter);
	}
	@Override
	public void resetChoosableFileFilters() {
		if (fileFilterList == null)
			fileFilterList = new ArrayList<>();
		else
			fileFilterList.clear();
		fileFilterList.add(getAcceptAllFileFilter());
		super.resetChoosableFileFilters();
	}
	/** Returns the added user choosable FileFilter at the index */
	public FileFilter getChoosableFileFilter(int index) {
		if (fileFilterList == null) {
			fileFilterList = new ArrayList<>();
			fileFilterList.add(getAcceptAllFileFilter());
		}
		return fileFilterList.get(index);
	}
	/** Sets the currently selected FileFilter to the passed index */
	public void setFileFilterIndex(int index) {
		if (fileFilterList == null) {
			fileFilterList = new ArrayList<>();
			fileFilterList.add(getAcceptAllFileFilter());
		}
		if (index >= 0 && index < fileFilterList.size())
			setFileFilter(fileFilterList.get(index));
	}
	/** Get the index of FileChooser's currently selected FileFilter */
	public int getFileFilterIndex() {
		if (fileFilterList == null) {
			fileFilterList = new ArrayList<>();
			fileFilterList.add(getAcceptAllFileFilter());
		}
		return fileFilterList.indexOf(getFileFilter());
	}
	/** Add a user choosable file filter with the passed description and file extension */
	public void addChoosableFileFilter(String description, String... fileExtensions) {
		addChoosableFileFilter(new FileNameExtensionFilter(description, fileExtensions));
	}
	
	//Show dialog overloads
	/** True if showOpenDialog() has been called, false if showSaveDialog() has been called */
	protected boolean isOpenDialog;
	/** The parent component that the FileChooser window will display over */
	protected Component parentComponent;
	@Override
	public int showOpenDialog(Component parent) {
		isOpenDialog = true;
		return super.showOpenDialog(parent);
	}
	@Override
	public int showSaveDialog(Component parent) {
		isOpenDialog = false;
		return super.showSaveDialog(parent);
	}
	/** Display the FileChooser in 'open file' mode */
	public int showOpenDialog() {
		return showOpenDialog(parentComponent);
	}
	/** Display the FileChooser in 'save file' mode */
	public int showSaveDialog() {
		return showSaveDialog(parentComponent);
	}
	/** Set the parent component that the FileChooser window will display over */
	public void setParentComponent(Component parentComponent) {
		this.parentComponent = parentComponent;
	}
	/** Get the parent component that the FileChooser window will display over */
	public Component getParentComponent() {
		return parentComponent;
	}
	
	/** The JDialog of the FileChooser */
	protected JDialog jDialog;
	@Override
	protected JDialog createDialog(Component parent) {
		jDialog = super.createDialog(parent);
		return jDialog;
	}
	
	@Override
	public void approveSelection() {
		File file = getSelectedFile();
		if (isOpenDialog && file != null && !file.exists()) { //Can only open files that exist
			JOptionPane.showOptionDialog(
					jDialog,
					fileErrorText + '\n' + file,
					getDialogTitle(),
					0,
					JOptionPane.ERROR_MESSAGE,
					null,
					new Object[] { fileErrorOkText },
					null);
			return;
		} else if (!isOpenDialog && file != null && file.exists()) { //Overwrite file prompt
			if (JOptionPane.showOptionDialog(
					jDialog,
					fileOverwriteText + '\n' + file,
					getDialogTitle(),
					0,
					JOptionPane.QUESTION_MESSAGE,
					null,
					new Object[] { fileOverwriteYesText, fileOverwriteNoText },
					null) != 0)
				return;
		}
		super.approveSelection();
	}
	//FileChooser dialog Strings
	/** The error message text when the selected file does not exist */
	protected String fileErrorText = "The file does not exist:";
	/** The 'OK' button text of the error message when the selected file does not exist */
	protected String fileErrorOkText = "OK";
	/** The text of the overwrite prompt message */
	protected String fileOverwriteText = "Overwrite the file?";
	/** The 'Yes' button text of the overwrite prompt message */
	protected String fileOverwriteYesText = "Yes";
	/** The 'No' button text of the overwrite prompt message */
	protected String fileOverwriteNoText = "No";
	/** Set the error message text when the selected file does not exist */
	public void setFileErrorText(String text) {
		fileErrorText = text;
	}
	/** Return the error message text when the selected file does not exist */
	public String getFileErrorText() {
		return fileErrorText;
	}
	/** Set the 'OK' button text of the error message when the selected file does not exist */
	public void setFileErrorOkText(String text) {
		fileErrorOkText = text;
	}
	/** Return the 'OK' button text of the error message when the selected file does not exist */
	public String getFileErrorOkText() {
		return fileErrorOkText;
	}
	/** Set the text of the overwrite prompt message */
	public void setFileOverwriteText(String text) {
		fileOverwriteText = text;
	}
	/** Return the text of the overwrite prompt message */
	public String getFileOverwriteText() {
		return fileOverwriteText;
	}
	/** Set the 'Yes' button text of the overwrite prompt message */
	public void setFileOverwriteYesText(String text) {
		fileOverwriteYesText = text;
	}
	/** Return the 'Yes' button text of the overwrite prompt message */
	public String getFileOverwriteYesText() {
		return fileOverwriteYesText;
	}
	/** Set the 'No' button text of the overwrite prompt message */
	public void setFileOverwriteNoText(String text) {
		fileOverwriteNoText = text;
	}
	/** Return the 'No' button text of the overwrite prompt message */
	public String getFileOverwriteNoText() {
		return fileOverwriteNoText;
	}
	
	
	//Get selected file override: Append the extension to the filename
	@Override
	public File getSelectedFile() {
		File selectedFile = super.getSelectedFile();
		if (selectedFile != null) {
			e: if (getFileFilter() instanceof FileNameExtensionFilter) {
				String file = selectedFile.toString();
				String[] extensions = ((FileNameExtensionFilter)getFileFilter()).getExtensions();
				if (isOpenDialog) {
					for (String extension: extensions)
						if (file.toLowerCase().endsWith("." + extension.toLowerCase())) {
							break e;
						} else {
							if ((selectedFile = new File(selectedFile.toString() + "." + extension)).exists())
								break e;
						}
				} else {
					for (String extension: extensions)
						if (file.toLowerCase().endsWith("." + extension.toLowerCase()))
							break e;
					selectedFile = new File(selectedFile.toString() + "." + extensions[0]);
				}
				
			}
		}
		return selectedFile;
	}
}