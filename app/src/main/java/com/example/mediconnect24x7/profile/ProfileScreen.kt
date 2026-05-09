package com.example.mediconnect24x7.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.mediconnect24x7.core.DoctorProfile
import com.example.mediconnect24x7.core.UserProfile
import com.example.mediconnect24x7.ui.theme.EmergencyRed
import com.example.mediconnect24x7.ui.theme.PremiumTeal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onSignOut: () -> Unit, onNavigateToSettings: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var profilePicUrl by remember { mutableStateOf("") }
    var userRole by remember { mutableStateOf("") }
    var createdAt by remember { mutableLongStateOf(0L) }
    var isLoading by remember { mutableStateOf(true) }

    // Doctor specific fields
    var specialization by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var consultationFee by remember { mutableStateOf("") }

    var showAgePicker by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                isUploading = true
                scope.launch(Dispatchers.IO) {
                    val base64Image = compressAndEncodeImage(context, uri)
                    withContext(Dispatchers.Main) {
                        if (base64Image != null) {
                            profilePicUrl = base64Image
                            isUploading = false
                            // Auto-save to Firestore immediately
                            if (currentUser != null) {
                                firestore.collection("users").document(currentUser.uid)
                                    .update("profilePicUrl", base64Image)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Profile Photo Updated Successfully", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        // If document doesn't exist yet, we might need to use set with merge
                                        firestore.collection("users").document(currentUser.uid)
                                            .set(mapOf("profilePicUrl" to base64Image), SetOptions.merge())
                                            .addOnSuccessListener {
                                                Toast.makeText(context, "Profile Photo Updated Successfully", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                            }
                        } else {
                            isUploading = false
                            Toast.makeText(context, "Upload failed: Could not process image", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    )

    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        name = document.getString("name") ?: ""
                        age = document.getString("age") ?: ""
                        gender = document.getString("gender") ?: "Male"
                        email = document.getString("email") ?: ""
                        phoneNumber = document.getString("phone") ?: ""
                        profilePicUrl = document.getString("profilePicUrl") ?: ""
                        userRole = document.getString("role") ?: ""
                        createdAt = document.getLong("createdAt") ?: 0L

                        if (userRole.lowercase() == "doctor") {
                            firestore.collection("doctors").document(currentUser.uid).get()
                                .addOnSuccessListener { doc ->
                                    if (doc.exists()) {
                                        specialization = doc.getString("specialization") ?: ""
                                        experience = doc.getLong("experience")?.toString() ?: ""
                                        consultationFee = doc.getDouble("consultationFee")?.toString() ?: ""
                                    }
                                }
                        }
                    }
                    isLoading = false
                }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to sign out from your account?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
                ) {
                    Text("Sign Out", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    if (showAgePicker) {
        AgePickerDialog(
            currentAge = age,
            onDismiss = { showAgePicker = false },
            onAgeSelected = { 
                age = it
                showAgePicker = false
            }
        )
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PremiumTeal)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7F6))
            .verticalScroll(rememberScrollState())
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(PremiumTeal,PremiumTeal,Color(0xFFD0E6E3))
                    ),
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                )
                .padding(bottom = 40.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNavigateToSettings()
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
                    }
                }
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .border(BorderStroke(4.dp, Color.White), CircleShape)
                            .clickable { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (profilePicUrl.isNotEmpty()) {
                            val model = if (profilePicUrl.startsWith("data:image")) {
                                try {
                                    val base64String = profilePicUrl.substringAfter("base64,")
                                    Base64.decode(base64String, Base64.DEFAULT)
                                } catch(e: Exception) {
                                    profilePicUrl
                                }
                            } else {
                                profilePicUrl
                            }
                            Image(
                                painter = rememberAsyncImagePainter(model),
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = if (name.isNotEmpty()) name.take(1).uppercase() else "?",
                                fontSize = 56.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        
                        if (isUploading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                    Surface(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        color = Color.White,
                        shadowElevation = 4.dp
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Change Picture",
                            modifier = Modifier.padding(10.dp),
                            tint = PremiumTeal
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = name.ifBlank { "Complete Your Profile" },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = userRole.uppercase(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .background(Color.Black.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .offset(y = (-20).dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Personal Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = PremiumTeal,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    ProfileField(
                        label = "Full Name",
                        value = name,
                        onValueChange = { name = it },
                        icon = Icons.Default.Person
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            Column {
                                Text(
                                    "Age",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                                )
                                OutlinedCard(
                                    onClick = { 
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showAgePicker = true 
                                    },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.outlinedCardColors(containerColor = Color.Transparent)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.CalendarToday, null, tint = PremiumTeal, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(age.ifBlank { "Set Age" }, color = if (age.isBlank()) Color.Gray else Color.Black)
                                    }
                                }
                            }
                        }
                        
                        Box(modifier = Modifier.weight(2f)) {
                            Column {
                                Text(
                                    "Gender",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .background(Color(0xFFF1F3F4), RoundedCornerShape(12.dp))
                                        .padding(4.dp)
                                ) {
                                    listOf("Male", "Female", "Others").forEach { g ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (gender == g) Color.White else Color.Transparent)
                                                .clickable { 
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    gender = g 
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                g,
                                                fontSize = 13.sp,
                                                fontWeight = if (gender == g) FontWeight.Bold else FontWeight.Normal,
                                                color = if (gender == g) PremiumTeal else Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Contact Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Contact Information",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = PremiumTeal,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    ProfileField(
                        label = "Email Address",
                        value = email,
                        onValueChange = { email = it },
                        icon = Icons.Default.Email
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ProfileField(
                        label = "Phone Number",
                        value = phoneNumber,
                        onValueChange = { /* Read only from auth */ },
                        icon = Icons.Default.Phone
                    )
                }
            }

            // Professional Details (Doctor Only)
            if (userRole.lowercase() == "doctor") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Professional Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = PremiumTeal,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        ProfileField(
                            label = "Specialization",
                            value = specialization,
                            onValueChange = { specialization = it },
                            icon = Icons.Default.MedicalServices
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                ProfileField(
                                    label = "Exp (Years)",
                                    value = experience,
                                    onValueChange = { experience = it },
                                    icon = Icons.Default.History
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                ProfileField(
                                    label = "Fee (₹)",
                                    value = consultationFee,
                                    onValueChange = { consultationFee = it },
                                    icon = Icons.Default.CurrencyRupee
                                )
                            }
                        }
                    }
                }
            }

            // Action Buttons
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (currentUser != null) {
                        try {
                            val profile = UserProfile(
                                uid = currentUser.uid,
                                name = name,
                                age = age,
                                gender = gender,
                                email = email,
                                phone = phoneNumber,
                                profilePicUrl = profilePicUrl,
                                role = userRole,
                                createdAt = if (createdAt == 0L) System.currentTimeMillis() else createdAt
                            )
                            firestore.collection("users").document(currentUser.uid)
                                .set(profile)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Profile Updated Successfully", Toast.LENGTH_SHORT).show()
                                }

                            if (userRole.lowercase() == "doctor") {
                                val doctorProfile = DoctorProfile(
                                    doctorId = currentUser.uid,
                                    userId = currentUser.uid,
                                    specialization = specialization,
                                    experience = experience.toIntOrNull() ?: 0,
                                    consultationFee = consultationFee.toDoubleOrNull() ?: 0.0,
                                    isAvailable = true
                                )
                                firestore.collection("doctors").document(currentUser.uid).set(doctorProfile)
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PremiumTeal)
            ) {
                Text("Save Changes", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            }

            OutlinedButton(
                onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showLogoutDialog = true 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = EmergencyRed),
                border = BorderStroke(1.dp, EmergencyRed.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Logout, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun AgePickerDialog(
    currentAge: String,
    onDismiss: () -> Unit,
    onAgeSelected: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Age", fontWeight = FontWeight.Bold) },
        text = {
            Box(modifier = Modifier.height(250.dp).fillMaxWidth()) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    (25..70).forEach { i ->
                        Text(
                            text = i.toString(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onAgeSelected(i.toString()) 
                                }
                                .padding(vertical = 14.dp),
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            color = if (currentAge == i.toString()) PremiumTeal else Color.Black,
                            fontWeight = if (currentAge == i.toString()) FontWeight.ExtraBold else FontWeight.Normal
                        )
                    }
                }
            }
        },
        confirmButton = {},
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White
    )
}

private fun compressAndEncodeImage(context: Context, uri: Uri): String? {
    return try {
        // Check for rotation in EXIF data
        var rotationDegrees = 0f
        context.contentResolver.openInputStream(uri)?.use { stream ->
            val exif = ExifInterface(stream)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            rotationDegrees = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        }

        val inputStream = context.contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        
        if (originalBitmap == null) return null
        
        val maxWidth = 500
        val maxHeight = 500
        val scale = Math.min(maxWidth.toFloat() / originalBitmap.width, maxHeight.toFloat() / originalBitmap.height)
        
        val scaledBitmap = if (scale < 1f || rotationDegrees != 0f) {
            val matrix = Matrix()
            if (scale < 1f) matrix.postScale(scale, scale)
            if (rotationDegrees != 0f) matrix.postRotate(rotationDegrees)
            Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)
        } else {
            originalBitmap
        }
        
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val byteArray = outputStream.toByteArray()
        
        "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP)
    } catch (e: Exception) {
        null
    }
}

@Composable
fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector
) {
    Column {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(icon, contentDescription = null, tint = PremiumTeal, modifier = Modifier.size(20.dp)) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PremiumTeal,
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                cursorColor = PremiumTeal,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            singleLine = true
        )
    }
}
