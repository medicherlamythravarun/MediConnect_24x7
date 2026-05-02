package com.example.mediconnect24x7

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun MediConnectHomeScreen(
    profilePicUrl: String = "",
    userName: String = "",
    userRole: String = "client",
    onNavigateToVideoCall: () -> Unit = {},
    onNavigateToDoctors: () -> Unit = {},
    onNavigateToRecords: () -> Unit = {},
    onNavigateToMedicines: () -> Unit = {},
    onNavigateToSymptoms: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        TopAppBar(
            title = {
                Text(
                    "MediConnect 24x7",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            actions = {
                IconButton(
                    onClick = onNavigateToProfile,
                    modifier = Modifier
                        .padding(end = 8.dp)
                ) {
                    if (profilePicUrl.isNotEmpty()) {
                        androidx.compose.foundation.Image(
                            painter = coil.compose.rememberAsyncImagePainter(profilePicUrl),
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else if (userName.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                userName.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = DarkGreen
            )
        )
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            HeroCard()
            HealthTipBanner()
            Text(
                text = "What do you need?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            ServiceGrid(
                userRole = userRole,
                onNavigateToVideoCall = onNavigateToVideoCall,
                onNavigateToDoctors = onNavigateToDoctors,
                onNavigateToRecords = onNavigateToRecords,
                onNavigateToMedicines = onNavigateToMedicines,
                onNavigateToSymptoms = onNavigateToSymptoms
            )
            EmergencyCard()
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun HeroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryGreen)
    ) {
        Box(modifier = Modifier.padding(24.dp)) {
            Column {
                Text(
                    "Namaste!",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp
                )
                Text(
                    "MediConnect 24x7",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Your trusted health companion",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
            }
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.2f),
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.TopEnd)
            )
        }
    }
}

@Composable
fun HealthTipBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = LightGreenBg,
        border = null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Health Tip: Eat green vegetables daily",
                fontSize = 14.sp,
                color = DarkGreen,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ServiceGrid(
    userRole: String,
    onNavigateToVideoCall: () -> Unit = {},
    onNavigateToDoctors: () -> Unit = {},
    onNavigateToRecords: () -> Unit = {},
    onNavigateToMedicines: () -> Unit = {},
    onNavigateToSymptoms: () -> Unit = {}
) {
    val services = if (userRole.lowercase() == "doctor") {
        listOf(
            ServiceItem("My\nAppointments", "Manage your schedule", Icons.Default.EventNote, Color(0xFFE3F2FD), Color(0xFF1976D2), onNavigateToDoctors),
            ServiceItem("Patient\nRecords", "View medical history", Icons.Default.Description, Color(0xFFE8F5E9), Color(0xFF388E3C), onNavigateToRecords),
            ServiceItem("Write\nPrescriptions", "Create new prescriptions", Icons.Default.NoteAdd, Color(0xFFFFF3E0), Color(0xFFF57C00), onNavigateToMedicines),
            ServiceItem("Hospital\nUpdates", "Announcements & news", Icons.Default.Campaign, Color(0xFFF3E5F5), Color(0xFF7B1FA2), onNavigateToSymptoms)
        )
    } else {
        listOf(
            ServiceItem("Video\nConsultation", "Talk to a doctor now", Icons.Default.Videocam, Color(0xFFE3F2FD), Color(0xFF1976D2), onNavigateToVideoCall),
            ServiceItem("Health Records", "Your medical history", Icons.Default.Description, Color(0xFFE8F5E9), Color(0xFF388E3C), onNavigateToRecords),
            ServiceItem("Medicine\nAvailability", "Find nearby pharmacies", Icons.Default.LocalPharmacy, Color(0xFFFFF3E0), Color(0xFFF57C00), onNavigateToMedicines),
            ServiceItem("Symptom\nChecker", "Check your symptoms", Icons.Default.Analytics, Color(0xFFF3E5F5), Color(0xFF7B1FA2), onNavigateToSymptoms)
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ServiceCard(services[0], Modifier.weight(1f))
            ServiceCard(services[1], Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ServiceCard(services[2], Modifier.weight(1f))
            ServiceCard(services[3], Modifier.weight(1f))
        }
    }
}

@Composable
fun ServiceCard(item: ServiceItem, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { item.onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(item.bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            Column {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.subtitle,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun EmergencyCard() {
    val context = androidx.compose.ui.platform.LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:104"))
                context.startActivity(intent)
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = EmergencyRed)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Emergency Helpline",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    "Call 104 for free medical helpline",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}
