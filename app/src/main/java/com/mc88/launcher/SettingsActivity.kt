package com.mc88.launcher

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider

class SettingsActivity : AppCompatActivity() {

    private var isFlashOn = false
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("launcher_prefs", MODE_PRIVATE)
        when (prefs.getString("theme", "light")) {
            "dark" -> setTheme(R.style.Theme_MC88Launcher_Dark)
            "warm" -> setTheme(R.style.Theme_MC88Launcher_Warm)
            else -> setTheme(R.style.Theme_MC88Launcher)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val radioLight = findViewById<RadioButton>(R.id.radioLight)
        val radioDark = findViewById<RadioButton>(R.id.radioDark)
        val radioWarm = findViewById<RadioButton>(R.id.radioWarm)
        val radioGroupTheme = findViewById<RadioGroup>(R.id.radioGroupTheme)

        val cbEasyMode = findViewById<CheckBox>(R.id.cbEasyMode)
        val radioVertical = findViewById<RadioButton>(R.id.radioVertical)
        val radioHorizontal = findViewById<RadioButton>(R.id.radioHorizontal)
        val radioGroupScroll = findViewById<RadioGroup>(R.id.radioGroupScroll)

        val radioSortName = findViewById<RadioButton>(R.id.radioSortName)
        val radioSortPackage = findViewById<RadioButton>(R.id.radioSortPackage)
        val radioGroupSort = findViewById<RadioGroup>(R.id.radioGroupSort)

        val sliderIconSize = findViewById<Slider>(R.id.sliderIconSize)
        val btnFlashlight = findViewById<Button>(R.id.btnFlashlight)
        val btnHiddenApps = findViewById<Button>(R.id.btnHiddenApps)
        val btnBack = findViewById<Button>(R.id.btnBack)

        try {
            cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            cameraId = cameraManager.cameraIdList[0]
        } catch (e: Exception) {
            btnFlashlight.isEnabled = false
        }

        btnFlashlight.setOnClickListener {
            try {
                isFlashOn = !isFlashOn
                cameraManager.setTorchMode(cameraId, isFlashOn)
                btnFlashlight.text = if (isFlashOn) "Turn Off Flashlight" else "Turn On Flashlight"
            } catch (e: Exception) {
                Toast.makeText(this, "Flashlight not available", Toast.LENGTH_SHORT).show()
            }
        }

        val currentTheme = prefs.getString("theme", "light")
        radioLight.isChecked = currentTheme == "light"
        radioDark.isChecked = currentTheme == "dark"
        radioWarm.isChecked = currentTheme == "warm"

        cbEasyMode.isChecked = prefs.getBoolean("easy_mode", false)
        radioVertical.isChecked = prefs.getString("scroll_mode", "vertical") == "vertical"
        radioHorizontal.isChecked = prefs.getString("scroll_mode", "vertical") == "horizontal"

        radioSortName.isChecked = prefs.getString("sort_order", "name") == "name"
        radioSortPackage.isChecked = prefs.getString("sort_order", "name") == "package"

        sliderIconSize.value = prefs.getInt("icon_size", 48).toFloat()

        radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            val theme = when (checkedId) {
                R.id.radioDark -> "dark"
                R.id.radioWarm -> "warm"
                else -> "light"
            }
            prefs.edit().putString("theme", theme).apply()
            recreate()
        }

        cbEasyMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("easy_mode", isChecked).apply()
        }

        radioGroupScroll.setOnCheckedChangeListener { _, checkedId ->
            val mode = if (checkedId == R.id.radioHorizontal) "horizontal" else "vertical"
            prefs.edit().putString("scroll_mode", mode).apply()
        }

        radioGroupSort.setOnCheckedChangeListener { _, checkedId ->
            val sort = if (checkedId == R.id.radioSortPackage) "package" else "name"
            prefs.edit().putString("sort_order", sort).apply()
        }

        sliderIconSize.addOnChangeListener { _, value, _ ->
            prefs.edit().putInt("icon_size", value.toInt()).apply()
        }

        btnHiddenApps.setOnClickListener {
            startActivity(Intent(this, HiddenAppsActivity::class.java))
        }
        
        btnBack.setOnClickListener { finish() }
    }
}
