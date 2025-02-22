package com.yogesh.unotifyer

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.io.File
import java.util.regex.Pattern

class Utils {
    companion object {
        const val ACTION_FINISH_ACTIVITY = "com.yogesh.unotifyer.ACTION_FINISH_ACTIVITY"
        private const val CHANNEL_ID = "payment_notifications"
        private const val CHANNEL_NAME = "Payment Notifications"
        private var NOTIFICATION_ID = 1
        fun extractTransactionDetails(text: String): PaymentDetails? {
            val pattern =
                Pattern.compile("Rs[ .]*(\\d+) from (.*?) via PhonePe for txn (T\\d+)|Rs[ .]*(\\d+) from (.+)")
            val matcher = pattern.matcher(text)

            if (matcher.find()) {
                val rupee: String? = matcher.group(1) ?: matcher.group(4)
                val nameOrNumber = matcher.group(2)?.trim() ?: matcher.group(5)?.trim()
                val txnId = matcher.group(3)
                println("amount: $rupee, name: $nameOrNumber, txn: $txnId")
                try {
                    if (rupee == null) return null
                    return PaymentDetails(rupee, nameOrNumber, txnId)
                } catch (e: Exception) {
                    e.printStackTrace()
                    println("Error occurred while extracting transaction details: ${e.message}")
                    return null
                }
            } else {
                println("No transaction details found in the text.")
                return null

            }
        }

        @SuppressLint("MissingPermission")
        fun showFullScreenPaymentNotification(
            context: Context,
            paymentDetails: PaymentDetails,
            messageText: String
        ) {
            // Create notification channel
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

            // Create full-screen intent
            NOTIFICATION_ID = (System.currentTimeMillis() / 10000).toInt()
            val fullScreenIntent = Intent(context, FullScreenPaymentActivity::class.java).apply {
                putExtra("paymentDetails",paymentDetails)
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
                .setContentTitle("Received Rs ${paymentDetails.amount}")
                .setContentText(messageText)
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

        fun saveTTSAsTempFile(
            context: Context,
            textToSpeech: TextToSpeech?,
            text: String,
            callback: (File) -> Unit
        ) {
            val fileName = "tts_output.mp3"
            val tempFile = File(context.filesDir, fileName)

            val params = Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "tts_output")
            textToSpeech?.setPitch(1.3f)
            textToSpeech?.setSpeechRate(0.9f)
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}

                override fun onDone(utteranceId: String?) {
                    callback(tempFile) // Return the file
                }

                override fun onError(utteranceId: String?) {
                    Log.e("TTS", "Error synthesizing TTS")
                }
            })

            textToSpeech?.synthesizeToFile(text, params, tempFile, "tts_output")
        }
    }
}