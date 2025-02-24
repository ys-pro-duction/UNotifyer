package com.yogesh.unotifyer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.yogesh.unotifyer.Utils.Companion.ACTION_FINISH_ACTIVITY
import com.yogesh.unotifyer.ui.theme.UNotifyerTheme
import java.io.File
import java.util.Locale
import kotlin.math.min


class FullScreenPaymentActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private var textToSpeech: TextToSpeech? = null
    private var preTTSPlayer: MediaPlayer? = null
    private var postTTSPlayer: MediaPlayer? = null
    private var receiveSound: MediaPlayer? = null
    private var ttsFile: File? = null
    private var paymentDetails: PaymentDetails? = null

    private val finishReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (ACTION_FINISH_ACTIVITY == intent?.action) {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val paymentDetails = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(
                "paymentDetails",
                PaymentDetails::class.java
            ) as PaymentDetails
        } else {
            intent.getSerializableExtra("paymentDetails") as PaymentDetails
        }
        this.paymentDetails = paymentDetails
        setContent {
            UNotifyerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        modifier = Modifier.padding(innerPadding),
                        paymentDetails = paymentDetails,
                        { finishAndRemoveTask() },
                        { playAudioWithTTS(paymentDetails.amount) }
                    )
                }
            }
        }
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
        Handler(Looper.getMainLooper()).postDelayed({ finishAfterTransition() }, 30000)
        registerReceiver(
            finishReceiver,
            IntentFilter(ACTION_FINISH_ACTIVITY),
            Context.RECEIVER_NOT_EXPORTED
        )

        preTTSPlayer = MediaPlayer.create(this, R.raw.pre)
        postTTSPlayer = MediaPlayer.create(this, R.raw.post)
        textToSpeech = TextToSpeech(this, this)
        playPaymetnReceiveSound()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale("hi", "IN"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                println("Language not supported")
            } else {
                playAudioWithTTS(paymentDetails?.amount.toString())
            }
        } else {
            println("Initialization failed")
        }
    }

    private fun playAudioWithTTS(text: String) {
        preTTSPlayer?.setOnCompletionListener {
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
        }
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                // TTS started
            }

            override fun onDone(utteranceId: String?) {
                postTTSPlayer?.start()
                postTTSPlayer?.setOnCompletionListener {
                    setVolume(0.5f)
                }
            }

            override fun onError(utteranceId: String?) {
                // Handle error
            }
        })

        preTTSPlayer?.start()
    }


    private fun playPaymetnReceiveSound() {
        setVolume(2f)
        receiveSound = MediaPlayer.create(this, R.raw.phonepe)
        receiveSound?.start()
    }

    private fun setVolume(volume: Float) {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val newVolume = min(maxVolume, (1 + currentVolume * volume).toInt())
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
    }

    override fun onDestroy() {
        unregisterReceiver(finishReceiver)
        ttsFile?.delete()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        preTTSPlayer?.release()
        postTTSPlayer?.release()
        receiveSound?.release()
        super.onDestroy()
    }


    @Composable
    fun Greeting(
        modifier: Modifier = Modifier,
        paymentDetails: PaymentDetails,
        close: () -> Unit,
        speak: () -> Unit
    ) {
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxSize()
        ) {
            IconButton(
                close, Modifier
                    .align(Alignment.Top)
                    .padding(24.dp)
                    .size(80.dp)
            ) {
                Icon(
                    painterResource(R.drawable.baseline_close), "Close", Modifier
                        .padding(20.dp)
                        .size(80.dp)
                )
            }
        }
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(text = "Rupee")
            Text(
                text = paymentDetails.amount,
                fontSize = MaterialTheme.typography.bodyLarge.fontSize.times(6),
                modifier = Modifier.padding(bottom = 50.dp)
            )
            Text(text = "From")
            Text(
                text = "${paymentDetails.name}",
                fontSize = MaterialTheme.typography.titleLarge.fontSize
            )
            Text(text = "At", Modifier.padding(top = 8.dp))
            Text(
                text = paymentDetails.time,
                fontSize = MaterialTheme.typography.titleLarge.fontSize
            )
            Text(text = "Transaction ID", Modifier.padding(top = 16.dp))
            Text(text = "${paymentDetails.txn}")
            Button(
                speak,
                Modifier
                    .padding(top = 50.dp),
                colors = ButtonDefaults.buttonColors(
                    MaterialTheme.colorScheme.background,
                    MaterialTheme.colorScheme.onPrimaryContainer
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimaryContainer)
            ) {
                Text(text = "Speak")
            }
        }
    }

}