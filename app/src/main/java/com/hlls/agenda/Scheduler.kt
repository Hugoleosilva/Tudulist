package com.hlls.agenda

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object Scheduler {
    const val CHANNEL = "lembretes"
    const val ACTION_ALARM = "com.hlls.agenda.ALARM"

    fun createChannel(ctx: Context) {
        val ch = NotificationChannel(CHANNEL, "Lembretes", NotificationManager.IMPORTANCE_HIGH)
        ch.description = "Aviso 30 minutos antes de cada atividade"
        ctx.getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
    }

    /** Primeiro lembrete (hora da atividade - 30 min) depois de [after]. */
    fun nextReminder(tasks: List<Task>, after: LocalDateTime): LocalDateTime? {
        var best: LocalDateTime? = null
        var d = after.toLocalDate()
        val end = d.plusDays(366)
        while (!d.isAfter(end)) {
            val b = best
            if (b != null && d.isAfter(b.toLocalDate().plusDays(1))) break
            for (t in tasks) {
                if (t.lead <= 0 || !t.occursOn(d)) continue
                val r = d.atTime(t.slotOn(d).time).minusMinutes(t.lead.toLong())
                if (r.isAfter(after) && (b == null || r.isBefore(best))) best = r
            }
            d = d.plusDays(1)
        }
        return best
    }

    fun reschedule(ctx: Context, after: LocalDateTime = LocalDateTime.now()) {
        val am = ctx.getSystemService(AlarmManager::class.java)
        val pi = PendingIntent.getBroadcast(
            ctx, 1,
            Intent(ctx, AgendaReceiver::class.java).setAction(ACTION_ALARM),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pi)
        val next = nextReminder(Store.load(ctx), after) ?: return
        val trigger = next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi)
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi)
        }
    }

    /** Notifica as atividades cujo lembrete caiu na janela do alarme que acabou de tocar. */
    fun notifyDue(ctx: Context) {
        val now = LocalDateTime.now()
        val tasks = Store.load(ctx)
        val today = LocalDate.now()
        val nm = ctx.getSystemService(NotificationManager::class.java)
        createChannel(ctx)
        val open = PendingIntent.getActivity(
            ctx, 0, Intent(ctx, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        for (o in occurrencesOn(tasks, today) + occurrencesOn(tasks, today.plusDays(1))) {
            if (o.task.lead <= 0) continue
            val reminder = o.at.minusMinutes(o.task.lead.toLong())
            if (reminder.isBefore(now.minusMinutes(3)) || reminder.isAfter(now.plusMinutes(1))) continue
            val n = Notification.Builder(ctx, CHANNEL)
                .setSmallIcon(R.drawable.ic_notif)
                .setContentTitle("${o.task.title} em ${leadLabel(o.task.lead)}")
                .setContentText("${o.timeText()} · ${o.task.category}")
                .setContentIntent(open)
                .setCategory(Notification.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .build()
            nm.notify((o.task.id xor o.date.toEpochDay()).toInt(), n)
        }
    }
}

class AgendaReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        if (intent.action == Scheduler.ACTION_ALARM) {
            Scheduler.notifyDue(ctx)
            Scheduler.reschedule(ctx, LocalDateTime.now().plusMinutes(1))
        } else {
            Scheduler.reschedule(ctx)
        }
    }
}
