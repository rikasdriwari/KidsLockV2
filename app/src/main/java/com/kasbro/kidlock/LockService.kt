package com.kasbro.kidlock

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.app.NotificationCompat

class LockService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var overlayView: View? = null
    private lateinit var windowManager: WindowManager

    private val tickRunnable = object : Runnable {
        override fun run() {
            val endTime = LockPrefs.getEndTime(applicationContext)
            val remaining = endTime - System.currentTimeMillis()
            if (remaining <= 0) {
                LockPrefs.setTimerRunning(applicationContext, false)
                showLockOverlay()
            } else {
                updateNotification(remaining)
                handler.postDelayed(this, 1000L)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val durationMillis = intent.getLongExtra(EXTRA_DURATION_MILLIS, 0L)
                val endTime = System.currentTimeMillis() + durationMillis
                LockPrefs.setEndTime(applicationContext, endTime)
                LockPrefs.setTimerRunning(applicationContext, true)
                startForeground(NOTIF_ID, buildNotification(durationMillis))
                handler.removeCallbacks(tickRunnable)
                handler.post(tickRunnable)
            }
            ACTION_STOP -> {
                handler.removeCallbacks(tickRunnable)
                LockPrefs.setTimerRunning(applicationContext, false)
                removeOverlay()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_RESUME -> {
                if (LockPrefs.isTimerRunning(applicationContext)) {
                    startForeground(
                        NOTIF_ID,
                        buildNotification(LockPrefs.getEndTime(applicationContext) - System.currentTimeMillis())
                    )
                    handler.removeCallbacks(tickRunnable)
                    handler.post(tickRunnable)
                } else if (LockPrefs.isLocked(applicationContext)) {
                    startForeground(NOTIF_ID, buildNotification(0))
                    showLockOverlay()
                }
            }
        }
        return START_STICKY
    }

    private fun showLockOverlay() {
        if (overlayView != null) return
        LockPrefs.setLocked(applicationContext, true)

        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.lock_overlay, null)
        val pinInput = view.findViewById<EditText>(R.id.pinInput)
        val unlockButton = view.findViewById<Button>(R.id.unlockButton)
        val errorText = view.findViewById<TextView>(R.id.errorText)

        unlockButton.setOnClickListener {
            val pin = pinInput.text.toString()
            if (PinUtils.verifyPin(applicationContext, pin)) {
                LockPrefs.setLocked(applicationContext, false)
                removeOverlay()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            } else {
                errorText.visibility = View.VISIBLE
                pinInput.text.clear()
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        )
        windowManager.addView(view, params)
        overlayView = view
    }

    private fun removeOverlay() {
        overlayView?.let {
            windowManager.removeView(it)
            overlayView = null
        }
    }

    private fun buildNotification(remainingMillis: Long): Notification {
        val minutes = (remainingMillis / 60000).coerceAtLeast(0)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notif_time_left, minutes))
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(remainingMillis: Long) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIF_ID, buildNotification(remainingMillis))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "com.kasbro.kidlock.action.START"
        const val ACTION_STOP = "com.kasbro.kidlock.action.STOP"
        const val ACTION_RESUME = "com.kasbro.kidlock.action.RESUME"
        const val EXTRA_DURATION_MILLIS = "extra_duration_millis"
        const val CHANNEL_ID = "kidlock_channel"
        const val NOTIF_ID = 1001
    }
}
