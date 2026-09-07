package fr.kvngch.memento

import android.content.Context
import java.time.LocalDate
import java.time.temporal.ChronoUnit

// Esperance de vie retenue pour le decompte, en annees.
// ponytail: constante, un second dialog de reglage si elle doit changer sans rebuild
const val EXPECTANCY_YEARS = 80L

private const val PREFS = "memento"
private const val KEY_BIRTH = "birth"

data class Life(val lived: Int, val total: Int) {
    val remaining: Int get() = total - lived
    val ratio: Float get() = lived.toFloat() / total
}

// Semaines revolues depuis la naissance et semaines que compte la vie entiere.
fun life(birth: LocalDate, today: LocalDate, years: Long = EXPECTANCY_YEARS): Life {
    val total = ChronoUnit.WEEKS.between(birth, birth.plusYears(years)).toInt()
    val lived = ChronoUnit.WEEKS.between(birth, today).toInt().coerceIn(0, total)
    return Life(lived, total)
}

fun birthDate(context: Context): LocalDate? =
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getString(KEY_BIRTH, null)
        ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

fun saveBirth(context: Context, date: LocalDate) {
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_BIRTH, date.toString())
        .apply()
}
