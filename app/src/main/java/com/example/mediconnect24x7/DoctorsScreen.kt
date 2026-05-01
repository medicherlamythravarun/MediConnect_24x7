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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorConsultationScreen(onNavigateToVideoCall: (Doctor) -> Unit = {}) {
    val doctors = remember {
        listOf(
            Doctor("Dr. Anil Verma", "Cardiologist", 4.9, 18, "Online", 400, "Available now", "AV"),
            Doctor("Dr. Priya Mehta", "Gynecologist", 4.7, 9, "Offline", 300, "Available at 4 PM", "PM"),
            Doctor("Dr. Rakesh Singh", "Pediatrician", 4.6, 14, "Online", 250, "Available now", "RS"),
            Doctor("Dr. Anjali Gupta", "Dermatologist", 4.8, 11, "Offline", 350, "Available tomorrow", "AG"),
            Doctor("Dr. Suman Reddy", "Neurologist", 4.9, 20, "Online", 600, "Available now", "SR"),
            Doctor("Dr. Vikram Patel", "Orthopedic", 4.5, 12, "Offline", 450, "Available at 6 PM", "VP"),
            Doctor("Dr. Sneha Rao", "Psychiatrist", 4.8, 8, "Online", 500, "Available now", "SR")
        )
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
            StatisticsRow()
            doctors.forEach { doctor ->
                DoctorCard(doctor, onNavigateToVideoCall)
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
fun DoctorCard(doctor: Doctor, onNavigateToVideoCall: (Doctor) -> Unit = {}) {
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
                    onClick = { if (doctor.status == "Online") onNavigateToVideoCall(doctor) },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (doctor.status == "Online") PrimaryGreen else Color(0xFFE8F5E9),
                        contentColor = if (doctor.status == "Online") Color.White else PrimaryGreen
                    ),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Videocam, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (doctor.status == "Online") "Consult" else "Unavailable", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
