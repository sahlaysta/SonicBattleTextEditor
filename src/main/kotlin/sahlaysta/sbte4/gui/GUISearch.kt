package sahlaysta.sbte4.gui

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Graphics
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.AbstractListModel
import javax.swing.Box
import javax.swing.DefaultListCellRenderer
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextField
import javax.swing.KeyStroke
import javax.swing.ListSelectionModel
import javax.swing.SwingUtilities
import javax.swing.border.EmptyBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.Position

//the search (Find) dialog. also the Errors dialog
internal class GUISearch(val gui: GUI) {

    private var searchDialog: SearchDialog? = null
    private var searchEntry = ""

    fun showSearchDialog() {
        SearchDialog.createAndShow(gui, isErrorsDialog = false)
    }

    fun showErrorsDialog() {
        SearchDialog.createAndShow(gui, isErrorsDialog = true)
    }

    fun update() {
        searchDialog?.update()
    }

    private class SearchDialog private constructor(val gui: GUI, val isErrorsDialog: Boolean) {

        companion object {
            fun createAndShow(gui: GUI, isErrorsDialog: Boolean) = SearchDialog(gui, isErrorsDialog)
        }

        private class SearchResult(val editorString: GUIEditorString,
                                   val searchMatchedOffsets: List<SearchMatchedOffset>)

        private class SearchMatchedOffset(val startOffset: Int, val endOffset: Int)

        private inner class CustomListModel : AbstractListModel<Any?>() {
            override fun getSize() = searchResults.size
            override fun getElementAt(index: Int): String {
                return searchResults[index].editorString.callInfo { _, displayText, _, _ -> displayText }
            }
            fun fireIntervalAdded(index0: Int, index1: Int) = super.fireIntervalAdded(this, index0, index1)
            fun fireIntervalRemoved(index0: Int, index1: Int) = super.fireIntervalRemoved(this, index0, index1)
        }

        private inner class CustomListCellRenderer : DefaultListCellRenderer() {

            init {
                putClientProperty("html.disable", true) //[HTMLDisabler]
            }

            //red cell text when a line has an error
            override fun getListCellRendererComponent(list: JList<*>?, value: Any?, index: Int, isSelected: Boolean,
                                                      cellHasFocus: Boolean): Component {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
                if (searchResults[index].editorString.callInfo { _, _, hasError, _ -> hasError })
                    foreground = Color.RED
                return this
            }

        }

        //when the text field is empty, show gray text "Search..."
        private class CustomJTextField : JTextField() {
            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)
                if (document.length == 0) {
                    g.color = disabledTextColor
                    g.drawString("Search...", insets.left, g.fontMetrics.maxAscent + insets.top)
                    caret.paint(g)
                }
            }
        }

        private val jDialog = JDialog(gui.jFrame, true)
        private var searchResults: List<SearchResult> = emptyList()
        private val searchField = CustomJTextField()
        private val searchResultsListModel = CustomListModel()
        private val searchResultsJList = object : JList<Any?>(searchResultsListModel) {
            override fun getNextMatch(prefix: String?, startIndex: Int, bias: Position.Bias?) = -1
        }
        private val searchResultsListJScrollPane = JScrollPane(searchResultsJList)
        private val goButton = JButton("Go")
        private val cancelButton = JButton("Cancel")
        private val backgroundQueueKey = Any()

        private fun initLayout() {
            val mainPanel = JPanel(BorderLayout())
            mainPanel.border = EmptyBorder(5, 5, 5, 5)
            searchField.document.addDocumentListener(object : DocumentListener {
                override fun insertUpdate(e: DocumentEvent?) = searchFieldChanged()
                override fun removeUpdate(e: DocumentEvent?) = searchFieldChanged()
                override fun changedUpdate(e: DocumentEvent?) = searchFieldChanged()
            })
            searchResultsJList.selectionMode = ListSelectionModel.SINGLE_SELECTION
            searchResultsJList.addPropertyChangeListener("UI") {
                searchResultsJList.cellRenderer = CustomListCellRenderer()
            }
            searchResultsJList.cellRenderer = CustomListCellRenderer()
            GUIUtil.addAction(searchResultsJList, KeyStroke.getKeyStroke("ENTER")) { jumpToSelectionAndClose() }
            GUIUtil.addAction(searchField, KeyStroke.getKeyStroke("DOWN")) { goDownOneOnList() }
            GUIUtil.addAction(searchField, KeyStroke.getKeyStroke("UP")) { goUpOneOnList() }
            GUIUtil.addAction(jDialog.rootPane, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
                KeyStroke.getKeyStroke("ESCAPE")) { closeSearchDialog() }
            searchResultsJList.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    if (e != null && e.clickCount == 2)
                        jumpToSelectionAndClose()
                }
            })
            searchResultsJList.addListSelectionListener {
                goButton.isEnabled = searchResultsJList.selectedIndex >= 0
            }
            goButton.isEnabled = searchResultsJList.selectedIndex >= 0
            val searchFieldPanel = JPanel(BorderLayout())
            searchFieldPanel.border = EmptyBorder(0, 0, 5, 0)
            searchField.addActionListener { jumpToSelectionAndClose() }
            searchFieldPanel.add(searchField)
            val buttonPanel = JPanel(BorderLayout())
            buttonPanel.border = EmptyBorder(5, 0, 0, 0)
            val buttonBox = Box.createHorizontalBox()
            goButton.setMnemonic('g')
            goButton.addActionListener { jumpToSelectionAndClose() }
            buttonBox.add(goButton)
            buttonBox.add(Box.createRigidArea(Dimension(5, 0)))
            cancelButton.addActionListener { closeSearchDialog() }
            cancelButton.setMnemonic('c')
            buttonBox.add(cancelButton)
            buttonPanel.add(buttonBox, BorderLayout.EAST)
            mainPanel.add(searchFieldPanel, BorderLayout.NORTH)
            mainPanel.add(searchResultsListJScrollPane, BorderLayout.CENTER)
            mainPanel.add(buttonPanel, BorderLayout.SOUTH)
            if (!isErrorsDialog)
                searchField.text = gui.search.searchEntry
            searchFieldChanged()
            updateTitle()
            jDialog.add(mainPanel)
        }

        fun update() = searchInBackground()

        private fun searchFieldChanged() {
            if (!isErrorsDialog)
                gui.search.searchEntry = searchField.text
            searchInBackground()
        }

        private fun searchInBackground() {
            class Result(val searchResults: List<SearchResult>)
            val search = searchField.text
            searchResultsJList.isEnabled = false
            gui.backgroundQueue.enqueue(
                key = backgroundQueueKey,
                backgroundFunction = { actionState ->
                    val searchRegex = makeRegexFromSearch(search)
                    val searchResults = ArrayList<SearchResult>()
                    for (editorString in gui.editor.allStrings()) {
                        if (actionState.actionIsCanceled()) return@enqueue null
                        val text: String?
                        val hasError: Boolean
                        editorString.callInfo { esText, _, esHasError, _ -> text = esText; hasError = esHasError }
                        if (text == null) continue
                        if (isErrorsDialog && !hasError) continue
                        if (search.isBlank()) {
                            searchResults.add(SearchResult(editorString, emptyList()))
                            continue
                        }
                        val regexMatchedOffsets = searchRegex.findAll(text)
                            .map { it.groups[regexNonWhiteSpaceSeparationGroupIndex]!! }
                            .map { SearchMatchedOffset(it.range.first, it.range.first + it.value.length) }
                            .filter { it.startOffset != it.endOffset }
                            .toList()
                        if (regexMatchedOffsets.isNotEmpty())
                            searchResults.add(SearchResult(editorString, regexMatchedOffsets))
                    }
                    return@enqueue Result(searchResults)
                },
                callbackFunction = { result ->
                    result!!
                    SwingUtilities.invokeLater {
                        setList(result.searchResults)
                        updateTitle()
                        searchResultsJList.isEnabled = true
                    }
                },
                errorCallbackFunction = { exception -> exception.printStackTrace() }
            )
        }

        private val regexNonWhiteSpaceSeparationGroupIndex = 2
        private fun makeRegexFromSearch(search: String): Regex {
            val regexPattern = search.asSequence()
                .filter { !it.isWhitespace() }
                .joinToString(separator = "\\s*", prefix = "(\\s*)(", postfix = ")(\\s*)") { getEscapedRegexChar(it) }
            return Regex(regexPattern, setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
        }

        private fun getEscapedRegexChar(ch: Char): String {
            return if (ch <= 0x7F.toChar()) "\\x" + String.format("%02X", ch.code) else ch.toString()
        }

        private fun clearList() {
            val oldSize = searchResults.size
            searchResults = emptyList()
            if (oldSize > 0)
                searchResultsListModel.fireIntervalRemoved(0, oldSize)
        }

        private fun setList(searchResults: List<SearchResult>) {
            clearList()
            this.searchResults = searchResults
            if (searchResults.isNotEmpty())
                searchResultsListModel.fireIntervalAdded(0, searchResults.size)
        }

        private fun jumpToSelectionAndClose() {
            val selectedIndex = searchResultsJList.selectedIndex
            if (selectedIndex >= 0) {
                jDialog.isVisible = false
                jDialog.dispose()
                val searchResult = searchResults[selectedIndex]
                gui.editor.selectAndScrollToVisible(searchResult.editorString)
            }
        }

        private fun goDownOneOnList() {
            if (!searchResultsJList.isEnabled) return
            val listIndex = searchResultsJList.selectedIndex
            if (listIndex < searchResults.size) {
                val newListIndex = listIndex + 1
                searchResultsJList.selectedIndex = newListIndex
                searchResultsJList.ensureIndexIsVisible(newListIndex)
            }
        }

        private fun goUpOneOnList() {
            if (!searchResultsJList.isEnabled) return
            val listIndex = searchResultsJList.selectedIndex
            if (listIndex > 0) {
                val newListIndex = listIndex - 1
                searchResultsJList.selectedIndex = newListIndex
                searchResultsJList.ensureIndexIsVisible(newListIndex)
            }
        }

        private fun closeSearchDialog() {
            gui.search.searchDialog = null
            jDialog.isVisible = false
            jDialog.dispose()
        }

        private fun updateTitle() {
            jDialog.title = if (isErrorsDialog) gui.menuBar.errorsJMenuItem.text else "Find"
        }

        init {
            gui.search.searchDialog = this
            initLayout()
            jDialog.size = Dimension(250, 300)
            jDialog.setLocationRelativeTo(gui.jFrame)
            jDialog.defaultCloseOperation = JDialog.DISPOSE_ON_CLOSE
            jDialog.isVisible = true
        }

    }

}