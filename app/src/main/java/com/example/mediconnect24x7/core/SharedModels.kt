package com.example.mediconnect24x7.core

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class Screen {
    Home, Doctors, Records, Medicines, Symptoms, VideoCall,
    Login, Profile, RoleSelection, DoctorDetails, CompleteProfile,
    Appointments, Prescribe, About, AdminUsers, Settings, AppConfig, Splash
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
    val uid: String = "",
    val name: String,
    val specialty: String,
    val rating: Double,
    val experience: Int,
    val status: String,
    val fee: Int,
    val availability: String,
    val initials: String
)



data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val age: String = "",
    val gender: String = "",
    val email: String = "",
    val phone: String = "",
    val profilePicUrl: String = "",
    val role: String = "", // "doctor", "client", "admin"
    val createdAt: Long = System.currentTimeMillis()
)

data class SymptomRecord(
    val id: String = "",
    val userId: String = "",
    val symptoms: String = "",
    val aiRecommendation: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class ReportRecord(
    val id: String = "",
    val userId: String = "",
    val fileName: String = "",
    val fileUrl: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
data class DoctorProfile(
    val doctorId: String = "",
    val userId: String = "",
    val specialization: String = "",
    val experience: Int = 0,
    val consultationFee: Double = 0.0,
    val isAvailable: Boolean = true
)

data class Appointment(
    val appointmentId: String = "",
    val clientId: String = "",
    val clientName: String = "",
    val doctorId: String = "",
    val doctorName: String = "",
    val participants: List<String> = emptyList(),
    val appointmentDate: String = "",
    val appointmentTime: String = "",
    val status: String = "Pending", // Pending, Confirmed, Cancelled, Completed
    val meetingId: String = "",
    val callEndTime: String = "",
    val callEndDate: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class AdminProfile(
    val adminId: String = "",
    val userId: String = ""
)

data class Prescription(
    val prescriptionId: String = "",
    val appointmentId: String = "",
    val doctorId: String = "",
    val clientId: String = "",
    val clientName: String = "",
    val doctorName: String = "",
    val medicineName: String = "",
    val dosage: String = "",
    val instructions: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
