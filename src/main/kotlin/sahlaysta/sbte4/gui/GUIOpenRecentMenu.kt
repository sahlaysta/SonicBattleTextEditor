package sahlaysta.sbte4.gui

import java.io.File
import javax.swing.JMenu
import javax.swing.JMenuItem

//the "Open recent" menu
internal class GUIOpenRecentMenu(val gui: GUI) {

    private companion object {
        const val OPEN_RECENT_ITEMS = 10
    }

    val jMenu = JMenu()
    val filePaths = ArrayList<String>()
    private val menuItems = Array(OPEN_RECENT_ITEMS) { JMenuItem() }

    init {
        updateMenuItems()
        menuItems.forEachIndexed { index, jMenuItem ->
            jMenu.add(jMenuItem)
            jMenuItem.addActionListener {
                if (gui.actions.preCloseROM(fromOpen = true))
                    gui.actions.openROM(filePaths[index])
            }
        }
    }

    fun addFilePath(filePath: String) {
        val realFilePath = run { try { File(filePath).canonicalPath } catch (_: Exception) { filePath } }
        filePaths.remove(realFilePath)
        filePaths.add(0, realFilePath)
        if (filePaths.size > OPEN_RECENT_ITEMS)
            filePaths.subList(OPEN_RECENT_ITEMS, filePaths.size).clear()
        updateMenuItems()
    }

    private fun updateMenuItems() {
        menuItems.forEachIndexed { index, jMenuItem ->
            val file = filePaths.elementAtOrNull(index)
            jMenuItem.text = (index + 1).toString().padStart(2, '0') + ": " + (file ?: "--")
            jMenuItem.isEnabled = file != null
            jMenuItem.setMnemonic((index + 1).toString().last())
        }
    }

}