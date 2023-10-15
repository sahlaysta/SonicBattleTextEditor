package sahlaysta.sbte4.gui

import java.awt.Component
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Graphics
import java.io.PrintWriter
import java.io.StringWriter
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.UIManager
import javax.swing.border.EmptyBorder

//the GUI modal dialog windows.
internal object GUIDialogs {

    private val jOptionPaneMaxSize = Dimension(500, 400)
    private enum class DialogType { NORMAL, ERROR, ICONLESS, PROMPT }
    private fun showDialog(parent: Component, msg: Any?, options: Array<out Any?>?, title: String, type: DialogType,
                           initialValue: Any?, jDialogCallback: ((JDialog) -> Unit)?): Any? {
        val jopType = when (type) {
            DialogType.NORMAL -> JOptionPane.INFORMATION_MESSAGE
            DialogType.ERROR -> JOptionPane.ERROR_MESSAGE
            DialogType.ICONLESS -> JOptionPane.PLAIN_MESSAGE
            DialogType.PROMPT -> JOptionPane.QUESTION_MESSAGE
        }
        val jop = JOptionPane(msg, jopType, JOptionPane.DEFAULT_OPTION, null, options, null)
        jop.initialValue = initialValue
        jop.componentOrientation = parent.componentOrientation
        jop.preferredSize = Dimension(
            jop.preferredSize.width.coerceAtMost(jOptionPaneMaxSize.width),
            jop.preferredSize.height.coerceAtMost(jOptionPaneMaxSize.height))
        val jDialog = jop.createDialog(parent, title)
        jDialog.minimumSize = Dimension(100, 150)
        jDialogCallback?.invoke(jDialog)
        jop.selectInitialValue()
        jDialog.isVisible = true
        val ret = jop.value
        jDialog.dispose()
        return ret
    }

    fun showYesNoDialog(parent: Component, msg: String, title: String): Boolean {
        val dlgMsg = msg.split('\n').map { createJLabel(it.ifEmpty { " " }) }.toTypedArray()
        return showDialog(parent, dlgMsg, arrayOf("Yes", "No"), title, DialogType.PROMPT, "Yes", null) == "Yes"
    }

    fun showNoYesDialog(parent: Component, msg: String, title: String): Boolean {
        val dlgMsg = msg.split('\n').map { createJLabel(it.ifEmpty { " " }) }.toTypedArray()
        return showDialog(parent, dlgMsg, arrayOf("Yes", "No"), title, DialogType.PROMPT, "No", null) == "Yes"
    }

    fun showErrMsgDialog(parent: Component, msg: String, title: String) {
        val dlgMsg = msg.split('\n').map { createJLabel(it.ifEmpty { " " }) }.toTypedArray()
        showDialog(parent, dlgMsg, null, title, DialogType.ERROR, null, null)
    }

    fun showFileReadErrDialog(parent: Component, filePath: String, exception: Exception, title: String) {
        showExceptionErrDialog("An error occured when opening", parent, filePath, exception, title)
    }

    fun showFileWriteErrDialog(parent: Component, filePath: String, exception: Exception, title: String) {
        showExceptionErrDialog("An error occured when saving", parent, filePath, exception, title)
    }

    private fun showExceptionErrDialog(errTypeMsg: String, parent: Component, filePath: String, exception: Exception,
                                       title: String) {
        val viewDetailsJButton = createClearJButton()
        val msg = arrayOf(
            createJLabel(errTypeMsg),
            createJLabel(filePath),
            createJLabel(" "),
            createJLabel(exception.javaClass.simpleName + (exception.message?.let { ": $it" } ?: "")),
            JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
                viewDetailsJButton.text = "<html><u>View stack trace</u></html>"
                add(viewDetailsJButton)
            })
        val jDialogCallback: (JDialog) -> Unit = { jDialog ->
            viewDetailsJButton.addActionListener {
                showTextBlockMsgDialog(jDialog, stackTraceToString(exception), "Stack trace")
            }
        }
        showDialog(parent, msg, null, title, DialogType.ERROR, null, jDialogCallback)
    }

    //JLabel with HTML disabled [HTMLDisabler]
    private fun createJLabel(text: String): JLabel {
        val jLabel = JLabel()
        jLabel.putClientProperty("html.disable", true)
        jLabel.text = text
        return jLabel
    }

    //JButton with a clear background (and a 1 pixel border to signify focus)
    private fun createClearJButton(): JButton {
        val jButton = JButton()
        jButton.background = UIManager.getColor("Label.background")
        jButton.border = object : EmptyBorder(1, 1, 1, 1) {
            override fun paintBorder(c: Component?, g: Graphics, x: Int, y: Int, width: Int, height: Int) {
                super.paintBorder(c, g, x, y, width, height)
                if (jButton.isFocusOwner && !jButton.model.isRollover) {
                    val oldColor = g.color
                    g.color = jButton.foreground
                    val clipBounds = g.clipBounds
                    g.drawRect(clipBounds.x, clipBounds.y, clipBounds.width - 1, clipBounds.height - 1)
                    g.color = oldColor
                }
            }
        }
        return jButton
    }

    private val jTextAreaMaxPreferredSize = Dimension(300, 150)
    fun showTextBlockMsgDialog(parent: Component, msg: String, title: String) {
        val jTextArea = JTextArea(msg)
        jTextArea.isEditable = false
        CaretBlinker.blinkCaret(jTextArea, blinkWhenNonEditable = true)
        val jScrollPane = JScrollPane(jTextArea)
        jScrollPane.border = EmptyBorder(0, 0, 0, 0)
        jScrollPane.preferredSize = Dimension(
            jScrollPane.preferredSize.width.coerceAtMost(jTextAreaMaxPreferredSize.width),
            jScrollPane.preferredSize.height.coerceAtMost(jTextAreaMaxPreferredSize.height))
        val jDialogCallback: (JDialog) -> Unit = { jDialog ->
            jDialog.isResizable = true
        }
        showDialog(parent, jScrollPane, null, title, DialogType.ICONLESS, null, jDialogCallback)
    }

    private fun stackTraceToString(throwable: Throwable): String {
        StringWriter().use { sw ->
            PrintWriter(sw).use { pw ->
                throwable.printStackTrace(pw)
                pw.flush()
                return sw.toString()
            }
        }
    }

}