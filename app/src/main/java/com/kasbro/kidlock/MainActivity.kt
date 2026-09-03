package com.kasbro.kidlock

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.kasbro.kidlock.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100
            )
        }

        binding.savePinButton.setOnClickListener {
            val pin = binding.pinInput.text.toString()
            if (pin.length < 4) {
                Toast.makeText(this, getString(R.string.pin_too_short), Toast.LENGTH_SHORT).show()
            } else {
                PinUtils.savePin(this, pin)
                Toast.makeText(this, getString(R.string.pin_saved), Toast.LENGTH_SHORT).show()
                binding.pinInput.text.clear()
            }
        }

        binding.startButton.setOnClickListener {
            if (!PinUtils.isPinSet(this)) {
                Toast.makeText(this, getString(R.string.set_pin_first), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!hasOverlayPermission()) {
                requestOverlayPermission()
                return@setOnClickListener
            }
            val minutes = binding.minutesInput.text.toString().toLongOrNull()
            if (minutes == null || minutes <= 0) {
                Toast.makeText(this, getString(R.string.enter_valid_minutes), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, LockService::class.java).apply {
                action = LockService.ACTION_START
                putExtra(LockService.EXTRA_DURATION_MILLIS, minutes * 60_000L)
            }
            startForegroundService(intent)
            Toast.makeText(this, getString(R.string.timer_started), Toast.LENGTH_SHORT).show()
        }

        binding.stopButton.setOnClickListener {
            val intent = Intent(this, LockService::class.java).apply {
                action = LockService.ACTION_STOP
            }
            startService(intent)
            Toast.makeText(this, getString(R.string.timer_stopped), Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasOverlayPermission(): Boolean =
        Settings.canDrawOverlays(this)

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivity(intent)
        Toast.makeText(this, getString(R.string.grant_overlay_permission), Toast.LENGTH_LONG).show()
    }
}
