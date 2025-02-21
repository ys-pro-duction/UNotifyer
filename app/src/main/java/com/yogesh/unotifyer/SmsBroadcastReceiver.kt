package com.yogesh.unotifyer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.provider.Telephony.Sms.Intents.SMS_RECEIVED_ACTION
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val TAG = "SmsBroadcastReceiver"

class SmsBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == SMS_RECEIVED_ACTION) {

            val smsMessages =
                Telephony.Sms.Intents.getMessagesFromIntent(intent)

            val phoneNumber = smsMessages[0].displayOriginatingAddress

            val smsMessageBuilder = StringBuilder()

            for (message in smsMessages) {
                smsMessageBuilder.append(message.displayMessageBody)
                smsMessageBuilder.append("\n")
            }
            context.getSharedPreferences("reciever", Context.MODE_PRIVATE).getString("number", null)
                ?.let {

                    if (it.equals(phoneNumber)) {
                        val sdf = DateTimeFormatter.ofPattern("hh:mm a")
                        val dt = LocalDateTime.now().format(sdf)
                        performTaskOnSmsReceived(context,
                            smsMessageBuilder.toString(),dt
                        )
                    }
                }
        }
    }

    private fun performTaskOnSmsReceived(context: Context, messageBody: String, dt: String) {
        FullScreenPaymentActivity.Get.showFullScreenPaymentNotification(
            context,
            messageBody,dt
        )
    }

}