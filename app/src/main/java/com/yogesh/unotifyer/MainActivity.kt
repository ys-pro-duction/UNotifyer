package com.yogesh.unotifyer

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.yogesh.unotifyer.ui.theme.UNotifyerTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UNotifyerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        modifier = Modifier.padding(innerPadding),
                        senderSetup = {
                            startActivity(Intent(this@MainActivity, SenderActivity::class.java))
                        },
                        receiverSetup = {
                            startActivity(
                                Intent(
                                    this@MainActivity,
                                    RecieverActivity::class.java
                                )
                            )
                        },
                        senderReceiverChange = { toggleSenderOrReceiver(it) },
                        isRecieverOrSender = isReciever()
                    )
                }
            }
        }
        if (!isNotificationListenerPermissionGranted()) redirectToSettings()
        if (!isSmsPermissionGranted()) askSmsPermissions()
        if (!isNotificationPermissionGranted()) askNotificationPermissions()
        toggleSenderOrReceiver(isReciever())
    }

    private fun isReciever(): Boolean {
        return getSharedPreferences("app", MODE_PRIVATE).getBoolean("reciever", false)
    }

    private fun toggleSenderOrReceiver(isReciever: Boolean) {
        getSharedPreferences("app", MODE_PRIVATE).edit().putBoolean("reciever", isReciever).apply()
        if (isReciever) {
            toggleNotificationListenerService(false)
            toggleSmsReciever(true)
        } else {
            toggleNotificationListenerService(true)
            toggleSmsReciever(false)
        }
    }

    private fun toggleNotificationListenerService(enabled: Boolean) {
        val pm = packageManager
        val componentName = ComponentName(this, MyNotificationListener::class.java)
        pm.setComponentEnabledSetting(
            componentName,
            if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    private fun toggleSmsReciever(enabled: Boolean) {
        val pm = packageManager
        val componentName = ComponentName(this, SmsBroadcastReceiver::class.java)
        pm.setComponentEnabledSetting(
            componentName,
            if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    private fun askSmsPermissions() {
        if (!isSmsPermissionGranted()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS
                ),
                102
            )

        }
    }

    private fun askNotificationPermissions() {
        if (!isSmsPermissionGranted()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                101
            )

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        askNotificationPermissions()
        if (!isNotificationListenerPermissionGranted()) redirectToSettings()
    }

    private fun isSmsPermissionGranted(): Boolean {
        return checkSelfPermission("android.permission.SEND_SMS") == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission("android.permission.RECEIVE_SMS") == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission("android.permission.READ_SMS") == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("InlinedApi")
    private fun isNotificationPermissionGranted(): Boolean {
        return checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    private var someActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
        }

    private fun redirectToSettings() {
        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            someActivityResultLauncher.launch(this)
        }
    }

    private fun isNotificationListenerPermissionGranted(): Boolean {
        val componentName = ComponentName(this, MyNotificationListener::class.java)
        val enabledListeners =
            Settings.Secure.getString(this.contentResolver, "enabled_notification_listeners")
        return enabledListeners?.contains(componentName.flattenToString()) ?: false
    }

    @Composable
    fun Greeting(
        modifier: Modifier = Modifier,
        senderSetup: () -> Unit,
        receiverSetup: () -> Unit,
        senderReceiverChange: (Boolean) -> Unit,
        isRecieverOrSender: Boolean
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.fillMaxSize()
        ) {
            var isReciever by remember { mutableStateOf(isRecieverOrSender) }
            Text(text = "I am a")
            Button(senderSetup, enabled = !isReciever) {
                Text("Sender")
            }
            Switch(
                isReciever,
                modifier = Modifier
                    .rotate(90f)
                    .padding(4.dp),
                onCheckedChange = { checked ->
                    isReciever = checked
                    senderReceiverChange(isReciever)
                },
                colors = SwitchDefaults.colors(
                    uncheckedTrackColor = SwitchDefaults.colors().checkedTrackColor,
                    uncheckedThumbColor = SwitchDefaults.colors().checkedThumbColor,
                    uncheckedBorderColor = SwitchDefaults.colors().checkedBorderColor
                )
            )
            Button(receiverSetup, enabled = isReciever) {
                Text("Receiver")
            }
        }
    }
}