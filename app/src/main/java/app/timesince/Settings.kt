package app.timesince

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import app.timesince.data.DisplayFormat
import kotlinx.coroutines.flow.map

private val Context.store by preferencesDataStore("settings")
enum class ThemeMode { SYSTEM, LIGHT, DARK }
data class AppSettings(val theme: ThemeMode = ThemeMode.SYSTEM, val defaultFormat: DisplayFormat = DisplayFormat.COMPACT, val defaultColor: Long = 0xFF2E7D32)
class SettingsRepository(private val context: Context) {
    private val theme = stringPreferencesKey("theme"); private val format = stringPreferencesKey("format"); private val color = longPreferencesKey("color")
    val settings = context.store.data.map { p -> AppSettings(ThemeMode.valueOf(p[theme] ?: ThemeMode.SYSTEM.name), DisplayFormat.valueOf(p[format] ?: DisplayFormat.COMPACT.name), p[color] ?: 0xFF2E7D32) }
    suspend fun theme(value: ThemeMode) = context.store.edit { it[theme] = value.name }
    suspend fun defaults(f: DisplayFormat, c: Long) = context.store.edit { it[format] = f.name; it[color] = c }
}
