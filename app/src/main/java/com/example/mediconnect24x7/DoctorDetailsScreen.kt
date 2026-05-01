package com.example.mediconnect24x7

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediconnect24x7.ui.theme.DarkGreen
import com.example.mediconnect24x7.ui.theme.PrimaryGreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorDetailsScreen(onComplete: () -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var specialization by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var consultationFee by remember { mutableStateOf("") }
    var isUpdating by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Professional Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = DarkGreen
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Complete Your Profile",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreen,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Text(
                text = "Please provide your professional information to start consulting patients.",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp).align(Alignment.Start)
            )

            DetailInputField(
                value = specialization,
                onValueChange = { specialization = it },
                label = "Specialization",
                placeholder = "e.g. Cardiologist, Dermatologist",
                icon = Icons.Default.Badge
            )

            Spacer(modifier = Modifier.height(16.dp))

            DetailInputField(
                value = experience,
                onValueChange = { experience = it },
                label = "Years of Experience",
                placeholder = "e.g. 10",
                icon = Icons.Default.Work,
                keyboardType = KeyboardType.Number
            )

            Spacer(modifier = Modifier.height(16.dp))

            DetailInputField(
                value = consultationFee,
                onValueChange = { consultationFee = it },
                label = "Consultation Fee (₹)",
                placeholder = "e.g. 500",
                icon = Icons.Default.CurrencyRupee,
                keyboardType = KeyboardType.Number
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    if (specialization.isBlank() || experience.isBlank() || consultationFee.isBlank()) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (currentUser != null) {
                        isUpdating = true
                        val doctorId = firestore.collection("doctors").document().id
                        val doctorProfile = DoctorProfile(
                            doctorId = doctorId,
                            userId = currentUser.uid,
                            specialization = specialization,
                            experience = experience.toIntOrNull() ?: 0,
                            consultationFee = consultationFee.toDoubleOrNull() ?: 0.0,
                            isAvailable = true
                        )

                        firestore.collection("doctors").document(currentUser.uid)
                            .set(doctorProfile)
                            .addOnSuccessListener {
                                isUpdating = false
                                onComplete()
                            }
                            .addOnFailureListener { e ->
                                isUpdating = false
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                enabled = !isUpdating
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save & Continue", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun DetailInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = PrimaryGreen) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryGreen,
            unfocusedBorderColor = Color.LightGray,
            focusedLabelColor = PrimaryGreen
        ),
        singleLine = true
    )
}
