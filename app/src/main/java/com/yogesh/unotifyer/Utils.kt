package com.yogesh.unotifyer

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
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
        fun extractTransactionDetails_old(text: String): PaymentDetails? {
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
        fun extractTransactionDetails(notification: String): PaymentDetails? {
            // Updated regex with lookahead to stop capture before keywords
            val amountRegex = Regex(
                """(?:Rs\.?|₹)\s*(\X+?)(?=\s+(?:from|via|for|txn)\b|$)""",
                RegexOption.IGNORE_CASE
            )

            val senderRegex =
                Regex("""(?:from|via)\s+([A-Za-z\s.'*&]+?|\*{6}\d{4})(?:\s+(?:via|for|txn)|$)""")
            val txnIdRegex = Regex("""txn\s+([A-Za-z0-9]+)""")

            val amountMatch = amountRegex.find(notification)
            val amountStr = amountMatch?.groupValues?.getOrNull(1)?.replace(",", "")

            val amountInt: Int? = amountStr?.filter { it.isDigit() }?.toIntOrNull() ?: amountStr?.let {
                val normalDigits = mapOf(
                    "0️⃣" to '0', "1️⃣" to '1', "2️⃣" to '2', "3️⃣" to '3', "4️⃣" to '4',
                    "5️⃣" to '5', "6️⃣" to '6', "7️⃣" to '7', "8️⃣" to '8', "9️⃣" to '9',
                    "𝟎" to '0', "𝟏" to '1', "𝟐" to '2', "𝟑" to '3', "𝟒" to '4',
                    "𝟓" to '5', "𝟔" to '6', "𝟕" to '7', "𝟖" to '8', "𝟗" to '9',
                    "\uD835\uDFF6" to '0', "\uD835\uDFF7" to '1', "\uD835\uDFF8" to '2',
                    "\uD835\uDFF9" to '3', "\uD835\uDFFA" to '4', "\uD835\uDFFB" to '5',
                    "\uD835\uDFFC" to '6', "\uD835\uDFFD" to '7', "\uD835\uDFFE" to '8',
                    "\uD835\uDFFF" to '9', "𝟘" to '0', "𝟙" to '1', "𝟚" to '2',
                    "𝟛" to '3', "𝟜" to '4', "𝟝" to '5', "𝟞" to '6', "𝟟" to '7',
                    "𝟠" to '8', "𝟡" to '9',"𝟬" to '0', "𝟭" to '1', "𝟮" to '2', "𝟯" to '3',
                    "𝟰" to '4', "𝟱" to '5', "𝟲" to '6', "𝟳" to '7', "𝟴" to '8', "𝟵" to '9'
                )

                Regex("""\X""")  // split into grapheme clusters
                    .findAll(it)
                    .mapNotNull { g -> normalDigits[g.value] }
                    .joinToString("")
                    .toIntOrNull()
            }


            val senderMatch = senderRegex.find(notification)
            val sender = senderMatch?.groupValues?.getOrNull(1)?.trim()

            val txnIdMatch = txnIdRegex.find(notification)
            val txnId = txnIdMatch?.groupValues?.getOrNull(1)?.trim()
            if (amountInt == null || amountInt <= 0) return null
            return PaymentDetails(amountInt.toString(),sender,txnId,notification)
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
//            val fullScreenIntent = Intent(context, FullScreenPaymentActivity::class.java).apply {
//                putExtra("paymentDetails", paymentDetails)
//            }
            TTSSpeaker(context, paymentDetails.amount.toInt())
            val fullScreenIntent = Intent(context, PhonePeUi::class.java).apply {
                putExtra("paymentDetails", paymentDetails)
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

        @SuppressLint("MissingPermission")
        internal fun getSimCardNumbers(context: Context): ArrayList<String>? {
            val subscriptionManager = context.getSystemService(SubscriptionManager::class.java)
            val subscriptionInfoList = subscriptionManager.activeSubscriptionInfoList

            if (subscriptionInfoList != null) {
                val numbers = arrayListOf<String>()
                for (i in 0 until subscriptionInfoList.size) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        numbers.add(subscriptionManager.getPhoneNumber(subscriptionInfoList[i].subscriptionId))
                    } else {
                        numbers.add(subscriptionInfoList[i].number)
                    }
                }
                return numbers
            }
            return null
        }

        @SuppressLint("MissingPermission")
        internal fun getSmsManager(context: Context, simIndex: Int): SmsManager? {
            try {
                val subscriptionManager = context.getSystemService(SubscriptionManager::class.java)
                val subscriptionInfoList = subscriptionManager.activeSubscriptionInfoList
                if (subscriptionInfoList != null) {
                    val subscriptionId = subscriptionInfoList[simIndex].subscriptionId
                    val smsManager: SmsManager =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            context.getSystemService(SmsManager::class.java)
                                .createForSubscriptionId(subscriptionId)
                        } else {
                            SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
                        }
                    return smsManager
                }
            } catch (e: IndexOutOfBoundsException) {
                return null
            }
            return null
        }
    }
}