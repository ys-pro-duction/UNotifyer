package com.yogesh.unotifyer

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.util.ArrayList

object PermissionLogic {
    private var senderPermissionAskedFor = 0
    private var receiverPermissionAskedFor = 0

    private fun askPermissions(context: Activity,permissions: ArrayList<String>) {
        ActivityCompat.requestPermissions(context,permissions.toArray(arrayOf()),100)
    }

    private fun redirectoSettings(context: Activity) {
        Intent().let {
            it.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts(
                "package",
                context.packageName, null
            )
            it.setData(uri)
            context.startActivity(it)
        }
        Toast.makeText(context, "Allow all required permissions", Toast.LENGTH_SHORT).show()
    }

    fun isAllSenderPermissionsGranted(context: Activity): Boolean {
        val permissions = arrayListOf<String>()
        if (!isSmsPermissionGranted(context)){
            permissions.add(Manifest.permission.SEND_SMS)
            permissions.add(Manifest.permission.READ_SMS)
            permissions.add(Manifest.permission.RECEIVE_SMS)
        }
        if (!isReadNumberSimPermissionGranted(context)){
            permissions.add(Manifest.permission.READ_PHONE_STATE)
            permissions.add(Manifest.permission.READ_PHONE_NUMBERS)
        }
        if (!permissions.isEmpty()){
            if (senderPermissionAskedFor >= 2){
                redirectoSettings(context)
                return false
            }
            senderPermissionAskedFor++
            askPermissions(context,permissions)
            return false
        }else if (!isNotificationListenerPermissionGranted(context)){
            askNotifictionListenerPermissionRedirectToSettings(context)
            return false
        }else{
            return true
        }
    }
    fun isAllReceiverPermissionsGranted(context: Activity): Boolean {
        val permissions = arrayListOf<String>()
        if (!isSmsPermissionGranted(context)){
            permissions.add(Manifest.permission.SEND_SMS)
            permissions.add(Manifest.permission.READ_SMS)
            permissions.add(Manifest.permission.RECEIVE_SMS)
        }
        if (!isNotificationPermissionGranted(context)){
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (!permissions.isEmpty()){
            if (receiverPermissionAskedFor >= 2){
                redirectoSettings(context)
                return false
            }
            receiverPermissionAskedFor++
            askPermissions(context,permissions)
            return false
        }else{
            return true
        }
    }

    private fun askNotificationPermissions(context: Activity) {
        if (!isSmsPermissionGranted(context)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            ) {
                Intent().let {
                    it.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts(
                        "package",
                        context.packageName, null
                    )
                    it.setData(uri)
                    context.startActivity(it)
                }
                Toast.makeText(context, "Notification permissions required", Toast.LENGTH_SHORT).show()
            } else
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
        }
    }

    private fun askSimNumberReadPermissions(context: Activity) {
        if (!isReadNumberSimPermissionGranted(context)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    context,
                    Manifest.permission.READ_PHONE_STATE
                ) || ActivityCompat.shouldShowRequestPermissionRationale(
                    context,
                    Manifest.permission.READ_PHONE_NUMBERS
                )
            ) {
                Intent().let {
                    it.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts(
                        "package",
                        context.packageName, null
                    )
                    it.setData(uri)
                    context.startActivity(it)
                }
                Toast.makeText(context, "permissions required", Toast.LENGTH_SHORT).show()
            } else
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_PHONE_NUMBERS),
                    105
                )
        }
    }

    private fun isSmsPermissionGranted(context: Activity): Boolean {
        return context.checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
                && context.checkSelfPermission(Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
                && context.checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
    }

    private fun isReadNumberSimPermissionGranted(context: Activity): Boolean {
        return context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                && context.checkSelfPermission(Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("InlinedApi")
    private fun isNotificationPermissionGranted(context: Activity): Boolean {
        return context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    private fun askNotifictionListenerPermissionRedirectToSettings(context: Activity) {
        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            context.startActivity(this)
        }
    }

    private fun isNotificationListenerPermissionGranted(context: Activity): Boolean {
        val componentName = ComponentName(context, MyNotificationListener::class.java)
        val enabledListeners =
            Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        return enabledListeners?.contains(componentName.flattenToString()) ?: false
    }
}