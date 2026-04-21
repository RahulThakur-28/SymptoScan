package com.example.rahul.symptoscan.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.rahul.symptoscan.ui.navigation.Screen

@Composable
fun BottomNavigationBar(navController: NavController) {

    val items = listOf(
        NavigationItem("Home", Screen.Home.route, Icons.Outlined.Home),
        NavigationItem("Chat", Screen.Chat.route, Icons.Outlined.Chat),
        NavigationItem("History", Screen.History.route, Icons.Outlined.History),
        NavigationItem("Profile", Screen.Profile.route, Icons.Outlined.Person)
    )


    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {

        items.forEach { item ->

            NavigationBarItem(
                selected = currentRoute == item.route,

                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(Screen.Home.route)
                        launchSingleTop = true
                    }
                },

                icon = {
                    Icon(
                        item.icon,
                        contentDescription = item.title
                    )
                },

                label = {
                    Text(item.title)
                },

                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            )
        }
    }
}

data class NavigationItem(
    val title: String,
    val route: String,
    val icon: ImageVector
)