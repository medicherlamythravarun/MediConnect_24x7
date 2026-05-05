package com.example.mediconnect24x7

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.mediconnect24x7.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

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
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7F6)) // Modern light background
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        TopAppBar(
            title = {
                Text(
                    "MediConnect",
                    color = PremiumTeal,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    letterSpacing = 0.5.sp
                )
            },
            actions = {
                IconButton(
                    onClick = onNavigateToProfile,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    if (profilePicUrl.isNotEmpty()) {
                        androidx.compose.foundation.Image(
                            painter = rememberAsyncImagePainter(profilePicUrl),
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else if (userName.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(PremiumTeal, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                userName.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            tint = PremiumTeal,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.White.copy(alpha = 0.9f)
            )
        )
        
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(600)) + slideInVertically(
                initialOffsetY = { 50 },
                animationSpec = tween(600)
            )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                HeroCard()
                HealthTipBanner()
                Text(
                    text = "How can we help today?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1F2937),
                    fontSize = 22.sp
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
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun HeroCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "hero_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    )
                )
                .padding(28.dp)
        ) {
            Column {
                Text(
                    "Hello there \uD83D\uDC4B",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Your Health, \nOur Priority",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 36.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Glassmorphic chip
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp),
                    border = null
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Trusted 24x7 Care",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.15f),
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 10.dp, y = 10.dp)
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
        "Sleep Well: Aim for 7-8 hours of quality sleep each night"
    )

    val pagerState = rememberPagerState(pageCount = { healthTips.size })

    LaunchedEffect(pagerState) {
        while (true) {
            delay(4000)
            if (!pagerState.isScrollInProgress) {
                val nextPage = (pagerState.currentPage + 1) % healthTips.size
                pagerState.animateScrollToPage(
                    page = nextPage,
                    animationSpec = tween(800, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 0.dp),
                pageSpacing = 0.dp
            ) { page ->
                val pageOffset = (
                    (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                ).absoluteValue

                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = 1f - pageOffset.coerceIn(0f, 1f)
                            translationX = pageOffset * 50f
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(PremiumMint.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = PremiumTeal,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = healthTips[page],
                        fontSize = 15.sp,
                        color = Color(0xFF374151),
                        fontWeight = FontWeight.Medium,
                        lineHeight = 22.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(healthTips.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration) PremiumTeal else Color(0xFFE5E7EB)
                    val width by animateDpAsState(if (pagerState.currentPage == iteration) 20.dp else 8.dp, label = "dot_width")
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }
        }
    }
}

@Composable
fun ServiceGrid(
    userRole: String,
    onNavigateToVideoCall: () -> Unit,
    onNavigateToDoctors: () -> Unit,
    onNavigateToRecords: () -> Unit,
    onNavigateToMedicines: () -> Unit,
    onNavigateToSymptoms: () -> Unit
) {
    val services = if (userRole.lowercase() == "doctor") {
        listOf(
            ServiceItem("Appointments", "Schedule", Icons.Default.EventNote, Color(0xFFEEF2FF), Color(0xFF4F46E5), onNavigateToDoctors),
            ServiceItem("Records", "History", Icons.Default.Description, Color(0xFFF0FDF4), Color(0xFF16A34A), onNavigateToRecords),
            ServiceItem("Prescriptions", "Create", Icons.Default.NoteAdd, Color(0xFFFFF7ED), Color(0xFFEA580C), onNavigateToMedicines),
            ServiceItem("Symptoms", "Checker", Icons.Default.Analytics, Color(0xFFFDF4FF), Color(0xFFC026D3), onNavigateToSymptoms)
        )
    } else {
        listOf(
            ServiceItem("Consult", "Talk to doctor", Icons.Default.Videocam, Color(0xFFEEF2FF), Color(0xFF4F46E5), onNavigateToVideoCall),
            ServiceItem("Records", "Your history", Icons.Default.Description, Color(0xFFF0FDF4), Color(0xFF16A34A), onNavigateToRecords),
            ServiceItem("Pharmacy", "Buy medicine", Icons.Default.LocalPharmacy, Color(0xFFFFF7ED), Color(0xFFEA580C), onNavigateToMedicines),
            ServiceItem("Symptoms", "AI Checker", Icons.Default.Analytics, Color(0xFFFDF4FF), Color(0xFFC026D3), onNavigateToSymptoms)
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "card_scale")
    val elevation by animateDpAsState(if (isPressed) 2.dp else 6.dp, label = "card_elevation")

    Card(
        modifier = modifier
            .height(170.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = item.onClick
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(item.bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.iconColor,
                    modifier = Modifier.size(30.dp)
                )
            }
            Column {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF1F2937)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.subtitle,
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun EmergencyCard() {
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:104"))
                context.startActivity(intent)
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(20.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(EmergencyRed.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = EmergencyRed.copy(alpha = alpha),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Emergency Help",
                    color = Color(0xFF1F2937),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    "Call 104 immediately",
                    color = Color(0xFF6B7280),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = Color(0xFFD1D5DB),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

