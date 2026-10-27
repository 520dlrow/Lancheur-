package com.mc88.launcher

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.mc88.launcher.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: AppAdapter
    private val appList = mutableListOf<AppInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("launcher_prefs", MODE_PRIVATE)
        when (prefs.getString("theme", "light")) {
            "dark" -> setTheme(R.style.Theme_MC88Launcher_Dark)
            "warm" -> setTheme(R.style.Theme_MC88Launcher_Warm)
            else -> setTheme(R.style.Theme_MC88Launcher)
        }
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!prefs.getBoolean("setup_complete", false)) {
            binding.setupLayout.visibility = View.VISIBLE
            binding.mainLayout.visibility = View.GONE
            binding.btnGetStarted.setOnClickListener {
                prefs.edit().putBoolean("setup_complete", true).apply()
                binding.setupLayout.visibility = View.GONE
                binding.mainLayout.visibility = View.VISIBLE
                loadApps()
            }
        } else {
            binding.setupLayout.visibility = View.GONE
            binding.mainLayout.visibility = View.VISIBLE
            loadApps()
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun loadApps() {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        
        val hiddenPrefs = getSharedPreferences("hidden_apps", MODE_PRIVATE)
        val prefs = getSharedPreferences("launcher_prefs", MODE_PRIVATE)
        val scrollMode = prefs.getString("scroll_mode", "vertical")

        appList.clear()
        for (info in resolveInfos) {
            val pkg = info.activityInfo.packageName
            if (pkg == packageName || hiddenPrefs.getBoolean(pkg, false)) continue
            appList.add(AppInfo(info.loadLabel(pm).toString(), info.loadIcon(pm), pkg))
        }
        appList.sortBy { it.name }

        adapter = AppAdapter(appList) { app ->
            packageManager.getLaunchIntentForPackage(app.packageName)?.let { startActivity(it) }
        }
        
        binding.recyclerView.layoutManager = if (scrollMode == "horizontal") {
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        } else {
            GridLayoutManager(this, 4)
        }
        
        binding.recyclerView.adapter = adapter
        adapter.setIconSize(prefs.getInt("icon_size", 48))
    }

    override fun onResume() {
        super.onResume()
        if (getSharedPreferences("launcher_prefs", MODE_PRIVATE).getBoolean("setup_complete", false)) {
            loadApps()
        }
    }
}
