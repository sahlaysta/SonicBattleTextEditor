package sahlaysta.sbte4.gui

import java.util.Locale
import javax.swing.AbstractButton
import javax.swing.JCheckBoxMenuItem
import javax.swing.JComponent
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.KeyStroke

//the GUI menu bar.
internal class GUIMenuBar(val gui: GUI) {

    val jMenuBar = JMenuBar()

    val fileJMenu = menu("&File", jMenuBar)
    val openJMenuItem = item("&Open", gui.actions.open, fileJMenu)
    val openRecentJMenu = openRecentMenu("Open &recent", fileJMenu)
    val saveJMenuItem = item("&Save", gui.actions.save, fileJMenu)
    val saveAsJMenuItem = item("Save &as", gui.actions.saveAs, fileJMenu)
    val closeJMenuItem = item("&Close", gui.actions.close, fileJMenu)

    val editJMenu = menu("&Edit", jMenuBar)
    val undoJMenuItem = item("&Undo", gui.actions.undo, editJMenu)
    val redoJMenuItem = item("&Redo", gui.actions.redo, editJMenu)
    init { editJMenu.addSeparator() }
    val goUpJMenuItem = item("&Go up one row", gui.actions.goUp, editJMenu)
    val goDownJMenuItem = item("Go &down one row", gui.actions.goDown, editJMenu)
    val expandJMenuItem = item("E&xpand/collapse row", gui.actions.expand, editJMenu)
    init { editJMenu.addSeparator() }
    val jsonExportJMenuItem = item("Export strings to &JSON", gui.actions.jsonExport, editJMenu)
    val jsonImportJMenuItem = item("&Import strings from JSON", gui.actions.jsonImport, editJMenu)

    val searchJMenu = menu("&Search", jMenuBar)
    val findJMenuItem = item("&Find", gui.actions.find, searchJMenu)
    val errorsJMenuItem = item("&Errors", gui.actions.errors, searchJMenu)

    val viewJMenu = menu("&View", jMenuBar)
    val darkThemeJMenuItem = checkBoxItem(
        "Dark &theme", gui.actions.darkTheme, viewJMenu, checked = true)
    val textPreviewJMenuItem = item("Text &preview", gui.actions.textPreview, viewJMenu)

    val helpJMenu = menu("&Help", jMenuBar)
    val aboutJMenuItem = item("&About", gui.actions.about, helpJMenu)

    private fun menu(text: String, parentJMenu: JComponent): JMenu {
        return menu(text, parentJMenu) { JMenu() }
    }

    private fun openRecentMenu(text: String, parentJMenu: JComponent): JMenu {
        return menu(text, parentJMenu) { gui.openRecentMenu.jMenu }
    }

    private fun item(text: String, action: GUIActions.GUIAction, menu: JMenu): JMenuItem {
        return item(text, action, menu) { JMenuItem() }
    }

    private fun checkBoxItem(text: String, action: GUIActions.GUIAction, menu: JMenu,
                             checked: Boolean = false): JCheckBoxMenuItem {
        return item(text, action, menu) { JCheckBoxMenuItem() }.apply { isSelected = checked }
    }

    private fun <T: JMenu> menu(text: String, parentJMenu: JComponent, fn: () -> T): T {
        val jMenu = fn()
        setTextAndMnemonic(jMenu, text)
        parentJMenu.add(jMenu)
        return jMenu
    }

    private fun <T : JMenuItem> item(text: String, action: GUIActions.GUIAction, menu: JMenu, fn: () -> T): T {
        val jMenuItem = fn()
        jMenuItem.text = text
        if (action is GUIActions.GUIActionWithShortcut) jMenuItem.accelerator = action.keyStroke
        setTextAndMnemonic(jMenuItem, text)
        jMenuItem.addActionListener { action.action() }
        menu.add(jMenuItem)
        return jMenuItem
    }

    private fun setTextAndMnemonic(btn: AbstractButton, text: String) {
        btn.text = text.replace("&", "")
        val indexOfAmpersand = text.indexOf('&')
        if (indexOfAmpersand == -1) return
        require(text.count { it == '&' } == 1) { text }
        require(indexOfAmpersand != text.lastIndex) { text }
        val indexOfMnemonic = indexOfAmpersand + 1
        val mnemonicChar = text[indexOfMnemonic].uppercase(Locale.ENGLISH)
        btn.mnemonic = KeyStroke.getKeyStroke(mnemonicChar).keyCode
        btn.displayedMnemonicIndex = indexOfMnemonic - 1
    }

}