package com.example.mediconnect24x7

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.mediconnect24x7.ui.theme.*
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomsScreen() {
    var symptomText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var chatHistory by remember { mutableStateOf<List<SymptomRecord>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val firestore = FirebaseFirestore.getInstance()
    val listState = rememberLazyListState()

    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            firestore.collection("users")
                .document(currentUser.uid)
                .collection("health_records")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e == null && snapshot != null) {
                        chatHistory = snapshot.documents.mapNotNull { it.toObject(SymptomRecord::class.java) }
                        coroutineScope.launch {
                            if (chatHistory.isNotEmpty()) {
                                listState.animateScrollToItem(chatHistory.size - 1)
                            }
                        }
                    }
                }
        }
    }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.get(0) ?: ""
            if (spokenText.isNotEmpty()) {
                symptomText = spokenText
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Describe your symptoms...")
            }
            try {
                speechLauncher.launch(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Voice input not supported on this device", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Health Assistant",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PremiumTeal)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF4F7F6))
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = 0.dp
                )
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (chatHistory.isEmpty() && !isLoading) {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Describe your symptoms to get an AI recommendation.", color = Color.Gray)
                        }
                    }
                } else {
                    items(chatHistory) { record ->
                        // User message
                        ChatBubble(
                            text = record.symptoms,
                            isUser = true,
                            timestamp = record.timestamp
                        )
                        // AI message
                        Spacer(modifier = Modifier.height(8.dp))
                        ChatBubble(
                            text = record.aiRecommendation,
                            isUser = false,
                            timestamp = record.timestamp
                        )
                    }
                }
                if (isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PremiumTeal)
                        }
                    }
                }
            }

            // Input Area
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasPermission) {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Describe your symptoms...")
                                }
                                try {
                                    speechLauncher.launch(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Voice input not supported", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        modifier = Modifier
                            .background(PremiumMint.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice Input", tint = PremiumTeal)
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    OutlinedTextField(
                        value = symptomText,
                        onValueChange = { symptomText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type symptoms...") },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PremiumTeal,
                            unfocusedBorderColor = Color.LightGray,
                            focusedContainerColor = Color(0xFFF9F9F9),
                            unfocusedContainerColor = Color(0xFFF9F9F9)
                        ),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    IconButton(
                        onClick = {
                            if (symptomText.isNotBlank() && !isLoading) {
                                val currentText = symptomText
                                symptomText = ""
                                coroutineScope.launch {
                                    isLoading = true
                                    val analysis = analyzeSymptomsWithGemini(currentText)
                                    if (currentUser != null && analysis != null) {
                                        val record = SymptomRecord(
                                            id = java.util.UUID.randomUUID().toString(),
                                            userId = currentUser.uid,
                                            symptoms = currentText,
                                            aiRecommendation = analysis,
                                            timestamp = System.currentTimeMillis()
                                        )
                                        firestore.collection("users")
                                            .document(currentUser.uid)
                                            .collection("health_records")
                                            .document(record.id)
                                            .set(record)
                                    }
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier
                            .background(if (symptomText.isNotBlank()) PremiumTeal else Color.LightGray, CircleShape),
                        enabled = symptomText.isNotBlank() && !isLoading
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(text: String, isUser: Boolean, timestamp: Long) {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val timeString = sdf.format(Date(timestamp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(PremiumMint.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PremiumTeal, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.weight(1f, fill = false),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                color = if (isUser) PremiumTeal else Color.White,
                shadowElevation = 1.dp
            ) {
                Text(
                    text = text,
                    modifier = Modifier.padding(12.dp),
                    color = if (isUser) Color.White else Color.Black,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            }
            Text(
                text = timeString,
                color = Color.Gray,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp)
            )
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(20.dp))
            }
        }
    }
}

private suspend fun analyzeSymptomsWithGemini(input: String): String {
    return try {
        val generativeModel = GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = "AIzaSyBmS18T6e9PDuSquTOjoWs7yAttmITscvg"
        )
        val prompt = """
            You are a professional medical assistant AI for the MediConnect 24/7 app.
            Analyze the following symptoms reported by the user: "$input"
            
            Provide a concise, empathetic, and professional response (maximum 4 sentences) that includes:
            1. A brief assessment of the symptoms (without providing a final diagnosis).
            2. Immediate self-care or safety recommendations.
            3. The specific type of medical specialist the user should consult.
            
            CRITICAL: Always state that this is not a medical diagnosis and that the user must consult a qualified healthcare professional for medical advice.
        """.trimIndent()
        
        val response = generativeModel.generateContent(prompt)
        response.text ?: "I'm sorry, I couldn't process your request at this time. Please consult a General Physician."
    } catch (e: Exception) {
        // The Generative AI SDK sometimes throws a MissingFieldException when parsing API errors like 403 Forbidden.
        // We'll return a clean message instead of the raw stack trace.
        if (e.message?.contains("PERMISSION_DENIED") == true || e.message?.contains("403") == true) {
            "Error: Access denied (403). Your device's debug SHA-1 fingerprint may not be whitelisted for this API key, or your region is restricted. Please consult a doctor."
        } else {
            "I'm sorry, I couldn't process your symptoms right now. Please try again later or consult a doctor."
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SymptomsScreenPreview() {
    MediConnect24x7Theme {
        SymptomsScreen()
    }
}
