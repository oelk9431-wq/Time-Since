package app.timesince

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.compose.*
import app.timesince.ui.*

class MainActivity : ComponentActivity() {
    private val vm by viewModels<MainViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); enableEdgeToEdge()
        setContent {
            val settings by vm.settings.collectAsState()
            val dark = when(settings.theme) { ThemeMode.SYSTEM -> isSystemInDarkTheme(); ThemeMode.LIGHT -> false; ThemeMode.DARK -> true }
            MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
                val nav = rememberNavController()
                NavHost(nav, "home") {
                    composable("home") { HomeScreen(vm, { nav.navigate("detail/$it") }, { nav.navigate("settings") }) }
                    composable("detail/{id}") { DetailScreen(vm, it.arguments?.getString("id")!!.toLong(), { nav.popBackStack() }, { nav.navigate("history/$it") }) }
                    composable("history/{id}") { HistoryScreen(vm, it.arguments?.getString("id")!!.toLong()) { nav.popBackStack() } }
                    composable("settings") { SettingsScreen(vm) { nav.popBackStack() } }
                }
            }
        }
    }
}
