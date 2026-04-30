package com.example.mediconnect24x7

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class Screen {
    Home, Doctors, Records, Medicines, Symptoms, VideoCall, Login, Profile
}

data class ServiceItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val bgColor: Color,
    val iconColor: Color,
    val onClick: () -> Unit
)

data class Doctor(
    val name: String,
    val specialty: String,
    val rating: Double,
    val experience: Int,
    val status: String,
    val fee: Int,
    val availability: String,
    val initials: String
)

data class HealthRecord(
    val name: String,
    val age: Int,
    val visits: Int,
    val bloodGroup: String,
    val allergies: String?,
    val lastVisitDate: String,
    val lastVisitReason: String,
    val initials: String
)

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val age: String = "",
    val gender: String = "Male",
    val email: String = "",
    val phone: String = "",
    val profilePicUrl: String = ""
)

data class SymptomRecord(
    val id: String = "",
    val userId: String = "",
    val symptoms: String = "",
    val aiRecommendation: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
