package com.mc88.launcher

import android.content.Intent
import android.os.Bundle
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import com.mc88.launcher.databinding.ActivityHiddenAppsBinding

class HiddenAppsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("launcher_prefs", MODE_PRIVATE)
        when (prefs.getString("theme", "light")) {
            "dark" -> setTheme(R.style.Theme_MC88Launcher_Dark)
            "warm" -> setTheme(R.style.Theme_MC88Launcher_Warm)
            else -> setTheme(R.style.Theme_MC88Launcher)
        }
        super.onCreate(savedInstanceState)
        val binding = ActivityHiddenAppsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        val hiddenPrefs = getSharedPreferences("hidden_apps", MODE_PRIVATE)

        for (info in resolveInfos) {
            val pkg = info.activityInfo.packageName
            if (pkg == packageName) continue
            val cb = CheckBox(this)
            cb.text = info.loadLabel(pm).toString()
            cb.isChecked = hiddenPrefs.getBoolean(pkg, false)
            cb.setOnCheckedChangeListener { _, isChecked ->
                hiddenPrefs.edit().putBoolean(pkg, isChecked).apply()
            }
            binding.container.addView(cb)
        }
        
        binding.btnBack.setOnClickListener { finish() }
    }
}
