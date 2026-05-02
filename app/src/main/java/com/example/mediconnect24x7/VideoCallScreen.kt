package com.example.mediconnect24x7

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import android.Manifest
import androidx.compose.material.icons.filled.Lock
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediconnect24x7.ui.theme.LightGreenBg
import com.example.mediconnect24x7.ui.theme.PrimaryGreen

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VideoCallScreen(
    doctor: Doctor?, 
    clientName: String = "",
    userID: String, 
    userName: String, 
    callID: String, 
    appointmentId: String,
    onEndCall: () -> Unit
) {
    val context = LocalContext.current
    val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()

    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    )

    val appID: Long = 1971360907
    val appSign = "33de71963f9d613f1dd5e7b6aed01377b472d039edc972b8435aa8d4b26aecc0"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(LightGreenBg, Color.White)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(PrimaryGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalHospital,
                    contentDescription = "Doctor Icon",
                    tint = PrimaryGreen,
                    modifier = Modifier.size(64.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = if (doctor != null) doctor.name else if (clientName.isNotEmpty()) "Patient: $clientName" else "Telehealth Session",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
            )
            
            Text(
                text = if (doctor != null) doctor.specialty else "Patient",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.Gray
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Surface(
                color = LightGreenBg,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(
                    text = if (permissionState.allPermissionsGranted) "Ready to connect" else "Permissions required",
                    color = if (permissionState.allPermissionsGranted) PrimaryGreen else Color.Red,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = {
                    if (permissionState.allPermissionsGranted) {
                        val intent = Intent(context, CallActivity::class.java).apply {
                            putExtra("appID", appID)
                            putExtra("appSign", appSign)
                            putExtra("userID", userID)
                            putExtra("userName", userName)
                            putExtra("callID", callID)
                        }
                        context.startActivity(intent)
                    } else {
                        permissionState.launchMultiplePermissionRequest()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (permissionState.allPermissionsGranted) PrimaryGreen else Color.DarkGray
                ),
                shape = RoundedCornerShape(28.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    imageVector = if (permissionState.allPermissionsGranted) Icons.Default.VideoCall else Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    if (permissionState.allPermissionsGranted) "Join Consultation" else "Grant Permissions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = {
                    if (appointmentId.isNotEmpty()) {
                        firestore.collection("appointments").document(appointmentId)
                            .update("status", "Completed")
                            .addOnCompleteListener {
                                onEndCall()
                            }
                    } else {
                        onEndCall()
                    }
                },
                modifier = Modifier.height(48.dp)
            ) {
                Text(
                    "Cancel or End Call",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (!permissionState.allPermissionsGranted && permissionState.shouldShowRationale) {
                Text(
                    text = "Camera and Microphone access are needed for the video call.",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}


