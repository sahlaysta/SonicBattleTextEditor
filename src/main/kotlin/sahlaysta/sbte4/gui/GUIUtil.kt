package sahlaysta.sbte4.gui

import sahlaysta.swing.JTextComponentEnhancer
import java.awt.Component
import java.awt.Container
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.ActionEvent
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import javax.swing.AbstractAction
import javax.swing.JComponent
import javax.swing.KeyStroke
import javax.swing.SwingUtilities
import javax.swing.text.AbstractDocument
import javax.swing.text.JTextComponent


//utilities.
internal object GUIUtil {

    fun getResourceStream(rsrc: String): InputStream {
        return requireNotNull(object { }.javaClass.getResourceAsStream(rsrc)) { "Could not find resource: $rsrc" }
    }

    fun getResourceByteArray(rsrc: String): ByteArray {
        return getResourceStream(rsrc).use { resourceStream ->
            DataInputStream(resourceStream).use { dataInputStream ->
                dataInputStream.readBytes()
            }
        }
    }

    fun getResourceString(rsrc: String): String {
        return getResourceStream(rsrc).use { resourceStream ->
            BufferedInputStream(resourceStream).use { bufferedInputStream ->
                InputStreamReader(bufferedInputStream, StandardCharsets.UTF_8).use { inputStreamReader ->
                    inputStreamReader.readText()
                }
            }
        }
    }

    fun addAction(jComponent: JComponent, keyStroke: KeyStroke, action: () -> Unit) {
        addAction(jComponent, JComponent.WHEN_FOCUSED, keyStroke, action)
    }

    fun addAction(jComponent: JComponent, inputMapCondition: Int, keyStroke: KeyStroke, action: () -> Unit) {
        val inputMap = jComponent.getInputMap(inputMapCondition)
        val actionMap = jComponent.actionMap!!
        if (inputMap[keyStroke] != null) return
        val abstractAction = object : AbstractAction("action") {
            override fun actionPerformed(e: ActionEvent?) = action()
        }
        val binding = "sahlaysta.action@${System.identityHashCode(abstractAction)}"
        inputMap.put(keyStroke, binding)
        actionMap.put(binding, abstractAction)
    }

    private val iloLock = Any()
    private val iloKeys = HashSet<Any?>()
    fun invokeLaterOnce(key: Any, action: () -> Unit) {
        synchronized(iloLock) {
            if (!iloKeys.contains(key)) {
                iloKeys.add(key)
                SwingUtilities.invokeLater {
                    iloKeys.remove(key)
                    action()
                }
            }
        }
    }

    fun setClipboardText(text: String) {
        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
    }

    inline fun <reified T : Component> findComponent(container: Container): T? =
        allComponents(container).find { it is T }?.let { (it as T) }

    private fun allComponents(container: Container): Sequence<Component> = sequence {
        for (component in container.components) {
            yield(component)
            if (component is Container) {
                for (childComponent in allComponents(component))
                    yield(childComponent)
            }
        }
    }

    fun clearUndoManager(jtc: JTextComponent) {
        val doc = jtc.document
        if (doc !is AbstractDocument) return
        doc.undoableEditListeners
            .filterIsInstance(JTextComponentEnhancer.CompoundUndoManager::class.java)
            .forEach { it.discardAllEdits() }
    }

}
