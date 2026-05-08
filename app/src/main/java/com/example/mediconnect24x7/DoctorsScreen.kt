package com.example.mediconnect24x7

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediconnect24x7.core.Appointment
import com.example.mediconnect24x7.core.Doctor
import com.example.mediconnect24x7.core.DoctorProfile
import com.example.mediconnect24x7.ui.theme.PremiumMint
import com.example.mediconnect24x7.ui.theme.PremiumTeal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorConsultationScreen(
    clientName: String,
    onNavigateToVideoCall: (Doctor, String, String) -> Unit = { _, _, _ -> },
    onJoinAppointment: (Appointment) -> Unit = {}
) {


    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var doctors by remember { mutableStateOf<List<Doctor>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var mainTab by remember { mutableStateOf(0) }
    val firestore = FirebaseFirestore.getInstance()

    val filteredDoctors = remember(doctors, searchQuery, selectedFilter) {
        doctors.filter { doctor ->
            val matchesSearch = doctor.name.contains(searchQuery, ignoreCase = true) || 
                              doctor.specialty.contains(searchQuery, ignoreCase = true)
            
            val matchesFilter = when (selectedFilter) {
                "All" -> true
                "Available" -> doctor.status == "Online"
                "General" -> doctor.specialty.contains("General", ignoreCase = true)
                "Specialist" -> !doctor.specialty.contains("General", ignoreCase = true)
                else -> true
            }
            
            matchesSearch && matchesFilter
        }
    }

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
                            doctorList.add(
                                Doctor(
                                    uid = profile.userId,
                                    name = "Dr. $name",
                                    specialty = profile.specialization,
                                    rating = 4.8, // Default rating for now
                                    experience = profile.experience,
                                    status = if (profile.isAvailable) "Online" else "Offline",
                                    fee = profile.consultationFee.toInt(),
                                    availability = if (profile.isAvailable) "Available now" else "Available later",
                                    initials = name.take(2).uppercase()
                                )
                            )
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
            .background(Color(0xFFF4F7F6))
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
            colors = TopAppBarDefaults.topAppBarColors(containerColor = PremiumTeal)
        )

        TabRow(
            selectedTabIndex = mainTab,
            containerColor = Color.White,
            contentColor = PremiumTeal,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[mainTab]),
                    color = PremiumTeal
                )
            }
        ) {
            Tab(selected = mainTab == 0, onClick = { mainTab = 0 }, text = { Text("Find Doctors") })
            Tab(selected = mainTab == 1, onClick = { mainTab = 1 }, text = { Text("My Consultations") })
        }

        if (mainTab == 0) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Search Engine Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    placeholder = { Text("Search by name or specialty...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PremiumTeal) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search", tint = Color.Gray)
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PremiumTeal,
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true
                )

                FilterChipBar(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it }
                )

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PremiumTeal)
                    }
                } else if (filteredDoctors.isEmpty()) {
                    EmptyDoctorsView()
                } else {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        filteredDoctors.forEach { doctor ->
                            DoctorCard(doctor, clientName, onNavigateToVideoCall)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        } else {
            AppointmentsScreen(showTopBar = false, onJoinCall = onJoinAppointment)
        }
    }
}

@Composable
fun FilterChipBar(selectedFilter: String, onFilterSelected: (String) -> Unit) {
    val filters = listOf("All", "Available", "General", "Specialist")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        filters.forEach { filter ->
            FilterChip(
                selected = filter == selectedFilter,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        filter,
                        fontWeight = FontWeight.Bold,
                        color = if (filter == selectedFilter) Color.White else PremiumTeal
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PremiumTeal,
                    containerColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = if (filter == selectedFilter) PremiumTeal else Color.LightGray.copy(alpha = 0.5f),
                    enabled = true,
                    selected = filter == selectedFilter,
                    borderWidth = 1.dp
                )
            )
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
                        .background(PremiumMint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = doctor.initials,
                        color = PremiumTeal,
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
                        Icon(Icons.Default.Schedule, null, modifier = Modifier.size(14.dp), tint = PremiumTeal)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(doctor.availability, fontSize = 12.sp, color = PremiumTeal, fontWeight = FontWeight.Medium)
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
                                .background(if (doctor.status == "Online") PremiumTeal else Color.Gray)
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
                            val meetingId = doctor.uid // Use doctor's own ID as the permanent call room
                            
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
                        containerColor = if (doctor.status == "Online") PremiumTeal else Color(0xFFE8F5E9),
                        contentColor = if (doctor.status == "Online") Color.White else PremiumTeal
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
