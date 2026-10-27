package com.mc88.launcher

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        setContentView(R.layout.activity_main)

        val mainLayout = findViewById<View>(R.id.mainLayout)
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)
        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        val tvDateInfo = findViewById<TextView>(R.id.tvDateInfo)
        val searchBar = findViewById<EditText>(R.id.searchBar)
        val bottomDock = findViewById<LinearLayout>(R.id.bottomDock)
        
        recyclerView = findViewById(R.id.recyclerView)

        mainLayout.visibility = View.VISIBLE
        updateHeaderTime(tvGreeting, tvDateInfo)

        if (prefs.getBoolean("show_dock", true)) {
            bottomDock.visibility = View.VISIBLE
        } else {
            bottomDock.visibility = View.GONE
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (::adapter.isInitialized) {
                    adapter.filter(s.toString())
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        loadApps()
    }

    private fun updateHeaderTime(tvGreeting: TextView, tvDateInfo: TextView) {
        val now = Date()
        val dayFormat = SimpleDateFormat("EEE.", Locale.ENGLISH)
        val timeFormat = SimpleDateFormat("h:mm", Locale.ENGLISH)
        val dateFormat = SimpleDateFormat("EEE d MMM", Locale.ENGLISH)
        
        tvGreeting.text = dayFormat.format(now)
        
        val batteryManager = getSystemService(BATTERY_SERVICE) as android.os.BatteryManager
        val batteryLevel = batteryManager.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY)
        
        tvDateInfo.text = "${timeFormat.format(now).uppercase()}  •  ${dateFormat.format(now).uppercase()}  •  $batteryLevel%"
    }

    private fun loadApps() {
        val prefs = getSharedPreferences("launcher_prefs", MODE_PRIVATE)
        val scrollMode = prefs.getString("scroll_mode", "vertical") ?: "vertical"
        val isEasyMode = prefs.getBoolean("easy_mode", false)
        val sortOrder = prefs.getString("sort_order", "name") ?: "name"
        val iconSize = prefs.getInt("icon_size", 48)
        val hiddenPrefs = getSharedPreferences("hidden_apps", MODE_PRIVATE)
        val pm = packageManager

        lifecycleScope.launch(Dispatchers.IO) {
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

            withContext(Dispatchers.Main) {
                adapter = AppAdapter(appList, scrollMode == "horizontal", isEasyMode) { app ->
                    pm.getLaunchIntentForPackage(app.packageName)?.let { startActivity(it) }
                }
                
                if (isEasyMode) {
                    recyclerView.layoutManager = GridLayoutManager(this@MainActivity, 2)
                } else if (scrollMode == "horizontal") {
                    recyclerView.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
                } else {
                    recyclerView.layoutManager = GridLayoutManager(this@MainActivity, 4)
                }
                
                recyclerView.adapter = adapter
                adapter.setIconSize(iconSize)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadApps()
    }
}
