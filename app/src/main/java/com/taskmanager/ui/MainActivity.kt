package com.taskmanager.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.navigationrail.NavigationRailView
import com.taskmanager.R
import com.taskmanager.databinding.ActivityMainBinding
import com.taskmanager.ui.auth.AuthActivity
import com.taskmanager.ui.tasks.TasksViewModel
import com.taskmanager.utils.isTablet
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private var navController: NavController? = null
    private val tasksViewModel: TasksViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (tasksViewModel.userId == null) {
            startActivity(Intent(this, AuthActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
            return
        }

        setupNavigation()
    }
    override fun attachBaseContext(base: Context) {
        val prefs = base.getSharedPreferences("settings", 0)
        val lang = prefs.getString("language", "en") ?: "en"
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = base.resources.configuration
        config.setLocale(locale)
        val context = base.createConfigurationContext(config)
        super.attachBaseContext(context)
    }
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment ?: return
        navController = navHostFragment.navController
        val nc = navController!!

        setSupportActionBar(binding.toolbar)

        // drawerLayout is present in both phone and tablet layouts
        val appBarConfig = AppBarConfiguration(
            setOf(R.id.tasksFragment, R.id.settingsFragment),
            binding.drawerLayout
        )
        NavigationUI.setupActionBarWithNavController(this, nc, appBarConfig)

        // Use findViewById to safely access views that may only exist in one layout variant
        if (isTablet()) {
            findViewById<NavigationRailView>(R.id.navView)?.let {
                NavigationUI.setupWithNavController(it, nc)
            }
        } else {
            findViewById<BottomNavigationView>(R.id.bottomNavView)?.let {
                NavigationUI.setupWithNavController(it, nc)
            }
        }

        findViewById<NavigationView>(R.id.navDrawer)?.let {
            NavigationUI.setupWithNavController(it, nc)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController?.navigateUp() == true || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
