package com.charlyghislain.openopenradio.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "menu") {
        composable("menu") { MenuScreen(navController) }
        composable("genres") { GenresView(navController) }
//        composable("content2") { ContentScreen2(navController) }
//        composable("content3") { ContentScreen3(navController) }// Add more composable destinations as needed
    }
}


@Composable
fun MenuScreen(navController: NavController) {
    LazyColumn(modifier = Modifier.fillMaxWidth(1f)) {
        item {
            MenuItem(label = "Genres",
                icon = Icons.Filled.Info,
                onClick = { navController.navigate("genres") })
        }
//        item { MenuItem(label = "test2", onClick = { navController.navigate("content2") }) }
//        item { MenuItem(label = "test3", onClick = { navController.navigate("content3") }) }
    }
}

@Composable
fun MenuItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label, // Provide a content description for accessibility
            modifier = Modifier.size(24.dp) // Adjust icon size as needed
        )
        Spacer(modifier = Modifier.width(8.dp)) // Add spacing between icon and text
        Text(text = label)
    }
}
