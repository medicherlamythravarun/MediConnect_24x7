package com.example.mediconnect24x7

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.*
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
import com.example.mediconnect24x7.ui.theme.*
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
    
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAgePicker by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                isUploading = true
                uploadProfilePicture(storage, currentUser?.uid ?: "", uri) { url ->
                    profilePicUrl = url
                    isUploading = false
                }
            }
        }
    )

    // Load user data from Firestore
    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        name = document.getString("name") ?: ""
                        age = document.getString("age") ?: ""
                        gender = document.getString("gender") ?: "Male"
                        email = document.getString("email") ?: ""
                        profilePicUrl = document.getString("profilePicUrl") ?: ""
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
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
        // Top Bar
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

        // Profile Picture
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
            
            // Edit icon overlay
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

        Spacer(modifier = Modifier.height(32.dp))

        // Profile Form
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
                
                // Age Picker Field
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
                
                // Gender Selection (3-way toggle)
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
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = {
                        if (currentUser != null) {
                            isSaving = true
                            val userData = hashMapOf(
                                "name" to name,
                                "age" to age,
                                "gender" to gender,
                                "email" to email,
                                "phone" to phoneNumber,
                                "profilePicUrl" to profilePicUrl
                            )
                            firestore.collection("users").document(currentUser.uid).set(userData)
                                .addOnSuccessListener { isSaving = false }
                                .addOnFailureListener { isSaving = false }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Save Profile Details", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
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
    onSuccess: (String) -> Unit
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
