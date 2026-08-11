package com.microtonal.scale9lab.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.microtonal.scale9lab.viewmodel.MainViewModel

// הגדרות צבעים
private val DarkBackground = Color(0xFF121212)
private val AccentCyan = Color(0xFF00E5FF)
private val KeyPressed = Color(0xFF2A2A3D)
private val KeyBackground = Color(0xFF1E1E1E)
private val AccentGold = Color(0xFFFFD700)

@Composable
fun RecordingScreen(viewModel: MainViewModel) {
    val isRecording by viewModel.isRecording.collectAsState()
    val lastExportedFile by viewModel.lastExportedFile.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Record & Export WAV",
            color = AccentCyan,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (!isRecording) {
            Button(
                onClick = { viewModel.startRecording() },
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp)
            ) {
                Text("Start Recording", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        } else {
            Button(
                onClick = { viewModel.stopRecording() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp)
            ) {
                Text("Stop Recording", color = MaterialTheme.colorScheme.onError, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.exportRecordingToWav() },
            enabled = !isRecording,
            colors = ButtonDefaults.buttonColors(containerColor = KeyPressed),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp)
        ) {
            Text("Export to WAV File", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        lastExportedFile?.let { file ->
            Card(
                colors = CardDefaults.cardColors(containerColor = KeyBackground),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Exported Successfully!",
                        color = AccentGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Path: ${file.absolutePath}",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
