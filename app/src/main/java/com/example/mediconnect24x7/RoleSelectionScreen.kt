package com.example.mediconnect24x7

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediconnect24x7.ui.theme.DarkGreen
import com.example.mediconnect24x7.ui.theme.LightGreenBg
import com.example.mediconnect24x7.ui.theme.PrimaryGreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RoleSelectionScreen(onRoleSelected: (String) -> Unit) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    
    var selectedRole by remember { mutableStateOf<String?>(null) }
    var isUpdating by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to MediConnect",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = DarkGreen
        )
        
        Text(
            text = "Please select your role to continue",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        RoleCard(
            title = "Patient / Client",
            description = "Book consultations and manage records",
            icon = Icons.Default.Person,
            isSelected = selectedRole == "client",
            onClick = { selectedRole = "client" }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        RoleCard(
            title = "Doctor",
            description = "Provide consultations and view patient history",
            icon = Icons.Default.MedicalServices,
            isSelected = selectedRole == "doctor",
            onClick = { selectedRole = "doctor" }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        RoleCard(
            title = "Admin",
            description = "Manage hospital staff and operations",
            icon = Icons.Default.AdminPanelSettings,
            isSelected = selectedRole == "admin",
            onClick = { selectedRole = "admin" }
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = {
                if (selectedRole != null && currentUser != null) {
                    isUpdating = true
                    val userRef = firestore.collection("users").document(currentUser.uid)
                    
                    // We use merge to only update the role if the document exists, 
                    // or create it with the role if it doesn't.
                    userRef.update("role", selectedRole)
                        .addOnSuccessListener {
                            if (selectedRole == "admin") {
                                val adminRef = firestore.collection("admins").document(currentUser.uid)
                                val adminProfile = AdminProfile(
                                    adminId = currentUser.uid,
                                    userId = currentUser.uid
                                )
                                adminRef.set(adminProfile)
                                    .addOnSuccessListener {
                                        isUpdating = false
                                        onRoleSelected(selectedRole!!)
                                    }
                                    .addOnFailureListener { e ->
                                        isUpdating = false
                                        Toast.makeText(context, "Admin Sync Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                isUpdating = false
                                onRoleSelected(selectedRole!!)
                            }
                        }
                        .addOnFailureListener {
                            // If document doesn't exist, update will fail, so we use set
                            val newProfile = UserProfile(
                                uid = currentUser.uid,
                                email = currentUser.email ?: "",
                                phone = currentUser.phoneNumber ?: "",
                                role = selectedRole!!
                            )
                            
                            val batch = firestore.batch()
                            batch.set(userRef, newProfile)
                            
                            if (selectedRole == "admin") {
                                val adminRef = firestore.collection("admins").document(currentUser.uid)
                                val adminProfile = AdminProfile(
                                    adminId = currentUser.uid,
                                    userId = currentUser.uid
                                )
                                batch.set(adminRef, adminProfile)
                            }
                            
                            batch.commit()
                                .addOnSuccessListener {
                                    isUpdating = false
                                    onRoleSelected(selectedRole!!)
                                }
                                .addOnFailureListener { e ->
                                    isUpdating = false
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
            enabled = selectedRole != null && !isUpdating
        ) {
            if (isUpdating) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Confirm Role", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun RoleCard(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) LightGreenBg else Color.White
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, PrimaryGreen)
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
        },
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) PrimaryGreen else Color(0xFFF1F8E9)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else PrimaryGreen,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (isSelected) DarkGreen else Color.Black
                )
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
