package sahlaysta.sbte4.rom

/**
 * Creates immutable views over collections.
 * Prevents downcast to MutableCollection, or Java interoperability that allows Collection mutation.
 */
object ImmutableCollections {

    fun <E> immutableList(list: List<E>) : List<E> {
        if (list !is MutableList) return list
        return if (list is RandomAccess) ImmutableRandomAccessList(list) else ImmutableNonRandomAccessList(list)
    }

    fun <E> immutableList(array: Array<out E>) : List<E> {
        return ImmutableArrayBackedList(@Suppress("UNCHECKED_CAST") (array as Array<Any?>))
    }

    fun <E> immutableSet(set: Set<E>): Set<E> {
        if (set !is MutableSet) return set
        return ImmutableSet(set)
    }

    private class ImmutableRandomAccessList<E>(val list: List<E>) : List<E>, RandomAccess {
        override val size get() = list.size
        override fun isEmpty() = list.isEmpty()
        override fun iterator() = ImmutableIterator(list.iterator())
        override fun contains(element: E) = list.contains(element)
        override fun containsAll(elements: Collection<E>) = list.containsAll(elements)
        override fun get(index: Int) = list[index]
        override fun listIterator() = ImmutableListIterator(list.listIterator())
        override fun listIterator(index: Int) = ImmutableListIterator(list.listIterator(index))
        override fun subList(fromIndex: Int, toIndex: Int) = immutableList(list.subList(fromIndex, toIndex))
        override fun indexOf(element: E) = list.indexOf(element)
        override fun lastIndexOf(element: E) = list.lastIndexOf(element)
        override fun equals(other: Any?) = @Suppress("SuspiciousEqualsCombination") (other === this || list == other)
        override fun hashCode() = list.hashCode()
        override fun toString() = list.toString()
    }

    private class ImmutableNonRandomAccessList<E>(val list: List<E>) : List<E> {
        override val size get() = list.size
        override fun isEmpty() = list.isEmpty()
        override fun iterator() = ImmutableIterator(list.iterator())
        override fun contains(element: E) = list.contains(element)
        override fun containsAll(elements: Collection<E>) = list.containsAll(elements)
        override fun get(index: Int) = list[index]
        override fun listIterator() = ImmutableListIterator(list.listIterator())
        override fun listIterator(index: Int) = ImmutableListIterator(list.listIterator(index))
        override fun subList(fromIndex: Int, toIndex: Int) = immutableList(list.subList(fromIndex, toIndex))
        override fun indexOf(element: E) = list.indexOf(element)
        override fun lastIndexOf(element: E) = list.lastIndexOf(element)
        override fun equals(other: Any?) = @Suppress("SuspiciousEqualsCombination") (other === this || list == other)
        override fun hashCode() = list.hashCode()
        override fun toString() = list.toString()
    }

    private class ImmutableArrayBackedList<out E>(val array: Array<out Any?>) : AbstractList<E>(), RandomAccess {
        override val size get() = array.size
        override fun get(index: Int): E {
            if (index < 0)
                throw IndexOutOfBoundsException("Negative index: $index")
            else if (index >= array.size)
                throw IndexOutOfBoundsException("Index: $index, Size: $size")
            else
                return (@Suppress("UNCHECKED_CAST") (array[index] as E))
        }
    }

    private class ImmutableSet<E>(val set: Set<E>) : Set<E> {
        override val size get() = set.size
        override fun isEmpty() = set.isEmpty()
        override fun iterator() = ImmutableIterator(set.iterator())
        override fun contains(element: E) = set.contains(element)
        override fun containsAll(elements: Collection<E>) = set.containsAll(elements)
        override fun equals(other: Any?) = @Suppress("SuspiciousEqualsCombination") (other === this || set == other)
        override fun hashCode() = set.hashCode()
        override fun toString() = set.toString()
    }

    private class ImmutableIterator<E>(val iterator: Iterator<E>) : Iterator<E> {
        override fun hasNext() = iterator.hasNext()
        override fun next() = iterator.next()
    }

    private class ImmutableListIterator<E>(val listIterator: ListIterator<E>) : ListIterator<E> {
        override fun hasNext() = listIterator.hasNext()
        override fun hasPrevious() = listIterator.hasPrevious()
        override fun next() = listIterator.next()
        override fun nextIndex() = listIterator.nextIndex()
        override fun previous() = listIterator.previous()
        override fun previousIndex() = listIterator.previousIndex()
    }

}