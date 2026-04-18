package com.example.pepperbell

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.pepperbell.tflite.TfLitePepperClassifier
import com.example.pepperbell.ui.PredictionViewModel
import com.example.pepperbell.ui.screens.PredictionScreen
import java.io.File

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: PredictionViewModel
    private lateinit var classifier: TfLitePepperClassifier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        classifier = TfLitePepperClassifier(this)
        viewModel = PredictionViewModel(classifier)

        setContent {
            PepperBellApp(
                viewModel = viewModel,
                onImageCaptured = ::onImageSelected
            )
        }
    }

    private fun onImageSelected(imageFile: File) {
        // no-op for now, hook kept for future analytics/history handling
    }

    override fun onDestroy() {
        super.onDestroy()
        classifier.close()
    }
}

@Composable
fun PepperBellApp(
    viewModel: PredictionViewModel,
    onImageCaptured: (File) -> Unit
) {
    var selectedImageFile by remember { mutableStateOf<File?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val file = uriToFile(context, it)
            if (file != null) {
                selectedImageFile = file
                onImageCaptured(file)
            }
        }
    }

    PredictionScreen(
        viewModel = viewModel,
        imageFile = selectedImageFile,
        onImagePickRequest = {
            imagePickerLauncher.launch("image/*")
        }
    )
}

private fun uriToFile(context: Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val fileName = "temp_image_${System.currentTimeMillis()}.jpg"
        val file = File(context.cacheDir, fileName)
        inputStream.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        file
    } catch (e: Exception) {
        null
    }
}
