package sahlaysta.sbte4.gui

import java.awt.Component
import java.io.File
import java.util.Locale
import javax.swing.JDialog
import javax.swing.JFileChooser
import javax.swing.JTextField
import javax.swing.filechooser.FileFilter
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.plaf.basic.BasicFileChooserUI

/* improved file chooser dialogs.
   added prompts such as "overwrite the file?"
   and auto-append file extension */
internal class GUIFileChooser {

    var lastDirectory: String? = null

    class FileSelection(val fileFilterIndex: Int, val fileDirectory: String)

    //remember the file selections of each key
    val fileSelections = HashMap<String, FileSelection>()

    fun fileChooserOpenFile(key: String, parent: Component?, title: String, filterName: String,
                            filterExtension: String): String? {
        val ofc = OpenFileChooser()
        ofc.key = key
        ofc.parent = parent
        ofc.title = title
        ofc.fileFilter = FileChooserFilter(filterName, filterExtension)
        return ofc.showDialog()
    }

    fun fileChooserSaveFile(key: String, parent: Component?, title: String, filterName: String,
                            filterExtension: String): String? {
        val sfc = SaveFileChooser()
        sfc.key = key
        sfc.parent = parent
        sfc.title = title
        sfc.fileFilter = FileChooserFilter(filterName, filterExtension)
        return sfc.showDialog()
    }

    fun fileChooserSaveDir(key: String, parent: Component?, title: String): String? {
        val sdc = SaveDirChooser()
        sdc.key = key
        sdc.parent = parent
        sdc.title = title
        return sdc.showDialog()
    }

    private enum class FileChooserType { OPEN, SAVE }

    private class FileChooserFilter(filterDescription: String, val filterExtension: String) : FileFilter() {
        private val fileNameExtensionFilter = FileNameExtensionFilter(filterDescription, filterExtension)
        override fun accept(f: File?): Boolean = fileNameExtensionFilter.accept(f)
        override fun getDescription(): String = fileNameExtensionFilter.description
    }

    private abstract inner class FileChooser(val type: FileChooserType) {

        private lateinit var transformedSelection: File
        val jfc = object : JFileChooser() {
            override fun createDialog(parent: Component?): JDialog {
                return super.createDialog(parent).also { dialog = it }
            }
            override fun accept(f: File?): Boolean {
                return f?.let { omitFile(it) } == false && super.accept(f)
            }
            override fun approveSelection() {
                val selectedFile = selectedFile!!
                preApproveSelection(selectedFile)?.let { transformedSelection = it; super.approveSelection() }
            }
        }

        init {
            jfc.addPropertyChangeListener(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY) {
                val textField = GUIUtil.findComponent<JTextField>(jfc)
                textField?.let { GUIUtil.clearUndoManager(it) }
            }
            jfc.addPropertyChangeListener(JFileChooser.SELECTED_FILES_CHANGED_PROPERTY) {
                val textField = GUIUtil.findComponent<JTextField>(jfc)
                textField?.let { GUIUtil.clearUndoManager(it) }
            }
        }

        lateinit var dialog: JDialog
        var key: String? = null
        var parent: Component? = null
        var title: String? = null
        var fileFilter: FileChooserFilter? = null

        open fun omitFile(file: File): Boolean { return false }
        open fun preShowDialog() { }
        open fun preApproveSelection(selectedFile: File): File? { return selectedFile }
        fun isDirChooser(): Boolean = this is SaveDirChooser

        fun showDialog(): String? {
            title?.let { jfc.dialogTitle = it }
            fileFilter?.let { jfc.addChoosableFileFilter(it); jfc.fileFilter = it }
            lastDirectory?.let { jfc.currentDirectory = File(it) }
            fileSelections[key]?.let {
                jfc.fileFilter = jfc.choosableFileFilters.elementAtOrNull(it.fileFilterIndex)
                jfc.currentDirectory = File(it.fileDirectory)
            }
            preShowDialog()
            val dialogResult = when (type) {
                FileChooserType.OPEN -> jfc.showOpenDialog(parent)
                FileChooserType.SAVE -> jfc.showSaveDialog(parent)
            }
            return if (dialogResult == JFileChooser.APPROVE_OPTION) {
                val selection = transformedSelection.toString()
                lastDirectory = if (isDirChooser()) selection else jfc.currentDirectory.toString()
                key?.let { fileSelections.put(it,
                    FileSelection(jfc.choosableFileFilters.indexOf(jfc.fileFilter), lastDirectory!!)) }
                selection
            } else null
        }

    }

    private inner class OpenFileChooser : FileChooser(FileChooserType.OPEN) {
        override fun preShowDialog() {
            jfc.isMultiSelectionEnabled = false
        }
        override fun preApproveSelection(selectedFile: File): File? {
            if (selectedFile.exists()) return selectedFile
            val file = appendFileExtension(selectedFile)
            if (file.exists()) return file
            val msg = "File does not exist:\n\n$file"
            GUIDialogs.showErrMsgDialog(dialog, msg, dialog.title)
            return null
        }
        private fun appendFileExtension(selectedFile: File): File {
            val fileFilter = jfc.fileFilter
            if (fileFilter is FileChooserFilter) {
                val fileExtension = fileFilter.filterExtension
                if (!selectedFile.toString().lowercase(Locale.ENGLISH)
                        .endsWith(".${fileExtension.lowercase(Locale.ENGLISH)}")) {
                    return File("$selectedFile.$fileExtension")
                }
            }
            return selectedFile
        }
    }

    private inner class SaveFileChooser : FileChooser(FileChooserType.SAVE) {
        override fun preShowDialog() {
            jfc.isMultiSelectionEnabled = false
        }
        override fun preApproveSelection(selectedFile: File): File? {
            val file = appendFileExtension(selectedFile)
            if (file.exists()) {
                val msg = "Overwrite the file?\n\n$file"
                if (!GUIDialogs.showNoYesDialog(dialog, msg, dialog.title))
                    return null
            }
            return file
        }
        private fun appendFileExtension(selectedFile: File): File {
            val fileFilter = jfc.fileFilter
            if (fileFilter is FileChooserFilter) {
                val fileExtension = fileFilter.filterExtension
                if (!selectedFile.toString().lowercase(Locale.ENGLISH)
                        .endsWith(".${fileExtension.lowercase(Locale.ENGLISH)}")) {
                    return File("$selectedFile.$fileExtension")
                }
            }
            return selectedFile
        }
    }

    private inner class SaveDirChooser : FileChooser(FileChooserType.SAVE) {
        override fun omitFile(file: File) = !file.isDirectory
        override fun preShowDialog() {
            fileFilter = null
            jfc.isMultiSelectionEnabled = false
            jfc.fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES
            jfc.choosableFileFilters.forEach { jfc.removeChoosableFileFilter(it) }
            jfc.addChoosableFileFilter(object : FileFilter() {
                override fun accept(f: File?) = true
                override fun getDescription() = "Folders"
            })
            val ui = jfc.ui
            if (ui is BasicFileChooserUI)
                ui.fileName = jfc.currentDirectory.absolutePath
        }
        override fun preApproveSelection(selectedFile: File): File? {
            if (!selectedFile.exists()) {
                val msg = "The directory does not exist:\n\n$selectedFile"
                GUIDialogs.showErrMsgDialog(dialog, msg, dialog.title)
                return null
            }
            if (!selectedFile.isDirectory) {
                val msg = "The existing path is not a directory:\n\n$selectedFile"
                GUIDialogs.showErrMsgDialog(dialog, msg, dialog.title)
                return null
            }
            return selectedFile
        }
    }

}