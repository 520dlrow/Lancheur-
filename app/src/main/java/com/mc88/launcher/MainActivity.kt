package com.mc88.launcher

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_main)

        val setupLayout = findViewById<View>(R.id.setupLayout)
        val mainLayout = findViewById<View>(R.id.mainLayout)
        val btnGetStarted = findViewById<Button>(R.id.btnGetStarted)
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)
        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        val tvDateInfo = findViewById<TextView>(R.id.tvDateInfo)

        updateHeaderTime(tvGreeting, tvDateInfo)

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

    private fun updateHeaderTime(tvGreeting: TextView, tvDateInfo: TextView) {
        val now = Date()
        val dayFormat = SimpleDateFormat("EEE.", Locale.ENGLISH)
        val timeFormat = SimpleDateFormat("h:mm", Locale.ENGLISH)
        val dateFormat = SimpleDateFormat("EEE d MMM", Locale.ENGLISH)
        
        tvGreeting.text = dayFormat.format(now).replace(".", ".")
        
        val batteryLevel = getBatteryLevel()
        val timeStr = timeFormat.format(now).uppercase()
        val dateStr = dateFormat.format(now).uppercase()
        
        tvDateInfo.text = "$timeStr  •  $dateStr  •  $batteryLevel%"
    }

    private fun getBatteryLevel(): Int {
        val batteryManager = getSystemService(BATTERY_SERVICE) as android.os.BatteryManager
        return batteryManager.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    private fun loadApps() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
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

        adapter = AppAdapter(appList, scrollMode == "horizontal") { app ->
            packageManager.getLaunchIntentForPackage(app.packageName)?.let { startActivity(it) }
        }
        
        if (scrollMode == "horizontal") {
            recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        } else {
            recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        }
        
        recyclerView.adapter = adapter
        adapter.setIconSize(prefs.getInt("icon_size", 48))
    }

    override fun onResume() {
        super.onResume()
        if (getSharedPreferences("launcher_prefs", MODE_PRIVATE).getBoolean("setup_complete", false)) {
            loadApps()
            updateHeaderTime(findViewById(R.id.tvGreeting), findViewById(R.id.tvDateInfo))
        }
    }
}
