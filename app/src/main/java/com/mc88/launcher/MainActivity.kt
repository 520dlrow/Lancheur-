package com.mc88.launcher

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: AppAdapter
    private val appList = mutableListOf<AppInfo>()
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("launcher_prefs", MODE_PRIVATE)
        when (prefs.getString("theme", "light")) {
            "dark" -> setTheme(R.style.Theme_MC88Launcher_Dark)
            "warm" -> setTheme(R.style.Theme_MC88Launcher_Warm)
            else -> setTheme(R.style.Theme_MC88Launcher)
        }
        super.onCreate(savedInstanceState)
        
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        
        setContentView(R.layout.activity_main)

        val setupLayout = findViewById<View>(R.id.setupLayout)
        val mainLayout = findViewById<View>(R.id.mainLayout)
        val btnGetStarted = findViewById<Button>(R.id.btnGetStarted)
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)
        val tvTimeBattery = findViewById<TextView>(R.id.tvTimeBattery)
        
        recyclerView = findViewById(R.id.recyclerView)

        updateHeaderTime(tvTimeBattery)

        if (!prefs.getBoolean("setup_complete", false)) {
            setupLayout.visibility = View.VISIBLE
            mainLayout.visibility = View.GONE
            btnGetStarted.setOnClickListener {
                prefs.edit().putBoolean("setup_complete", true).apply()
                setupLayout.visibility = View.GONE
                mainLayout.visibility = View.VISIBLE
                loadApps()
            }
        } else {
            setupLayout.visibility = View.GONE
            mainLayout.visibility = View.VISIBLE
            loadApps()
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun updateHeaderTime(tvTimeBattery: TextView) {
        val now = Date()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        
        val batteryManager = getSystemService(BATTERY_SERVICE) as android.os.BatteryManager
        val batteryLevel = batteryManager.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY)
        
        tvTimeBattery.text = "${timeFormat.format(now)}  •  $batteryLevel%"
    }

    private fun loadApps() {
        val prefs = getSharedPreferences("launcher_prefs", MODE_PRIVATE)
        val scrollMode = prefs.getString("scroll_mode", "vertical") ?: "vertical"
        val isEasyMode = prefs.getBoolean("easy_mode", false)
        val sortOrder = prefs.getString("sort_order", "name") ?: "name"
        val iconSize = prefs.getInt("icon_size", 48)
        val hiddenPrefs = getSharedPreferences("hidden_apps", MODE_PRIVATE)
        val pm = packageManager

        val intent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        
        appList.clear()
        for (info in resolveInfos) {
            val pkg = info.activityInfo.packageName
            if (pkg == packageName || hiddenPrefs.getBoolean(pkg, false)) continue
            appList.add(AppInfo(info.loadLabel(pm).toString(), info.loadIcon(pm), pkg))
        }

        if (sortOrder == "name") {
            appList.sortBy { it.name }
        } else {
            appList.sortBy { it.packageName }
        }

        adapter = AppAdapter(appList, scrollMode == "horizontal", isEasyMode) { app ->
            pm.getLaunchIntentForPackage(app.packageName)?.let { startActivity(it) }
        }
        
        if (isEasyMode) {
            recyclerView.layoutManager = GridLayoutManager(this, 2)
        } else if (scrollMode == "horizontal") {
            recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        } else {
            recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        }
        
        recyclerView.adapter = adapter
        adapter.setIconSize(iconSize)
    }

    override fun onResume() {
        super.onResume()
        loadApps()
        updateHeaderTime(findViewById(R.id.tvTimeBattery))
    }
}
