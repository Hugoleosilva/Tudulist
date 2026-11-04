package com.hlls.agenda

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Locale

val PT: Locale = Locale.forLanguageTag("pt-BR")
val DAYS = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")
/** Antecedência do alerta, em minutos (0 = sem alerta). */
val LEADS = listOf(0, 15, 30, 60, 90)

fun leadLabel(min: Int): String = when (min) {
    0 -> "Nenhum"
    60 -> "1h"
    90 -> "1h30"
    else -> "$min min"
}

val DAYS_SUN =listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb")
val WEEK_ORDER = listOf(
    DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
)

/** Domingo da semana que contém [d] (calendário padrão: domingo a sábado). */
fun weekStart(d: LocalDate): LocalDate = d.minusDays((d.dayOfWeek.value % 7).toLong())

object C {
    val bg = Color.parseColor("#0F0F12")
    val card = Color.parseColor("#1A1B20")
    val chip = Color.parseColor("#2A2B33")
    val text = Color.parseColor("#F2F2F5")
    val sub = Color.parseColor("#9A9AA6")
    val accent = Color.parseColor("#FF8A4C")
    val CATS = listOf("Treino", "Estudo", "Outro")

    fun cat(c: String): Int = when (c) {
        "Treino" -> Color.parseColor("#FF8A4C")
        "Estudo" -> Color.parseColor("#5B9DFF")
        else -> Color.parseColor("#B28DFF")
    }
}

enum class Repeat(val label: String) {
    ONCE("Uma vez"), DAILY("Todo dia"), WEEKLY("Semanal"), MONTHLY("Mensal")
}

/** Horário de uma atividade num dia (fim opcional). */
data class Slot(val time: LocalTime, val end: LocalTime?) {
    fun text(): String = if (end != null) "$time–$end" else "$time"

    /** Sem fim definido, considera 1h de duração para checar choques. */
    fun effectiveEnd(): LocalTime = end ?: if (time.hour >= 23) LocalTime.of(23, 59) else time.plusHours(1)
}

fun overlaps(a: Slot, b: Slot): Boolean = a.time < b.effectiveEnd() && b.time < a.effectiveEnd()

data class Task(
    val id: Long,
    val title: String,
    val category: String,
    val time: LocalTime,
    val repeat: Repeat,
    val start: LocalDate,
    val days: Set<DayOfWeek>,
    val end: LocalTime? = null,
    val notes: String = "",
    val lead: Int = 30,
    /** Exceções de um dia só: horário diferente nessa data. */
    val moved: Map<LocalDate, Slot> = emptyMap(),
    /** Dias em que esta atividade não acontece. */
    val skipped: Set<LocalDate> = emptySet()
) {
    fun slotOn(d: LocalDate): Slot = moved[d] ?: Slot(time, end)

    fun timeText(): String = if (end != null) "$time–$end" else "$time"

    fun occursOn(d: LocalDate): Boolean {
        if (d.isBefore(start) || d in skipped) return false
        return when (repeat) {
            Repeat.ONCE -> d == start
            Repeat.DAILY -> true
            Repeat.WEEKLY -> d.dayOfWeek in days
            Repeat.MONTHLY -> d.dayOfMonth == minOf(start.dayOfMonth, d.lengthOfMonth())
        }
    }

    fun recurrenceText(): String = when (repeat) {
        Repeat.ONCE -> "%02d/%02d/%d".format(start.dayOfMonth, start.monthValue, start.year)
        Repeat.DAILY -> "Todo dia"
        Repeat.WEEKLY -> days.sortedBy { it.value % 7 }.joinToString(" · ") { DAYS[it.value - 1] }
        Repeat.MONTHLY -> "Todo dia ${start.dayOfMonth}"
    }

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("category", category)
        put("time", time.toString())
        put("repeat", repeat.name)
        put("start", start.toString())
        put("days", days.joinToString(",") { it.value.toString() })
        put("end", end?.toString() ?: "")
        put("notes", notes)
        put("lead", lead)
        put("moved", JSONObject().apply {
            moved.forEach { (d, s) -> put(d.toString(), "${s.time}|${s.end ?: ""}") }
        })
        put("skipped", skipped.joinToString(","))
    }

    companion object {
        fun fromJson(o: JSONObject) = Task(
            id = o.getLong("id"),
            title = o.getString("title"),
            category = o.getString("category"),
            time = LocalTime.parse(o.getString("time")),
            repeat = Repeat.valueOf(o.getString("repeat")),
            start = LocalDate.parse(o.getString("start")),
            days = o.getString("days").split(",").filter { it.isNotBlank() }
                .map { DayOfWeek.of(it.toInt()) }.toSet(),
            end = o.optString("end", "").takeIf { it.isNotBlank() }?.let { LocalTime.parse(it) },
            notes = o.optString("notes", ""),
            lead = o.optInt("lead", 30),
            moved = o.optJSONObject("moved")?.let { m ->
                m.keys().asSequence().associate { k ->
                    val p = m.getString(k).split("|")
                    LocalDate.parse(k) to Slot(
                        LocalTime.parse(p[0]),
                        p.getOrNull(1)?.takeIf { it.isNotBlank() }?.let { LocalTime.parse(it) }
                    )
                }
            } ?: emptyMap(),
            skipped = o.optString("skipped", "").split(",").filter { it.isNotBlank() }
                .map { LocalDate.parse(it) }.toSet()
        )
    }
}

data class Occ(val task: Task, val date: LocalDate) {
    val slot: Slot get() = task.slotOn(date)
    val time: LocalTime get() = slot.time
    val end: LocalTime? get() = slot.end
    val at: LocalDateTime get() = date.atTime(time)
    fun timeText(): String = slot.text()
}

fun occurrencesOn(tasks: List<Task>, d: LocalDate): List<Occ> =
    tasks.filter { it.occursOn(d) }.map { Occ(it, d) }.sortedBy { it.time }

/** "● 06:00  Muay Thai" com a bolinha na cor da categoria. */
fun occLine(o: Occ, textColor: Int): CharSequence {
    val sb = SpannableStringBuilder()
    sb.append("●")
    sb.setSpan(ForegroundColorSpan(C.cat(o.task.category)), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    val start = sb.length
    sb.append("  ${o.timeText()}  ${o.task.title}")
    sb.setSpan(ForegroundColorSpan(textColor), start, sb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    return sb
}
