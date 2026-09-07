package fr.kvngch.memento

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.widget.RemoteViews
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

private const val COLS = 52
private const val LIVED = 0xFFE6E1D7.toInt()
private const val NOW = 0xFFC0392B.toInt()
private const val LEFT = 0xFF262421.toInt()
private const val DIM = 0xFF6E6A63.toInt()

class MementoWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { render(context, manager, it) }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        manager: AppWidgetManager,
        id: Int,
        options: Bundle
    ) {
        render(context, manager, id)
    }
}

// Un seul ImageView : une grille de 4000 points ne peut pas etre faite de RemoteViews.
fun render(context: Context, manager: AppWidgetManager, id: Int) {
    val metrics = context.resources.displayMetrics
    val options = manager.getAppWidgetOptions(id)
    val width = sizePx(options, AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 320, metrics.density, metrics.widthPixels)
    val height = sizePx(options, AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 400, metrics.density, metrics.heightPixels)
    val life = birthDate(context)?.let { life(it, LocalDate.now(), expectancy(context).toLong()) }

    val views = RemoteViews(context.packageName, R.layout.widget_memento)
    views.setImageViewBitmap(R.id.canvas, draw(width, height, life))
    views.setContentDescription(R.id.canvas, describe(life))
    views.setOnClickPendingIntent(
        R.id.canvas,
        PendingIntent.getActivity(
            context,
            id,
            Intent(context, ConfigActivity::class.java)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    )
    manager.updateAppWidget(id, views)
}

fun renderAll(context: Context, extraId: Int = AppWidgetManager.INVALID_APPWIDGET_ID) {
    val manager = AppWidgetManager.getInstance(context)
    val ids = manager.getAppWidgetIds(ComponentName(context, MementoWidget::class.java)).toSet()
    (ids + extraId).filter { it != AppWidgetManager.INVALID_APPWIDGET_ID }
        .forEach { render(context, manager, it) }
}

// Le systeme plafonne la memoire d'un widget a 6 octets par pixel d'ecran : rester
// dans les dimensions de l'ecran garde le bitmap ARGB (4 octets) sous la limite.
private fun sizePx(options: Bundle, key: String, fallbackDp: Int, density: Float, cap: Int): Int {
    val dp = options.getInt(key, 0).takeIf { it > 0 } ?: fallbackDp
    return min((dp * density).toInt(), cap).coerceAtLeast(1)
}

private fun draw(width: Int, height: Int, life: Life?): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val w = width.toFloat()
    val h = height.toFloat()
    val pad = w * 0.07f

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.color = Color.BLACK
    canvas.drawRoundRect(0f, 0f, w, h, w * 0.05f, w * 0.05f, paint)

    val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
    }

    val titleSize = max(9f, h * 0.026f)
    text.color = DIM
    text.textSize = titleSize
    text.letterSpacing = 0.4f
    canvas.drawText("MEMENTO MORI", w / 2f, pad + titleSize, text)
    text.letterSpacing = 0f

    if (life == null) {
        text.color = LIVED
        text.textSize = max(11f, h * 0.030f)
        canvas.drawText("Toucher pour régler", w / 2f, h / 2f, text)
        canvas.drawText("la date de naissance", w / 2f, h / 2f + text.textSize * 1.4f, text)
        return bitmap
    }

    val numbers = NumberFormat.getInstance(Locale.FRANCE)
    val smallSize = max(9f, h * 0.022f)
    val labelSize = max(10f, h * 0.026f)
    val bigSize = max(14f, h * 0.055f)

    val smallY = h - pad
    val labelY = smallY - smallSize * 2.2f
    val bigY = labelY - labelSize * 1.6f

    text.textSize = smallSize
    canvas.drawText(
        "${numbers.format(life.total)} au total  ·  " +
            String.format(Locale.FRANCE, "%.1f %%", life.ratio * 100) + " vécu",
        w / 2f, smallY, text
    )

    text.textSize = labelSize
    text.letterSpacing = 0.25f
    canvas.drawText("SEMAINES RESTANTES", w / 2f, labelY, text)
    text.letterSpacing = 0f

    text.color = LIVED
    text.textSize = bigSize
    canvas.drawText(numbers.format(life.remaining), w / 2f, bigY, text)

    // Une case par semaine, 52 par ligne : une ligne vaut une annee de vie.
    val rows = ceil(life.total / COLS.toFloat()).toInt()
    val top = pad + titleSize * 2.4f
    val bottom = bigY - bigSize * 1.1f
    val cell = min((w - 2 * pad) / COLS, (bottom - top) / rows)
    if (cell < 1f) return bitmap

    val originX = (w - cell * COLS) / 2f
    val originY = top + ((bottom - top) - cell * rows) / 2f
    val radius = max(1f, cell * 0.33f)
    for (i in 0 until life.total) {
        paint.color = when {
            i < life.lived -> LIVED
            i == life.lived -> NOW
            else -> LEFT
        }
        canvas.drawCircle(
            originX + (i % COLS + 0.5f) * cell,
            originY + (i / COLS + 0.5f) * cell,
            radius,
            paint
        )
    }
    return bitmap
}

// Le widget est un bitmap : sans ceci TalkBack n'a rien a lire.
private fun describe(life: Life?): String =
    if (life == null) "Memento mori, date de naissance à régler"
    else "Memento mori, ${life.lived} semaines vécues sur ${life.total}, ${life.remaining} restantes"
