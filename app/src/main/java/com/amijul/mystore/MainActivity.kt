package com.amijul.mystore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.amijul.mystore.presentation.navigation.MyStoreApp
import com.amijul.mystore.ui.theme.MyStoreTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyStoreTheme {
                Scaffold(modifier = Modifier.fillMaxSize().background(Color(0xFFFFBFC0))) { innerPadding ->
                    MyStoreApp()
                }
            }
        }
    }
}

