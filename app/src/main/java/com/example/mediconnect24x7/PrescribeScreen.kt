package com.example.mediconnect24x7

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediconnect24x7.ui.theme.PremiumTeal
import com.example.mediconnect24x7.ui.theme.PremiumMint
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescribeScreen() {
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val context = LocalContext.current
    
    var completedAppointments by remember { mutableStateOf<List<Appointment>>(emptyList()) }
    var selectedAppointment by remember { mutableStateOf<Appointment?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Prescription form state
    var medicineName by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            firestore.collection("appointments")
                .whereEqualTo("doctorId", currentUser.uid)
                .whereEqualTo("status", "Completed")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    completedAppointments = querySnapshot.toObjects(Appointment::class.java)
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7F6))
    ) {
        TopAppBar(
            title = { Text("Prescribe Medicine", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                if (selectedAppointment != null) {
                    IconButton(onClick = { selectedAppointment = null }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White,
                titleContentColor = PremiumTeal
            )
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PremiumTeal)
            }
        } else if (selectedAppointment == null) {
            // Show list of completed appointments
            if (completedAppointments.isEmpty()) {
                EmptyPrescribeView()
            } else {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Select a completed consultation to prescribe medicines.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(completedAppointments) { appointment ->
                            CompletedAppointmentItem(appointment) {
                                selectedAppointment = appointment
                            }
                        }
                    }
                }
            }
        } else {
            // Show prescription form
            PrescriptionForm(
                appointment = selectedAppointment!!,
                medicineName = medicineName,
                onMedicineChange = { medicineName = it },
                dosage = dosage,
                onDosageChange = { dosage = it },
                instructions = instructions,
                onInstructionsChange = { instructions = it },
                isSaving = isSaving,
                onSave = {
                    if (medicineName.isBlank() || dosage.isBlank()) {
                        Toast.makeText(context, "Please enter medicine name and dosage", Toast.LENGTH_SHORT).show()
                        return@PrescriptionForm
                    }
                    
                    isSaving = true
                    val prescriptionId = firestore.collection("prescriptions").document().id
                    val prescriptionData = mapOf(
                        "prescriptionId" to prescriptionId,
                        "appointmentId" to selectedAppointment!!.appointmentId,
                        "doctorId" to currentUser?.uid,
                        "clientId" to selectedAppointment!!.clientId,
                        "medicineName" to medicineName,
                        "dosage" to dosage,
                        "instructions" to instructions,
                        "timestamp" to System.currentTimeMillis()
                    )
                    
                    firestore.collection("prescriptions").document(prescriptionId)
                        .set(prescriptionData)
                        .addOnSuccessListener {
                            isSaving = false
                            Toast.makeText(context, "Prescription saved successfully", Toast.LENGTH_SHORT).show()
                            selectedAppointment = null
                            medicineName = ""
                            dosage = ""
                            instructions = ""
                        }
                        .addOnFailureListener { e ->
                            isSaving = false
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            )
        }
    }
}

@Composable
fun CompletedAppointmentItem(appointment: Appointment, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PremiumTeal)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Patient ID: ${appointment.clientId.takeLast(6)}", fontWeight = FontWeight.Bold)
                Text("Date: ${appointment.appointmentDate}", fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}

@Composable
fun PrescriptionForm(
    appointment: Appointment,
    medicineName: String,
    onMedicineChange: (String) -> Unit,
    dosage: String,
    onDosageChange: (String) -> Unit,
    instructions: String,
    onInstructionsChange: (String) -> Unit,
    isSaving: Boolean,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            "Patient: ${appointment.clientId.takeLast(6)}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PremiumTeal
        )
        Text(
            "Consultation Date: ${appointment.appointmentDate}",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = medicineName,
            onValueChange = onMedicineChange,
            label = { Text("Medicine Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = dosage,
            onValueChange = onDosageChange,
            label = { Text("Dosage (e.g. 1-0-1)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = instructions,
            onValueChange = onInstructionsChange,
            label = { Text("Special Instructions") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PremiumTeal),
            enabled = !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Submit Prescription", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun EmptyPrescribeView() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.NoteAdd,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "No Consultations Ready",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray
        )
        Text(
            "Only consultations that have ended will appear here for prescription. Complete a session to start prescribing.",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
