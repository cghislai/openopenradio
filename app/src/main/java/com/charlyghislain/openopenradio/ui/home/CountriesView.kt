package com.charlyghislain.openopenradio.ui.home

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.charlyghislain.openopenradio.ui.components.CountryList


@Composable
fun CountriesView(navController: NavController) {
    val context = LocalContext.current

    CountryList(onClick = { country ->
        navController.navigate("${ROUTE_STATIONS}/${ROUTE_COUNTRIES}/${country}")
    })
}