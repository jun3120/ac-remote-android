package com.jun3120.acremote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.jun3120.acremote.ui.compose.AcRemoteApp
import com.jun3120.acremote.ui.compose.theme.AcRemoteTheme
import com.jun3120.acremote.ui.compose.theme.Surface as AppSurface

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AcRemoteTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = AppSurface) {
                    AcRemoteApp()
                }
            }
        }
    }
}
