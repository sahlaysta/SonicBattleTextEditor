package sahlaysta.sbte4.gui

import com.formdev.flatlaf.ui.FlatRootPaneUI
import com.formdev.flatlaf.ui.FlatTitlePane
import javax.swing.JComponent
import javax.swing.JFrame

/* disables HTML in the FlatLaf title pane. HTML triggers whenever a string starts with "<html>".
   that can cause unintended side-effects! */
internal object HTMLDisabler {

    //protected field: titlePane
    private val titlePaneField = FlatRootPaneUI::class.java.getDeclaredField("titlePane")
        .apply { isAccessible = true }

    //protected field: titleLabel
    private val titleLabelField = FlatTitlePane::class.java.getDeclaredField("titleLabel")
        .apply { isAccessible = true }

    fun disableHTMLInTitlePane(jFrame: JFrame) {
        val rootPaneUI = jFrame.rootPane?.ui
        if (rootPaneUI !is FlatRootPaneUI) return
        val titlePane = titlePaneField.get(rootPaneUI) as FlatTitlePane
        val titleLabel = titleLabelField.get(titlePane) as JComponent
        titleLabel.putClientProperty("html.disable", true)
    }

}