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

@Composable
fun RecordingScreen(viewModel: MainViewModel) {
    val isRecording by viewModel.isRecording.collectAsState()
    val lastExportedFile by viewModel.lastExportedFile.collectAsState()

    // צבעים מקומיים למניעת התנגשות מול Theme.kt
    val bgColor = Color(0xFF121212)
    val cyanColor = Color(0xFF00E5FF)
    val keyPressedColor = Color(0xFF2A2A3D)
    val keyBgColor = Color(0xFF1E1E1E)
    val goldColor = Color(0xFFFFD700)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Record & Export WAV",
            color = cyanColor,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (!isRecording) {
            Button(
                onClick = { viewModel.startRecording() },
                colors = ButtonDefaults.buttonColors(containerColor = cyanColor),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp)
            ) {
                Text("Start Recording", color = bgColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
            colors = ButtonDefaults.buttonColors(containerColor = keyPressedColor),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp)
        ) {
            Text("Export to WAV File", color = goldColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        lastExportedFile?.let { file ->
            Card(
                colors = CardDefaults.cardColors(containerColor = keyBgColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Exported Successfully!",
                        color = goldColor,
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
