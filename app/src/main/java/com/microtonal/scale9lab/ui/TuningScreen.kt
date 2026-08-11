package com.microtonal.scale9lab.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.microtonal.scale9lab.viewmodel.MainViewModel

@Composable
fun TuningScreen(viewModel: MainViewModel) {
    val currentScale by viewModel.currentScale.collectAsState()
    var rootInput by remember(currentScale.rootFrequencyHz) {
        mutableStateOf(String.format("%.2f", currentScale.rootFrequencyHz))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "Microtonal Tuning Lab",
            color = AccentCyan,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = rootInput,
                onValueChange = {
                    rootInput = it
                    it.toDoubleOrNull()?.let { newRoot ->
                        viewModel.setRootFrequency(newRoot)
                    }
                },
                label = { Text("Root Frequency (Hz)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(180.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentCyan,
                    unfocusedBorderColor = KeyBackground
                )
            )

            Button(
                onClick = { viewModel.resetScale() },
                colors = ButtonDefaults.buttonColors(containerColor = KeyPressed)
            ) {
                Text("Reset Scale", color = AccentGold)
            }
        }

        Divider(color = KeyBackground, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(currentScale.notes) { index, note ->
                var freqInput by remember(note.baseFrequencyHz) {
                    mutableStateOf(String.format("%.2f", note.baseFrequencyHz))
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = KeyBackground),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${note.id + 1}. ${note.nameHebrew}",
                                color = AccentGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Ratio: ${note.ratioNumerator}/${note.ratioDenominator} | Cents: ${String.format("%.1f", note.getCents(currentScale.rootFrequencyHz))}",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp
                            )
                        }

                        OutlinedTextField(
                            value = freqInput,
                            onValueChange = {
                                freqInput = it
                                it.toDoubleOrNull()?.let { newFreq ->
                                    viewModel.updateNoteFrequency(index, newFreq)
                                }
                            },
                            label = { Text("Hz") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(110.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentCyan,
                                unfocusedBorderColor = DarkBackground
                            )
                        )
                    }
                }
            }
        }
    }
}
