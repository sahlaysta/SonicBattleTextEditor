package sahlaysta.sbte4.gui

import javax.swing.text.BadLocationException
import javax.swing.text.Document
import javax.swing.text.Segment

//access the chars of a Document without having to convert to a String every time.
internal object DocumentOptimizer {

    fun documentAsCharSequence(document: Document): CharSequence {
        return DocumentCharSequence(document)
    }

    private class DocumentCharSequence(val document: Document) : CharSequence {

        val segment = Segment().apply { isPartialReturn = true }

        override val length get() = document.length

        override fun get(index: Int): Char {
            try {
                document.getText(index, 1, segment)
            } catch (e: BadLocationException) { throw IndexOutOfBoundsException() }
            return segment.array[segment.offset]
        }

        override fun toString(): String {
            return document.getText(0, document.length)
        }

        override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
            try {
                return document.getText(startIndex, endIndex - startIndex)
            } catch (e: BadLocationException) { throw IndexOutOfBoundsException() }
        }

        override fun hashCode(): Int {
            var hash = 0
            for (ch in this)
                hash = 31 * hash + ch.code
            return hash
        }

        override fun equals(other: Any?): Boolean {
            return other === this || (other is CharSequence && contentEquals(other))
        }

    }

}