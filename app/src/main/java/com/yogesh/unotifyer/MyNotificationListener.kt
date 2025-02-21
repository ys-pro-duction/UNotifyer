package com.yogesh.unotifyer

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.telephony.SmsManager
import android.util.Log

private const val TAG = "MyNotificationListener"

class MyNotificationListener : NotificationListenerService() {
    private var componentName: ComponentName? = null

    override fun onCreate() {
        super.onCreate()
    }

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

    override fun onListenerConnected() {
        super.onListenerConnected()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()

        if (componentName == null) {
            componentName = ComponentName(this, this::class.java)
        }

        componentName?.let { requestRebind(it) }
    }

    private fun smsSendMessage(text: String) {
        val smsManager = getSystemService(SmsManager::class.java)
        smsManager.sendTextMessage(
            getSharedPreferences("sender", MODE_PRIVATE).getString("number", null), null, text,
            null, null
        )
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val packageName = sbn?.packageName ?: ""
        val extras = sbn?.notification?.extras
        val text = extras?.getCharSequence("android.text").toString()
        if (packageName.equals("com.phonepe.app.business")) {
            smsSendMessage(text)
            Log.d(TAG, "Notification Received: $text,    package name: $packageName")
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