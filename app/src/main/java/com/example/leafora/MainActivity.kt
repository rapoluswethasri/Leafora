package com.example.leafora

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val LeafGreen = Color(0xFF176B3A)
private val LightGreen = Color(0xFFEAF5EC)
private val DarkText = Color(0xFF193528)
private val MutedText = Color(0xFF64756A)
private val PageBackground = Color(0xFFF7FAF6)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = PageBackground
                ) {
                    LeaforaHome()
                }
            }
        }
    }
}

@Composable
private fun LeaforaHome() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var resultText by remember { mutableStateOf("") }
    var careText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val validator = remember(context) {
        LeafInputValidator(context)
    }

    DisposableEffect(validator) {
        onDispose {
            validator.close()
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = context.contentResolver
                    .openInputStream(uri)
                    ?.use { input ->
                        android.graphics.BitmapFactory.decodeStream(input)
                    }

                if (bitmap != null) {
                    selectedBitmap = bitmap
                    resultText = ""
                    careText = ""
                } else {
                    resultText = "Image could not be opened. Please select another image."
                    careText = ""
                }
            } catch (e: Exception) {
                resultText = "Unable to open this image. Please try another one."
                careText = ""
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = LeafGreen,
                        shape = RoundedCornerShape(15.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("🌿", fontSize = 27.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Leafora",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = LeafGreen
                )
                Text(
                    text = "Smart Plant Health Assistant",
                    fontSize = 13.sp,
                    color = MutedText
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = LightGreen
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Healthy plants, smarter farming.",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Select a plant leaf image to check whether it is a leaf and identify possible diseases.",
                    fontSize = 14.sp,
                    color = MutedText,
                    lineHeight = 21.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = { imagePicker.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LeafGreen
                    )
                ) {
                    Text(
                        text = "Choose Leaf Image",
                        modifier = Modifier.padding(vertical = 5.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        selectedBitmap?.let { bitmap ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Selected plant image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Image selected",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkText
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (!isLoading) {
                                isLoading = true
                                resultText = ""
                                careText = ""

                                scope.launch {
                                    try {
                                        val isLeaf = withContext(
                                            Dispatchers.Default
                                        ) {
                                            validator.isLeaf(bitmap)
                                        }

                                        if (!isLeaf) {
                                            resultText =
                                                "This image may not contain a plant leaf."
                                            careText =
                                                "Please choose a clear plant leaf photo with good lighting."
                                        } else {
                                            val diseaseResult =
                                                withContext(Dispatchers.Default) {
                                                    val classifier =
                                                        LeaforaClassifier(context)
                                                    try {
                                                        classifier.classify(bitmap)
                                                    } finally {
                                                        classifier.close()
                                                    }
                                                }

                                            resultText =
                                                diseaseResult.details.toString()

                                            careText =
                                                "Check the affected leaf carefully. Follow suitable crop-care guidance for the identified disease."
                                        }
                                    } catch (e: Exception) {
                                        resultText =
                                            "Unable to analyse this image. Please try again."
                                        careText = ""
                                        android.util.Log.e(
                                            "Leafora",
                                            "Image analysis failed",
                                            e
                                        )
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LeafGreen
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Analysing...")
                        } else {
                            Text(
                                text = "Detect Disease",
                                modifier = Modifier.padding(vertical = 5.dp),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            selectedBitmap = null
                            resultText = ""
                            careText = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Choose Another Image")
                    }
                }
            }
        } ?: run {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🌱", fontSize = 42.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Your plant scan starts here",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        color = DarkText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Choose a clear leaf photo to begin.",
                        fontSize = 14.sp,
                        color = MutedText,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        if (resultText.isNotBlank()) {
            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text = "Scan Result",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = LeafGreen
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = resultText,
                        fontSize = 15.sp,
                        color = DarkText,
                        lineHeight = 23.sp
                    )

                    if (careText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Plant Care",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = LeafGreen
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = careText,
                            fontSize = 14.sp,
                            color = MutedText,
                            lineHeight = 21.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = LightGreen
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text(
                    text = "Designed for farmers",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = LeafGreen
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "• On-device image analysis\n• Plant leaf validation before disease detection\n• Works offline after installation",
                    fontSize = 14.sp,
                    color = DarkText,
                    lineHeight = 23.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Leafora • AI Based Plant Disease Detection",
            fontSize = 12.sp,
            color = MutedText,
            textAlign = TextAlign.Center
        )
    }
}
