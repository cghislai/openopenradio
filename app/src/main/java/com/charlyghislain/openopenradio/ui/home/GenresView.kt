package com.charlyghislain.openopenradio.ui.home

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.charlyghislain.openopenradio.ui.components.GenreList


@Composable
fun GenresView(navController: NavController) {
    val context = LocalContext.current

    GenreList(onClick = { genre ->
        Toast.makeText(context, genre, Toast.LENGTH_SHORT).show()
    })
}