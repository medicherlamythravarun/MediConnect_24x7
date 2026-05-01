package com.example.mediconnect24x7
//73822 43069
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediconnect24x7.ui.theme.DarkGreen
import com.example.mediconnect24x7.ui.theme.LightGreenBg
import com.example.mediconnect24x7.ui.theme.PrimaryGreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteProfileScreen(onComplete: () -> Unit) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var email by remember { mutableStateOf(currentUser?.email ?: "") }
    var isUpdating by remember { mutableStateOf(false) }
    var showAgePicker by remember { mutableStateOf(false) }

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
            .background(Color(0xFFF8F9FA))
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = "Almost There!",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = DarkGreen
        )
        
        Text(
            text = "Please complete your profile to continue",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
        )

        ProfileField(
            label = "Full Name",
            value = name,
            onValueChange = { name = it },
            icon = Icons.Default.Badge
        )

        Spacer(modifier = Modifier.height(20.dp))

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
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Gender",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp).align(Alignment.Start)
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

        Spacer(modifier = Modifier.height(24.dp))

        ProfileField(
            label = "Email Address",
            value = email,
            onValueChange = { email = it },
            icon = Icons.Default.Email
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                if (name.isBlank() || age.isBlank()) {
                    Toast.makeText(context, "Please fill in your name and age", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (currentUser != null) {
                    isUpdating = true
                    firestore.collection("users").document(currentUser.uid).get()
                        .addOnSuccessListener { doc ->
                            val role = doc.getString("role") ?: "client"
                            val profile = UserProfile(
                                uid = currentUser.uid,
                                name = name,
                                age = age,
                                gender = gender,
                                email = email,
                                phone = currentUser.phoneNumber ?: "",
                                profilePicUrl = "",
                                role = role
                            )
                            
                            firestore.collection("users").document(currentUser.uid)
                                .set(profile)
                                .addOnSuccessListener {
                                    isUpdating = false
                                    onComplete()
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
            enabled = !isUpdating
        ) {
            if (isUpdating) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Complete Registration", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}
