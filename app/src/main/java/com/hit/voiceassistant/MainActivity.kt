package com.hit.voiceassistant

import android.Manifest
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 96, 48, 48)
        }
        TextView(this).apply {
            text = "Voice Assistant\n\n\"लॉक\" बोलो तो फ़ोन लॉक होगा।\n\"ऑन\" बोलो तो स्क्रीन जलेगी।\n\nनीचे 1, 2, 3 क्रम से दबाओ।\n"
            textSize = 18f
            box.addView(this)
        }
        fun btn(t: String, f: () -> Unit) {
            Button(this).apply { text = t; setOnClickListener { f() }; box.addView(this) }
        }
        btn("1. Device Admin चालू करो") {
            val i = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, ComponentName(this, AdminReceiver::class.java))
                .putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "फ़ोन लॉक करने के लिए ज़रूरी है")
            startActivity(i)
        }
        btn("2. माइक की अनुमति दो") {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.POST_NOTIFICATIONS), 1)
        }
        btn("3. Assistant शुरू करो") {
            startForegroundService(Intent(this, VoiceService::class.java))
        }
        btn("Assistant बंद करो") {
            stopService(Intent(this, VoiceService::class.java))
        }
        setContentView(box)
    }
}
