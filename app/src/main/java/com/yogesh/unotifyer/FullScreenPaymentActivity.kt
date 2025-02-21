package com.yogesh.unotifyer

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
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
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.yogesh.unotifyer.ui.theme.UNotifyerTheme
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.regex.Pattern


class FullScreenPaymentActivity : ComponentActivity() {
    private var textToSpeech: TextToSpeech? = null
    private var preTTSPlayer: MediaPlayer? = null
    private var postTTSPlayer: MediaPlayer? = null
    private var ttsFile: File? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preTTSPlayer = MediaPlayer.create(this, R.raw.pre)
        postTTSPlayer = MediaPlayer.create(this, R.raw.post)

        val data = extractTransactionDetails(intent.getStringExtra("paymentDetails") ?: "")
        data?.put("time",intent.getStringExtra("time").toString())

        setContent {
            UNotifyerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        modifier = Modifier.padding(innerPadding),
                        paymentDetails = data,
                        { finishAndRemoveTask() },
                        { playSequence() }
                    )
                }
            }
        }

        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale("hi", "IN")
                data?.get("amount")?.let { amount ->
                    saveTTSAsTempFile(amount) { file ->
                        ttsFile = file
                        playSequence()
                    }
                }
            }
        }

        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
    }

    private fun saveTTSAsTempFile(text: String, callback: (File) -> Unit) {
        val fileName = "tts_output.mp3"
        val tempFile = File(filesDir, fileName)

        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "tts_output")
        textToSpeech?.setPitch(1.3f)
        textToSpeech?.setSpeechRate(0.9f)
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}

            override fun onDone(utteranceId: String?) {
                Log.d("TTS", "TTS file saved: ${tempFile.absolutePath}")
                callback(tempFile) // Return the file
            }

            override fun onError(utteranceId: String?) {
                Log.e("TTS", "Error synthesizing TTS")
            }
        })

        textToSpeech?.synthesizeToFile("$text", params, tempFile, "tts_output")
    }

    private fun playSequence() {
        preTTSPlayer?.setOnCompletionListener {
            ttsFile?.let {
                MediaPlayer().apply {
                    setDataSource(it.path)
                    prepare()
                    setOnCompletionListener {
                        postTTSPlayer?.start()
                    }
                    start()
                }
            }
        }
        preTTSPlayer?.start()
    }

    private fun extractTransactionDetails(text: String): HashMap<String, String>? {
        val pattern = Pattern.compile("Rs (\\d+) from (.+?) via PhonePe for txn (T\\d+)")
        val matcher = pattern.matcher(text)

        if (matcher.find()) {
            val rupee = matcher.group(1)
            val nameOrNumber = matcher.group(2)?.trim()
            val txnId = matcher.group(3)
            try {
                return hashMapOf(
                    Pair("amount", rupee),
                    Pair("name", nameOrNumber ?: "NULL"),
                    Pair("txn", txnId)
                )
            } catch (e: Exception) {
                println("Error occurred while extracting transaction details: ${e.message}")
                return null
            }
        } else {
            println("No transaction details found in the text.")
            return null
        }
    }

    companion object Get {
        private const val CHANNEL_ID = "payment_notifications"
        private const val CHANNEL_NAME = "Payment Notifications"
        private var NOTIFICATION_ID = 1

        @SuppressLint("MissingPermission")
        fun showFullScreenPaymentNotification(
            context: Context,
            paymentDetails: String,
            time: String
        ) {
            // Create notification channel
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Channel for payment notifications"
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                }
                val notificationManager: NotificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }

            // Create full-screen intent
            NOTIFICATION_ID = (1..200).random()
            val fullScreenIntent = Intent(context, FullScreenPaymentActivity::class.java).apply {
                putExtra("paymentDetails", paymentDetails)
                putExtra("time", time)
            }
            val fullScreenPendingIntent = PendingIntent.getActivity(
                context,
                0,
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Create notification
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Payment Received")
                .setContentText(paymentDetails)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .build()

            // Show notification
            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID, notification)
            }
        }
    }
}

@Composable
fun Greeting(
    modifier: Modifier = Modifier,
    paymentDetails: HashMap<String, String>?,
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
        paymentDetails?.let {
            Text(text = "Rupee")
            Text(
                text = it["amount"].toString(),
                fontSize = MaterialTheme.typography.bodyLarge.fontSize.times(6),
                modifier = Modifier.padding(bottom = 50.dp)
            )
            Text(text = "From")
            Text(
                text = it["name"].toString(),
                fontSize = MaterialTheme.typography.titleLarge.fontSize
            )
            Text(text = "At", Modifier.padding(top = 8.dp))
            Text(text = "${it["time"]}", fontSize = MaterialTheme.typography.titleLarge.fontSize)
            Text(text = "Transaction ID", Modifier.padding(top = 16.dp))
            Text(text = "${it["txn"]}")
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
        } ?: run {
            Text(text = "No payment details available.")
        }
    }
}