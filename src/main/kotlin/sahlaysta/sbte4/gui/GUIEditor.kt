package sahlaysta.sbte4.gui

import sahlaysta.sbte4.rom.SBTEMutableBlob
import sahlaysta.sbte4.rom.SBTEROMData
import sahlaysta.sbte4.rom.SBTEROMType
import sahlaysta.sbte4.rom.SBTEStringDescription
import sahlaysta.sbte4.rom.SBTEStringLanguage
import sahlaysta.sbte4.rom.blob.string.SBTEStringDecoder
import java.util.TreeMap
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.border.EmptyBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.AbstractDocument
import javax.swing.text.AttributeSet
import javax.swing.text.DocumentFilter
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath

//the editor tree and text box.
internal class GUIEditor(val gui: GUI) {

    val jSplitPane: JSplitPane
    val tree = GUIEditorTree(gui)
    val textBox = GUIEditorTextBox(gui)
    private val lock = Any()

    init {
        val treeJScrollPane = JScrollPane(tree.jTree)
        treeJScrollPane.border = EmptyBorder(0, 0, 0, 0)

        val textBoxJScrollPane = JScrollPane(textBox.jTextArea)
        textBoxJScrollPane.border = EmptyBorder(0, 5, 5, 5)

        jSplitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT, treeJScrollPane, textBoxJScrollPane)
        jSplitPane.resizeWeight = 1.0

        tree.jTree.isRequestFocusEnabled = false
        tree.jTree.addTreeSelectionListener {
            if (userModifyingTreeSelection) {
                val editorString = selectedString
                if (editorString != null) {
                    handleStringNodeSelectionChanged(editorString)
                } else {
                    handleStringNodeSelectionCleared()
                }
            }
        }

        textBox.jTextArea.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) = handleTextBoxTextModified()
            override fun removeUpdate(e: DocumentEvent?) = handleTextBoxTextModified()
            override fun changedUpdate(e: DocumentEvent?) = handleTextBoxTextModified()
        })
        addUndoListener()
        textBox.jTextArea.addCaretListener { handleTextBoxCaretChanged() }

        clearEditor()
        addActions()
    }

    var romData: SBTEROMData? = null; private set
    var romFilePath: String? = null; private set
    var stringData: Map<SBTEStringLanguage, Map<SBTEStringDescription, List<GUIEditorString>>>? = null; private set
    var hasUnsavedImportedStrings = false

    fun setROMData(romData: SBTEROMData, romFilePath: String) {
        synchronized(lock) {
            tree.jTree.selectionModel.clearSelection()
            GUI.gui?.undoManager?.clear()
            val rootNode = DefaultMutableTreeNode(null, true)
            rootNode.add(DefaultMutableTreeNode("ROM: ${sbteROMTypeToString(romData.romType)}", false))
            val treeModel = DefaultTreeModel(rootNode)
            val stringData = romData.romStringData.stringGroups.groupByTo(TreeMap()) { it.language }
                .mapValues { (language, stringGroups) -> stringGroups.groupByTo(TreeMap()) { it.description }
                    .mapValues { (description, stringGroups) ->
                        stringGroups.flatMap { it.strings.map { blob ->
                            GUIEditorStringImpl(treeModel, blob, language, description)
                        } }
                    }
                }
            stringData.forEach { (language, map) ->
                val languageNode = DefaultMutableTreeNode(sbteLanguageToString(language), true)
                var i = 1
                map.forEach { (description, editorStrings) ->
                    val regionNode = DefaultMutableTreeNode("R${i++} - ${sbteDescriptionToString(description)}", true)
                    editorStrings.forEach { regionNode.add(it.treeNode) }
                    languageNode.add(regionNode)
                }
                rootNode.add(languageNode)
            }
            tree.jTree.model = treeModel
            this.romData = romData
            this.romFilePath = romFilePath
            this.stringData = stringData
            this.hasUnsavedImportedStrings = false
            updateErrorCount()
            handleStringNodeSelectionCleared()
            gui.search.update()
        }
    }

    fun clearEditor() {
        synchronized(lock) {
            tree.jTree.selectionModel.clearSelection()
            GUI.gui?.undoManager?.clear()
            val treeModel = emptyTreeModel()
            tree.jTree.model = treeModel
            this.romData = null
            this.romFilePath = null
            this.stringData = null
            this.hasUnsavedImportedStrings = false
            updateErrorCount()
            handleStringNodeSelectionCleared()
            gui.search.update()
        }
    }

    private fun emptyTreeModel() = DefaultTreeModel(DefaultMutableTreeNode(null, false))

    private fun sbteROMTypeToString(type: SBTEROMType): String {
        return when (type) {
            SBTEROMType.US -> "USA"
            SBTEROMType.EU -> "EU"
            SBTEROMType.JP -> "Japan"
        }
    }

    private fun sbteLanguageToString(language: SBTEStringLanguage): String {
        return when (language) {
            SBTEStringLanguage.JAPANESE -> "Japanese"
            SBTEStringLanguage.ENGLISH -> "English"
            SBTEStringLanguage.FRENCH -> "French"
            SBTEStringLanguage.GERMAN -> "German"
            SBTEStringLanguage.ITALIAN -> "Italian"
            SBTEStringLanguage.SPANISH -> "Spanish"
        }
    }

    private fun sbteDescriptionToString(description: SBTEStringDescription): String {
        return when (description) {
            SBTEStringDescription.STORY_MODE -> "Story mode"
            SBTEStringDescription.EMERL_CARD_DESCRIPTIONS -> "Emerl card descriptions"
            SBTEStringDescription.OPTIONS_MENU -> "Options menu"
            SBTEStringDescription.BATTLE_MENU -> "Battle menu"
            SBTEStringDescription.BATTLE_RULES_MENU -> "Battle rules menu"
            SBTEStringDescription.TRAINING_MODE_MENU -> "Training mode menu"
            SBTEStringDescription.MINIGAME_MENU -> "Minigame menu"
            SBTEStringDescription.BATTLE_RECORD_MENU -> "Battle record menu"
            SBTEStringDescription.CAPTURED_SKILL -> "\"Captured skill\""
            SBTEStringDescription.STORY_MODE_MENU -> "Story mode menu"
        }
    }

    fun updateROMFilePath(filePath: String) {
        this.romFilePath = filePath
    }

    private fun addActions() {
        //(having the shortcuts on the component rather than only in the JMenuBar fixes the delay)
        GUIUtil.addAction(tree.jTree, gui.actions.goUp.keyStroke) { gui.actions.goUp.action() }
        GUIUtil.addAction(tree.jTree, gui.actions.goDown.keyStroke) { gui.actions.goDown.action() }
        GUIUtil.addAction(tree.jTree, gui.actions.expand.keyStroke) { gui.actions.expand.action() }
        GUIUtil.addAction(textBox.jTextArea, gui.actions.goUp.keyStroke) { gui.actions.goUp.action() }
        GUIUtil.addAction(textBox.jTextArea, gui.actions.goDown.keyStroke) { gui.actions.goDown.action() }
        GUIUtil.addAction(textBox.jTextArea, gui.actions.expand.keyStroke) { gui.actions.expand.action() }
    }

    private inner class GUIEditorStringImpl(val treeModel: DefaultTreeModel,
                                            override val romBlob: SBTEMutableBlob,
                                            override val language: SBTEStringLanguage,
                                            override val description: SBTEStringDescription)
        : GUIEditorString() {

        override val mutex get() = lock
        override var text = romBlob.binary?.let { SBTEStringDecoder.decodeString(it) }
        override var displayText = getDisplayTextFromText(text)
        override var hasError = false
        override var caretPosition = 0

        override val treeNode = TreeNodeImpl()
        override val treePath get() = TreePath(treeModel.getPathToRoot(treeNode))

        inner class TreeNodeImpl : DefaultMutableTreeNode(null, false) {
            val editorString get() = this@GUIEditorStringImpl
        }

        init {
            treeNode.userObject = displayText
        }

        override fun updateText(text: String?) {
            synchronized(lock) {
                this.text = text
                displayText = getDisplayTextFromText(text)
                treeNode.userObject = displayText
                treeModel.nodeChanged(treeNode)
                refreshTreeLater()
                if (!userModifyingTextBox && selectedString === this)
                    handleStringNodeSelectionChanged(this)
                checkErrorsInBackground()
            }
        }

        override fun updateCaret(pos: Int) {
            synchronized(lock) {
                caretPosition = pos
            }
        }

        private fun getDisplayTextFromText(text: String?) = text?.replace('\n', '↵') ?: "[?]"

        //use SonicBattleStringTokenizer to check for malformatted SBTE-styled strings
        private fun checkErrorsInBackground() {
            class Result(val hasError: Boolean)
            val text = text
            gui.backgroundQueue.enqueue(
                key = this,
                backgroundFunction = { actionState ->
                    if (text == null) return@enqueue Result(hasError = false)
                    for (token in SonicBattleStringTokenizer.tokenizeSonicBattleString(text)) {
                        if (actionState.actionIsCanceled()) return@enqueue null
                        if (token is SonicBattleStringTokenizer.Token.UnknownUTF32CharToken
                            || token is SonicBattleStringTokenizer.Token.BadLeftBracketToken
                            || token is SonicBattleStringTokenizer.Token.BadRightBracketToken) {
                            return@enqueue Result(hasError = true)
                        }
                    }
                    return@enqueue Result(hasError = false)
                },
                callbackFunction = { result ->
                    result!!
                    synchronized(lock) { hasError = result.hasError }
                    refreshTreeLater()
                },
                errorCallbackFunction = { exception -> exception.printStackTrace() }
            )
        }

        private fun refreshTreeLater() {
            GUIUtil.invokeLaterOnce(key = "editorrefresh") {
                tree.jTree.revalidate()
                tree.jTree.repaint()
                updateErrorCount()
                gui.search.update()
            }
        }

    }

    val selectedString: GUIEditorString? get() {
        val nodeObject = tree.jTree.selectionPath?.lastPathComponent
        return if (nodeObject is GUIEditorStringImpl.TreeNodeImpl) nodeObject.editorString else null
    }

    private fun handleStringNodeSelectionChanged(editorString: GUIEditorString) {
        editorString.callInfo { text, _, _, caretPosition ->
            if (text == null) {
                handleStringNodeSelectionCleared()
                return
            }
            setTextBoxTextSafely(text)
            setTextBoxCaretSafely(caretPosition)
            if (!textBox.jTextArea.isEditable)
                textBox.jTextArea.isEditable = true
            if (gui.textPreview.isShown)
                gui.textPreview.setText(text, isUserEdit = false, gui.editor.romData!!.romType)
        }
    }

    private fun handleStringNodeSelectionCleared() {
        textBox.jTextArea.select(0, 0)
        setTextBoxTextSafely("")
        if (textBox.jTextArea.isEditable)
            textBox.jTextArea.isEditable = false
        if (gui.textPreview.isShown)
            gui.textPreview.clearText()
    }

    var userModifyingTextBox = true

    private fun setTextBoxTextSafely(text: String) {
        userModifyingTextBox = false
        try {
            textBox.jTextArea.text = text
        } finally {
            userModifyingTextBox = true
        }
    }

    private fun setTextBoxCaretSafely(caretPosition: Int) {
        userModifyingTextBox = false
        try {
            textBox.jTextArea.caret.dot = caretPosition
        } finally {
            userModifyingTextBox = true
        }
    }

    private fun handleTextBoxTextModified() {
        if (!userModifyingTextBox) return
        val text = textBox.jTextArea.text
        val editorString = selectedString
        if (editorString != null) {
            tree.ensureRowIsVisible(tree.jTree.getRowForPath(editorString.treePath))
            editorString.updateText(text)
            if (gui.textPreview.isShown)
                gui.textPreview.setText(text, isUserEdit = true, gui.editor.romData!!.romType)
        }
    }

    private fun handleTextBoxCaretChanged() {
        if (!userModifyingTextBox) return
        selectedString?.updateCaret(textBox.jTextArea.caret.dot)
    }

    fun allStrings(): Sequence<GUIEditorString> = sequence {
        stringData?.forEach { (_, map) -> map.forEach { (_, editorStrings) -> editorStrings.forEach { yield(it) } } }
    }

    private var userModifyingTreeSelection = true

    fun selectAndScrollToVisible(editorString: GUIEditorString) {
        val treePath = editorString.treePath
        treePath.parentPath?.let { tree.jTree.expandPath(it) }
        val pathRow = tree.jTree.getRowForPath(treePath)
        tree.ensureRowIsVisible(pathRow)
        userModifyingTreeSelection = false
        try {
            tree.jTree.setSelectionRow(pathRow)
        } finally {
            userModifyingTreeSelection = true
        }
        handleStringNodeSelectionChanged(editorString)
    }

    fun lineHasError(nodeObject: Any?): Boolean {
        return nodeObject is GUIEditorStringImpl.TreeNodeImpl
                && nodeObject.editorString.callInfo { _, _, hasError, _ -> hasError }
    }

    fun waitForBackgroundThread() {
        gui.backgroundQueue.waitFor()
    }

    private fun updateErrorCount() {
        val errorCount = allStrings().count { it.callInfo { _, _, hasError, _ -> hasError } }
        gui.menuBar.errorsJMenuItem.text = "Errors ($errorCount)"
    }

    private fun addUndoListener() {
        val document = textBox.jTextArea.document as AbstractDocument
        document.documentFilter = object : DocumentFilter() {
            override fun insertString(fb: FilterBypass?, offset: Int, string: String, attr: AttributeSet?) {
                if (userModifyingTextBox)
                    selectedString?.let { gui.undoManager.addEdit(it, offset, "", string) }
                super.insertString(fb, offset, string, attr)
            }
            override fun remove(fb: FilterBypass?, offset: Int, length: Int) {
                if (userModifyingTextBox)
                    selectedString?.let { gui.undoManager.addEdit(it, offset, document.getText(offset, length), "") }
                super.remove(fb, offset, length)
            }
            override fun replace(fb: FilterBypass?, offset: Int, length: Int, text: String, attrs: AttributeSet?) {
                if (userModifyingTextBox)
                    selectedString?.let { gui.undoManager.addEdit(it, offset, document.getText(offset, length), text) }
                super.replace(fb, offset, length, text, attrs)
            }
        }
    }

}