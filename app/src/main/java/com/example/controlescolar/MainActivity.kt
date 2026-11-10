package com.example.controlescolar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.controlescolar.ui.navigation.AppNavHost
import com.example.controlescolar.ui.theme.ControlEscolarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ControlEscolarTheme {
                AppNavHost()
            }
        }
    }
}
