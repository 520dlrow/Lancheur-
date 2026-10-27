package com.mc88.launcher

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class HiddenAppsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("launcher_prefs", MODE_PRIVATE)
        when (prefs.getString("theme", "light")) {
            "dark" -> setTheme(R.style.Theme_MC88Launcher_Dark)
            "warm" -> setTheme(R.style.Theme_MC88Launcher_Warm)
            else -> setTheme(R.style.Theme_MC88Launcher)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hidden_apps)

        val container = findViewById<LinearLayout>(R.id.container)
        val btnBack = findViewById<Button>(R.id.btnBack)
        val searchBar = findViewById<EditText>(R.id.searchBarHidden)

        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        val hiddenPrefs = getSharedPreferences("hidden_apps", MODE_PRIVATE)

        val allAppsList = mutableListOf<Pair<String, String>>()
        for (info in resolveInfos) {
            val pkg = info.activityInfo.packageName
            if (pkg == packageName) continue
            allAppsList.add(Pair(info.loadLabel(pm).toString(), pkg))
        }
        allAppsList.sortBy { it.first }

        fun renderList(query: String) {
            container.removeAllViews()
            for (app in allAppsList) {
                if (query.isNotEmpty() && !app.first.contains(query, ignoreCase = true)) continue
                
                val cb = CheckBox(this)
                cb.text = app.first
                cb.isChecked = hiddenPrefs.getBoolean(app.second, false)
                cb.textSize = 16f
                cb.setPadding(0, 16, 0, 16)
                cb.setOnCheckedChangeListener { _, isChecked ->
                    hiddenPrefs.edit().putBoolean(app.second, isChecked).apply()
                }
                container.addView(cb)
            }
        }

        renderList("")

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                renderList(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        
        btnBack.setOnClickListener { finish() }
    }
}
