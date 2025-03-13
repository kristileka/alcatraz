package alcatraz.common

class NonNullList<T>(
    elements: List<T?>,
) : ArrayList<T>(elements.mapNotNull { it }) {
    constructor(vararg elements: T?) : this(elements.asList())
}

fun <T> nonNullListOf(vararg elements: T?): NonNullList<T> =
    if (elements.isNotEmpty()) {
        NonNullList(elements.asList())
    } else {
        NonNullList()
    }
