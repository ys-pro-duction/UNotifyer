package com.yogesh.unotifyer

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.widget.Toast

class MyNotificationListener : NotificationListenerService() {
    private var componentName: ComponentName? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (componentName == null) {
            componentName = ComponentName(this, this::class.java)
        }

        componentName?.let {
            requestRebind(it)
            toggleNotificationListenerService(it)
        }
        return START_REDELIVER_INTENT
    }

    private fun toggleNotificationListenerService(componentName: ComponentName) {
        val pm = packageManager
        pm.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        pm.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()

        if (componentName == null) {
            componentName = ComponentName(this, this::class.java)
        }

        componentName?.let { requestRebind(it) }
    }

    private fun smsSendMessage(text: String) {
        println(text)
        val number = getSharedPreferences("sender", MODE_PRIVATE).getString("number", null)
        val simIndex = getSharedPreferences("sender", MODE_PRIVATE).getInt("sim", 0)
        val smsManager = Utils.getSmsManager(this, simIndex)
        val paymentDetails = Utils.extractTransactionDetails(text) ?: return
        println(paymentDetails)
        if (smsManager != null) {
            smsManager.sendTextMessage(number, null, paymentDetails.notificationText, null, null)
        } else {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val packageName = sbn?.packageName ?: ""
        val extras = sbn?.notification?.extras
        val text = extras?.getCharSequence("android.text").toString()
        if (packageName.equals("com.phonepe.app.business")) {
            smsSendMessage(text)
        }
        if (packageName.equals("com.android.shell")) {
            smsSendMessage(text)
        }
//            FullScreenPaymentActivity.Get.showFullScreenPaymentNotification(
//                this,
//                text,
//                "99:99 PM"
//            )
    }
}