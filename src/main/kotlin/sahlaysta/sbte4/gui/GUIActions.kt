package sahlaysta.sbte4.gui

import com.formdev.flatlaf.FlatDarkLaf
import com.formdev.flatlaf.FlatLaf
import com.formdev.flatlaf.FlatLightLaf
import com.formdev.flatlaf.extras.FlatAnimatedLafChange
import sahlaysta.sbte4.rom.SBTEROM
import sahlaysta.sbte4.rom.SBTEROMData
import sahlaysta.sbte4.rom.blob.string.SBTEStringEncoder
import sahlaysta.swing.JTextComponentEnhancer
import java.awt.Toolkit
import java.awt.event.KeyEvent
import java.io.File
import java.nio.file.Files
import javax.swing.KeyStroke
import javax.swing.UIManager

//the GUI actions.
internal class GUIActions(val gui: GUI) {

    sealed class GUIAction(val action: () -> Unit)

    class GUIActionWithShortcut(val keyStroke: KeyStroke, action: () -> Unit) : GUIAction(action)

    class GUIActionWithoutShortcut(action: () -> Unit) : GUIAction(action)

    private val mMask = Toolkit.getDefaultToolkit().menuShortcutKeyMask
    private val shiftMask = KeyEvent.SHIFT_DOWN_MASK

    val open = GUIActionWithShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_O, mMask), ::openAction)
    private fun openAction() {
        if (!preCloseROM(fromOpen = true)) return
        val filePath = gui.fileChooser.fileChooserOpenFile(
            "open", gui.jFrame, "Open ROM", "GBA Files", "gba")
        if (filePath != null) {
            closeROM()
            openROM(filePath)
        }
    }

    val save = GUIActionWithShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_S, mMask), ::saveAction)
    private fun saveAction() {
        if (!preSaveROM()) return
        saveROM(gui.editor.romFilePath!!, fromSaveAs = false)
    }

    val saveAs = GUIActionWithShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_S, mMask or shiftMask), ::saveAsAction)
    private fun saveAsAction() {
        if (!preSaveROM()) return
        val filePath = gui.fileChooser.fileChooserSaveFile(
            "save", gui.jFrame, "Save ROM as", "GBA Files", "gba")
        if (filePath != null)
            saveROM(filePath, fromSaveAs = true)
    }

    val close = GUIActionWithShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_W, mMask), ::closeAction)
    private fun closeAction() {
        if (preCloseROM(fromOpen = false))
            closeROM()
    }

    val undo = GUIActionWithShortcut(JTextComponentEnhancer.getPlatformUndoKeyStrokes()[0], ::undoAction)
    private fun undoAction() {
        gui.undoManager.apply { if (canUndo()) undo() }
    }

    val redo = GUIActionWithShortcut(JTextComponentEnhancer.getPlatformRedoKeyStrokes()[0], ::redoAction)
    private fun redoAction() {
        gui.undoManager.apply { if (canRedo()) redo() }
    }

    val goUp = GUIActionWithShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_UP, mMask), ::goUpAction)
    private fun goUpAction() {
        gui.editor.tree.goUpOneRow()
    }

    val goDown = GUIActionWithShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, mMask), ::goDownAction)
    private fun goDownAction() {
        gui.editor.tree.goDownOneRow()
    }

    val expand = GUIActionWithShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, mMask), ::expandAction)
    private fun expandAction() {
        gui.editor.tree.toggleExpandSelectedRow()
    }

    val jsonExport = GUIActionWithoutShortcut(::jsonExportAction)
    private fun jsonExportAction() {
        val filePath = gui.fileChooser.fileChooserSaveFile(
            "jsonexport", gui.jFrame, gui.menuBar.jsonExportJMenuItem.text, "JSON Files", "json")
        if (filePath != null)
            exportJSON(filePath)
    }

    val jsonImport = GUIActionWithoutShortcut(::jsonImportAction)
    private fun jsonImportAction() {
        val filePath = gui.fileChooser.fileChooserOpenFile(
            "jsonimport", gui.jFrame, gui.menuBar.jsonImportJMenuItem.text, "JSON Files", "json")
        if (filePath != null)
            importJSON(filePath)
    }

    val find = GUIActionWithShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_F, mMask), ::findAction)
    private fun findAction() {
        gui.search.showSearchDialog()
    }

    val errors = GUIActionWithoutShortcut(::errorsAction)
    private fun errorsAction() {
        gui.search.showErrorsDialog()
    }

    val darkTheme = GUIActionWithoutShortcut(::darkThemeAction)
    private fun darkThemeAction() {
        FlatAnimatedLafChange.showSnapshot()
        if (gui.menuBar.darkThemeJMenuItem.isSelected)
            UIManager.setLookAndFeel(FlatDarkLaf::class.java.name)
        else
            UIManager.setLookAndFeel(FlatLightLaf::class.java.name)
        FlatLaf.updateUI()
        FlatAnimatedLafChange.hideSnapshotWithAnimation()
    }

    val textPreview = GUIActionWithoutShortcut(::textPreviewAction)
    private fun textPreviewAction() {
        gui.textPreview.showTextPreview()
    }

    val about = GUIActionWithShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), ::aboutAction)
    private fun aboutAction() {
        GUIDialogs.showTextBlockMsgDialog(
            gui.jFrame, GUIUtil.getResourceString("/aboutmessage.txt"), gui.menuBar.aboutJMenuItem.text)
    }

    private var saveUndoIndex = 0
    val romIsOpen get() = gui.editor.romData != null
    val hasUnsavedChanges get() = gui.editor.hasUnsavedImportedStrings || saveUndoIndex != gui.undoManager.undoIndex

    fun openROM(filePath: String) {
        val canonicalFilePath: String
        val romData: SBTEROMData
        try {
            canonicalFilePath = File(filePath).canonicalPath
            require(File(filePath).length() < (256 * (1024 * 1024))) { "File too big > 256 MB" }
            val rom = Files.readAllBytes(File(filePath).toPath())
            romData = SBTEROM.readROMData(rom)
        } catch (e: Exception) {
            e.printStackTrace()
            GUIDialogs.showFileReadErrDialog(gui.jFrame, filePath, e, gui.menuBar.openJMenuItem.text)
            return
        }
        gui.editor.setROMData(romData, canonicalFilePath)
        gui.openRecentMenu.addFilePath(gui.editor.romFilePath!!)
        whenROMOpened()
    }

    fun preSaveROM(): Boolean {
        gui.editor.waitForBackgroundThread()
        for (editorString in gui.editor.allStrings()) {
            val text: String?
            val hasError: Boolean
            editorString.callInfo { esText, _, esHasError, _ -> text = esText; hasError = esHasError }
            if (text == null) {
                editorString.romBlob.binary = null
            } else if (hasError) {
                gui.search.showErrorsDialog()
                return false
            } else {
                editorString.romBlob.binary = SBTEStringEncoder.encodeString(text)
            }
        }
        return true
    }

    fun saveROM(filePath: String, fromSaveAs: Boolean) {
        try {
            SBTEROM.saveROMData(gui.editor.romData!!)
            Files.write(File(filePath).toPath(), gui.editor.romData!!.rom)
        } catch (e: Exception) {
            e.printStackTrace()
            GUIDialogs.showFileWriteErrDialog(gui.jFrame, filePath, e,
                (if (fromSaveAs) gui.menuBar.saveAsJMenuItem else gui.menuBar.saveJMenuItem).text)
            return
        }
        gui.openRecentMenu.addFilePath(filePath)
        if (fromSaveAs)
            gui.editor.updateROMFilePath(filePath)
        gui.editor.hasUnsavedImportedStrings = false
        saveUndoIndex = gui.undoManager.undoIndex
        whenUndoStateChanged()
    }

    fun preCloseROM(fromOpen: Boolean): Boolean {
        if (romIsOpen && hasUnsavedChanges) {
            if (!GUIDialogs.showNoYesDialog(gui.jFrame, "Close without saving?",
                    (if (fromOpen) gui.menuBar.openJMenuItem else gui.menuBar.closeJMenuItem).text)) {
                return false
            }
        }
        return true
    }

    fun closeROM() {
        gui.editor.waitForBackgroundThread()
        gui.editor.clearEditor()
        whenROMClosed()
    }

    fun exportJSON(filePath: String) {
        try {
            gui.jsonExport.exportJSON(filePath)
        } catch(e: Exception) {
            e.printStackTrace()
            GUIDialogs.showFileWriteErrDialog(gui.jFrame, filePath, e, gui.menuBar.jsonExportJMenuItem.text)
            return
        }
    }

    fun importJSON(filePath: String) {
        try {
            gui.jsonExport.importJSON(filePath)
        } catch(e: Exception) {
            e.printStackTrace()
            GUIDialogs.showFileReadErrDialog(gui.jFrame, filePath, e, gui.menuBar.jsonImportJMenuItem.text)
            return
        }
    }

    fun whenGUIStarted() {
        enableOrDisableJMenuItems(romOpen = false)
    }

    fun whenGUIExited() {
        gui.prefs.writePrefs()
    }

    fun whenROMOpened() {
        enableOrDisableJMenuItems(romOpen = true)
        whenUndoStateChanged()
    }

    fun whenROMClosed() {
        enableOrDisableJMenuItems(romOpen = false)
        whenUndoStateChanged()
    }

    fun whenUndoStateChanged() {
        gui.menuBar.undoJMenuItem.isEnabled = gui.undoManager.canUndo()
        gui.menuBar.redoJMenuItem.isEnabled = gui.undoManager.canRedo()
        if (!gui.undoManager.canUndo() && !gui.undoManager.canRedo())
            saveUndoIndex = 0
        if (gui.editor.romData != null) {
            if (hasUnsavedChanges) {
                gui.jFrame.title = "*${gui.editor.romFilePath} - Sonic Battle Text Editor"
            } else {
                gui.jFrame.title = "${gui.editor.romFilePath} - Sonic Battle Text Editor"
            }
        } else {
            gui.jFrame.title = "Sonic Battle Text Editor"
        }
    }

    private fun enableOrDisableJMenuItems(romOpen: Boolean) {
        gui.menuBar.saveJMenuItem.isEnabled = romOpen
        gui.menuBar.saveAsJMenuItem.isEnabled = romOpen
        gui.menuBar.closeJMenuItem.isEnabled = romOpen
        gui.menuBar.editJMenu.isEnabled = romOpen
        gui.menuBar.searchJMenu.isEnabled = romOpen
    }

}