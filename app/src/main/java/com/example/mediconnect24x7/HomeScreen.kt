package com.example.mediconnect24x7

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.mediconnect24x7.core.ServiceItem
import com.example.mediconnect24x7.ui.theme.EmergencyRed
import com.example.mediconnect24x7.ui.theme.GradientEnd
import com.example.mediconnect24x7.ui.theme.GradientStart
import com.example.mediconnect24x7.ui.theme.PremiumMint
import com.example.mediconnect24x7.ui.theme.PremiumTeal
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
    onNavigateToProfile: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onNavigateToAdminUsers: () -> Unit = {},
    onNavigateToAppConfig: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "MediConnect",
                        color = PremiumTeal,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        letterSpacing = 0.5.sp
                    )
                    if (userRole.lowercase() == "doctor") {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PremiumMint.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, PremiumTeal.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Verified, 
                                    contentDescription = null, 
                                    tint = PremiumTeal,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Doctor",
                                    color = PremiumTeal,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    } else if (userRole.lowercase() == "admin") {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEE2E2), // Light Red for admin badge
                            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Security, 
                                    contentDescription = null, 
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Admin",
                                    color = Color(0xFFEF4444),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateToProfile()
                    },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    if (profilePicUrl.isNotEmpty()) {
                        val model = if (profilePicUrl.startsWith("data:image")) {
                            try {
                                val base64String = profilePicUrl.substringAfter("base64,")
                                android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                            } catch(e: Exception) {
                                profilePicUrl
                            }
                        } else {
                            profilePicUrl
                        }
                        androidx.compose.foundation.Image(
                            painter = rememberAsyncImagePainter(model),
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
                animationSpec = tween(400)
            )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                HeroCard(onClick = onNavigateToAbout)
                HealthTipBanner()
                Text(
                    text = "MediConnect services at your fingertips",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1F2937),
                    fontSize = 20.sp
                )
                ServiceGrid(
                    userRole = userRole,
                    onNavigateToVideoCall = onNavigateToVideoCall,
                    onNavigateToDoctors = onNavigateToDoctors,
                    onNavigateToRecords = onNavigateToRecords,
                    onNavigateToMedicines = onNavigateToMedicines,
                    onNavigateToSymptoms = onNavigateToSymptoms,
                    onNavigateToAdminUsers = onNavigateToAdminUsers,
                    onNavigateToAppConfig = onNavigateToAppConfig
                )
                EmergencyCard()
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun HeroCard(onClick: () -> Unit = {}) {
    val haptic = LocalHapticFeedback.current
    val infiniteTransition = rememberInfiniteTransition(label = "hero_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 900,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse,
        ),

        label = "hero_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable { 
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick() 
            },
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    )
                )
                .padding(28.dp)
        ) {
            Column {
                Text(
                    "online medical support  \uD83E\uDD1D",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Your Health\nOur Priority",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 36.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Glass box
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
            Image(
                painter = painterResource(R.drawable.appnobg),
                contentDescription = "transparent logo",
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 30.dp, y = 25.dp)
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
    onNavigateToSymptoms: () -> Unit,
    onNavigateToAdminUsers: () -> Unit = {},
    onNavigateToAppConfig: () -> Unit = {}
) {
    val services = if (userRole.lowercase() == "doctor") {
        listOf(
            ServiceItem(
                "Appointments",
                "check",
                Icons.Default.EventNote,
                Color(0xFFEEF2FF),
                Color(0xFF4F46E5),
                onNavigateToDoctors
            ),
            ServiceItem(
                "Records",
                "History",
                Icons.Default.Description,
                Color(0xFFF0FDF4),
                Color(0xFF16A34A),
                onNavigateToRecords
            ),
            ServiceItem(
                "Prescriptions",
                "Create",
                Icons.Default.NoteAdd,
                Color(0xFFFFF7ED),
                Color(0xFFEA580C),
                onNavigateToMedicines
            ),
            ServiceItem(
                "Symptoms",
                "Checker",
                Icons.Default.Analytics,
                Color(0xFFFDF4FF),
                Color(0xFFC026D3),
                onNavigateToSymptoms
            )
        )
    } else if (userRole.lowercase() == "admin") {
        listOf(
            ServiceItem(
                "Users",
                "Manage all users",
                Icons.Default.People,
                Color(0xFFEEF2FF),
                Color(0xFF4F46E5),
                onNavigateToAdminUsers
            ),
            ServiceItem(
                "Analytics",
                "System stats",
                Icons.Default.Analytics,
                Color(0xFFF0FDF4),
                Color(0xFF16A34A),
                {}),
            ServiceItem(
                "Reports",
                "View reports",
                Icons.Default.Description,
                Color(0xFFFFF7ED),
                Color(0xFFEA580C),
                {}),
            ServiceItem(
                "Settings",
                "App config",
                Icons.Default.Settings,
                Color(0xFFFDF4FF),
                Color(0xFFC026D3),
                onNavigateToAppConfig)
        )
    } else {
        listOf(
            ServiceItem(
                "Consult",
                "Talk to doctor",
                Icons.Default.Videocam,
                Color(0xFFEEF2FF),
                Color(0xFF4F46E5),
                onNavigateToVideoCall
            ),
            ServiceItem(
                "Records",
                "Your history",
                Icons.Default.Description,
                Color(0xFFF0FDF4),
                Color(0xFF16A34A),
                onNavigateToRecords
            ),
            ServiceItem(
                "Pharmacy",
                "Buy medicine",
                Icons.Default.LocalPharmacy,
                Color(0xFFFFF7ED),
                Color(0xFFEA580C),
                onNavigateToMedicines
            ),
            ServiceItem(
                "Symptoms",
                "AI Checker",
                Icons.Default.Analytics,
                Color(0xFFFDF4FF),
                Color(0xFFC026D3),
                onNavigateToSymptoms
            )
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
    val haptic = LocalHapticFeedback.current
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
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    item.onClick()
                }
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White,
                            item.bgColor
                        )
                    )
                )
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = item.iconColor.copy(alpha = 0.04f),
                modifier = Modifier
                    .size(130.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 20.dp, y = 20.dp)
                    .graphicsLayer {
                        rotationZ = -15f
                    }
            )
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
}

@Composable
fun EmergencyCard() {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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

