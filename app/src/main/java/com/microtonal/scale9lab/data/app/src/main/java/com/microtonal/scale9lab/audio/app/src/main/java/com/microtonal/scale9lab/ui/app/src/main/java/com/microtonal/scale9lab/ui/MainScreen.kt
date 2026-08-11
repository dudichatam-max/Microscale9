package com.microtonal.scale9lab.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.microtonal.scale9lab.data.WaveformType
import com.microtonal.scale9lab.viewmodel.MainViewModel

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val currentScale by viewModel.currentScale.collectAsState()
    val currentOctave by viewModel.currentOctave.collectAsState()
    val waveform by viewModel.waveform.collectAsState()
    val masterVolume by viewModel.masterVolume.collectAsState()
    val sustainEnabled by viewModel.sustainEnabled.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "MicroScale 9 - Keyboard",
            color = AccentCyan,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Waveform", color = MaterialTheme.colorScheme.onBackground, fontSize = 12.sp)
                Row {
                    WaveformType.values().forEach { type ->
                        FilterChip(
                            selected = waveform == type,
                            onClick = { viewModel.setWaveform(type) },
                            label = { Text(type.displayName, fontSize = 10.sp) },
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Octave: ", color = MaterialTheme.colorScheme.onBackground)
                IconButton(onClick = { viewModel.setOctave(currentOctave - 1) }) {
                    Text("-", color = AccentCyan, fontSize = 24.sp)
                }
                Text("$currentOctave", color = AccentGold, fontWeight = FontWeight.Bold)
                IconButton(onClick = { viewModel.setOctave(currentOctave + 1) }) {
                    Text("+", color = AccentCyan, fontSize = 24.sp)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Sustain", color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(end = 8.dp))
                Switch(
                    checked = sustainEnabled,
                    onCheckedChange = { viewModel.setSustain(it) }
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(currentScale.notes) { index, note ->
                val effectiveFreq = note.getEffectiveFrequency(currentScale.rootFrequencyHz, currentOctave)
                val cents = note.getCents(currentScale.rootFrequencyHz)

                PianoKey(
                    noteName = note.nameHebrew,
                    freqText = String.format("%.1f Hz", effectiveFreq),
                    centsText = String.format("%.0f c", cents),
                    onDown = { viewModel.onNoteDown(index) },
                    onUp = { viewModel.onNoteUp(index) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Vol", color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(end = 8.dp))
            Slider(
                value = masterVolume,
                onValueChange = { viewModel.setVolume(it) },
                valueRange = 0f..1f,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@SuppressLint("ClickableViewAccessibility")
@Composable
fun PianoKey(
    noteName: String,
    freqText: String,
    centsText: String,
    onDown: () -> Unit,
    onUp: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isPressed) KeyPressed else KeyBackground)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onDown()
                        tryAwaitRelease()
                        isPressed = false
                        onUp()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = noteName,
                color = AccentGold,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = freqText,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp
            )
            Text(
                text = centsText,
                color = AccentCyan,
                fontSize = 11.sp
            )
        }
    }
}
