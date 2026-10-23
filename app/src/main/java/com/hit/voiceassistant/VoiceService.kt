package com.hit.voiceassistant

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

class VoiceService : Service() {
    private var sr: SpeechRecognizer? = null
    private val h = Handler(Looper.getMainLooper())

    override fun onBind(i: Intent?): IBinder? = null

    override fun onStartCommand(i: Intent?, f: Int, id: Int): Int {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel("va", "Voice Assistant", NotificationManager.IMPORTANCE_LOW))
        val n = Notification.Builder(this, "va")
            .setContentTitle("Assistant सुन रहा है")
            .setContentText("\"लॉक\" या \"ऑन\" बोलो")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .build()
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(1, n)
        }
        if (!SpeechRecognizer.isRecognitionAvailable(this)) { stopSelf(); return START_NOT_STICKY }
        listen()
        return START_STICKY
    }

    private fun listen() {
        sr?.destroy()
        sr = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(p: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(v: Float) {}
                override fun onBufferReceived(b: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onPartialResults(p: Bundle?) {}
                override fun onEvent(t: Int, p: Bundle?) {}
                override fun onError(e: Int) { h.postDelayed({ listen() }, 1000) }
                override fun onResults(r: Bundle?) {
                    val t = r?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.joinToString(" ")?.lowercase() ?: ""
                    act(t)
                    h.postDelayed({ listen() }, 500)
                }
            })
        }
        val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
        sr?.startListening(i)
    }

    private fun act(t: String) {
        if (t.contains("लॉक") || Regex("\\block\\b").containsMatchIn(t)) {
            val dpm = getSystemService(DevicePolicyManager::class.java)
            if (dpm.isAdminActive(ComponentName(this, AdminReceiver::class.java))) dpm.lockNow()
        } else if (t.contains("ऑन") || t.contains("चालू") || Regex("\\bon\\b").containsMatchIn(t)) {
            val pm = getSystemService(PowerManager::class.java)
            @Suppress("DEPRECATION")
            pm.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "va:wake"
            ).acquire(5000)
        }
    }

    override fun onDestroy() {
        h.removeCallbacksAndMessages(null)
        sr?.destroy()
        super.onDestroy()
    }
}
