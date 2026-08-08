package com.rahul.symptoscan.presentation.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahul.symptoscan.ui.theme.BluePrimary

@Composable
fun HomeBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 16.dp
    ) {
        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 0.dp,
            modifier = Modifier.height(80.dp)
        ) {
            NavigationItem(
                label = "Home",
                icon = Icons.Default.Home,
                selected = currentRoute == "home",
                onClick = { onNavigate("home") }
            )
            NavigationItem(
                label = "Assess",
                icon = Icons.Default.AddCircleOutline,
                selected = currentRoute == "assess",
                onClick = { onNavigate("assess") }
            )
            NavigationItem(
                label = "History",
                icon = Icons.Default.History,
                selected = currentRoute == "history",
                onClick = { onNavigate("history") }
            )
            NavigationItem(
                label = "AI",
                icon = Icons.Default.SmartToy,
                selected = currentRoute == "ai",
                onClick = { onNavigate("ai") }
            )
            NavigationItem(
                label = "Profile",
                icon = Icons.Default.Person,
                selected = currentRoute == "profile",
                onClick = { onNavigate("profile") }
            )
        }
    }
}

@Composable
private fun RowScope.NavigationItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (selected) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(BluePrimary.copy(alpha = 0.1f))
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = BluePrimary
                        )
                    }
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = Color.Gray
                    )
                }
            }
        },
        label = {
            Text(
                text = label,
                fontSize = 12.sp,
                color = if (selected) BluePrimary else Color.Gray
            )
        },
        colors = NavigationBarItemDefaults.colors(
            indicatorColor = Color.Transparent
        )
    )
}
