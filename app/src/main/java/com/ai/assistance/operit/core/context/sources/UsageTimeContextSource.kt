package com.ai.assistance.operit.core.context.sources

import android.app.usage.UsageStatsManager
import android.content.Context
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import java.util.Calendar

object UsageTimeContextSource {
    fun collect(context: Context): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) return null

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.PACKAGE_USAGE_STATS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }

        try {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                ?: return null

            val calendar = Calendar.getInstance()
            val endTime = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val startTime = calendar.timeInMillis

            val usageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )

            if (usageStats.isNullOrEmpty()) return null

            val topApps = usageStats
                .filter { it.totalTimeInForeground > 60000 }
                .sortedByDescending { it.totalTimeInForeground }
                .take(5)

            if (topApps.isEmpty()) return null

            return buildString {
                appendLine("Screen usage (last 24h):")
                for (stat in topApps) {
                    val minutes = stat.totalTimeInForeground / 60000
                    appendLine("- ${stat.packageName}: ${minutes}min")
                }
            }
        } catch (e: Exception) {
            return null
        }
    }
}
