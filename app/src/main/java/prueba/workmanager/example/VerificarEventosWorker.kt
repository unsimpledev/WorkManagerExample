package prueba.workmanager.example

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.provider.CalendarContract
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class VerificarEventosWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val eventosProximos = obtenerEventosProximos()
        if (eventosProximos.isNotEmpty()) {
            val mensaje = "Próximos eventos: " + eventosProximos.joinToString(", ")
            mostrarNotificacion(mensaje)
        }
        return Result.success()
    }

    private fun obtenerEventosProximos(): List<String> {
        val eventos = mutableListOf<String>()
        val contentResolver = applicationContext.contentResolver
        val uri = CalendarContract.Events.CONTENT_URI
        val projection = arrayOf(
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.EVENT_TIMEZONE // Obtenemos la zona horaria del evento
        )
        val zonaHorariaLocal = TimeZone.getDefault()
        val ahoraLocal = Calendar.getInstance(zonaHorariaLocal).apply {
            set(Calendar.HOUR_OF_DAY, 0) // Inicio del día local
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val mananaLocal = ahoraLocal + TimeUnit.DAYS.toMillis(1) - 1 // Fin del día local
        val selection = "${CalendarContract.Events.DTSTART} BETWEEN ? AND ?"
        val selectionArgs = arrayOf(ahoraLocal.toString(), mananaLocal.toString())

        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)
        cursor?.use {
            while (it.moveToNext()) {
                val titulo = it.getString(0)
                val zonaEvento = it.getString(2) // Obtenemos la zona horaria del evento

                val timestampEvento = it.getLong(1)
                val eventoEnZonaLocal = Calendar.getInstance(TimeZone.getTimeZone(zonaEvento)).apply {
                    timeInMillis = timestampEvento
                }

                // Convertimos el evento a hora local
                val eventoEnHoraLocal = Calendar.getInstance(zonaHorariaLocal).apply {
                    timeInMillis = eventoEnZonaLocal.timeInMillis
                }

                // Si el evento está dentro del rango del día local, lo agregamos
                if (eventoEnHoraLocal.timeInMillis in ahoraLocal..mananaLocal) {
                    eventos.add(titulo)
                }
            }
        }
        return eventos
    }


    private fun mostrarNotificacion(mensaje: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "eventos_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Eventos",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notif)
            .setContentTitle("Recordatorio de eventos")
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(101, notification)
    }
}
