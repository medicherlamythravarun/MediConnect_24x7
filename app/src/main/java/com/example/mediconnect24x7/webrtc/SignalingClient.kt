package com.example.mediconnect24x7.webrtc

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

class SignalingClient(
    private val roomId: String,
    private val listener: SignalingListener
) {
    private val db = FirebaseFirestore.getInstance()
    private val roomRef = db.collection("calls").document(roomId)

    interface SignalingListener {
        fun onOfferReceived(description: SessionDescription)
        fun onAnswerReceived(description: SessionDescription)
        fun onIceCandidateReceived(candidate: IceCandidate)
    }

    fun init() {
        roomRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                val type = snapshot.getString("type")
                val sdp = snapshot.getString("sdp")

                if (type == "OFFER" && sdp != null) {
                    listener.onOfferReceived(SessionDescription(SessionDescription.Type.OFFER, sdp))
                } else if (type == "ANSWER" && sdp != null) {
                    listener.onAnswerReceived(SessionDescription(SessionDescription.Type.ANSWER, sdp))
                }
            }
        }

        roomRef.collection("candidates").addSnapshotListener { snapshot, _ ->
            snapshot?.documentChanges?.forEach { change ->
                val data = change.document.data
                val sdpMid = data["sdpMid"] as? String
                val sdpMLineIndex = (data["sdpMLineIndex"] as? Number)?.toInt()
                val sdpCandidate = data["candidate"] as? String

                if (sdpMid != null && sdpMLineIndex != null && sdpCandidate != null) {
                    val candidate = IceCandidate(sdpMid, sdpMLineIndex, sdpCandidate)
                    listener.onIceCandidateReceived(candidate)
                }
            }
        }
    }

    fun sendOffer(description: SessionDescription) {
        val offerMap = hashMapOf(
            "type" to "OFFER",
            "sdp" to description.description
        )
        roomRef.set(offerMap).addOnSuccessListener {
            Log.d("SignalingClient", "Offer sent")
        }
    }

    fun sendAnswer(description: SessionDescription) {
        val answerMap = hashMapOf(
            "type" to "ANSWER",
            "sdp" to description.description
        )
        roomRef.update(answerMap as Map<String, Any>).addOnSuccessListener {
            Log.d("SignalingClient", "Answer sent")
        }
    }

    fun sendIceCandidate(candidate: IceCandidate, isLocal: Boolean) {
        val candidateMap = hashMapOf(
            "serverUrl" to candidate.serverUrl,
            "sdpMid" to candidate.sdpMid,
            "sdpMLineIndex" to candidate.sdpMLineIndex,
            "candidate" to candidate.sdp
        )
        val candidateType = if (isLocal) "callerCandidates" else "calleeCandidates"
        roomRef.collection("candidates").add(candidateMap)
    }
    
    fun clearRoom() {
        roomRef.delete()
    }
}
