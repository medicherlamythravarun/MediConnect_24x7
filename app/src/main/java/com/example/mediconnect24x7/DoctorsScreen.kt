package com.example.mediconnect24x7

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediconnect24x7.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorConsultationScreen(clientName: String, onNavigateToVideoCall: (Doctor, String, String) -> Unit = { _, _, _ -> }) {


    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var doctors by remember { mutableStateOf<List<Doctor>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val firestore = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        firestore.collection("doctors").get()
            .addOnSuccessListener { querySnapshot ->
                val doctorProfiles = querySnapshot.toObjects(DoctorProfile::class.java)
                
                // Fetch user names for these doctors
                val doctorList = mutableListOf<Doctor>()
                var processedCount = 0
                
                if (doctorProfiles.isEmpty()) {
                    isLoading = false
                    return@addOnSuccessListener
                }

                doctorProfiles.forEach { profile ->
                    firestore.collection("users").document(profile.userId).get()
                        .addOnSuccessListener { userDoc ->
                            val name = userDoc.getString("name") ?: "Unknown Doctor"
                            doctorList.add(Doctor(
                                uid = profile.userId,
                                name = "Dr. $name",
                                specialty = profile.specialization,
                                rating = 4.8, // Default rating for now
                                experience = profile.experience,
                                status = if (profile.isAvailable) "Online" else "Offline",
                                fee = profile.consultationFee.toInt(),
                                availability = if (profile.isAvailable) "Available now" else "Available later",
                                initials = name.take(2).uppercase()
                            ))
                            processedCount++
                            if (processedCount == doctorProfiles.size) {
                                doctors = doctorList
                                isLoading = false
                            }
                        }
                        .addOnFailureListener {
                            processedCount++
                            if (processedCount == doctorProfiles.size) {
                                doctors = doctorList
                                isLoading = false
                            }
                        }
                }
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        TopAppBar(
            title = {
                Text(
                    "Video Consultation",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkGreen)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FilterChipBar()
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            } else if (doctors.isEmpty()) {
                EmptyDoctorsView()
            } else {
                doctors.forEach { doctor ->
                    DoctorCard(doctor, clientName, onNavigateToVideoCall)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun FilterChipBar() {
    val filters = listOf("All", "Available", "General", "Specialist")
    var selectedFilter by remember { mutableStateOf("Specialist") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        filters.forEach { filter ->
            FilterChip(
                selected = filter == selectedFilter,
                onClick = { selectedFilter = filter },
                label = {
                    Text(
                        filter,
                        fontWeight = FontWeight.Bold,
                        color = if (filter == selectedFilter) Color.White else PrimaryGreen
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryGreen,
                    containerColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = if (filter == selectedFilter) PrimaryGreen else Color.LightGray.copy(alpha = 0.5f),
                    enabled = true,
                    selected = filter == selectedFilter,
                    borderWidth = 1.dp
                )
            )
        }
    }
}

@Composable
fun StatisticsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard("4", "Available", Color(0xFFE8F5E9), PrimaryGreen, Modifier.weight(1f))
        StatCard("6", "Total Doctors", Color(0xFFE3F2FD), Color(0xFF1976D2), Modifier.weight(1f))
        StatCard("~5 min", "Avg Wait", Color(0xFFFFF3E0), Color(0xFFF57C00), Modifier.weight(1f))
    }
}

@Composable
fun StatCard(value: String, label: String, bgColor: Color, textColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = null
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
            Text(label, fontSize = 10.sp, color = textColor.copy(alpha = 0.7f), fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun DoctorCard(doctor: Doctor, clientName: String, onNavigateToVideoCall: (Doctor, String, String) -> Unit = { _, _, _ -> }) {


    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    var isBooking by remember { mutableStateOf(false) }


    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(LightGreenBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = doctor.initials,
                        color = PrimaryGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(doctor.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                    Text(doctor.specialty, color = Color.Gray, fontSize = 14.sp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                        Text(" ${doctor.rating} · ${doctor.experience} yrs exp", fontSize = 13.sp, color = Color.Gray)
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(Icons.Default.Schedule, null, modifier = Modifier.size(14.dp), tint = PrimaryGreen)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(doctor.availability, fontSize = 12.sp, color = PrimaryGreen, fontWeight = FontWeight.Medium)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (doctor.status == "Online") PrimaryGreen else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(doctor.status, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.4f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Consultation Fee", fontSize = 11.sp, color = Color.Gray)
                    Text("₹${doctor.fee}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.Black)
                }

                Button(
                    onClick = { 
                        if (doctor.status == "Online" && currentUser != null && !isBooking) {
                            isBooking = true
                            val appointmentId = UUID.randomUUID().toString()
                            val meetingId = "call_${currentUser.uid.take(5)}_${doctor.uid.take(5)}_${System.currentTimeMillis() % 10000}"
                            
                            val appointment = Appointment(
                                appointmentId = appointmentId,
                                clientId = currentUser.uid,
                                doctorId = doctor.uid,
                                appointmentDate = "Today",
                                appointmentTime = "Now",
                                status = "Ongoing",
                                meetingId = meetingId
                            )
                            
                            val appointmentData = hashMapOf(
                                "appointmentId" to appointmentId,
                                "clientId" to currentUser.uid,
                                "clientName" to clientName,
                                "doctorId" to doctor.uid,
                                "participants" to listOf(currentUser.uid, doctor.uid),
                                "appointmentDate" to "Today",
                                "appointmentTime" to "Now",
                                "status" to "Ongoing",
                                "meetingId" to meetingId,
                                "timestamp" to System.currentTimeMillis()
                            )
                            
                            firestore.collection("appointments").document(appointmentId)
                                .set(appointmentData)
                                .addOnSuccessListener {
                                    isBooking = false
                                    onNavigateToVideoCall(doctor, meetingId, appointmentId)
                                }
                                .addOnFailureListener {
                                    isBooking = false
                                }
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (doctor.status == "Online") PrimaryGreen else Color(0xFFE8F5E9),
                        contentColor = if (doctor.status == "Online") Color.White else PrimaryGreen
                    ),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    if (isBooking) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Videocam, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (doctor.status == "Online") "Consult" else "Unavailable", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyDoctorsView() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No Doctors Found",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray
        )
        Text(
            "There are currently no doctors available in your area or matching your criteria.",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 8.dp)
        )
    }
}
