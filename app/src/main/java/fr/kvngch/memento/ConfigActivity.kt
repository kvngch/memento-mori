package fr.kvngch.memento

import android.app.Activity
import android.app.DatePickerDialog
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.NumberPicker
import android.widget.RadioGroup
import android.widget.TextView
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DATE = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRANCE)

// Ecran de configuration du widget, ouvert a l'ajout puis a chaque appui dessus.
// Les deux reglages tiennent sur le meme ecran, avec la date de fin qui en decoule :
// le titre d'un DatePickerDialog en mode calendrier n'est pas affiche, un dialogue
// seul ne dirait donc pas ce qu'il demande.
class ConfigActivity : Activity() {

    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var birth = LocalDate.now().minusYears(30)
    private lateinit var birthButton: Button
    private lateinit var agePicker: NumberPicker
    private lateinit var scaleGroup: RadioGroup
    private lateinit var summary: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        widgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        setResult(RESULT_CANCELED, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContentView(R.layout.activity_config)
        birth = birthDate(this) ?: birth
        birthButton = findViewById(R.id.birth)
        summary = findViewById(R.id.summary)
        agePicker = findViewById<NumberPicker>(R.id.age).apply {
            minValue = 1
            maxValue = 120
            value = expectancy(this@ConfigActivity)
            wrapSelectorWheel = false
            setOnValueChangedListener { _, _, _ -> refresh() }
        }

        scaleGroup = findViewById<RadioGroup>(R.id.scale).apply {
            check(
                when (scale(this@ConfigActivity)) {
                    Scale.DAYS -> R.id.scale_days
                    Scale.WEEKS -> R.id.scale_weeks
                    Scale.MONTHS -> R.id.scale_months
                }
            )
            setOnCheckedChangeListener { _, _ -> refresh() }
        }

        birthButton.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    birth = LocalDate.of(year, month + 1, day)
                    refresh()
                },
                birth.year,
                birth.monthValue - 1,
                birth.dayOfMonth
            ).apply {
                datePicker.maxDate = System.currentTimeMillis()
                show()
            }
        }

        findViewById<Button>(R.id.done).setOnClickListener {
            saveBirth(this, birth)
            saveExpectancy(this, agePicker.value)
            saveScale(this, selectedScale())
            renderAll(this, widgetId)
            setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
            finish()
        }

        refresh()
    }

    private fun selectedScale(): Scale = when (scaleGroup.checkedRadioButtonId) {
        R.id.scale_days -> Scale.DAYS
        R.id.scale_months -> Scale.MONTHS
        else -> Scale.WEEKS
    }

    private fun refresh() {
        val years = agePicker.value.toLong()
        val scale = selectedScale()
        val counted = life(birth, LocalDate.now(), years, scale)
        val numbers = NumberFormat.getInstance(Locale.FRANCE)
        birthButton.text = birth.format(DATE)
        summary.text = getString(
            R.string.summary,
            birth.plusYears(years).format(DATE),
            numbers.format(counted.total),
            scale.noun,
            numbers.format(counted.remaining)
        )
    }
}
