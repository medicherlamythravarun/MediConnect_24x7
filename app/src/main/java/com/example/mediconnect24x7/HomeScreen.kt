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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.absoluteValue
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
    val healthTips = listOf(
        "Health Tip: Eat green vegetables daily for better digestion",
        "Stay Hydrated: Drink at least 8 glasses of water every day",
        "Move More: A 30-minute walk can boost your heart health",
        "Sleep Well: Aim for 7-8 hours of quality sleep each night",
        "Mindfulness: Take 5 minutes daily for deep breathing",
        "Healthy Heart: Reduce salt intake to keep your blood pressure in check"
    )

    val pagerState = rememberPagerState(pageCount = { healthTips.size })

    // Auto-scroll logic
    LaunchedEffect(pagerState) {
        while (true) {
            delay(5000) // Slightly longer pause for readability
            if (!pagerState.isScrollInProgress) {
                val nextPage = (pagerState.currentPage + 1) % healthTips.size
                pagerState.animateScrollToPage(
                    page = nextPage,
                    animationSpec = tween(
                        durationMillis = 1000,
                        easing = LinearOutSlowInEasing
                    )
                )
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp), // More rounded for premium feel
        color = LightGreenBg.copy(alpha = 0.7f),
        border = null
    ) {
        Column {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 0.dp),
                pageSpacing = 0.dp
            ) { page ->
                // Calculate the absolute offset for this page to apply smooth transitions
                val pageOffset = (
                        (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                    ).absoluteValue

                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .graphicsLayer {
                            // Fade effect
                            alpha = 1f - pageOffset.coerceIn(0f, 1f)
                            // Subtle scale effect
                            val scale = 1f - (pageOffset * 0.05f).coerceIn(0f, 0.05f)
                            scaleX = scale
                            scaleY = scale
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = healthTips[page],
                        fontSize = 14.sp,
                        color = DarkGreen,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 20.sp
                    )
                }
            }
            
            // Page Indicators (Dots)
            Row(
                Modifier
                    .height(12.dp)
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(healthTips.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration) PrimaryGreen else PrimaryGreen.copy(alpha = 0.3f)
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(6.dp)
                    )
                }
            }
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
            ServiceItem("Symptom\nChecker", "Check your symptoms", Icons.Default.Analytics, Color(0xFFF3E5F5), Color(0xFF7B1FA2), onNavigateToSymptoms)
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
