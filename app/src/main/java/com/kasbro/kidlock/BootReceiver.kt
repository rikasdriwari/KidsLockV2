package com.kasbro.kidlock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            if (LockPrefs.isTimerRunning(context) || LockPrefs.isLocked(context)) {
                val serviceIntent = Intent(context, LockService::class.java).apply {
                    action = LockService.ACTION_RESUME
                }
                context.startForegroundService(serviceIntent)
            }
        }
    }
}
