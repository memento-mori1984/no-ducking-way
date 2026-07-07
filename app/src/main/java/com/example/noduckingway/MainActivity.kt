package com.example.noduckingway

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.radiobutton.MaterialRadioButton

class MainActivity : AppCompatActivity() {

    private lateinit var switchNoDucking: MaterialSwitch
    private lateinit var statusTitle: TextView
    private lateinit var statusSub: TextView
    private lateinit var modeGroup: RadioGroup
    private lateinit var modeMixer: MaterialRadioButton
    private lateinit var modeOwner: MaterialRadioButton
    private lateinit var switchBoot: MaterialSwitch
    private lateinit var batteryButton: MaterialButton
    private var suppressSwitchCallback = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startDuckingService()
        } else {
            Toast.makeText(
                this,
                "Notification permission is required to run NoDuckingWay in the background.",
                Toast.LENGTH_LONG
            ).show()
            setProtectionSwitch(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusTitle = findViewById(R.id.statusTitle)
        statusSub = findViewById(R.id.statusSub)
        switchNoDucking = findViewById(R.id.switchDucking)
        modeGroup = findViewById(R.id.modeGroup)
        modeMixer = findViewById(R.id.modeMixer)
        modeOwner = findViewById(R.id.modeOwner)
        switchBoot = findViewById(R.id.switchBoot)
        batteryButton = findViewById(R.id.batteryButton)

        loadPreferences()
        bindListeners()
        maybePromptBatteryOptimization()
        refreshUi()
    }

    override fun onResume() {
        super.onResume()
        refreshUi()
    }

    private fun loadPreferences() {
        when (NoDuckingPrefs.getMode(this)) {
            NoDuckingPrefs.MODE_OWNER -> modeOwner.isChecked = true
            else -> modeMixer.isChecked = true
        }
        switchBoot.isChecked = NoDuckingPrefs.isStartOnBootEnabled(this)
    }

    private fun bindListeners() {
        switchNoDucking.setOnCheckedChangeListener { _, isChecked ->
            if (suppressSwitchCallback) return@setOnCheckedChangeListener
            if (isChecked) {
                checkAndRequestPermissionThenStart()
            } else {
                stopDuckingService()
            }
        }

        modeGroup.setOnCheckedChangeListener { _, checkedId ->
            val mode = if (checkedId == R.id.modeOwner) {
                NoDuckingPrefs.MODE_OWNER
            } else {
                NoDuckingPrefs.MODE_MIXER
            }
            NoDuckingPrefs.setMode(this, mode)
            if (NoDuckingService.isRunning(this)) {
                restartDuckingService()
                Toast.makeText(this, "Switched to $mode mode", Toast.LENGTH_SHORT).show()
            }
        }

        switchBoot.setOnCheckedChangeListener { _, isChecked ->
            NoDuckingPrefs.setStartOnBoot(this, isChecked)
        }

        batteryButton.setOnClickListener { requestBatteryExemption() }
    }

    private fun maybePromptBatteryOptimization() {
        if (NoDuckingPrefs.wasBatteryPromptShown(this)) return
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            Toast.makeText(this, R.string.battery_exemption_hint, Toast.LENGTH_LONG).show()
        }
        NoDuckingPrefs.setBatteryPromptShown(this)
    }

    private fun requestBatteryExemption() {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (pm.isIgnoringBatteryOptimizations(packageName)) {
            Toast.makeText(this, "Battery optimization already disabled for this app.", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:$packageName")
        }
        startActivity(intent)
    }

    private fun refreshUi() {
        val active = NoDuckingService.isRunning(this)
        setProtectionSwitch(active)
        statusTitle.setText(if (active) R.string.status_on else R.string.status_off)
        statusSub.setText(if (active) R.string.status_sub_on else R.string.status_sub_off)
        modeGroup.isEnabled = true
    }

    private fun setProtectionSwitch(checked: Boolean) {
        suppressSwitchCallback = true
        switchNoDucking.isChecked = checked
        suppressSwitchCallback = false
    }

    private fun checkAndRequestPermissionThenStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        startDuckingService()
    }

    private fun startDuckingService() {
        val serviceIntent = Intent(this, NoDuckingService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
        Toast.makeText(this, "No ducking enabled", Toast.LENGTH_SHORT).show()
        refreshUi()
    }

    private fun restartDuckingService() {
        stopService(Intent(this, NoDuckingService::class.java))
        startDuckingService()
    }

    private fun stopDuckingService() {
        stopService(Intent(this, NoDuckingService::class.java))
        Toast.makeText(this, "No ducking disabled", Toast.LENGTH_SHORT).show()
        refreshUi()
    }
}