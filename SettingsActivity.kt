package com.mc88.launcher

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mc88.launcher.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("launcher_prefs", MODE_PRIVATE)
        when (prefs.getString("theme", "light")) {
            "dark" -> setTheme(R.style.Theme_MC88Launcher_Dark)
            "warm" -> setTheme(R.style.Theme_MC88Launcher_Warm)
            else -> setTheme(R.style.Theme_MC88Launcher)
        }
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.radioLight.isChecked = prefs.getString("theme", "light") == "light"
        binding.radioDark.isChecked = prefs.getString("theme", "light") == "dark"
        binding.radioWarm.isChecked = prefs.getString("theme", "light") == "warm"

        binding.radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            val theme = when (checkedId) {
                R.id.radioDark -> "dark"
                R.id.radioWarm -> "warm"
                else -> "light"
            }
            prefs.edit().putString("theme", theme).apply()
            recreate()
        }

        binding.sliderIconSize.value = prefs.getInt("icon_size", 48).toFloat()
        binding.sliderIconSize.addOnChangeListener { _, value, _ ->
            prefs.edit().putInt("icon_size", value.toInt()).apply()
        }

        binding.radioVertical.isChecked = prefs.getString("scroll_mode", "vertical") == "vertical"
        binding.radioHorizontal.isChecked = prefs.getString("scroll_mode", "vertical") == "horizontal"

        binding.radioGroupScroll.setOnCheckedChangeListener { _, checkedId ->
            val mode = if (checkedId == R.id.radioHorizontal) "horizontal" else "vertical"
            prefs.edit().putString("scroll_mode", mode).apply()
        }

        binding.btnHiddenApps.setOnClickListener {
            startActivity(Intent(this, HiddenAppsActivity::class.java))
        }
        
        binding.btnBack.setOnClickListener { finish() }
    }
}
