package com.hlls.agenda

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextClock
import android.widget.TextView
import android.widget.Toast
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

class MainActivity : Activity() {
    private val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
    private val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT
    private val dialogTheme = android.R.style.Theme_Material_Dialog_Alert

    private lateinit var content: LinearLayout
    private lateinit var tabViews: List<TextView>
    private var tab = 0
    private var weekRef = LocalDate.now()
    private var monthRef = YearMonth.now()
    private var selDay = LocalDate.now()
    private var tasks: List<Task> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Scheduler.createChannel(this)
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
        }
        Scheduler.reschedule(this)
        setContentView(buildRoot())
        render()
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    // ---------- helpers de UI ----------

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun text(s: CharSequence, size: Float = 15f, color: Int = C.text, bold: Boolean = false) =
        TextView(this).apply {
            text = s
            setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
            setTextColor(color)
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }

    private fun round(color: Int, radiusDp: Int, strokeColor: Int? = null) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radiusDp).toFloat()
        if (strokeColor != null) setStroke(dp(1), strokeColor)
    }

    private fun lp(w: Int, h: Int, bottom: Int = 0) =
        LinearLayout.LayoutParams(w, h).apply { bottomMargin = dp(bottom) }

    private fun longDate(d: LocalDate) =
        d.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", PT)).replaceFirstChar { it.uppercase() }

    private fun shortDate(d: LocalDate) =
        d.format(DateTimeFormatter.ofPattern("d MMM", PT))

    private fun chip(label: String, weighted: Boolean) = text(label, 14f).apply {
        gravity = Gravity.CENTER
        setPadding(dp(if (weighted) 4 else 14), dp(9), dp(if (weighted) 4 else 14), dp(9))
        layoutParams = if (weighted) LinearLayout.LayoutParams(0, WRAP, 1f).apply { rightMargin = dp(6) }
        else LinearLayout.LayoutParams(WRAP, WRAP)
    }

    private fun setChip(v: TextView, on: Boolean, color: Int) {
        v.background = round(if (on) color else C.chip, 20)
        v.setTextColor(if (on) 0xFF101010.toInt() else C.text)
    }

    private fun row(views: List<View>) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        views.forEach { addView(it) }
    }

    // ---------- estrutura ----------

    private fun buildRoot(): View {
        val frame = FrameLayout(this).apply { setBackgroundColor(C.bg) }
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(20), dp(16), dp(20), dp(8))
        }
        header.addView(TextClock(this).apply {
            format24Hour = "HH:mm"
            format12Hour = "HH:mm"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 40f)
            setTextColor(C.text)
            typeface = Typeface.DEFAULT_BOLD
        })
        header.addView(TextClock(this).apply {
            format24Hour = "EEE, d 'de' MMM"
            format12Hour = "EEE, d 'de' MMM"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTextColor(C.sub)
            setPadding(dp(14), dp(10), 0, 0)
        })

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(8), dp(16), dp(96))
        }
        val scroll = ScrollView(this).apply {
            isVerticalScrollBarEnabled = false
            addView(content)
        }

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(C.card)
        }
        tabViews = listOf("Hoje", "Amanhã", "Semana", "Mês").mapIndexed { i, n ->
            text(n, 14f, C.sub, true).apply {
                gravity = Gravity.CENTER
                setOnClickListener { tab = i; render() }
            }
        }
        tabViews.forEach { bar.addView(it, LinearLayout.LayoutParams(0, dp(56), 1f)) }

        col.addView(header)
        col.addView(scroll, LinearLayout.LayoutParams(MATCH, 0, 1f))
        col.addView(bar, LinearLayout.LayoutParams(MATCH, WRAP))
        frame.addView(col, FrameLayout.LayoutParams(MATCH, MATCH))

        val fab = text("+", 30f, 0xFF101010.toInt(), true).apply {
            gravity = Gravity.CENTER
            background = round(C.accent, 29)
            setOnClickListener {
                val d = when (tab) {
                    1 -> LocalDate.now().plusDays(1)
                    3 -> selDay
                    else -> LocalDate.now()
                }
                editTask(null, d)
            }
        }
        frame.addView(fab, FrameLayout.LayoutParams(dp(58), dp(58), Gravity.END or Gravity.BOTTOM).apply {
            rightMargin = dp(20)
            bottomMargin = dp(76)
        })
        return frame
    }

    private fun render() {
        tasks = Store.load(this)
        content.removeAllViews()
        when (tab) {
            0 -> {
                heading("Hoje", longDate(LocalDate.now()))
                list(LocalDate.now())
            }
            1 -> {
                val d = LocalDate.now().plusDays(1)
                heading("Lembretes", "Amanhã · " + longDate(d))
                list(d)
            }
            2 -> renderWeek()
            else -> renderMonth()
        }
        tabViews.forEachIndexed { i, v -> v.setTextColor(if (i == tab) C.accent else C.sub) }
    }

    private fun heading(title: String, sub: String) {
        content.addView(text(title, 26f, C.text, true).apply { setPadding(dp(4), dp(8), 0, 0) })
        content.addView(text(sub, 14f, C.sub).apply { setPadding(dp(4), 0, 0, dp(12)) })
    }

    private fun list(d: LocalDate) {
        val occs = occurrencesOn(tasks, d)
        if (occs.isEmpty()) {
            content.addView(text("Nada marcado. Toque em + para adicionar.", 14f, C.sub).apply {
                setPadding(dp(4), dp(8), 0, 0)
            })
        } else {
            occs.forEach { content.addView(card(it), lp(MATCH, WRAP, 8)) }
        }
    }

    private fun card(o: Occ): View {
        val r = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = round(C.card, 14)
            setPadding(dp(14), dp(12), dp(14), dp(12))
            setOnClickListener { showDetails(o.task, o.date) }
        }
        r.addView(View(this).apply { background = round(C.cat(o.task.category), 2) }, LinearLayout.LayoutParams(dp(4), dp(40)))
        val times = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), 0, dp(14), 0)
        }
        times.addView(text(o.time.toString(), 20f, C.text, true))
        o.end?.let { times.addView(text(it.toString(), 12f, C.sub)) }
        r.addView(times)
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        col.addView(text(o.task.title, 16f, C.text, true))
        col.addView(text(
            "${o.task.category} · ${o.task.recurrenceText()}" + if (o.task.notes.isNotBlank()) " · 📝" else "",
            12f, C.sub
        ))
        r.addView(col, LinearLayout.LayoutParams(0, WRAP, 1f))
        return r
    }

    private fun nav(label: String, onLabel: () -> Unit, onPrev: () -> Unit, onNext: () -> Unit) {
        val r = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        val arrow = { s: String, f: () -> Unit ->
            text(s, 28f, C.accent, true).apply {
                gravity = Gravity.CENTER
                setOnClickListener { f(); render() }
            }
        }
        r.addView(arrow("‹", onPrev), LinearLayout.LayoutParams(dp(48), dp(48)))
        r.addView(text(label, 18f, C.text, true).apply {
            gravity = Gravity.CENTER
            setOnClickListener { onLabel(); render() }
        }, LinearLayout.LayoutParams(0, WRAP, 1f))
        r.addView(arrow("›", onNext), LinearLayout.LayoutParams(dp(48), dp(48)))
        content.addView(r, lp(MATCH, WRAP, 8))
    }

    // ---------- semana ----------

    private fun renderWeek() {
        val monday = weekStart(weekRef)
        nav(
            "${shortDate(monday)} – ${shortDate(monday.plusDays(6))}",
            { weekRef = LocalDate.now() },
            { weekRef = weekRef.minusWeeks(1) },
            { weekRef = weekRef.plusWeeks(1) }
        )
        for (i in 0..6) content.addView(weekRow(monday.plusDays(i.toLong())), lp(MATCH, WRAP, 8))
    }

    private fun weekRow(d: LocalDate): View {
        val isToday = d == LocalDate.now()
        val r = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            background = round(C.card, 14, if (isToday) C.accent else null)
            setPadding(dp(12), dp(10), dp(12), dp(10))
            setOnClickListener { editTask(null, d) }
        }
        val left = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }
        left.addView(text(DAYS[d.dayOfWeek.value - 1].uppercase(), 11f, if (isToday) C.accent else C.sub, true))
        left.addView(text("${d.dayOfMonth}", 20f, if (isToday) C.accent else C.text, true))
        r.addView(left, LinearLayout.LayoutParams(dp(48), WRAP))

        val right = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val occs = occurrencesOn(tasks, d)
        if (occs.isEmpty()) {
            right.addView(text("livre", 13f, C.sub))
        } else {
            occs.forEach { o ->
                right.addView(text(occLine(o, C.text), 14f).apply {
                    setPadding(0, dp(3), 0, dp(3))
                    setOnClickListener { showDetails(o.task, o.date) }
                })
            }
        }
        r.addView(right, LinearLayout.LayoutParams(0, WRAP, 1f))
        return r
    }

    // ---------- mês ----------

    private fun renderMonth() {
        val name = monthRef.month.getDisplayName(TextStyle.FULL, PT).replaceFirstChar { it.uppercase() }
        nav(
            "$name ${monthRef.year}",
            { monthRef = YearMonth.now(); selDay = LocalDate.now() },
            { monthRef = monthRef.minusMonths(1) },
            { monthRef = monthRef.plusMonths(1) }
        )
        val heads = LinearLayout(this)
        DAYS_SUN.forEach { heads.addView(text(it, 11f, C.sub).apply { gravity = Gravity.CENTER }, LinearLayout.LayoutParams(0, WRAP, 1f)) }
        content.addView(heads, lp(MATCH, WRAP, 4))

        val offset = monthRef.atDay(1).dayOfWeek.value % 7
        val len = monthRef.lengthOfMonth()
        val rows = (offset + len + 6) / 7
        for (rIdx in 0 until rows) {
            val line = LinearLayout(this)
            for (c in 0..6) {
                val day = rIdx * 7 + c - offset + 1
                val cellLp = LinearLayout.LayoutParams(0, dp(52), 1f).apply { setMargins(dp(2), dp(2), dp(2), dp(2)) }
                if (day in 1..len) line.addView(monthCell(monthRef.atDay(day)), cellLp)
                else line.addView(View(this), cellLp)
            }
            content.addView(line)
        }

        content.addView(text(longDate(selDay), 16f, C.text, true).apply { setPadding(dp(4), dp(18), 0, dp(10)) })
        list(selDay)
    }

    private fun monthCell(d: LocalDate): View {
        val isToday = d == LocalDate.now()
        val selected = d == selDay
        val cell = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = round(
                if (selected) 0x33FF8A4C else 0x00000000,
                10,
                if (isToday) C.accent else null
            )
            setOnClickListener { selDay = d; render() }
        }
        cell.addView(text("${d.dayOfMonth}", 15f, if (isToday) C.accent else C.text, isToday).apply {
            gravity = Gravity.CENTER
        })
        val cats = occurrencesOn(tasks, d).map { it.task.category }.distinct().take(3)
        val dots = SpannableStringBuilder(" ")
        cats.forEachIndexed { i, c ->
            if (i == 0) dots.clear()
            dots.append("●")
            dots.setSpan(ForegroundColorSpan(C.cat(c)), dots.length - 1, dots.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        cell.addView(text(dots, 9f).apply { gravity = Gravity.CENTER })
        return cell
    }

    // ---------- detalhes ----------

    private fun showDetails(t: Task, d: LocalDate) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), dp(8))
        }
        box.addView(text(t.slotOn(d).text(), 22f, C.text, true))
        box.addView(text("${t.category} · ${t.recurrenceText()}", 14f, C.sub).apply {
            setPadding(0, dp(4), 0, dp(14))
        })
        val has = t.notes.isNotBlank()
        box.addView(text(if (has) t.notes else "Sem anotações.", 15f, if (has) C.text else C.sub))
        val builder = AlertDialog.Builder(this, dialogTheme)
            .setTitle(t.title)
            .setView(ScrollView(this).apply { addView(box) })
            .setPositiveButton("Editar") { _, _ -> editTask(t, d) }
            .setNegativeButton("Fechar", null)
        if (t.repeat != Repeat.ONCE) {
            builder.setNeutralButton("Só este dia") { _, _ ->
                changeDay(t, d) { upd ->
                    checkAndSave(tasks.map { if (it.id == upd.id) upd else it }, upd.id) { render() }
                }
            }
        }
        val dlg = builder.create()
        dlg.show()
        dlg.window?.setBackgroundDrawable(round(C.card, 20))
    }

    // ---------- choque de horário ----------

    private class Conflict(val date: LocalDate, val other: Task, val extraDays: Int)

    /** Primeiro dia (nos próximos 90) em que [candId] bate com outra atividade. */
    private fun findConflict(work: List<Task>, candId: Long): Conflict? {
        val cand = work.firstOrNull { it.id == candId } ?: return null
        var first: Pair<LocalDate, Task>? = null
        var count = 0
        val today = LocalDate.now()
        for (i in 0..90) {
            val d = today.plusDays(i.toLong())
            if (!cand.occursOn(d)) continue
            val s = cand.slotOn(d)
            val other = work.firstOrNull { it.id != candId && it.occursOn(d) && overlaps(s, it.slotOn(d)) }
                ?: continue
            count++
            if (first == null) first = d to other
        }
        return first?.let { Conflict(it.first, it.second, count - 1) }
    }

    /** Salva [work]; se [candId] bater com outra atividade, pergunta o que fazer antes. */
    private fun checkAndSave(work: List<Task>, candId: Long, onSaved: () -> Unit) {
        val c = findConflict(work, candId)
        if (c == null) {
            Store.save(this, work)
            onSaved()
            return
        }
        val cand = work.first { it.id == candId }
        val d = c.date
        fun replace(upd: Task) = work.map { if (it.id == upd.id) upd else it }
        val keep = if (c.extraDays > 0) "Manter os dois assim (em ${c.extraDays + 1} dias)" else "Manter os dois assim"
        val options = arrayOf(
            "Alterar só \"${cand.title}\" neste dia (${cand.slotOn(d).text()})",
            "Alterar só \"${c.other.title}\" neste dia (${c.other.slotOn(d).text()})",
            keep
        )
        AlertDialog.Builder(this, dialogTheme)
            .setTitle("Choque de horário · " + longDate(d))
            .setItems(options) { _, which ->
                when (which) {
                    0 -> changeDay(cand, d) { upd -> checkAndSave(replace(upd), candId, onSaved) }
                    1 -> changeDay(c.other, d) { upd -> checkAndSave(replace(upd), candId, onSaved) }
                    else -> {
                        Store.save(this, work)
                        onSaved()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /** Muda o horário de [t] só em [d] (ou pula o dia), sem mexer nas outras datas. */
    private fun changeDay(t: Task, d: LocalDate, onDone: (Task) -> Unit) {
        val s = t.slotOn(d)
        var st = s.time
        var en = s.end ?: addHour(st)
        val stBtn = chip("", false)
        val enBtn = chip("", false)
        fun refresh() {
            setChip(stBtn, false, C.accent)
            setChip(enBtn, false, C.accent)
            stBtn.text = st.toString()
            enBtn.text = en.toString()
        }
        stBtn.setOnClickListener {
            TimePickerDialog(this, dialogTheme, { _, h, m ->
                st = LocalTime.of(h, m)
                if (!en.isAfter(st)) en = addHour(st)
                refresh()
            }, st.hour, st.minute, true).show()
        }
        enBtn.setOnClickListener {
            TimePickerDialog(this, dialogTheme, { _, h, m ->
                val e = LocalTime.of(h, m)
                if (e.isAfter(st)) en = e
                else Toast.makeText(this, "O fim deve ser depois do início", Toast.LENGTH_SHORT).show()
                refresh()
            }, en.hour, en.minute, true).show()
        }
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), dp(8))
        }
        box.addView(text(longDate(d) + " · só este dia", 14f, C.sub).apply { setPadding(0, 0, 0, dp(8)) })
        val r = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val c1 = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        c1.addView(text("Início", 13f, C.sub).apply { setPadding(0, dp(8), 0, dp(6)) }); c1.addView(stBtn)
        val c2 = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), 0, 0, 0) }
        c2.addView(text("Fim", 13f, C.sub).apply { setPadding(0, dp(8), 0, dp(6)) }); c2.addView(enBtn)
        r.addView(c1); r.addView(c2)
        box.addView(r)
        box.addView(text("As outras datas continuam como estão.", 12f, C.sub).apply { setPadding(0, dp(14), 0, 0) })
        refresh()
        val dlg = AlertDialog.Builder(this, dialogTheme)
            .setTitle("Mudar \"${t.title}\"")
            .setView(box)
            .setPositiveButton("Salvar") { _, _ ->
                onDone(t.copy(moved = t.moved + (d to Slot(st, en)), skipped = t.skipped - d))
            }
            .setNeutralButton("Pular este dia") { _, _ ->
                onDone(t.copy(skipped = t.skipped + d, moved = t.moved - d))
            }
            .setNegativeButton("Cancelar", null)
            .create()
        dlg.show()
        dlg.window?.setBackgroundDrawable(round(C.card, 20))
    }

    private fun addHour(t: LocalTime) = if (t.hour >= 23) LocalTime.of(23, 59) else t.plusHours(1)

    // ---------- cadastro / edição ----------

    private fun editTask(existing: Task?, defDate: LocalDate) {
        var category = existing?.category ?: C.CATS[0]
        var time = existing?.time ?: LocalTime.of(6, 0)
        var rep = existing?.repeat ?: Repeat.ONCE
        var date = existing?.start ?: defDate
        var end = existing?.end ?: addHour(time)
        var lead = existing?.lead ?: 30
        val days =(existing?.days ?: setOf(date.dayOfWeek)).toMutableSet()

        val input = EditText(this).apply {
            setText(existing?.title ?: "")
            hint = "Ex: Muay Thai"
            setTextColor(C.text)
            setHintTextColor(C.sub)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            setSingleLine()
        }
        val catChips = C.CATS.map { chip(it, true) }
        val repChips = Repeat.values().map { chip(it.label, true) }
        val dayChips = WEEK_ORDER.map { chip(DAYS[it.value - 1], true) }
        val leadChips = LEADS.map { chip(leadLabel(it), true) }
        val notesInput = EditText(this).apply {
            setText(existing?.notes ?: "")
            hint = "Ex: levar luvas, revisar capítulo 3..."
            setTextColor(C.text)
            setHintTextColor(C.sub)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            minLines = 2
            maxLines = 6
            gravity = Gravity.TOP
        }
        val timeBtn = chip("", false)
        val endBtn = chip("", false)
        val dateBtn = chip("", false)
        val dateLabel = text("Data", 13f, C.sub)
        val hint = text("", 13f, C.sub)
        val daysRow = row(dayChips).apply { setPadding(0, dp(8), 0, 0) }

        fun refresh() {
            C.CATS.forEachIndexed { i, c -> setChip(catChips[i], c == category, C.cat(c)) }
            Repeat.values().forEachIndexed { i, r -> setChip(repChips[i], r == rep, C.accent) }
            WEEK_ORDER.forEachIndexed { i, d -> setChip(dayChips[i], d in days, C.accent) }
            setChip(timeBtn, false, C.accent)
            setChip(endBtn, false, C.accent)
            endBtn.text = end.toString()
            LEADS.forEachIndexed { i, m -> setChip(leadChips[i], m == lead, C.accent) }
            setChip(dateBtn, false, C.accent)
            daysRow.visibility = if (rep == Repeat.WEEKLY) View.VISIBLE else View.GONE
            timeBtn.text = time.toString()
            dateLabel.text = if (rep == Repeat.ONCE) "Data" else "A partir de"
            dateBtn.text = "%02d/%02d/%d".format(date.dayOfMonth, date.monthValue, date.year)
            hint.text = "Repete: " + Task(0, "", category, time, rep, date, days).recurrenceText() +
                if (lead > 0) " · alerta ${leadLabel(lead)} antes" else " · sem alerta"
        }

        catChips.forEachIndexed { i, v -> v.setOnClickListener { category = C.CATS[i]; refresh() } }
        repChips.forEachIndexed { i, v -> v.setOnClickListener { rep = Repeat.values()[i]; refresh() } }
        leadChips.forEachIndexed { i, v -> v.setOnClickListener { lead = LEADS[i]; refresh() } }
        dayChips.forEachIndexed { i, v ->
            v.setOnClickListener {
                val d = WEEK_ORDER[i]
                if (!days.add(d)) days.remove(d)
                refresh()
            }
        }
        timeBtn.setOnClickListener {
            TimePickerDialog(this, dialogTheme, { _, h, m ->
                time = LocalTime.of(h, m)
                if (!end.isAfter(time)) end = addHour(time)
                refresh()
            }, time.hour, time.minute, true).show()
        }
        endBtn.setOnClickListener {
            TimePickerDialog(this, dialogTheme, { _, h, m ->
                val e = LocalTime.of(h, m)
                if (e.isAfter(time)) end = e
                else Toast.makeText(this, "O fim deve ser depois do início", Toast.LENGTH_SHORT).show()
                refresh()
            }, end.hour, end.minute, true).show()
        }
        dateBtn.setOnClickListener {
            DatePickerDialog(this, dialogTheme, { _, y, m, d -> date = LocalDate.of(y, m + 1, d); refresh() },
                date.year, date.monthValue - 1, date.dayOfMonth).show()
        }

        fun label(s: String) = text(s, 13f, C.sub).apply { setPadding(0, dp(14), 0, dp(6)) }
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), dp(8))
        }
        box.addView(label("Atividade"))
        box.addView(input)
        box.addView(label("Tipo"))
        box.addView(row(catChips))
        box.addView(label("Repetir"))
        box.addView(row(repChips))
        box.addView(daysRow)
        val when2 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val c1 = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        c1.addView(label("Início")); c1.addView(timeBtn)
        val c2 = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), 0, 0, 0) }
        c2.addView(label("Fim")); c2.addView(endBtn)
        when2.addView(c1); when2.addView(c2)
        box.addView(when2)
        dateLabel.setPadding(0, dp(14), 0, dp(6))
        box.addView(dateLabel)
        box.addView(dateBtn)
        box.addView(label("Alerta antes do início"))
        box.addView(row(leadChips))
        box.addView(label("Anotações (opcional)"))
        box.addView(notesInput)
        box.addView(hint.apply { setPadding(0, dp(16), 0, dp(8)) })
        refresh()

        val builder = AlertDialog.Builder(this, dialogTheme)
            .setTitle(if (existing == null) "Nova atividade" else "Editar atividade")
            .setView(ScrollView(this).apply { addView(box) })
            .setPositiveButton("Salvar", null)
            .setNegativeButton("Cancelar", null)
        if (existing != null) builder.setNeutralButton("Excluir", null)
        val dlg = builder.create()
        dlg.show()
        dlg.window?.setBackgroundDrawable(round(C.card, 20))

        dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val n = input.text.toString().trim()
            if (n.isEmpty()) {
                input.error = "Dê um nome"
                return@setOnClickListener
            }
            if (rep == Repeat.WEEKLY && days.isEmpty()) {
                Toast.makeText(this, "Escolha ao menos um dia da semana", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val t = Task(
                existing?.id ?: System.currentTimeMillis(), n, category, time, rep, date,
                if (rep == Repeat.WEEKLY) days.toSet() else emptySet(),
                end, notesInput.text.toString().trim(), lead,
                existing?.moved ?: emptyMap(), existing?.skipped ?: emptySet()
            )
            checkAndSave(tasks.filter { it.id != t.id } + t, t.id) {
                dlg.dismiss()
                render()
            }
        }
        if (existing != null) {
            dlg.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                AlertDialog.Builder(this, dialogTheme)
                    .setTitle("Excluir atividade?")
                    .setMessage("\"${existing.title}\" e todas as repetições serão removidas.")
                    .setPositiveButton("Excluir") { _, _ ->
                        Store.save(this, tasks.filter { it.id != existing.id })
                        dlg.dismiss()
                        render()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }
    }
}
