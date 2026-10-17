package com.hananelsabag.volunteerhours.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    companion object {
        val THEME_KEY         = stringPreferencesKey("theme")
        val NOTIFICATIONS_KEY = booleanPreferencesKey("notifications_enabled")
        val ONBOARDING_KEY    = booleanPreferencesKey("has_seen_onboarding")
    }

    val themeFlow         = context.dataStore.data.map { it[THEME_KEY]         ?: "system" }
    val notificationsFlow = context.dataStore.data.map { it[NOTIFICATIONS_KEY] ?: false    }
    val hasSeenOnboarding = context.dataStore.data.map { it[ONBOARDING_KEY]    ?: false    }

    suspend fun setTheme(theme: String)          { context.dataStore.edit { it[THEME_KEY]         = theme   } }
    suspend fun setNotifications(enabled: Boolean) { context.dataStore.edit { it[NOTIFICATIONS_KEY] = enabled } }
    suspend fun setOnboardingSeen()              { context.dataStore.edit { it[ONBOARDING_KEY]    = true    } }
}
