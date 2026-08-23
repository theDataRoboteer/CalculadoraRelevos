package com.cortes.calculadoraderelevos

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Clase principal que gestiona el ciclo de vida de la aplicación.
 * Como estudiantes de Data Science, vemos esto como el contenedor que inicializa
 * el entorno de ejecución y aplica restricciones de hardware (como la orientación).
 */
import com.cortes.calculadoraderelevos.ui.theme.CalculadoraDeRelevosTheme
import androidx.compose.runtime.Composable

class MainActivity : ComponentActivity() {
    // Variable de control para la persistencia de la Splash Screen (Carga inicial).
    private var keepSplashScreen = true

    override fun onCreate(savedInstanceState: Bundle?) {
        // Instalamos el motor de la Splash Screen antes de la creación para una carga limpia.
        val splashScreen = installSplashScreen()
        
        // Mantener la pantalla de carga durante un intervalo fijo para asegurar legibilidad del logo.
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }
        
        lifecycleScope.launch {
            delay(2000) // Latencia artificial de 2 segundos.
            keepSplashScreen = false
        }

        super.onCreate(savedInstanceState)

        /**
         * Lógica de filtrado por tipo de dispositivo (Responsive Control):
         * Si el ancho mínimo es menor a 600dp (Teléfono), bloqueamos a vertical.
         * En tablets (>= 600dp), permitimos rotación para mejorar la UX en pantallas grandes.
         */
        val isTablet = resources.configuration.smallestScreenWidthDp >= 600
        if (!isTablet) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        // Habilitamos renderizado de borde a borde para aprovechar toda la matriz de píxeles.
        enableEdgeToEdge()
        
        setContent {
            // Inyectamos el tema y lanzamos el orquestador visual principal.
            CalculadoraDeRelevosTheme {
                RelayApp()
            }
        }
    }
}
