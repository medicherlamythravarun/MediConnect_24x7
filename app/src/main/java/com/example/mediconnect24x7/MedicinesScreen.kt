package com.example.mediconnect24x7

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.mediconnect24x7.ui.theme.PremiumTeal
import com.example.mediconnect24x7.ui.theme.PremiumMint
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicinesScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    var medicines by remember { mutableStateOf<List<MedicineRecord>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            firestore.collection("users")
                .document(currentUser.uid)
                .collection("medicines")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        val meds = snapshot.documents.mapNotNull { it.toObject(MedicineRecord::class.java) }
                        
                        val today = Calendar.getInstance()
                        today.set(Calendar.HOUR_OF_DAY, 0)
                        today.set(Calendar.MINUTE, 0)
                        val startOfDay = today.timeInMillis
                        
                        val updatedMeds = meds.map { med ->
                            if (med.isTakenToday && med.lastTakenTimestamp < startOfDay) {
                                val updatedMed = med.copy(isTakenToday = false)
                                firestore.collection("users").document(currentUser.uid)
                                    .collection("medicines").document(med.id).set(updatedMed)
                                updatedMed
                            } else {
                                med
                            }
                        }
                        
                        medicines = updatedMeds
                        isLoading = false
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medicine Tracker", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PremiumTeal)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PremiumTeal,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Medicine")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Medicine")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF4F7F6))
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PremiumTeal)
            } else if (medicines.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Medication, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No medicines added yet.", color = Color.Gray, fontSize = 16.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val morningMeds = medicines.filter { it.timeOfDay == "Morning" }
                    val afternoonMeds = medicines.filter { it.timeOfDay == "Afternoon" }
                    val eveningMeds = medicines.filter { it.timeOfDay == "Evening" }

                    if (morningMeds.isNotEmpty()) {
                        item { TimelineHeader("Morning", Icons.Default.WbSunny, Color(0xFFFBC02D)) }
                        items(morningMeds) { med -> MedicineCard(med) }
                    }
                    if (afternoonMeds.isNotEmpty()) {
                        item { TimelineHeader("Afternoon", Icons.Default.LightMode, Color(0xFFF57C00)) }
                        items(afternoonMeds) { med -> MedicineCard(med) }
                    }
                    if (eveningMeds.isNotEmpty()) {
                        item { TimelineHeader("Evening", Icons.Default.NightsStay, Color(0xFF5E35B1)) }
                        items(eveningMeds) { med -> MedicineCard(med) }
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showAddDialog) {
        AddMedicineDialog(onDismiss = { showAddDialog = false })
    }
}

@Composable
fun TimelineHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
    }
}

@Composable
fun MedicineCard(medicine: MedicineRecord) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    
    val formIcon = when (medicine.form) {
        "Capsule" -> Icons.Default.Vaccines
        "Tablet" -> Icons.Default.Medication
        "Liquid" -> Icons.Default.WaterDrop
        "Inhaler" -> Icons.Default.Air
        else -> Icons.Default.MedicalServices
    }
    
    val cardBgColor = if (medicine.isTakenToday) PremiumMint.copy(alpha = 0.15f) else Color.White
    val textColor = if (medicine.isTakenToday) Color.Gray else Color.Black
    val iconColor = if (medicine.isTakenToday) PremiumTeal.copy(alpha = 0.5f) else PremiumTeal

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (medicine.isTakenToday) 0.dp else 2.dp),
        border = BorderStroke(1.dp, if (medicine.isTakenToday) PremiumTeal.copy(alpha = 0.3f) else Color.LightGray.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (medicine.isTakenToday) Color.Transparent else PremiumMint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(formIcon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medicine.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = textColor,
                    textDecoration = if (medicine.isTakenToday) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                )
                Text(
                    text = "${medicine.dosage} • ${medicine.form}",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
                
                if (medicine.inventoryCount < 5 && !medicine.isTakenToday) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Refill Soon: ${medicine.inventoryCount} left", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                } else if (!medicine.isTakenToday) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${medicine.inventoryCount} left", color = Color.Gray, fontSize = 11.sp)
                }
            }
            
            val buttonBgColor by animateColorAsState(if (medicine.isTakenToday) PremiumTeal.copy(alpha = 0.15f) else PremiumTeal)
            val buttonTextColor by animateColorAsState(if (medicine.isTakenToday) PremiumTeal else Color.White)

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        val user = auth.currentUser
                        if (user != null) {
                            val newTakenState = !medicine.isTakenToday
                            val newInventory = if (newTakenState) {
                                maxOf(0, medicine.inventoryCount - 1)
                            } else {
                                medicine.inventoryCount + 1
                            }
                            
                            val updatedMed = medicine.copy(
                                isTakenToday = newTakenState,
                                inventoryCount = newInventory,
                                lastTakenTimestamp = if (newTakenState) System.currentTimeMillis() else medicine.lastTakenTimestamp
                            )
                            
                            firestore.collection("users").document(user.uid)
                                .collection("medicines").document(medicine.id)
                                .set(updatedMed)
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    },
                color = buttonBgColor,
                contentColor = buttonTextColor
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .animateContentSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (medicine.isTakenToday) Icons.Default.Check else Icons.Default.AddTask,
                        contentDescription = "Mark Taken",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (medicine.isTakenToday) "Taken" else "Take",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
            
            IconButton(
                onClick = {
                    val user = auth.currentUser
                    if (user != null) {
                        firestore.collection("users").document(user.uid)
                            .collection("medicines").document(medicine.id).delete()
                    }
                }
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.5f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineDialog(onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var timeOfDay by remember { mutableStateOf("Morning") }
    var form by remember { mutableStateOf("Tablet") }
    var inventory by remember { mutableStateOf("30") }
    var isScanning by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.get(0) ?: ""
            if (spokenText.isNotEmpty()) {
                isScanning = true
                coroutineScope.launch {
                    val parsed = parseMedicineWithAI(spokenText)
                    if (parsed != null) {
                        name = parsed.name
                        dosage = parsed.dosage
                        timeOfDay = parsed.timeOfDay
                        form = parsed.form
                        inventory = parsed.inventoryCount.toString()
                        Toast.makeText(context, "AI Parsed Prescription!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Could not understand prescription.", Toast.LENGTH_SHORT).show()
                    }
                    isScanning = false
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Medicine", fontWeight = FontWeight.Bold, color = PremiumTeal) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (isScanning) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI is scanning...", color = PremiumTeal)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                Button(
                    onClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Read your prescription (e.g. Take two 500mg Paracetamol tablets every morning)")
                        }
                        try {
                            speechLauncher.launch(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Voice input not supported", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8F5E9), contentColor = PremiumTeal)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Auto-fill with AI Voice", fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Medicine Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dosage, onValueChange = { dosage = it },
                        label = { Text("Dosage (e.g. 500mg)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = inventory, onValueChange = { inventory = it },
                        label = { Text("Total Pills") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Time of Day", fontSize = 12.sp, color = Color.Gray)
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Morning", "Afternoon", "Evening").forEach { t ->
                        FilterChip(
                            selected = timeOfDay == t,
                            onClick = { timeOfDay = t },
                            label = { Text(t) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text("Form", fontSize = 12.sp, color = Color.Gray)
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Tablet", "Capsule", "Liquid", "Inhaler").forEach { f ->
                        FilterChip(
                            selected = form == f,
                            onClick = { form = f },
                            label = { Text(f) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val auth = FirebaseAuth.getInstance()
                    val user = auth.currentUser
                    if (user != null) {
                        if (name.isNotBlank()) {
                            val firestore = FirebaseFirestore.getInstance()
                            val newMed = MedicineRecord(
                                id = UUID.randomUUID().toString(),
                                userId = user.uid,
                                name = name,
                                dosage = dosage,
                                timeOfDay = timeOfDay,
                                form = form,
                                inventoryCount = inventory.toIntOrNull() ?: 30
                            )
                            firestore.collection("users").document(user.uid)
                                .collection("medicines").document(newMed.id)
                                .set(newMed)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Medicine Added!", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Failed to add: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        } else {
                            Toast.makeText(context, "Please enter a medicine name", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PremiumTeal)
            ) {
                Text("Save Medicine")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

suspend fun parseMedicineWithAI(input: String): MedicineRecord? {
    return try {
        val generativeModel = GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = "AIzaSyA9mnIAjVwjaQGc9eztetuQhZJnS30I3L8"
        )
        val prompt = """
            Parse the following spoken prescription into structured data.
            Prescription: "$input"
            Return exactly a comma-separated format: NAME,DOSAGE,TIMEOFDAY,FORM
            Where TIMEOFDAY must be one of: Morning, Afternoon, Evening.
            Where FORM must be one of: Tablet, Capsule, Liquid, Inhaler.
            If missing, guess logically or leave empty for dosage. 
            Example output: Paracetamol,500mg,Morning,Tablet
            DO NOT ADD ANY OTHER TEXT.
        """.trimIndent()
        
        val response = generativeModel.generateContent(prompt)
        val resultText = response.text?.trim() ?: return null
        
        val parts = resultText.split(",")
        if (parts.size >= 4) {
            MedicineRecord(
                name = parts[0].trim(),
                dosage = parts[1].trim(),
                timeOfDay = parts[2].trim(),
                form = parts[3].trim(),
                inventoryCount = 30
            )
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}
