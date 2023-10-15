package sahlaysta.sbte4.gui

import sahlaysta.sbte4.rom.blob.string.SBTEStringEncoder
import java.awt.Color
import java.awt.Component
import java.awt.Rectangle
import java.awt.event.MouseEvent
import javax.swing.JMenuItem
import javax.swing.JPopupMenu
import javax.swing.JTree
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.event.MouseInputAdapter
import javax.swing.text.Position
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel

//the editor tree.
internal class GUIEditorTree(val gui: GUI) {

    val jTree = object : JTree() {
        override fun getNextMatch(prefix: String?, startingRow: Int, bias: Position.Bias?): TreePath? {
            //(disable jumping to tree items by first-letter key press)
            return null
        }
    }

    init {
        jTree.model = DefaultTreeModel(DefaultMutableTreeNode(null, false))
        jTree.isEditable = false
        jTree.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        jTree.showsRootHandles = true
        jTree.isRootVisible = false
        jTree.addPropertyChangeListener("UI") {
            jTree.cellRenderer = CustomTreeCellRenderer()
            optimizeJTreeFixedRowHeight()
        }
        jTree.cellRenderer = CustomTreeCellRenderer()
        optimizeJTreeFixedRowHeight()
        enableJTreeMouseDrag()
    }

    private inner class CustomTreeCellRenderer : DefaultTreeCellRenderer() {

        init {
            putClientProperty("html.disable", true) //[HTMLDisabler]

            //remove those ugly default JTree icons
            closedIcon = null
            leafIcon = null
            openIcon = null
        }

        //always use focus colors
        val selectionBackground: Color? = UIManager.getColor("Tree.selectionBackground")?.let { Color(it.rgb, true) }
        val selectionForeground: Color? = UIManager.getColor("Tree.selectionForeground")?.let { Color(it.rgb, true) }
        override fun getBackgroundSelectionColor(): Color? = selectionBackground ?: super.getBackgroundSelectionColor()
        override fun getTextSelectionColor(): Color? = selectionForeground ?: super.getTextSelectionColor()

        //red cell text when a line has an error
        override fun getTreeCellRendererComponent(tree: JTree?, value: Any?, sel: Boolean, expanded: Boolean,
                                                  leaf: Boolean, row: Int, hasFocus: Boolean): Component {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus)
            if (GUI.gui?.editor?.lineHasError(value) == true) foreground = Color.RED
            return this
        }

    }

    //JTree optimization: set the fixed row height to the row default height.
    private fun optimizeJTreeFixedRowHeight() {
        val cellRenderer = jTree.cellRenderer
        if (cellRenderer != null) {
            jTree.rowHeight = cellRenderer.getTreeCellRendererComponent(
                jTree, "a", false, false, false, 0, false).preferredSize.height
        }
    }

    //allows added JTree support for mouse scroll by dragging.
    private fun enableJTreeMouseDrag() {
        jTree.autoscrolls = true
        val mia = object : MouseInputAdapter() {
            override fun mousePressed(e: MouseEvent?) = mousePath(e, isDrag = false)
            override fun mouseClicked(e: MouseEvent?) = mousePath(e, isDrag = false)
            override fun mouseDragged(e: MouseEvent?) = mousePath(e, isDrag = true)
        }
        jTree.addMouseListener(mia)
        jTree.addMouseMotionListener(mia)
    }

    private fun mousePath(e: MouseEvent?, isDrag: Boolean) {
        if (e == null || e.isConsumed) return
        val isLeftMouse = SwingUtilities.isLeftMouseButton(e)
        val isRightMouse = SwingUtilities.isRightMouseButton(e)
        if (!isLeftMouse && !isRightMouse) return
        if (isRightMouse && isDrag) return
        if (isLeftMouse && e.id == MouseEvent.MOUSE_CLICKED) return
        val path = jTree.getClosestPathForLocation(e.x, e.y)
        if (path != null) {
            if (jTree.selectionPath !== path)
                jTree.selectionPath = path
            val pathRow = jTree.getRowForPath(path)
            ensureRowIsVisible(pathRow)
            if (isRightMouse && e.id == MouseEvent.MOUSE_CLICKED)
                showCopyToHexPopup(e)
        }
    }

    fun goUpOneRow() {
        val row = (jTree.leadSelectionRow.coerceAtLeast(0) - 1).coerceAtLeast(0)
        jTree.setSelectionRow(row)
        ensureRowIsVisible(row)
    }

    fun goDownOneRow() {
        val row = (jTree.leadSelectionRow + 1).coerceAtMost(jTree.rowCount - 1)
        jTree.setSelectionRow(row)
        ensureRowIsVisible(row)
    }

    fun toggleExpandSelectedRow() {
        val path = jTree.leadSelectionPath
        if (path != null) {
            val node = path.lastPathComponent
            if (node is TreeNode && !node.isLeaf) {
                if (jTree.isExpanded(path)) {
                    jTree.collapsePath(path)
                } else {
                    jTree.expandPath(path)
                    val row = jTree.getRowForPath(path)
                    if (row != -1) {
                        ensureRowsAreVisible(jTree, row, row + getExpandedChildCount(jTree, node))
                    }
                }
            }
        }
    }

    private fun showCopyToHexPopup(e: MouseEvent) {
        val editorString = gui.editor.selectedString ?: return
        editorString.callInfo { text, _, _, _ -> text } ?: return
        val jPopupMenu = JPopupMenu()
        val jMenuItem = JMenuItem("Copy HEX to clipboard")
        jMenuItem.addActionListener {
            editorString.callInfo { text, _, _, _ ->
                try {
                    val bytes = SBTEStringEncoder.encodeString(text!!)
                    val hex = (0..<bytes.size).joinToString("") { String.format("%02X", bytes[it]) }
                    GUIUtil.setClipboardText(hex)
                } catch (e: Exception) {
                    e.printStackTrace()
                    GUIUtil.setClipboardText("Error")
                }
            }
        }
        jPopupMenu.add(jMenuItem)
        jPopupMenu.show(e.component, e.x, e.y)
    }

    fun ensureRowIsVisible(row: Int) = ensureRowsAreVisible(jTree, row, row)

    private fun ensureRowsAreVisible(tree: JTree, beginRow: Int, endRow: Int) {
        //this is copied from BasicTreeUI.ensureRowsAreVisible()
        val ui = tree.ui
        if (beginRow >= 0 && endRow < ui.getRowCount(tree)) {
            if (beginRow == endRow) {
                val scrollBounds: Rectangle = ui.getPathBounds(tree, ui.getPathForRow(tree, beginRow))
                scrollBounds.x = tree.getVisibleRect().x
                scrollBounds.width = 1
                tree.scrollRectToVisible(scrollBounds)
            } else {
                val beginRect: Rectangle = ui.getPathBounds(tree, ui.getPathForRow(tree, beginRow))
                val visRect = tree.getVisibleRect()
                var testRect = beginRect
                val beginY = beginRect.y
                val maxY = beginY + visRect.height
                var counter: Int = beginRow + 1
                while (counter <= endRow) {
                    testRect = ui.getPathBounds(
                        tree,
                        ui.getPathForRow(tree, counter)
                    )
                    if (testRect.y + testRect.height > maxY) counter = endRow
                    counter++
                }
                tree.scrollRectToVisible(
                    Rectangle(
                        visRect.x, beginY, 1,
                        testRect.y + testRect.height -
                                beginY
                    )
                )
            }
        }
    }

    private fun getExpandedChildCount(tree: JTree, node: TreeNode): Int {
        if (tree.model !is DefaultTreeModel) return 0
        var count = 0
        for (i in 0..<node.childCount) {
            count++
            val childNode = node.getChildAt(i)
            if (isExpanded(tree, childNode))
                count += getExpandedChildCount(tree, node)
        }
        return count
    }

    private fun isExpanded(tree: JTree, node: TreeNode): Boolean {
        return !node.isLeaf && tree.isExpanded(TreePath((tree.model as DefaultTreeModel).getPathToRoot(node)))
    }

}