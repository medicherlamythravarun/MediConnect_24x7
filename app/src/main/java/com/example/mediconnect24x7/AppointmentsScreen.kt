package com.example.mediconnect24x7

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediconnect24x7.ui.theme.PremiumTeal
import com.example.mediconnect24x7.ui.theme.PremiumMint
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsScreen(showTopBar: Boolean = true, onJoinCall: (Appointment) -> Unit = {}) {

    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    
    var selectedTabIndex by remember { mutableStateOf(0) }
    var upcomingAppointments by remember { mutableStateOf<List<Appointment>>(emptyList()) }
    var historyAppointments by remember { mutableStateOf<List<Appointment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    DisposableEffect(currentUser?.uid) {
        var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
        if (currentUser != null) {
            listenerRegistration = firestore.collection("appointments")
                .whereArrayContains("participants", currentUser.uid)
                .addSnapshotListener { querySnapshot, error ->
                    if (error != null) {
                        isLoading = false
                        return@addSnapshotListener
                    }
                    if (querySnapshot != null) {
                        val allAppointments = querySnapshot.toObjects(Appointment::class.java).sortedByDescending { it.timestamp }
                        upcomingAppointments = allAppointments.filter { it.status != "Completed" && it.status != "Cancelled" }
                        historyAppointments = allAppointments.filter { it.status == "Completed" || it.status == "Cancelled" }
                    }
                    isLoading = false
                }
        } else {
            isLoading = false
        }
        
        onDispose {
            listenerRegistration?.remove()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7F6))
    ) {
        if (showTopBar) {
            TopAppBar(
                title = { Text("My Appointments", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = PremiumTeal
                )
            )
        }

        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.White,
            contentColor = PremiumTeal,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = PremiumTeal
                )
            }
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("Upcoming") }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("History") }
            )
        }

        val currentList = if (selectedTabIndex == 0) upcomingAppointments else historyAppointments

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PremiumTeal)
            }
        } else if (currentList.isEmpty()) {
            EmptyAppointmentsView(selectedTabIndex == 1)
        } else {
            LazyColumn(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(currentList) { appointment ->
                    AppointmentCard(appointment, onJoinCall)
                }
            }
        }
    }
}

@Composable
fun AppointmentCard(appointment: Appointment, onJoinCall: (Appointment) -> Unit = {}) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = PremiumMint.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = PremiumTeal)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(appointment.clientName.ifEmpty { "Patient" }, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(appointment.status, color = if (appointment.status == "Confirmed" || appointment.status == "Ongoing") PremiumTeal else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(appointment.appointmentDate, fontSize = 14.sp, color = Color.DarkGray)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (appointment.status == "Completed" && appointment.callEndTime.isNotEmpty()) "Ended: ${appointment.callEndTime}" else appointment.appointmentTime, fontSize = 14.sp, color = Color.DarkGray)
                }
            }

            if (appointment.status == "Ongoing") {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onJoinCall(appointment) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PremiumTeal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Join Call Now")
                }
            }
        }
    }
}

@Composable
fun EmptyAppointmentsView(isHistory: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.EventAvailable,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            if (isHistory) "No Consultation History" else "No Appointments Yet",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray
        )
        Text(
            if (isHistory) "Your past consultations will appear here." else "You don't have any scheduled appointments at the moment. New requests will appear here.",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
