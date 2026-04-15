package com.example.mediconnect24x7

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediconnect24x7.ui.theme.MediConnect24x7Theme

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
    var currentScreen by remember { mutableStateOf(Screen.Home) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentScreen = currentScreen,
                onScreenSelected = { currentScreen = it }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentScreen) {
                Screen.Home -> MediConnectHomeScreen(
                    onNavigateToVideoCall = { currentScreen = Screen.VideoCall },
                    onNavigateToDoctors = { currentScreen = Screen.Doctors },
                    onNavigateToRecords = { currentScreen = Screen.Records }
                )
                Screen.Doctors -> DoctorConsultationScreen(
                    onNavigateToVideoCall = { currentScreen = Screen.VideoCall }
                )
                Screen.VideoCall -> VideoCallScreen(
                    onEndCall = { currentScreen = Screen.Home }
                )
                Screen.Records -> RecordsScreen()
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Screen: ${currentScreen.name}")
                    }
                }
            }
        }
    }
}

enum class Screen {
    Home, Doctors, Records, Medicines, Symptoms, VideoCall
}

val PrimaryGreen = Color(0xFF2E7D32)
val DarkGreen = Color(0xFF1B5E20)
val LightGreenBg = Color(0xFFE8F5E9)
val EmergencyRed = Color(0xFFC62828)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediConnectHomeScreen(
    onNavigateToVideoCall: () -> Unit = {},
    onNavigateToDoctors: () -> Unit = {},
    onNavigateToRecords: () -> Unit = {}
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
            ServiceGrid(onNavigateToVideoCall, onNavigateToDoctors, onNavigateToRecords)
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
    onNavigateToVideoCall: () -> Unit = {},
    onNavigateToDoctors: () -> Unit = {},
    onNavigateToRecords: () -> Unit = {}
) {
    val services = listOf(
        ServiceItem("Video\nConsultation", "Talk to a doctor now", Icons.Default.Videocam, Color(0xFFE3F2FD), Color(0xFF1976D2), onNavigateToVideoCall),
        ServiceItem("Health Records", "Your medical history", Icons.Default.Description, Color(0xFFE8F5E9), Color(0xFF388E3C), onNavigateToRecords),
        ServiceItem("Medicine\nAvailability", "Find nearby pharmacies", Icons.Default.LocalPharmacy, Color(0xFFFFF3E0), Color(0xFFF57C00), {}),
        ServiceItem("Symptom\nChecker", "Check your symptoms", Icons.Default.Analytics, Color(0xFFF3E5F5), Color(0xFF7B1FA2), {})
    )

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
    Card(
        modifier = Modifier.fillMaxWidth(),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorConsultationScreen(onNavigateToVideoCall: () -> Unit = {}) {
    val doctors = remember {
        listOf(
            Doctor("Dr. Anil Verma", "Cardiologist", 4.9, 18, "Online", 400, "Available now", "AV"),
            Doctor("Dr. Priya Mehta", "Gynecologist", 4.7, 9, "Offline", 300, "Available at 4 PM", "PM"),
            Doctor("Dr. Rakesh Singh", "Pediatrician", 4.6, 14, "Online", 250, "Available now", "RS"),
            Doctor("Dr. Anjali Gupta", "Dermatologist", 4.8, 11, "Offline", 350, "Available tomorrow", "AG")
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
fun DoctorCard(doctor: Doctor, onNavigateToVideoCall: () -> Unit = {}) {
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {

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
                    Column {
                        Text(doctor.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                        Text(doctor.specialty, color = Color.Gray, fontSize = 14.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                            Text(" ${doctor.rating} · ${doctor.experience} yrs exp", fontSize = 13.sp, color = Color.Gray)
                        }
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

            Divider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.4f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Consultation Fee", fontSize = 11.sp, color = Color.Gray)
                    Text("₹${doctor.fee}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.Black)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, modifier = Modifier.size(16.dp), tint = PrimaryGreen)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(doctor.availability, fontSize = 12.sp, color = PrimaryGreen, fontWeight = FontWeight.Medium)
                }

                Button(
                    onClick = { if (doctor.status == "Online") onNavigateToVideoCall() },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (doctor.status == "Online") PrimaryGreen else Color(0xFFE8F5E9),
                        contentColor = if (doctor.status == "Online") Color.White else PrimaryGreen
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Icon(Icons.Default.Videocam, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (doctor.status == "Online") "Consult" else "Unavailable", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun VideoCallScreen(onEndCall: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(DarkGreen.copy(alpha = 0.8f), Color.Black)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        null,
                        modifier = Modifier.size(80.dp),
                        tint = Color.White.copy(alpha = 0.5f)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Dr. Anil Verma",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "04:20",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 16.sp
                )
            }
        }

        Card(
            modifier = Modifier
                .padding(24.dp)
                .size(100.dp, 150.dp)
                .align(Alignment.TopEnd)
                .padding(top = 40.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Gray.copy(alpha = 0.3f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Face, null, tint = Color.White.copy(alpha = 0.5f))
            }
        }

        Surface(
            modifier = Modifier
                .padding(bottom = 48.dp)
                .width(280.dp)
                .height(80.dp)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(40.dp),
            color = Color.White.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Mic, null, tint = Color.White)
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Videocam, null, tint = Color.White)
                }
                FloatingActionButton(
                    onClick = onEndCall,
                    containerColor = EmergencyRed,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Default.CallEnd, null)
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Chat, null, tint = Color.White)
                }
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
