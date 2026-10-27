package com.mc88.launcher

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider

class SettingsActivity : AppCompatActivity() {

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

        val cbDynamicColors = findViewById<CheckBox>(R.id.cbDynamicColors)
        val cbEasyMode = findViewById<CheckBox>(R.id.cbEasyMode)
        val cbShowDock = findViewById<CheckBox>(R.id.cbShowDock)

        val radioVertical = findViewById<RadioButton>(R.id.radioVertical)
        val radioHorizontal = findViewById<RadioButton>(R.id.radioHorizontal)
        val radioGroupScroll = findViewById<RadioGroup>(R.id.radioGroupScroll)

        val radioSortName = findViewById<RadioButton>(R.id.radioSortName)
        val radioSortPackage = findViewById<RadioButton>(R.id.radioSortPackage)
        val radioGroupSort = findViewById<RadioGroup>(R.id.radioGroupSort)

        val sliderIconSize = findViewById<Slider>(R.id.sliderIconSize)
        val btnHiddenApps = findViewById<Button>(R.id.btnHiddenApps)
        val btnBack = findViewById<Button>(R.id.btnBack)

        val currentTheme = prefs.getString("theme", "light")
        radioLight.isChecked = currentTheme == "light"
        radioDark.isChecked = currentTheme == "dark"
        radioWarm.isChecked = currentTheme == "warm"

        cbDynamicColors.isChecked = prefs.getBoolean("dynamic_colors", false)
        cbEasyMode.isChecked = prefs.getBoolean("easy_mode", false)
        cbShowDock.isChecked = prefs.getBoolean("show_dock", true)

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

        cbDynamicColors.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dynamic_colors", isChecked).apply()
        }

        cbEasyMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("easy_mode", isChecked).apply()
        }

        cbShowDock.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("show_dock", isChecked).apply()
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
            showSecurityPrompt()
        }
        
        btnBack.setOnClickListener { finish() }
    }

    private fun showSecurityPrompt() {
        val prefs = getSharedPreferences("launcher_prefs", MODE_PRIVATE)
        val savedPin = prefs.getString("secure_pin", "")

        if (savedPin.isNullOrEmpty()) {
            val input = EditText(this)
            input.hint = "Set a new PIN"
            AlertDialog.Builder(this)
                .setTitle("Setup PIN")
                .setView(input)
                .setPositiveButton("Save") { _, _ ->
                    prefs.edit().putString("secure_pin", input.text.toString()).apply()
                    Toast.makeText(this, "PIN Saved", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HiddenAppsActivity::class.java))
                }
                .show()
        } else {
            val input = EditText(this)
            input.hint = "Enter PIN"
            AlertDialog.Builder(this)
                .setTitle("Secure Area")
                .setView(input)
                .setPositiveButton("Unlock") { _, _ ->
                    if (input.text.toString() == savedPin) {
                        startActivity(Intent(this, HiddenAppsActivity::class.java))
                    } else {
                        Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show()
                    }
                }
                .show()
        }
    }
}
