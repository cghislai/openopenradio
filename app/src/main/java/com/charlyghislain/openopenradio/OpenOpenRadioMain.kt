package com.charlyghislain.openopenradio

import android.os.Bundle
import android.view.Menu
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.charlyghislain.openopenradio.databinding.ActivityOpenOpenRadioMainBinding
import com.charlyghislain.openopenradio.ui.home.BackNavigationListener

class OpenOpenRadioMain : AppCompatActivity(), BackNavigationListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityOpenOpenRadioMainBinding

    // Callback set once navigation component is set up
    private var onNavigateUpCallback: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOpenOpenRadioMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarOpenOpenRadioMain.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(false)

        val drawerLayout: DrawerLayout = binding.drawerLayout
//        val navView: NavigationView = binding.navView
//        val navController = findNavController(R.id.nav_host_fragment_content_open_open_radio_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
            ), drawerLayout
        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.open_open_radio_main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        onNavigateUpCallback?.invoke() ?: return false
        return true;

//        return super.onSupportNavigateUp()
//        val navController = findNavController(R.id.nav_host_fragment_content_open_open_radio_main)
//        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onNavigateUp(): Boolean {
        onNavigateUpCallback?.invoke() ?: return false
        return true
    }

    override fun onNavigateUpCallback(callback: () -> Unit) {
        onNavigateUpCallback = callback
    }

    override fun onBackNavigationAvailable(available: Boolean) {
        supportActionBar?.setDisplayShowHomeEnabled(available)
        supportActionBar?.setDisplayHomeAsUpEnabled(available)
    }
}