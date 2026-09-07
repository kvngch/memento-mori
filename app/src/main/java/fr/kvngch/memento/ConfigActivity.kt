package fr.kvngch.memento

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.NumberPicker
import java.time.LocalDate

// Ecran de configuration du widget : date de naissance, puis age de fin.
class ConfigActivity : Activity() {

    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID

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
        askBirthDate()
    }

    private fun askBirthDate() {
        val start = birthDate(this) ?: LocalDate.now().minusYears(30)
        DatePickerDialog(
            this,
            { _, year, month, day ->
                saveBirth(this, LocalDate.of(year, month + 1, day))
                askExpectancy()
            },
            start.year,
            start.monthValue - 1,
            start.dayOfMonth
        ).apply {
            setTitle(getString(R.string.birth_date))
            datePicker.maxDate = System.currentTimeMillis()
            setOnCancelListener { finish() }
            show()
        }
    }

    private fun askExpectancy() {
        val picker = NumberPicker(this).apply {
            minValue = 1
            maxValue = 120
            value = expectancy(this@ConfigActivity)
            wrapSelectorWheel = false
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.end_age)
            .setView(picker)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                saveExpectancy(this, picker.value)
                done()
            }
            // Renoncer a l'age de fin garde celui deja enregistre, la date est prise.
            .setOnCancelListener { done() }
            .show()
    }

    private fun done() {
        renderAll(this, widgetId)
        setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
        finish()
    }
}
