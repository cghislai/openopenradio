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
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

const val ROUTE_GENRES = "genres"
const val ROUTE_COUNTRIES = "countries"
const val ROUTE_LANGUAGES = "languages"
const val ROUTE_STATIONS = "stations"
const val PARAM_STATION_FILTER_TYPE = "filter_type"
const val PARAM_STATION_FILTER_VALUE = "filter_value"

@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "menu") {
        composable("menu") { MenuScreen(navController) }
        composable(ROUTE_GENRES) { GenresView(navController) }
        composable(ROUTE_COUNTRIES) { CountriesView(navController) }
        composable(ROUTE_LANGUAGES) { LanguagesView(navController) }

        composable(
            "${ROUTE_STATIONS}/{${PARAM_STATION_FILTER_TYPE}}/{${PARAM_STATION_FILTER_VALUE}}",
            arguments = listOf(
                navArgument(PARAM_STATION_FILTER_TYPE) { type = NavType.StringType },
                navArgument(PARAM_STATION_FILTER_VALUE) { type = NavType.StringType },
            )
        ) { navBackStackEntry ->
            val filterTYpe = navBackStackEntry.arguments?.getString(PARAM_STATION_FILTER_TYPE)
            val filterValue = navBackStackEntry.arguments?.getString(PARAM_STATION_FILTER_VALUE)
            StationsView(navController, filterTYpe, filterValue)
        }
    }
}


@Composable
fun MenuScreen(navController: NavController) {
    LazyColumn(modifier = Modifier.fillMaxWidth(1f)) {
        item {
            MenuItem(label = "Genres",
                icon = Icons.Filled.Folder,
                onClick = { navController.navigate(ROUTE_GENRES) })
            MenuItem(label = "Countries",
                icon = Icons.Filled.Folder,
                onClick = { navController.navigate(ROUTE_COUNTRIES) })
            MenuItem(label = "Languages",
                icon = Icons.Filled.Folder,
                onClick = { navController.navigate(ROUTE_LANGUAGES) })
        }
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
