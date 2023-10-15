package sahlaysta.sbte4.gui

import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import javax.swing.JTextArea
import javax.swing.Timer
import javax.swing.event.CaretListener
import javax.swing.text.DefaultCaret
import javax.swing.text.JTextComponent

//there's a glitch that the JTextComponent caret doesn't blink when 'editable' is changed, so blink it manually
internal object CaretBlinker {

    fun blinkCaret(jta: JTextArea, blinkWhenNonEditable: Boolean) {
        val caret = jta.caret
        if (caret is DefaultCaret)
            modCaret(jta, caret, blinkWhenNonEditable) { JTextArea().caret?.blinkRate ?: 500 }
    }

    private const val CARET_MOD_KEY = "sahlaysta.caretmod"
    private fun modCaret(jtc: JTextComponent, caret: DefaultCaret, blinkWhenNonEditable: Boolean,
                         lazyBlinkRate: () -> Int) {
        if (jtc.getClientProperty(CARET_MOD_KEY) == true) return
        jtc.putClientProperty(CARET_MOD_KEY, true)
        caret.blinkRate = 0
        var visible = false
        val blinker = Timer(lazyBlinkRate()) {
            visible = !visible
            caret.isVisible = visible
        }
        val startBlinker = {
            visible = true
            caret.isVisible = true
            blinker.start()
        }
        val restartBlinker = {
            visible = true
            caret.isVisible = true
            blinker.restart()
        }
        val stopBlinker = {
            visible = false
            caret.isVisible = false
            blinker.stop()
        }
        val shouldStartBlinker = { jtc.isFocusOwner && jtc.isEnabled && (jtc.isEditable || blinkWhenNonEditable) }
        val focusListener = object : FocusListener {
            override fun focusGained(e: FocusEvent?) {
                if (shouldStartBlinker()) {
                    startBlinker()
                } else {
                    stopBlinker()
                }
            }
            override fun focusLost(e: FocusEvent?) {
                stopBlinker()
            }
        }
        val caretListener = CaretListener {
            if (shouldStartBlinker()) {
                restartBlinker()
            }
        }
        val propertyChangeListener = PropertyChangeListener {
            val propertyName = it.propertyName
            if (propertyName == "editable" || propertyName == "enabled") {
                if (shouldStartBlinker()) {
                    startBlinker()
                } else {
                    stopBlinker()
                }
            }
        }
        jtc.addFocusListener(focusListener)
        jtc.addCaretListener(caretListener)
        jtc.addPropertyChangeListener(propertyChangeListener)
        if (shouldStartBlinker()) {
            startBlinker()
        } else {
            stopBlinker()
        }
        val deinstallPropertyChangeListener = object : PropertyChangeListener {
            override fun propertyChange(evt: PropertyChangeEvent?) {
                if (evt != null && evt.oldValue === caret && evt.newValue !== caret) {
                    blinker.stop()
                    jtc.removeFocusListener(focusListener)
                    jtc.removeCaretListener(caretListener)
                    jtc.removePropertyChangeListener(propertyChangeListener)
                    jtc.putClientProperty(CARET_MOD_KEY, null)
                    jtc.removePropertyChangeListener("caret", this)
                }
            }
        }
        jtc.addPropertyChangeListener("caret", deinstallPropertyChangeListener)
    }

}