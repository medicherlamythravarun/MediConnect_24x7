package com.example.mediconnect24x7

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.mediconnect24x7.ui.theme.*
import com.example.mediconnect24x7.webrtc.SignalingClient
import com.example.mediconnect24x7.webrtc.WebRTCClient
import org.webrtc.*

@Composable
fun VideoCallScreen(onEndCall: () -> Unit) {
    val context = LocalContext.current
    var hasPermissions by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermissions = permissions[Manifest.permission.CAMERA] == true &&
                permissions[Manifest.permission.RECORD_AUDIO] == true
    }

    LaunchedEffect(Unit) {
        if (!hasPermissions) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            )
        }
    }

    if (hasPermissions) {
        VideoCallContent(onEndCall = onEndCall)
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Permissions required for video call.")
        }
    }
}

@Composable
fun VideoCallContent(onEndCall: () -> Unit) {
    val context = LocalContext.current
    var webRTCClient by remember { mutableStateOf<WebRTCClient?>(null) }
    var signalingClient by remember { mutableStateOf<SignalingClient?>(null) }
    
    val roomId = "test_room" // Shared room for demonstration
    
    val localView = remember { SurfaceViewRenderer(context).apply { setEnableHardwareScaler(true) } }
    val remoteView = remember { SurfaceViewRenderer(context).apply { setEnableHardwareScaler(true) } }

    DisposableEffect(Unit) {
        val observer = object : PeerConnection.Observer {
            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {}
            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
            override fun onIceCandidate(candidate: IceCandidate) {
                signalingClient?.sendIceCandidate(candidate, true)
            }
            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
            override fun onAddStream(stream: MediaStream) {
                if (stream.videoTracks.isNotEmpty()) {
                    stream.videoTracks[0].addSink(remoteView)
                }
            }
            override fun onRemoveStream(p0: MediaStream?) {}
            override fun onDataChannel(p0: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
            override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) {}
        }

        val client = WebRTCClient(context, observer)
        webRTCClient = client
        
        localView.init(client.eglContext(), null)
        remoteView.init(client.eglContext(), null)
        
        client.startLocalVideoCapture(localView)

        val sigClient = SignalingClient(roomId, object : SignalingClient.SignalingListener {
            override fun onOfferReceived(description: SessionDescription) {
                client.setRemoteDescription(description)
                client.answer(object : SdpObserver {
                    override fun onCreateSuccess(desc: SessionDescription?) {
                        desc?.let { signalingClient?.sendAnswer(it) }
                    }
                    override fun onSetSuccess() {}
                    override fun onCreateFailure(p0: String?) {}
                    override fun onSetFailure(p0: String?) {}
                })
            }

            override fun onAnswerReceived(description: SessionDescription) {
                client.setRemoteDescription(description)
            }

            override fun onIceCandidateReceived(candidate: IceCandidate) {
                client.addIceCandidate(candidate)
            }
        })
        signalingClient = sigClient
        sigClient.init()

        onDispose {
            sigClient.clearRoom()
            client.onDestroy()
            localView.release()
            remoteView.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Remote View
        AndroidView(
            factory = { remoteView },
            modifier = Modifier.fillMaxSize()
        )

        // Doctor Info Overlay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Dr. Anil Verma",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Connecting...",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp
            )
        }

        // Local View
        Card(
            modifier = Modifier
                .padding(24.dp)
                .size(100.dp, 150.dp)
                .align(Alignment.TopEnd)
                .padding(top = 40.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
        ) {
            AndroidView(
                factory = { localView },
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Connect Button (Offer)
        Button(
            onClick = {
                webRTCClient?.call(object : SdpObserver {
                    override fun onCreateSuccess(desc: SessionDescription?) {
                        desc?.let { signalingClient?.sendOffer(it) }
                    }
                    override fun onSetSuccess() {}
                    override fun onCreateFailure(p0: String?) {}
                    override fun onSetFailure(p0: String?) {}
                })
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 150.dp, end = 24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
        ) {
            Text("Start Call")
        }

        // Bottom Controls
        Surface(
            modifier = Modifier
                .padding(bottom = 48.dp)
                .width(280.dp)
                .height(80.dp)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(40.dp),
            color = Color.White.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Mic, null, tint = Color.White)
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Videocam, null, tint = Color.White)
                }
                FloatingActionButton(
                    onClick = onEndCall,
                    containerColor = EmergencyRed,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Default.CallEnd, null)
                }
                IconButton(onClick = {}) {
                    Icon(Icons.AutoMirrored.Filled.Chat, null, tint = Color.White)
                }
            }
        }
    }
}
