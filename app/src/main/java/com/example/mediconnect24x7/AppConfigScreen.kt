package com.example.mediconnect24x7

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediconnect24x7.ui.theme.PremiumMint
import com.example.mediconnect24x7.ui.theme.PremiumTeal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppConfigScreen() {
    var collections by remember { mutableStateOf<List<String>>(emptyList()) }
    var isFetchingCollections by remember { mutableStateOf(true) }
    var refreshKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(refreshKey) {
        isFetchingCollections = true
        try {
            val user = FirebaseAuth.getInstance().currentUser
            val tokenResult = user?.getIdToken(false)?.await()
            val token = tokenResult?.token
            
            if (token != null) {
                val fetchedIds = withContext(Dispatchers.IO) {
                    val url = URL("https://firestore.googleapis.com/v1/projects/teleconnecdt24x7/databases/(default)/documents:listCollectionIds")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "POST"
                    connection.setRequestProperty("Authorization", "Bearer $token")
                    connection.setRequestProperty("Content-Type", "application/json")
                    connection.doOutput = true
                    
                    if (connection.responseCode == 200) {
                        val response = connection.inputStream.bufferedReader().readText()
                        val json = JSONObject(response)
                        val ids = mutableListOf<String>()
                        val array = json.optJSONArray("collectionIds")
                        if (array != null) {
                            for (i in 0 until array.length()) {
                                ids.add(array.getString(i))
                            }
                        }
                        ids
                    } else {
                        emptyList()
                    }
                }
                
                if (fetchedIds.isNotEmpty()) {
                    collections = fetchedIds.sorted()
                } else {
                    // Fallback to known collections if auto-fetch yields nothing
                    collections = listOf("users", "doctors", "appointments", "prescriptions", "admins").sorted()
                }
            }
        } catch (e: Exception) {
            collections = listOf("users", "doctors", "appointments", "prescriptions", "admins").sorted()
        } finally {
            isFetchingCollections = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7F6))
    ) {
        TopAppBar(
            title = {
                Text(
                    "App Config",
                    color = PremiumTeal,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                )
            },
            actions = {
                IconButton(onClick = { refreshKey++ }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = PremiumTeal)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Firestore Collections",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                    if (isFetchingCollections) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = PremiumTeal
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            if (collections.isEmpty() && !isFetchingCollections) {
                item {
                    Text("No collections found or access denied.", color = Color.Red, fontSize = 14.sp)
                }
            } else {
                items(collections) { collectionName ->
                    CollectionCard(collectionName)
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "System Information",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                SystemInfoCard()
            }
        }
    }
}

@Composable
fun CollectionCard(name: String) {
    var count by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    val firestore = FirebaseFirestore.getInstance()

    LaunchedEffect(name) {
        isLoading = true
        try {
            val snapshot = firestore.collection(name).get().await()
            count = snapshot.size()
        } catch (e: Exception) {
            count = -1
        } finally {
            isLoading = false
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(PremiumMint.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Storage,
                    contentDescription = null,
                    tint = PremiumTeal,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name.replaceFirstChar { it.uppercase() },
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1F2937)
                )
                Text(
                    text = "Path: /$name",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = PremiumTeal
                )
            } else {
                Surface(
                    color = if (count == -1) Color.Red.copy(alpha = 0.1f) else PremiumTeal.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (count == -1) "Error" else "$count Docs",
                        color = if (count == -1) Color.Red else PremiumTeal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SystemInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF3F4F6))
            InfoRow("Database", "Cloud Firestore")
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF3F4F6))

        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
        Text(text = value, color = Color(0xFF1F2937), fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}
