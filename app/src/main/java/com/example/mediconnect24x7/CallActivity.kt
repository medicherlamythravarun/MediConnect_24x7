package com.example.mediconnect24x7

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment

class CallActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val appID = intent.getLongExtra("appID", 0L)
        val appSign = intent.getStringExtra("appSign") ?: ""
        val userID = intent.getStringExtra("userID") ?: ""
        val userName = intent.getStringExtra("userName") ?: ""
        val callID = intent.getStringExtra("callID") ?: ""
        
        val config = ZegoUIKitPrebuiltCallConfig.oneOnOneVideoCall()
        val fragment = ZegoUIKitPrebuiltCallFragment.newInstance(
            appID, appSign, userID, userName, callID, config
        )
        
        val frameLayout = FrameLayout(this)
        frameLayout.id = View.generateViewId()
        setContentView(frameLayout)
        
        supportFragmentManager.beginTransaction()
            .replace(frameLayout.id, fragment)
            .commitNow()
    }
}
