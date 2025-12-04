package com.amijul.mystore.domain.navigation

import androidx.compose.runtime.Composable

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit
)
