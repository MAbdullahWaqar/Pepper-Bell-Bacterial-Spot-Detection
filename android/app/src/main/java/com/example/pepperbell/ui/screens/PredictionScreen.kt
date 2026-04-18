package com.example.pepperbell.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.pepperbell.ui.PredictionUiState
import com.example.pepperbell.ui.PredictionViewModel
import java.io.File

@Composable
fun PredictionScreen(
    viewModel: PredictionViewModel,
    imageFile: File?,
    onImagePickRequest: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5EFE2))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "🌶️ Pepper Bell",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F8A6C),
                letterSpacing = 1.sp
            )
            Text(
                text = "Bacterial Spot Detector",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A2F23),
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Text(
                text = "Upload a leaf image for instant on-device AI detection (TensorFlow Lite)",
                fontSize = 14.sp,
                color = Color(0xFF46685A),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Upload Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        color = Color(0xFF1F8A6C).copy(alpha = 0.45f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .background(
                        color = Color.White.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable(enabled = uiState !is PredictionUiState.Loading) { onImagePickRequest() }
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📸 Choose Leaf Image",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F8A6C)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Predict Button
            Button(
                onClick = {
                    if (imageFile != null) {
                        viewModel.predictImage(imageFile)
                    }
                },
                enabled = imageFile != null && uiState !is PredictionUiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEA6E3D)
                ),
                shape = RoundedCornerShape(999.dp)
            ) {
                Text(
                    text = if (uiState is PredictionUiState.Loading) "Analyzing..." else "Predict Disease",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Image Preview
            if (imageFile != null && imageFile.exists()) {
                AsyncImage(
                    model = imageFile,
                    contentDescription = "Selected leaf image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, Color(0xFF1A2F23).copy(alpha = 0.16f), RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Results
            when (uiState) {
                is PredictionUiState.Error -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFB52B2B).copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = (uiState as PredictionUiState.Error).message,
                            color = Color(0xFFB52B2B),
                            modifier = Modifier.padding(16.dp),
                            fontSize = 14.sp
                        )
                    }
                }
                is PredictionUiState.Success -> {
                    val result = (uiState as PredictionUiState.Success).result
                    ResultCard(result)
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ResultCard(result: com.example.pepperbell.data.PredictionResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.78f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF1A2F23).copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Prediction Result",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A2F23)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = result.prediction,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F8A6C)
            )

            Text(
                text = "Confidence: ${String.format("%.2f", result.confidence)}%",
                fontSize = 14.sp,
                color = Color(0xFF46685A),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confidence bars
            result.allConfidences.entries.sortedByDescending { it.value }.forEach { (label, value) ->
                ConfidenceBar(label, value)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ConfidenceBar(label: String, value: Double) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(
                text = "${String.format("%.2f", value)}%",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(Color(0xFFE0E8E2), RoundedCornerShape(999.dp))
                .clip(RoundedCornerShape(999.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth((value / 100.0).toFloat())
                    .background(Color(0xFF1F8A6C), RoundedCornerShape(999.dp))
            )
        }
    }
}
