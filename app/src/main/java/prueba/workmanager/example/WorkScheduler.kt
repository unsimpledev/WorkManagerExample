package prueba.workmanager.example

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object WorkScheduler {

    fun programarWorkManagerDiario(context: Context) {
        val horaEjecucion = 8 // 8 AM
        val minutoEjecucion = 0 // Minuto exacto

        // Hora local del dispositivo
        val zonaHorariaLocal = TimeZone.getDefault()
        val ahora = Calendar.getInstance(zonaHorariaLocal)

        val calendar = Calendar.getInstance(zonaHorariaLocal).apply {
            set(Calendar.HOUR_OF_DAY, horaEjecucion)
            set(Calendar.MINUTE, minutoEjecucion)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // Si la hora ya pasó hoy, programar para mañana
            if (before(ahora)) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val delayInicial = calendar.timeInMillis - System.currentTimeMillis()

        //Un eemplo de restricciones
        //val constraints = Constraints.Builder()
        //    .setRequiredNetworkType(NetworkType.CONNECTED) // Solo si hay internet
        //   .build()

        val workRequest = PeriodicWorkRequestBuilder<VerificarEventosWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delayInicial, TimeUnit.MILLISECONDS) // ⏳ Comienza a las 8 AM
            //.setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "verificar_eventos_calendar",
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,  // Reemplaza si ya existe
            workRequest
        )
    }
}
