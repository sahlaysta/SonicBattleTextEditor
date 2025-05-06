package sahlaysta.sbte4.gui

import com.formdev.flatlaf.FlatClientProperties
import com.formdev.flatlaf.FlatDarkLaf
import sahlaysta.swing.JTextComponentEnhancer
import java.awt.Dimension
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.UIManager

//the SBTE gui.
internal class GUI private constructor() {

    companion object {

        private var _gui: GUI? = null
        val gui: GUI? get() = _gui

        fun start(romFilePath: String? = null) {
            SwingUtilities.invokeLater {
                _gui = GUI()
                if (romFilePath != null) {
                    SwingUtilities.invokeLater {
                        _gui!!.actions.openROM(romFilePath)
                    }
                }
            }
        }

    }

    val jFrame: JFrame
    val actions: GUIActions
    val menuBar: GUIMenuBar
    val fileChooser: GUIFileChooser
    val openRecentMenu: GUIOpenRecentMenu
    val editor: GUIEditor
    val undoManager: GUIUndoManager
    val search: GUISearch
    val jsonExport: GUIJSONExport
    val textPreview: GUITextPreview
    val backgroundQueue: GUIBackgroundQueue
    val prefs: GUIPrefs

    init {
        UIManager.put("ScrollBar.width", 15)
        UIManager.put("ScrollBar.height", 12)
        UIManager.put("Tree.paintLines", true)
        FlatDarkLaf.setup()
        JTextComponentEnhancer.applyGlobalEnhancer()

        backgroundQueue = GUIBackgroundQueue()

        jFrame = JFrame("Sonic Battle Text Editor")
        jFrame.iconImage = ImageIcon(GUIUtil.getResourceByteArray("/icon.png")).image
        jFrame.rootPane.putClientProperty(FlatClientProperties.MENU_BAR_EMBEDDED, false)
        jFrame.defaultCloseOperation = JFrame.DO_NOTHING_ON_CLOSE

        fileChooser = GUIFileChooser()

        textPreview = GUITextPreview(this)

        jsonExport = GUIJSONExport(this)

        search = GUISearch(this)

        actions = GUIActions(this)

        openRecentMenu = GUIOpenRecentMenu(this)

        menuBar = GUIMenuBar(this)
        jFrame.jMenuBar = menuBar.jMenuBar

        editor = GUIEditor(this)
        jFrame.add(editor.jSplitPane)

        undoManager = GUIUndoManager(this)

        prefs = GUIPrefs(this)

        jFrame.minimumSize = Dimension(250, 250)
        jFrame.addWindowListener(object : WindowAdapter() {
            override fun windowOpened(e: WindowEvent?) {
                editor.textBox.jTextArea.requestFocus()
                jFrame.removeWindowListener(this)
            }
        })
        jFrame.addWindowListener(object: WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                if (actions.preCloseROM(fromOpen = false)) {
                    actions.closeROM()
                    actions.whenGUIExited()
                    jFrame.dispose()
                }
            }
        })

        prefs.loadPrefs()

        jFrame.isVisible = true
        HTMLDisabler.disableHTMLInTitlePane(jFrame)

        actions.whenGUIStarted()
    }

}