package com.charlyghislain.openopenradio

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.charlyghislain.openopenradio.databinding.ActivityOpenOpenRadioMainBinding
import com.charlyghislain.openopenradio.ui.model.MainViewModel
import com.charlyghislain.openopenradio.ui.model.NestedNavState
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OpenOpenRadioMain : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityOpenOpenRadioMainBinding
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        binding = ActivityOpenOpenRadioMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarOpenOpenRadioMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navHost: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_open_open_radio_main) as NavHostFragment
        val navView: NavigationView = binding.navView
        val navController = navHost.navController as NavHostController
        appBarConfiguration = AppBarConfiguration(setOf(), drawerLayout)

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        supportActionBar?.setHomeButtonEnabled(false)


        lifecycleScope.launch {
            mainViewModel.nestedNavState.collect { state ->
                handleNestedNavigationChange(navController, state)
            }
        }
        navController.addOnDestinationChangedListener { _, d, _ ->
            handleMainNavigationChange(d)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.open_open_radio_main, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_open_open_radio_main)
        return when (item.itemId) {
            R.id.action_settings -> {
                navController.navigate(R.id.nav_settings)
//                onBackNavigationAvailable(true)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_open_open_radio_main)
        val isHome = navController.currentBackStackEntry?.destination?.id == R.id.nav_home
        return if (!isHome) {
            navController.navigateUp()
        } else if (mainViewModel.nestedNavigationUpHandler != null) {
            mainViewModel.nestedNavigationUpHandler!!.invoke()
        } else {
            true;
        }
    }

    private fun handleMainNavigationChange(destination: NavDestination) {
        val backAvailable = when (destination.id) {
            R.id.nav_home -> false
            else -> true
        }
        setBackNavigationAvailable(backAvailable)
    }

    private fun handleNestedNavigationChange(
        mainController: NavHostController,
        state: NestedNavState
    ) {
        if (state.isBackStackEmpty != null) {
            mainController.enableOnBackPressed(!state.isBackStackEmpty)
            setBackNavigationAvailable(!state.isBackStackEmpty)
        }
        if (state.currentTitle != null) {
            supportActionBar?.setTitle(state.currentTitle)
        }
    }

    private fun setBackNavigationAvailable(available: Boolean) {
        val mask = (ActionBar.DISPLAY_SHOW_HOME
                or ActionBar.DISPLAY_HOME_AS_UP)
        val bits = when (available) {
            true -> ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_HOME_AS_UP
            else -> 0
        }
        supportActionBar?.setDisplayOptions(bits, mask)
    }
}
