package com.charlyghislain.openopenradio.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.charlyghislain.openopenradio.R

class ComposePlaceholderFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController() // Get nested NavController
        navController.navigate(getString(R.string.nav_home_root))
    }
}