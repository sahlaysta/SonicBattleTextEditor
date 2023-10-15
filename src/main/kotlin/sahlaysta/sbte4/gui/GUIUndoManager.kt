package sahlaysta.sbte4.gui

import sahlaysta.swing.JTextComponentEnhancer
import java.util.LinkedList
import javax.swing.text.AbstractDocument
import javax.swing.text.JTextComponent
import javax.swing.undo.UndoableEdit

//the undo/redo history of the editor
internal class GUIUndoManager(val gui: GUI) {

    private companion object {
        const val MAX_UNDO_HISTORY = 100
    }

    private class Edit(val editorString: GUIEditorString, val offset: Int, val removedSubstring: String,
                       val insertedSubstring: String)

    private class EditGroup(val edits: LinkedList<Edit>)

    private val editGroups = ArrayList<EditGroup>()
    var undoIndex = 0; private set

    init {
        setUndoManagerModel(gui.editor.textBox.jTextArea)
    }

    fun addEdit(editorString: GUIEditorString, offset: Int, removedSubstring: String, insertedSubstring: String) {
        addEdit(Edit(editorString, offset, removedSubstring, insertedSubstring))
        gui.actions.whenUndoStateChanged()
    }

    fun clear() {
        editGroups.clear()
        undoIndex = 0
        gui.actions.whenUndoStateChanged()
    }

    private fun addEdit(edit: Edit) {
        if (undoIndex < editGroups.size)
            editGroups.subList(undoIndex, editGroups.size).clear()
        editGroups.add(EditGroup(LinkedList<Edit>().apply { add(edit) }))
        undoIndex++
    }

    fun canUndo() = undoIndex > 0

    fun undo() {
        if (!canUndo()) return
        val editGroup = editGroups[undoIndex - 1]
        for (edit in editGroup.edits.reversed()) {
            val editorString = edit.editorString
            editorString.callInfo { text, _, _, _ ->
                text!!
                val undoedText = text.replaceRange(
                    edit.offset, edit.offset + edit.insertedSubstring.length, edit.removedSubstring)
                editorString.updateText(undoedText)
                editorString.updateCaret(edit.offset + edit.removedSubstring.length)
                gui.editor.selectAndScrollToVisible(editorString)
            }
        }
        undoIndex--
        gui.actions.whenUndoStateChanged()
    }

    fun canRedo() = undoIndex < editGroups.size

    fun redo() {
        if (!canRedo()) return
        val editGroup = editGroups[undoIndex]
        for (edit in editGroup.edits) {
            val editorString = edit.editorString
            editorString.callInfo { text, _, _, _ ->
                text!!
                val redoedText = text.replaceRange(
                    edit.offset, edit.offset + edit.removedSubstring.length, edit.insertedSubstring)
                editorString.updateText(redoedText)
                editorString.updateCaret(edit.offset + edit.insertedSubstring.length)
                gui.editor.selectAndScrollToVisible(editorString)
            }
        }
        undoIndex++
        gui.actions.whenUndoStateChanged()
    }

    private inner class UndoManagerModel(jtc: JTextComponent) : JTextComponentEnhancer.CompoundUndoManager(jtc) {

        override fun canUndo() = this@GUIUndoManager.canUndo()
        override fun undo() = this@GUIUndoManager.undo()
        override fun canRedo() = this@GUIUndoManager.canRedo()
        override fun redo() = this@GUIUndoManager.redo()

        init {
            limit = 2 //for detecting compound edits
        }

        override fun addEdit(anEdit: UndoableEdit?): Boolean {
            val ret = super.addEdit(anEdit)
            if (ret) {
                if (gui.editor.userModifyingTextBox && gui.editor.selectedString != null) {

                    //merge a compound edit into an editgroup
                    if (isCompoundEdit(anEdit)) {
                        if (undoIndex > 1) {
                            val editGroup1 = editGroups[undoIndex - 1]
                            val editGroup2 = editGroups[undoIndex - 2]
                            editGroup2.edits.addAll(editGroup1.edits)
                            editGroups.removeAt(undoIndex - 1)
                            undoIndex--
                        }
                    }

                    //trim the undo history to max
                    if (editGroups.size > MAX_UNDO_HISTORY) {
                        editGroups.subList(0, editGroups.size - MAX_UNDO_HISTORY).clear()
                        undoIndex = editGroups.size
                    }

                }
            }
            return ret
        }

    }

    private fun setUndoManagerModel(jtc: JTextComponent) {
        val doc = jtc.document as AbstractDocument
        doc.undoableEditListeners
            .filterIsInstance(JTextComponentEnhancer.CompoundUndoManager::class.java)
            .forEach { doc.removeUndoableEditListener(it) }
        doc.addUndoableEditListener(UndoManagerModel(jtc))
    }

}