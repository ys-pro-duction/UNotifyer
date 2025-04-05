package com.yogesh.unotifyer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yogesh.unotifyer.Utils.Companion.ACTION_FINISH_ACTIVITY

class PhonePeUi : ComponentActivity() {
    private val finishReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (ACTION_FINISH_ACTIVITY == intent?.action) {
                finish()
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val paymentDetails = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(
                "paymentDetails",
                PaymentDetails::class.java
            ) as PaymentDetails
        } else {
            intent.getSerializableExtra("paymentDetails") as PaymentDetails
        }
        setContent {
            NewPaymentScreen({finishAndRemoveTask()},paymentDetails)
        }
//        TTSSpeaker(this, paymentDetails.amount.toInt())
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
    }

    override fun onDestroy() {
        unregisterReceiver(finishReceiver)
        super.onDestroy()
    }
}

@Composable
fun NewPaymentScreen(closePaymentScreen: () -> Unit, paymentDetails: PaymentDetails) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF017a0c)), // Green background
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        TopBar(closePaymentScreen)
        Spacer(modifier = Modifier.height(32.dp))
        Text("New Payment!", style = TextStyle(color = Color.White, fontSize = 36.sp))
        Spacer(modifier = Modifier.height(32.dp))
        PaymentCard(rupees = paymentDetails.amount, from = paymentDetails.name.toString())
    }
}

@Composable
fun TopBar(closePaymentScreen: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = "Close",
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(48.dp)
                .padding(8.dp).clickable(onClick = closePaymentScreen)
        )
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(Color.White, CircleShape)
                .wrapContentSize(Alignment.Center)
                .align(Alignment.Center)
        ) {
            Text(
                text = "पे",
                style = TextStyle(
                    color = Color(0xFF017a0c),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                ),
                fontFamily = FontFamily.Cursive, modifier = Modifier.padding(top = 10.dp, end = 2.dp)
            )
        }
    }
}

@Composable
fun PaymentCard(rupees: String = "123", from: String = "Yogesh Swami") {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(16.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(start = 16.dp, bottom = 16.dp, end = 16.dp,top = 4.dp,)
        ) {
            Text(
                text = "now",
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Color.Black.copy(alpha = 0.6f)
                ), modifier = Modifier.align(Alignment.End)
            )
            Text(
                text = "₹ $rupees",
                style = TextStyle(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "From $from",
                style = TextStyle(
                    fontSize = 18.sp,
                    color = Color.Black.copy(0.7f)
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    NewPaymentScreen({}, PaymentDetails("100"))
}