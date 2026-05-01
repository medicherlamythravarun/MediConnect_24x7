package com.example.mediconnect24x7

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class Screen {
    Home, Doctors, Records, Medicines, Symptoms, VideoCall, Login, Profile, RoleSelection, DoctorDetails, CompleteProfile, Appointments, Prescribe
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
    val gender: String = "",
    val email: String = "",
    val phone: String = "",
    val profilePicUrl: String = "",
    val role: String = "" // "doctor", "client", "admin"
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

data class MedicineRecord(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val dosage: String = "",
    val timeOfDay: String = "Morning",
    val form: String = "Tablet",
    val inventoryCount: Int = 30,
    val isTakenToday: Boolean = false,
    val lastTakenTimestamp: Long = 0L
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
    val doctorId: String = "",
    val appointmentDate: String = "",
    val appointmentTime: String = "",
    val status: String = "Pending", // Pending, Confirmed, Cancelled, Completed
    val meetingId: String = ""
)

data class Availability(
    val availabilityId: String = "",
    val doctorId: String = "",
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val isAvailable: Boolean = true
)

data class AdminProfile(
    val adminId: String = "",
    val userId: String = ""
)
