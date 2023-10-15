package sahlaysta.sbte4.gui

import sahlaysta.sbte4.rom.SBTEMutableBlob
import sahlaysta.sbte4.rom.SBTEStringDescription
import sahlaysta.sbte4.rom.SBTEStringLanguage
import javax.swing.tree.TreeNode
import javax.swing.tree.TreePath
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

//the text string of the editor
internal abstract class GUIEditorString {

    abstract val romBlob: SBTEMutableBlob //The ROM blob that holds the binary of this string [SBTEROM]
    abstract val language: SBTEStringLanguage
    abstract val description: SBTEStringDescription

    abstract val treeNode: TreeNode
    abstract val treePath: TreePath

    protected abstract val mutex: Any //The lock when accessing the info of this string [synchronized]
    protected abstract val text: String? //The text of this string.
    protected abstract val displayText: String //The display text of this string, shown on list.
    protected abstract val hasError: Boolean //If this string has one or more formatting errors.
    protected abstract val caretPosition: Int //The last caret position of the text box of this string.

    abstract fun updateText(text: String?)
    abstract fun updateCaret(pos: Int)

    @OptIn(ExperimentalContracts::class)
    inline fun getInfo(action: (text: String?,
                                displayText: String,
                                hasError: Boolean,
                                caretPosition: Int) -> Unit) {
        contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
        @Suppress("DEPRECATION")
        synchronized(`access$mutex`) {
            return action(`access$text`, `access$displayText`, `access$hasError`, `access$caretPosition`)
        }
    }

    @OptIn(ExperimentalContracts::class)
    inline fun <T> callInfo(action: (text: String?,
                                     displayText: String,
                                     hasError: Boolean,
                                     caretPosition: Int) -> T): T {
        contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
        @Suppress("DEPRECATION")
        synchronized(`access$mutex`) {
            return action(`access$text`, `access$displayText`, `access$hasError`, `access$caretPosition`)
        }
    }

    @Deprecated(message = "")
    @PublishedApi
    @Suppress("DeprecatedCallableAddReplaceWith", "PropertyName", "Unused")
    internal val `access$mutex` get() = mutex

    @Deprecated(message = "")
    @PublishedApi
    @Suppress("DeprecatedCallableAddReplaceWith", "PropertyName", "Unused")
    internal val `access$text` get() = text

    @Deprecated(message = "")
    @PublishedApi
    @Suppress("DeprecatedCallableAddReplaceWith", "PropertyName", "Unused")
    internal val `access$displayText` get() = displayText

    @Deprecated(message = "")
    @PublishedApi
    @Suppress("DeprecatedCallableAddReplaceWith", "PropertyName", "Unused")
    internal val `access$hasError` get() = hasError

    @Deprecated(message = "")
    @PublishedApi
    @Suppress("DeprecatedCallableAddReplaceWith", "PropertyName", "Unused")
    internal val `access$caretPosition` get() = caretPosition

}