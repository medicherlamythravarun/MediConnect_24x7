package com.example.mediconnect24x7

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

    Scaffold(
        bottomBar = {
            if (currentScreen != Screen.Login) {
                BottomNavigationBar(
                    currentScreen = currentScreen,
                    onScreenSelected = { currentScreen = it }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentScreen) {
                Screen.Login -> LoginScreen(
                    onLoginSuccess = { currentScreen = Screen.Home }
                )
                Screen.Home -> MediConnectHomeScreen(
                    onNavigateToVideoCall = { currentScreen = Screen.VideoCall },
                    onNavigateToDoctors = { currentScreen = Screen.Doctors },
                    onNavigateToRecords = { currentScreen = Screen.Records },
                    onNavigateToMedicines = { currentScreen = Screen.Medicines },
                    onNavigateToSymptoms = { currentScreen = Screen.Symptoms }
                )
                Screen.Doctors -> DoctorConsultationScreen(
                    onNavigateToVideoCall = { currentScreen = Screen.VideoCall }
                )
                Screen.VideoCall -> VideoCallScreen(
                    onEndCall = { currentScreen = Screen.Home }
                )
                Screen.Records -> RecordsScreen()
                Screen.Medicines -> MedicinesScreen()
                Screen.Symptoms -> SymptomsScreen()
            }
        }
    }
}

@Composable
fun BottomNavigationBar(currentScreen: Screen, onScreenSelected: (Screen) -> Unit) {
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
