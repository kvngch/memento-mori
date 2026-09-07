package fr.kvngch.memento

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate
import java.time.temporal.ChronoUnit

// Age de fin par defaut, en annees, tant que rien n'a ete regle.
const val DEFAULT_EXPECTANCY = 80

private const val PREFS = "memento"
private const val KEY_BIRTH = "birth"
private const val KEY_EXPECTANCY = "expectancy"
private const val KEY_SCALE = "scale"

// Unite de la grille. Le nombre de colonnes fait qu'une ligne vaut toujours une annee.
enum class Scale(
    val cols: Int,
    val chrono: ChronoUnit,
    val noun: String,
    val remaining: String
) {
    DAYS(365, ChronoUnit.DAYS, "jours", "JOURS RESTANTS"),
    WEEKS(52, ChronoUnit.WEEKS, "semaines", "SEMAINES RESTANTES"),
    MONTHS(12, ChronoUnit.MONTHS, "mois", "MOIS RESTANTS")
}

data class Life(val lived: Int, val total: Int) {
    val remaining: Int get() = total - lived
    val ratio: Float get() = lived.toFloat() / total
}

// Unites revolues depuis la naissance et unites que compte la vie entiere.
fun life(birth: LocalDate, today: LocalDate, years: Long, scale: Scale): Life {
    val total = scale.chrono.between(birth, birth.plusYears(years)).toInt()
    val lived = scale.chrono.between(birth, today).toInt().coerceIn(0, total)
    return Life(lived, total)
}

private fun prefs(context: Context): SharedPreferences =
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

fun birthDate(context: Context): LocalDate? =
    prefs(context).getString(KEY_BIRTH, null)
        ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

fun saveBirth(context: Context, date: LocalDate) {
    prefs(context).edit().putString(KEY_BIRTH, date.toString()).apply()
}

fun expectancy(context: Context): Int =
    prefs(context).getInt(KEY_EXPECTANCY, DEFAULT_EXPECTANCY)

fun saveExpectancy(context: Context, years: Int) {
    prefs(context).edit().putInt(KEY_EXPECTANCY, years).apply()
}

fun scale(context: Context): Scale =
    runCatching { Scale.valueOf(prefs(context).getString(KEY_SCALE, "")!!) }
        .getOrDefault(Scale.WEEKS)

fun saveScale(context: Context, scale: Scale) {
    prefs(context).edit().putString(KEY_SCALE, scale.name).apply()
}
