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
import com.example.mediconnect24x7.core.Appointment
import com.example.mediconnect24x7.core.Prescription
import com.example.mediconnect24x7.ui.theme.PremiumTeal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    
    // History state
    var pastPrescriptions by remember { mutableStateOf<List<Prescription>>(emptyList()) }
    var selectedTab by remember { mutableStateOf(0) }
    
    // Prescription form state
    var medicineName by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var doctorName by remember { mutableStateOf("") }

    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { doc ->
                    doctorName = doc.getString("name") ?: ""
                }
        }
    }

    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            firestore.collection("appointments")
                .whereEqualTo("doctorId", currentUser.uid)
                .whereEqualTo("status", "Completed")
                .addSnapshotListener { querySnapshot, _ ->
                    if (querySnapshot != null) {
                        completedAppointments = querySnapshot.toObjects(Appointment::class.java)
                    }
                    isLoading = false
                }
                
            firestore.collection("prescriptions")
                .whereEqualTo("doctorId", currentUser.uid)
                .addSnapshotListener { querySnapshot, _ ->
                    if (querySnapshot != null) {
                        pastPrescriptions = querySnapshot.toObjects(Prescription::class.java)
                            .sortedByDescending { it.timestamp }
                    }
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
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = PremiumTeal
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("Patients", modifier = Modifier.padding(16.dp), fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal, color = if (selectedTab == 0) PremiumTeal else Color.Gray)
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("History", modifier = Modifier.padding(16.dp), fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal, color = if (selectedTab == 1) PremiumTeal else Color.Gray)
                }
            }
            
            if (selectedTab == 0) {
                if (completedAppointments.isEmpty()) {
                    EmptyPrescribeView("No Consultations Ready", "Only consultations that have ended will appear here.")
                } else {
                    LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(completedAppointments) { appointment ->
                            CompletedAppointmentItem(appointment) {
                                selectedAppointment = appointment
                            }
                        }
                    }
                }
            } else {
                if (pastPrescriptions.isEmpty()) {
                    EmptyPrescribeView("No History", "You haven't prescribed any medicines yet.")
                } else {
                    LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(pastPrescriptions) { prescription ->
                            DoctorPrescriptionCard(prescription)
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
                        "doctorName" to (if (doctorName.startsWith("Dr. ")) doctorName else "Dr. $doctorName"),
                        "clientId" to selectedAppointment!!.clientId,
                        "clientName" to selectedAppointment!!.clientName,
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
                Text(appointment.clientName.ifEmpty { "Patient" }, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("ID: ${appointment.clientId.takeLast(6)} · ${if (appointment.callEndDate.isNotEmpty()) appointment.callEndDate else appointment.appointmentDate}", fontSize = 12.sp, color = Color.Gray)
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
            "Patient: ${appointment.clientName.ifEmpty { "N/A" }}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = PremiumTeal
        )
        Text(
            "Patient ID: ${appointment.clientId.takeLast(6)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray
        )
        Text(
            "Consultation Date: ${if (appointment.callEndDate.isNotEmpty()) appointment.callEndDate else appointment.appointmentDate}",
            fontSize = 13.sp,
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
fun DoctorPrescriptionCard(prescription: Prescription) {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val dateString = sdf.format(Date(prescription.timestamp))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MedicalServices, contentDescription = null, tint = PremiumTeal)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(prescription.clientName.ifEmpty { "Patient" }, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Medicine: ${prescription.medicineName}", fontSize = 14.sp, color = Color.DarkGray)
                    Text("Dosage: ${prescription.dosage}", fontSize = 12.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(dateString, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun EmptyPrescribeView(title: String = "No Consultations Ready", subtitle: String = "Only consultations that have ended will appear here for prescription. Complete a session to start prescribing.") {
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
            title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray
        )
        Text(
            subtitle,
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
