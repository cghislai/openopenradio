package com.charlyghislain.openopenradio.ui.home

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.charlyghislain.openopenradio.ui.components.StationsList


@Composable
fun StationsView(
    navController: NavController,
    filterType: String?,
    filterValue: String?
) {
    val context = LocalContext.current

    StationsList(onClick = { station ->
        Toast.makeText(context, station, Toast.LENGTH_SHORT).show()
    }, filterType, filterValue)
}