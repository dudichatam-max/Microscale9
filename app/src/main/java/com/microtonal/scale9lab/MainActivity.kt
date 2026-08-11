package com.microtonal.scale9lab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.microtonal.scale9lab.ui.*
import com.microtonal.scale9lab.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MicrotonalTheme {
                var selectedTab by remember { mutableIntStateOf(0) }

                Scaffold(
                    bottomBar = {
                        NavigationBar(containerColor = KeyBackground) {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = { Icon(Icons.Default.MusicNote, contentDescription = "Keyboard") },
                                label = { Text("Keyboard") }
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = { Icon(Icons.Default.GraphicEq, contentDescription = "Tuning") },
                                label = { Text("טיוּנינג") }
                            )
                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                icon = { Icon(Icons.Default.GraphicEq, contentDescription = "Record") },
                                label = { Text("Record & WAV") }
                            )
                        }
                    }
                ) { paddingValues ->
                    Surface(modifier = Modifier.padding(paddingValues)) {
                        when (selectedTab) {
                            0 -> MainScreen(viewModel = viewModel)
                            1 -> TuningScreen(viewModel = viewModel)
                            2 -> RecordingScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
