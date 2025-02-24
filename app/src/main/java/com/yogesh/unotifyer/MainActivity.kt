package com.yogesh.unotifyer

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
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
                            openSenderSetup()
                        },
                        receiverSetup = { openReceiverSetup()},
                        senderReceiverChange = { toggleSenderOrReceiver(it) },
                        isRecieverOrSender = isReciever()
                    )
                }
            }
        }
        toggleSenderOrReceiver(isReciever())
//        Utils.showFullScreenPaymentNotification(this,PaymentDetails("100","","",""),"payment received")
//        toggleNotificationListenerService(true)
//        toggleSmsReciever(true)
    }

    private fun openReceiverSetup() {
        if (PermissionLogic.isAllReceiverPermissionsGranted(this)) {
            startActivity(Intent(this@MainActivity, RecieverActivity::class.java))
        }
    }

    private fun openSenderSetup() {
        if (PermissionLogic.isAllSenderPermissionsGranted(this)) {
            startActivity(Intent(this@MainActivity, SenderActivity::class.java))
        }
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        println(permissions)
        println(grantResults)
        println(deviceId)
        println(requestCode)
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