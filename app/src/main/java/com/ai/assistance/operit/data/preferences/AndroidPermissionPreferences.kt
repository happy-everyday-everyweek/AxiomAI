package com.ai.assistance.operit.data.preferences

import android.content.Context
import com.ai.assistance.operit.util.AppLogger
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ai.assistance.operit.core.tools.system.AndroidPermissionLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.androidPermissionDataStore: DataStore<Preferences> by
        preferencesDataStore(name = "android_permission_preferences")

lateinit var androidPermissionPreferences: AndroidPermissionPreferences
    private set

fun initAndroidPermissionPreferences(context: Context) {
    androidPermissionPreferences = AndroidPermissionPreferences(context)
}

class AndroidPermissionPreferences(private val context: Context) {
    companion object {
        private const val TAG = "AndroidPermissionPrefs"

        private val PREFERRED_PERMISSION_LEVEL = stringPreferencesKey("preferred_permission_level")
    }

    val preferredPermissionLevelFlow: Flow<AndroidPermissionLevel?> =
            context.androidPermissionDataStore.data.map { preferences ->
                val levelString = preferences[PREFERRED_PERMISSION_LEVEL]
                if (levelString != null) AndroidPermissionLevel.fromString(levelString) else null
            }

    suspend fun savePreferredPermissionLevel(permissionLevel: AndroidPermissionLevel) {
        AppLogger.d(TAG, "Saving preferred permission level: $permissionLevel")
        context.androidPermissionDataStore.edit { preferences ->
            preferences[PREFERRED_PERMISSION_LEVEL] = permissionLevel.name
        }
    }

    fun getPreferredPermissionLevel(): AndroidPermissionLevel? {
        return runBlocking {
            try {
                preferredPermissionLevelFlow.first()
            } catch (e: Exception) {
                AppLogger.e(TAG, "Error getting preferred permission level", e)
                null
            }
        }
    }

    fun isPermissionLevelSet(): Boolean {
        return runBlocking {
            try {
                preferredPermissionLevelFlow.first() != null
            } catch (e: Exception) {
                AppLogger.e(TAG, "Error checking if permission level is set", e)
                false
            }
        }
    }

    suspend fun resetPermissionLevel() {
        AppLogger.d(TAG, "Resetting permission level")
        context.androidPermissionDataStore.edit { preferences ->
            preferences.remove(PREFERRED_PERMISSION_LEVEL)
        }
    }
}
