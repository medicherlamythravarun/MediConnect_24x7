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


