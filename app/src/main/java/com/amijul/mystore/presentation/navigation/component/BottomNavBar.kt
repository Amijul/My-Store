package com.amijul.mystore.presentation.navigation.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.amijul.mystore.domain.navigation.BottomNavItem
import com.amijul.mystore.ui.home.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun BottomNavBar( homeViewModel: HomeViewModel = koinViewModel()) {


    val bottomNav by homeViewModel.bottomNav.collectAsStateWithLifecycle()


    val items = listOf(
        BottomNavItem(
            route = "home",
            label = "Home",
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") }
        ),
        BottomNavItem(
            route = "orders",
            label = "Orders",
            icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "Orders") }
        ),
        BottomNavItem(
            route = "account",
            label = "Account",
            icon = { Icon(Icons.Filled.AccountCircle, contentDescription = "Account") }
        )
    )


    NavigationBar {
        items.forEachIndexed { index,  item ->

            val selected = bottomNav == index
            NavigationBarItem(
                selected = selected,
                onClick = {
                    homeViewModel.setBottomNav(index = index)

                },
                icon = item.icon,
                label = { Text(item.label) },
                alwaysShowLabel = true
            )
        }
    }
}
