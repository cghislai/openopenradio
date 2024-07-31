package com.charlyghislain.openopenradio.ui.home

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.charlyghislain.openopenradio.ui.components.LanguageList


@Composable
fun LanguagesView(navController: NavController) {
    val context = LocalContext.current

    LanguageList(onClick = { language ->
        navController.navigate("${ROUTE_STATIONS}/${ROUTE_LANGUAGES}/${language}")
    })
}