package com.example.mediconnect24x7
//7382243069


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import coil.compose.rememberAsyncImagePainter
import com.example.mediconnect24x7.ui.theme.*
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainAppContent()
        }
    }
}

@Composable
fun MainAppContent() {
    val auth = FirebaseAuth.getInstance()
    var currentScreen by remember { 
        mutableStateOf(if (auth.currentUser != null) Screen.Home else Screen.Login) 
    }
    var selectedDoctor by remember { mutableStateOf<Doctor?>(null) }
    var selectedCallID by remember { mutableStateOf("") }
    var selectedAppointmentId by remember { mutableStateOf("") }
    var selectedClientName by remember { mutableStateOf("") }


    var userRole by remember { mutableStateOf("") }
    var userProfilePic by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var isRoleLoaded by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            if (isRoleLoaded && currentScreen != Screen.Login && currentScreen != Screen.RoleSelection && currentScreen != Screen.DoctorDetails && currentScreen != Screen.CompleteProfile) {
                BottomNavigationBar(
                    currentScreen = currentScreen,
                    userRole = userRole,
                    profilePicUrl = userProfilePic,
                    userName = userName,
                    onScreenSelected = { currentScreen = it }
                )
            }
        }
    ) { paddingValues ->
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        // Role Check logic
        LaunchedEffect(auth.currentUser, currentScreen) {
            val user = auth.currentUser
            if (user != null && (currentScreen == Screen.Home || currentScreen == Screen.Profile)) {
                firestore.collection("users").document(user.uid).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val role = document.getString("role") ?: ""
                            userRole = role
                            userProfilePic = document.getString("profilePicUrl") ?: ""
                            userName = document.getString("name") ?: ""
                            if (role.isEmpty()) {
                                currentScreen = Screen.RoleSelection
                            } else {
                                val name = document.getString("name")
                                if (name.isNullOrBlank()) {
                                    currentScreen = Screen.CompleteProfile
                                } else if (role == "doctor") {
                                    // Check if doctor profile is complete
                                    firestore.collection("doctors").document(user.uid).get()
                                        .addOnSuccessListener { doc ->
                                            if (!doc.exists()) {
                                                currentScreen = Screen.DoctorDetails
                                            }
                                        }
                                }
                            }
                            isRoleLoaded = true
                        } else {
                            // User document doesn't exist yet, must select role
                            userRole = ""
                            isRoleLoaded = true
                            currentScreen = Screen.RoleSelection
                        }
                    }
            } else if (user == null) {
                isRoleLoaded = true
            }
        }
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentScreen) {
                Screen.Login -> LoginScreen(
                    onLoginSuccess = { 
                        currentScreen = Screen.Home 
                    }
                )
                Screen.RoleSelection -> RoleSelectionScreen(
                    onRoleSelected = { role ->
                        if (role == "doctor") {
                            currentScreen = Screen.DoctorDetails
                        } else {
                            currentScreen = Screen.CompleteProfile
                        }
                    }
                )
                Screen.DoctorDetails -> DoctorDetailsScreen(
                    onComplete = { currentScreen = Screen.CompleteProfile },
                    onBack = { currentScreen = Screen.RoleSelection }
                )
                Screen.CompleteProfile -> CompleteProfileScreen(
                    onComplete = { currentScreen = Screen.Home }
                )
                Screen.Home -> MediConnectHomeScreen(
                    profilePicUrl = userProfilePic,
                    userName = userName,
                    userRole = userRole,
                    onNavigateToVideoCall = { currentScreen = if (userRole == "doctor") Screen.Appointments else Screen.Doctors },
                    onNavigateToDoctors = { currentScreen = if (userRole == "doctor") Screen.Appointments else Screen.Doctors },
                    onNavigateToRecords = { currentScreen = Screen.Records },
                    onNavigateToMedicines = { currentScreen = if (userRole == "doctor") Screen.Prescribe else Screen.Medicines },
                    onNavigateToSymptoms = { currentScreen = Screen.Symptoms },
                    onNavigateToProfile = { currentScreen = Screen.Profile }
                )
                Screen.Appointments -> AppointmentsScreen(
                    onJoinCall = { appointment ->
                        selectedCallID = appointment.meetingId
                        selectedAppointmentId = appointment.appointmentId
                        selectedClientName = appointment.clientName
                        selectedDoctor = null
                        currentScreen = Screen.VideoCall
                    }
                )

                Screen.Prescribe -> PrescribeScreen()
                Screen.Doctors -> DoctorConsultationScreen(
                    clientName = userName,
                    onNavigateToVideoCall = { doctor, callID, appointmentId ->
                        selectedDoctor = doctor
                        selectedCallID = callID
                        selectedAppointmentId = appointmentId
                        currentScreen = Screen.VideoCall 
                    }
                )
                Screen.VideoCall -> VideoCallScreen(
                    doctor = selectedDoctor,
                    clientName = selectedClientName,
                    userID = auth.currentUser?.uid ?: "user_${System.currentTimeMillis() % 10000}",
                    userName = if (userName.isNotEmpty()) userName else "User_${auth.currentUser?.uid?.take(4)}",
                    callID = selectedCallID,
                    appointmentId = selectedAppointmentId,
                    onEndCall = { 
                        selectedDoctor = null
                        selectedCallID = ""
                        selectedAppointmentId = ""
                        selectedClientName = ""
                        currentScreen = Screen.Home 
                    }
                )
                Screen.Records -> RecordsScreen()
                Screen.Medicines -> MedicinesScreen()
                Screen.Symptoms -> SymptomsScreen()
                Screen.Profile -> ProfileScreen(
                    onSignOut = {
                        auth.signOut()
                        currentScreen = Screen.Login
                    }
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    currentScreen: Screen, 
    userRole: String, 
    profilePicUrl: String,
    userName: String,
    onScreenSelected: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == Screen.Home,
            onClick = { onScreenSelected(Screen.Home) },
            icon = { Icon(if (currentScreen == Screen.Home) Icons.Default.Home else Icons.Outlined.Home, null) },
            label = { Text("Home", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryGreen,
                selectedTextColor = PrimaryGreen,
                unselectedIconColor = Color.LightGray,
                unselectedTextColor = Color.LightGray,
                indicatorColor = LightGreenBg
            )
        )
        
        if (userRole.lowercase() != "doctor") {
            NavigationBarItem(
                selected = currentScreen == Screen.Doctors,
                onClick = { onScreenSelected(Screen.Doctors) },
                icon = { Icon(if (currentScreen == Screen.Doctors) Icons.Default.MedicalServices else Icons.Outlined.MedicalServices, null) },
                label = { Text("Doctors", fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryGreen,
                    selectedTextColor = PrimaryGreen,
                    unselectedIconColor = Color.LightGray,
                    unselectedTextColor = Color.LightGray,
                    indicatorColor = LightGreenBg
                )
            )
        } else {
            NavigationBarItem(
                selected = currentScreen == Screen.Appointments,
                onClick = { onScreenSelected(Screen.Appointments) },
                icon = { Icon(if (currentScreen == Screen.Appointments) Icons.Default.EventNote else Icons.Outlined.EventNote, null) },
                label = { Text("Upcoming", fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryGreen,
                    selectedTextColor = PrimaryGreen,
                    unselectedIconColor = Color.LightGray,
                    unselectedTextColor = Color.LightGray,
                    indicatorColor = LightGreenBg
                )
            )
        }
        NavigationBarItem(
            selected = currentScreen == Screen.Records,
            onClick = { onScreenSelected(Screen.Records) },
            icon = { Icon(if (currentScreen == Screen.Records) Icons.Default.Folder else Icons.Outlined.Folder, null) },
            label = { Text("Records", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryGreen,
                selectedTextColor = PrimaryGreen,
                unselectedIconColor = Color.LightGray,
                unselectedTextColor = Color.LightGray,
                indicatorColor = LightGreenBg
            )
        )
        if (userRole.lowercase() != "doctor") {
            NavigationBarItem(
                selected = currentScreen == Screen.Medicines,
                onClick = { onScreenSelected(Screen.Medicines) },
                icon = { Icon(if (currentScreen == Screen.Medicines) Icons.Default.LocalPharmacy else Icons.Outlined.LocalPharmacy, null) },
                label = { Text("Medicines", fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryGreen,
                    selectedTextColor = PrimaryGreen,
                    unselectedIconColor = Color.LightGray,
                    unselectedTextColor = Color.LightGray,
                    indicatorColor = LightGreenBg
                )
            )
        } else {
            NavigationBarItem(
                selected = currentScreen == Screen.Prescribe,
                onClick = { onScreenSelected(Screen.Prescribe) },
                icon = { Icon(if (currentScreen == Screen.Prescribe) Icons.Default.NoteAdd else Icons.Outlined.NoteAdd, null) },
                label = { Text("Prescribe", fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryGreen,
                    selectedTextColor = PrimaryGreen,
                    unselectedIconColor = Color.LightGray,
                    unselectedTextColor = Color.LightGray,
                    indicatorColor = LightGreenBg
                )
            )
        }
        NavigationBarItem(
            selected = currentScreen == Screen.Symptoms,
            onClick = { onScreenSelected(Screen.Symptoms) },
            icon = { Icon(if (currentScreen == Screen.Symptoms) Icons.Default.MonitorHeart else Icons.Outlined.MonitorHeart, null) },
            label = { Text("Symptoms", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryGreen,
                selectedTextColor = PrimaryGreen,
                unselectedIconColor = Color.LightGray,
                unselectedTextColor = Color.LightGray,
                indicatorColor = LightGreenBg
            )
        )
    }
}
