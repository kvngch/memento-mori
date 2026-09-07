package fr.kvngch.memento

import android.content.Context
import java.time.LocalDate
import java.time.temporal.ChronoUnit

// Age de fin par defaut, en annees, tant que rien n'a ete regle.
const val DEFAULT_EXPECTANCY = 80

private const val PREFS = "memento"
private const val KEY_BIRTH = "birth"
private const val KEY_EXPECTANCY = "expectancy"

data class Life(val lived: Int, val total: Int) {
    val remaining: Int get() = total - lived
    val ratio: Float get() = lived.toFloat() / total
}

// Semaines revolues depuis la naissance et semaines que compte la vie entiere.
fun life(birth: LocalDate, today: LocalDate, years: Long): Life {
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

fun expectancy(context: Context): Int =
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getInt(KEY_EXPECTANCY, DEFAULT_EXPECTANCY)

fun saveExpectancy(context: Context, years: Int) {
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .edit()
        .putInt(KEY_EXPECTANCY, years)
        .apply()
}
