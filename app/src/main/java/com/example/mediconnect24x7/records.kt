package com.example.mediconnect24x7

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediconnect24x7.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen() {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val currentUser = auth.currentUser
    
    var symptomRecords by remember { mutableStateOf<List<SymptomRecord>>(emptyList()) }
    var reportRecords by remember { mutableStateOf<List<ReportRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isUploading by remember { mutableStateOf(false) }
    
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("AI Consultations", "Uploaded Reports")

    // File Picker Launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && currentUser != null) {
            isUploading = true
            
            // Get file name
            var fileName = "Medical_Report_${System.currentTimeMillis()}"
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) fileName = cursor.getString(nameIndex)
                }
            }

            val storageRef = storage.reference.child("reports/${currentUser.uid}/${UUID.randomUUID()}_$fileName")
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        val reportRecord = ReportRecord(
                            id = UUID.randomUUID().toString(),
                            userId = currentUser.uid,
                            fileName = fileName,
                            fileUrl = downloadUrl.toString(),
                            timestamp = System.currentTimeMillis()
                        )
                        
                        firestore.collection("users")
                            .document(currentUser.uid)
                            .collection("uploaded_reports")
                            .document(reportRecord.id)
                            .set(reportRecord)
                            .addOnSuccessListener {
                                isUploading = false
                                Toast.makeText(context, "Report uploaded successfully!", Toast.LENGTH_SHORT).show()
                                selectedTabIndex = 1 // Switch to reports tab
                            }
                            .addOnFailureListener {
                                isUploading = false
                                Toast.makeText(context, "Failed to save record.", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener {
                    isUploading = false
                    Toast.makeText(context, "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Fetch AI Consultations
    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            firestore.collection("users")
                .document(currentUser.uid)
                .collection("health_records")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e == null && snapshot != null) {
                        symptomRecords = snapshot.documents.mapNotNull { it.toObject(SymptomRecord::class.java) }
                    }
                    if (selectedTabIndex == 0) isLoading = false
                }
                
            firestore.collection("users")
                .document(currentUser.uid)
                .collection("uploaded_reports")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e == null && snapshot != null) {
                        reportRecords = snapshot.documents.mapNotNull { it.toObject(ReportRecord::class.java) }
                    }
                    if (selectedTabIndex == 1) isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Health Records",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkGreen)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { filePickerLauncher.launch("*/*") },
                containerColor = PrimaryGreen,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = "Upload")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Upload Report")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F9FA))
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = PrimaryGreen,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = PrimaryGreen
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { 
                            Text(
                                title, 
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) PrimaryGreen else Color.Gray
                            ) 
                        }
                    )
                }
            }

            if (isUploading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = PrimaryGreen)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE3F2FD),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CloudDone, null, tint = Color(0xFF1976D2), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Synced with Cloud", fontSize = 12.sp, color = Color(0xFF1976D2), fontWeight = FontWeight.Medium)
                        }
                    }
                    val count = if (selectedTabIndex == 0) symptomRecords.size else reportRecords.size
                    Text("$count records", color = Color.Gray, fontSize = 13.sp)
                }

                val currentListEmpty = (selectedTabIndex == 0 && symptomRecords.isEmpty()) || (selectedTabIndex == 1 && reportRecords.isEmpty())

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                } else if (currentListEmpty) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                if (selectedTabIndex == 0) Icons.Default.History else Icons.Default.Description, 
                                null, 
                                modifier = Modifier.size(64.dp), 
                                tint = Color.LightGray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No ${if (selectedTabIndex == 0) "consultations" else "reports"} found", color = Color.Gray)
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (selectedTabIndex == 0) {
                            items(symptomRecords) { record ->
                                SymptomRecordCard(record, onDelete = {
                                    if (currentUser != null) {
                                        firestore.collection("users").document(currentUser.uid)
                                            .collection("health_records").document(record.id).delete()
                                            .addOnSuccessListener {
                                                Toast.makeText(context, "Consultation deleted", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                })
                            }
                        } else {
                            items(reportRecords) { record ->
                                ReportRecordCard(record, onDelete = {
                                    if (currentUser != null) {
                                        firestore.collection("users").document(currentUser.uid)
                                            .collection("uploaded_reports").document(record.id).delete()
                                            .addOnSuccessListener {
                                                try { storage.getReferenceFromUrl(record.fileUrl).delete() } catch (e: Exception) {}
                                                Toast.makeText(context, "Report deleted", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                })
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp)) // padding for FAB
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SymptomRecordCard(record: SymptomRecord, onDelete: () -> Unit) {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val dateString = sdf.format(Date(record.timestamp))
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(LightGreenBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.MedicalInformation, null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("AI Consultation", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                        Text(dateString, color = Color.Gray, fontSize = 12.sp)
                    }
                }
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, null, tint = Color.Gray)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color.Red) },
                            onClick = {
                                expanded = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Symptoms reported:",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = DarkGreen
            )
            Text(
                record.symptoms,
                fontSize = 14.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF1F8E9)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "AI Suggestion",
                            fontSize = 12.sp,
                            color = PrimaryGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        record.aiRecommendation,
                        fontSize = 13.sp,
                        color = Color.Black,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun RecordCard(record: HealthRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(LightGreenBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, tint = PrimaryGreen)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(record.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                        Text("Age: ${record.age} · ${record.visits} visits", color = Color.Gray, fontSize = 13.sp)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmergencyRed
                    ) {
                        Text(
                            text = record.bloodGroup,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
                }
            }

            if (record.allergies != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Warning,
                        null,
                        tint = Color(0xFFF57C00),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Allergic to: ${record.allergies}",
                        color = Color(0xFFF57C00),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFE8F5E9)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarToday, null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Last visit: ${record.lastVisitDate} · ${record.lastVisitReason}",
                        fontSize = 12.sp,
                        color = DarkGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun ReportRecordCard(record: ReportRecord, onDelete: () -> Unit) {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val dateString = sdf.format(Date(record.timestamp))
    val context = LocalContext.current
    val isPdf = record.fileName.endsWith(".pdf", ignoreCase = true)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(record.fileUrl))
                context.startActivity(intent)
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(LightGreenBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isPdf) Icons.Default.PictureAsPdf else Icons.Default.Image, 
                            null, 
                            tint = PrimaryGreen, 
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            record.fileName, 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 15.sp, 
                            color = Color.Black,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(dateString, color = Color.Gray, fontSize = 12.sp)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
                    }
                    Icon(Icons.Default.CloudDownload, null, tint = PrimaryGreen)
                }
            }
        }
    }
}
