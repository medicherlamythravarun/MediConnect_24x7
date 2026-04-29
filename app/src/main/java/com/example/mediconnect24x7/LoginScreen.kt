package com.example.mediconnect24x7

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediconnect24x7.ui.theme.*
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var timerSeconds by remember { mutableStateOf(119) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Timer logic
    LaunchedEffect(isOtpSent) {
        if (isOtpSent) {
            timerSeconds = 119
            while (timerSeconds > 0) {
                delay(1000)
                timerSeconds--
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEDF2EF)), // Premium light greenish background
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.MedicalServices,
                contentDescription = null,
                tint = DarkGreen,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                "MediConnect 24/7",
                color = DarkGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        }

        // Main Content Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // User Icon with pulsing effect placeholder
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(LightGreenBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "Welcome Back",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
                Text(
                    "Log in to access your health portal anytime.",
                    fontSize = 15.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))

                if (!isOtpSent) {
                    // Phone Number Input Section
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Phone Number",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Country Code Dropdown
                            Surface(
                                modifier = Modifier
                                    .height(56.dp)
                                    .width(85.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF1F4F2),
                                border = borderStroke()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text("+91", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                    Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(20.dp))
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            // Phone Input
                            TextField(
                                value = phoneNumber,
                                onValueChange = { if (it.length <= 10) phoneNumber = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("00000 00000", color = Color.LightGray) },
                                shape = RoundedCornerShape(12.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFF1F4F2),
                                    unfocusedContainerColor = Color(0xFFF1F4F2),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            if (phoneNumber.length == 10) {
                                isLoading = true
                                errorMessage = null
                                sendOtp(auth, "+91$phoneNumber", context as Activity, 
                                    onCodeSent = { id -> 
                                        verificationId = id
                                        isOtpSent = true 
                                        isLoading = false
                                    },
                                    onError = { 
                                        errorMessage = it
                                        isLoading = false
                                    }
                                )
                            } else {
                                errorMessage = "Please enter a valid 10-digit number"
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Send OTP", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(10.dp))
                            Icon(Icons.Default.ArrowForward, null)
                        }
                    }
                } else {
                    // OTP Verification Section
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "OTP Verification",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Surface(
                                color = Color(0xFFB9F6CA),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = String.format("%02d:%02d", timerSeconds / 60, timerSeconds % 60),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 13.sp,
                                    color = DarkGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            "Enter the 6-digit code sent to your mobile.",
                            fontSize = 15.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 10.dp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // OTP Digit Boxes
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Using a simple TextField for input but styling it to look like boxes is complex in a single field.
                            // For a premium look, we'll use a single field with wide letter spacing and a custom background.
                            TextField(
                                value = otpCode,
                                onValueChange = { if (it.length <= 6) otpCode = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    textAlign = TextAlign.Center,
                                    letterSpacing = 12.sp,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryGreen
                                ),
                                shape = RoundedCornerShape(12.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedIndicatorColor = PrimaryGreen,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                if (otpCode.length == 6) {
                                    isLoading = true
                                    verifyCode(auth, verificationId, otpCode, 
                                        onSuccess = { onLoginSuccess() },
                                        onError = { 
                                            errorMessage = it
                                            isLoading = false
                                        }
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text("Verify & Login", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("Didn't receive code? ", color = Color.Gray, fontSize = 15.sp)
                            Text(
                                "Resend OTP",
                                color = PrimaryGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        errorMessage?.let {
            Surface(
                modifier = Modifier.padding(16.dp),
                color = Color(0xFFFFEBEE),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    it, 
                    color = EmergencyRed, 
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Security Badges
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 40.dp)
        ) {
            Text(
                "SECURE HEALTHCARE ACCESS",
                fontSize = 11.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                Text(" SSL Encrypted", fontSize = 13.sp, color = Color.Gray)
                Spacer(modifier = Modifier.width(20.dp))
                Icon(Icons.Default.VerifiedUser, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                Text(" HIPAA Compliant", fontSize = 13.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun borderStroke() = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))

private fun sendOtp(
    auth: FirebaseAuth,
    phoneNumber: String,
    activity: Activity,
    onCodeSent: (String) -> Unit,
    onError: (String) -> Unit
) {
    val options = PhoneAuthOptions.newBuilder(auth)
        .setPhoneNumber(phoneNumber)
        .setTimeout(60L, TimeUnit.SECONDS)
        .setActivity(activity)
        .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Potential for auto-retrieval
            }

            override fun onVerificationFailed(e: FirebaseException) {
                onError(e.localizedMessage ?: "Verification failed. Check network or phone number.")
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                onCodeSent(verificationId)
            }
        })
        .build()
    PhoneAuthProvider.verifyPhoneNumber(options)
}

private fun verifyCode(
    auth: FirebaseAuth,
    verificationId: String,
    code: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val credential = PhoneAuthProvider.getCredential(verificationId, code)
    auth.signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess()
            } else {
                onError(task.exception?.localizedMessage ?: "Invalid OTP code. Please try again.")
            }
        }
}
