package com.example.mediconnect24x7

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.mediconnect24x7.ui.theme.DarkGreen
import com.example.mediconnect24x7.ui.theme.EmergencyRed
import com.example.mediconnect24x7.ui.theme.LightGreenBg
import com.example.mediconnect24x7.ui.theme.PrimaryGreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onSignOut: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val currentUser = auth.currentUser
    val context = LocalContext.current
    
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf(currentUser?.phoneNumber ?: "") }
    var profilePicUrl by remember { mutableStateOf("") }
    var userRole by remember { mutableStateOf("") }
    
    // Doctor specific fields
    var specialization by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var consultationFee by remember { mutableStateOf("") }
    var isDoctorProfileLoaded by remember { mutableStateOf(false) }
    
    var isLoading by remember { mutableStateOf(true) }
    var isUploading by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAgePicker by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                isUploading = true
                uploadProfilePicture(
                    storage = storage,
                    userId = currentUser?.uid ?: "",
                    imageUri = uri,
                    onSuccess = { url ->
                        profilePicUrl = url
                        isUploading = false
                    },
                    onFailure = { e ->
                        isUploading = false
                        Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e("ProfileScreen", "Image upload failed", e)
                    }
                )
            }
        }
    )

    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val profile = document.toObject(UserProfile::class.java)
                        if (profile != null) {
                            name = profile.name
                            age = profile.age
                            gender = profile.gender
                            email = profile.email
                            phoneNumber = profile.phone
                            profilePicUrl = profile.profilePicUrl
                            userRole = profile.role
                        }
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                    android.widget.Toast.makeText(context, "Failed to load profile", android.widget.Toast.LENGTH_SHORT).show()
                }

            // Fetch doctor details if user is a doctor
            firestore.collection("doctors").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val doctorProfile = document.toObject(DoctorProfile::class.java)
                        if (doctorProfile != null) {
                            specialization = doctorProfile.specialization
                            experience = doctorProfile.experience.toString()
                            consultationFee = doctorProfile.consultationFee.toString()
                            isDoctorProfileLoaded = true
                        }
                    }
                }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Confirm Logout") },
            text = { Text("Are you sure you want to log out of your account?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAF9))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "My Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreen
            )
            IconButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.background(Color(0xFFFFEBEE), CircleShape)
            ) {
                Icon(Icons.Default.Logout, contentDescription = "Sign Out", tint = EmergencyRed)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .size(120.dp)
                .background(LightGreenBg, CircleShape)
                .clickable {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (profilePicUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(profilePicUrl),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else if (name.isNotEmpty()) {
                Text(
                    name.take(1).uppercase(),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
            } else {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = PrimaryGreen
                )
            }
            
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(36.dp)
                    .background(PrimaryGreen, CircleShape)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }

            if (isUploading) {
                CircularProgressIndicator(modifier = Modifier.size(40.dp), color = PrimaryGreen)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            if (name.isNotEmpty()) name else "Complete your profile",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            phoneNumber,
            fontSize = 14.sp,
            color = Color.Gray
        )

        if (userRole.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = when(userRole.lowercase()) {
                    "doctor" -> Color(0xFFE3F2FD)
                    "admin" -> Color(0xFFFFF3E0)
                    else -> LightGreenBg
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = userRole.uppercase(),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = when(userRole.lowercase()) {
                        "doctor" -> Color(0xFF1976D2)
                        "admin" -> Color(0xFFE65100)
                        else -> DarkGreen
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                ProfileField(
                    label = "Full Name",
                    value = name,
                    onValueChange = { name = it },
                    icon = Icons.Default.Badge
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Column {
                    Text(
                        "Age",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                    OutlinedCard(
                        onClick = { showAgePicker = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = Color.Transparent)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CalendarToday, null, tint = PrimaryGreen)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(if (age.isEmpty()) "Select Age" else age, color = if (age.isEmpty()) Color.Gray else Color.Black)
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, null, tint = Color.Gray)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    "Gender",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    val genders = listOf("Male", "Female", "Others")
                    genders.forEachIndexed { index, label ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = genders.size),
                            onClick = { gender = label },
                            selected = gender == label,
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = LightGreenBg,
                                activeContentColor = DarkGreen,
                                inactiveContainerColor = Color.White,
                                inactiveContentColor = Color.Gray
                            )
                        ) {
                            Text(label, fontSize = 13.sp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                ProfileField(
                    label = "Email Address",
                    value = email,
                    onValueChange = { email = it },
                    icon = Icons.Default.Email
                )
                
                if (userRole.lowercase() == "doctor") {
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        "Professional Details",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreen,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    ProfileField(
                        label = "Specialization",
                        value = specialization,
                        onValueChange = { specialization = it },
                        icon = Icons.Default.Badge
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ProfileField(
                        label = "Experience (Years)",
                        value = experience,
                        onValueChange = { experience = it },
                        icon = Icons.Default.CalendarToday
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ProfileField(
                        label = "Consultation Fee (₹)",
                        value = consultationFee,
                        onValueChange = { consultationFee = it },
                        icon = Icons.Default.Email // Using Email icon as placeholder, will change to something better if available
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = {
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
                                    role = userRole
                                )
                                firestore.collection("users").document(currentUser.uid)
                                    .set(profile)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Profile Updated Successfully", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("ProfileScreen", "Error saving profile", e)
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                    }

                                // Save doctor details if user is a doctor
                                if (userRole.lowercase() == "doctor") {
                                    val doctorProfile = DoctorProfile(
                                        doctorId = currentUser.uid,
                                        userId = currentUser.uid,
                                        specialization = specialization,
                                        experience = experience.toIntOrNull() ?: 0,
                                        consultationFee = consultationFee.toDoubleOrNull() ?: 0.0,
                                        isAvailable = true
                                    )
                                    firestore.collection("doctors").document(currentUser.uid)
                                        .set(doctorProfile)
                                }
                            } catch (e: Exception) {
                                Log.e("ProfileScreen", "Error during profile preparation", e)
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                ) {
                    Text("Save Profile Details", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        TextButton(
            onClick = { showLogoutDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Out from Account", color = EmergencyRed, fontWeight = FontWeight.Medium)
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun AgePickerDialog(
    currentAge: String,
    onDismiss: () -> Unit,
    onAgeSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Age") },
        text = {
            Box(modifier = Modifier.height(200.dp).fillMaxWidth()) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    (1..100).forEach { i ->
                        Text(
                            text = i.toString(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAgeSelected(i.toString()) }
                                .padding(vertical = 12.dp, horizontal = 24.dp),
                            fontSize = 18.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = if (currentAge == i.toString()) PrimaryGreen else Color.Black,
                            fontWeight = if (currentAge == i.toString()) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        },
        confirmButton = {}
    )
}

private fun uploadProfilePicture(
    storage: FirebaseStorage,
    userId: String,
    imageUri: Uri,
    onSuccess: (String) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val ref = storage.reference.child("profile_pics/$userId.jpg")
    ref.putFile(imageUri)
        .continueWithTask { task ->
            if (!task.isSuccessful) task.exception?.let { throw it }
            ref.downloadUrl
        }
        .addOnSuccessListener { url ->
            onSuccess(url.toString())
        }
        .addOnFailureListener { e ->
            onFailure(e)
        }
}

@Composable
fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(icon, contentDescription = null, tint = PrimaryGreen) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryGreen,
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                cursorColor = PrimaryGreen
            ),
            singleLine = true
        )
    }
}
