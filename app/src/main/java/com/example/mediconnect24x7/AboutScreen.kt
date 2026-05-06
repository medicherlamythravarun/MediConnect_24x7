package com.example.mediconnect24x7

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Troubleshoot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediconnect24x7.ui.theme.PremiumMint
import com.example.mediconnect24x7.ui.theme.PremiumTeal
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7F6))
    ) {
        TopAppBar(
            title = { Text("About App", color = PremiumTeal, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PremiumTeal)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(800)) + slideInVertically(
                initialOffsetY = { 100 },
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Card
                AboutHeaderCard()

                // Content Cards
                SectionCard(
                    title = "Problem Statement Title",
                    content = "Telemedicine Access for Rural Healthcare in Nabha",
                    icon = Icons.Default.Info,
                    isTitle = true
                )

                SectionCard(
                    title = "Problem Description",
                    content = "Nabha and its surrounding rural areas face significant healthcare challenges. The local Civil Hospital operates at less than 50% staff capacity, with only 11 doctors for 23 sanctioned posts. Patients from 173 villages travel long distances, often missing work, only to find that specialists are unavailable or medicines are out of stock. Poor road conditions and sanitation further hinder access. Many residents lack timely medical care, leading to worsened health outcomes and increased financial strain.",
                    icon = Icons.Default.Troubleshoot
                )

                SectionCard(
                    title = "Impact / Why this problem needs to be solved",
                    content = "This problem directly affects the health and livelihood of thousands of rural residents, especially daily-wage earners and farmers. Lack of accessible healthcare leads to preventable complications, financial losses, and overall decline in community well-being. Addressing this issue would improve healthcare delivery, reduce unnecessary travel, and enhance quality of life for a large, underserved population.",
                    icon = Icons.Default.HealthAndSafety
                )

                SectionCard(
                    title = "Expected Outcomes",
                    content = "• A multilingual telemedicine app for video consultations with doctors.\n• Digital health records accessible offline for rural patients.\n• Real-time updates on medicine availability at local pharmacies.\n• AI-powered symptom checker optimized for low-bandwidth areas.\n• A scalable solution for other rural regions in India.",
                    icon = Icons.Default.Lightbulb
                )

                SectionCard(
                    title = "Relevant Stakeholders / Beneficiaries",
                    content = "• Rural patients in Nabha and surrounding villages.\n• Nabha Civil Hospital staff.\n• Punjab Health Department.\n• Local pharmacies.\n• Daily-wage workers and farmers.",
                    icon = Icons.Default.People
                )

                SectionCard(
                    title = "Supporting Data",
                    content = "• Nabha Civil Hospital serves 173 villages but has only 11 out of 23 sanctioned doctors.\n• Only 31% of rural Punjab households have internet access, highlighting the need for offline features.\n• Telemedicine adoption in India is growing at a 31% CAGR (2020–2025).\n• Sources: Local news reports and government health statistics.",
                    icon = Icons.Default.Info
                )

                DevelopersSection()

                Spacer(modifier = Modifier.height(10.dp))
                
                Text(
                    text = "HealthTech",
                    color = PremiumTeal,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun AboutHeaderCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .height(140.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(PremiumTeal, PremiumMint)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "MediConnect 24x7",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "App Details",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SectionCard(title: String, content: String, icon: ImageVector, isTitle: Boolean = false) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PremiumMint.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = PremiumTeal, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = PremiumTeal
                )
            }
            Text(
                content,
                fontSize = if (isTitle) 16.sp else 14.sp,
                color = Color(0xFF4B5563),
                lineHeight = 22.sp,
                fontWeight = if (isTitle) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun DevelopersSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PremiumMint.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.People, contentDescription = null, tint = PremiumTeal, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Our Developers",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = PremiumTeal
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // To use actual images, pass the resource ID like: imageRes = R.drawable.dev1_photo
                DeveloperProfile(name = "Varun", delayMillis = 0, imageRes = R.drawable.developer_prathi)
                DeveloperProfile(name = "Prathish", delayMillis = 300, imageRes = R.drawable.developer_prathi)
                DeveloperProfile(name = "Manoj", delayMillis = 600, imageRes = R.drawable.developer_prathi)
            }
        }
    }
}

@Composable
fun DeveloperProfile(name: String, delayMillis: Int, imageRes: Int? = null) {
    val infiniteTransition = rememberInfiniteTransition(label = "anim_$name")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing, delayMillis = delayMillis),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_$name"
    )
    


    val offsetY by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing, delayMillis = delayMillis),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset_$name"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(90.dp)
    ) {
        Box(
            modifier = Modifier
                .offset(y = offsetY.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale

                }
                .size(70.dp)
                .clip(CircleShape)
                .background(PremiumMint.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            if (imageRes != null) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = "Photo of $name",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Photo of $name",
                    tint = PremiumTeal,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = name,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF4B5563)
        )
    }
}

