package prueba.workmanager.example

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import prueba.workmanager.example.ui.theme.WorkManagerExampleTheme
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private val permisosLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { resultados ->
            resultados.forEach { (permiso, concedido) ->
                if (concedido) {
                    Log.d("Permisos", "Permiso concedido: $permiso")
                } else {
                    Log.d("Permisos", "Permiso denegado: $permiso")
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val permisosNecesarios = mutableListOf<String>()

        // Verificar permiso de notificaciones (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permisosNecesarios.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        // Verificar permiso de calendario (Android 6+)
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            permisosNecesarios.add(android.Manifest.permission.READ_CALENDAR)
        }

        // Si hay permisos pendientes, lanzamos el launcher
        if (permisosNecesarios.isNotEmpty()) {
            permisosLauncher.launch(permisosNecesarios.toTypedArray())
        }

        WorkScheduler.programarWorkManagerDiario(this)

        setContent {
            WorkManagerExampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }


}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WorkManagerExampleTheme {
        Greeting("Android")
    }
}