package fr.kvngch.memento

import android.app.Activity
import android.app.DatePickerDialog
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import java.time.LocalDate

// Ecran de configuration du widget : la date de naissance, rien d'autre.
class ConfigActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        setResult(RESULT_CANCELED, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id))
        if (id == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val start = birthDate(this) ?: LocalDate.now().minusYears(30)
        DatePickerDialog(
            this,
            { _, year, month, day ->
                saveBirth(this, LocalDate.of(year, month + 1, day))
                renderAll(this, id)
                setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id))
                finish()
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
}
