package alcatraz.common

import java.util.Locale.getDefault

fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(getDefault()) else it.toString() }
}