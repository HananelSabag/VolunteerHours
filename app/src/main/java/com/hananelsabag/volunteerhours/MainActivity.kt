package com.hananelsabag.volunteerhours

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import com.hananelsabag.volunteerhours.data.SettingsRepository
import com.hananelsabag.volunteerhours.notification.CHANNEL_ID
import com.hananelsabag.volunteerhours.ui.MainScreen
import com.hananelsabag.volunteerhours.ui.theme.VolunteerHoursTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        enableEdgeToEdge()
        val settingsRepo = SettingsRepository(applicationContext)
        setContent {
            val theme by settingsRepo.themeFlow.collectAsState(initial = "system")
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (theme) {
                "light" -> false
                "dark"  -> true
                else    -> systemDark
            }
            VolunteerHoursTheme(darkTheme = darkTheme) {
                MainScreen(settingsRepo = settingsRepo)
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "תזכורות התנדבות",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "תזכורת יומית לעדכון שעות התנדבות" }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}
