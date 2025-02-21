package com.yogesh.unotifyer

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yogesh.unotifyer.ui.theme.UNotifyerTheme


class SenderActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UNotifyerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SenderMainView(
                        modifier = Modifier.padding(innerPadding),
                        receiverNumber = getSharedPreferences(
                            "sender",
                            Context.MODE_PRIVATE
                        ).getString("number", "")
                            .toString(),
                        saveNumber = { number: String -> saveNumber(number) }
                    )
                }
            }
        }
    }

    private fun saveNumber(number: String) {
        getSharedPreferences("sender", Context.MODE_PRIVATE).edit().putString("number", number)
            .apply()
    }
}

@Composable
fun SenderMainView(
    modifier: Modifier = Modifier,
    saveNumber: (number: String) -> Unit,
    receiverNumber: String
) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Setup to send notification",
            modifier = modifier
        )
        var mobileNumber by remember { mutableStateOf(receiverNumber) }

        TextField(
            value = mobileNumber,
            onValueChange = { mobileNumber = it },
            label = { Text("Receiver mobile number") },
            placeholder = { Text("example: +911234567890") }
        )
        Row(
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Button(onClick = {
                saveNumber(mobileNumber)
            }) {
                Text("Save")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    UNotifyerTheme {
        SenderMainView(
            saveNumber = {},
            receiverNumber = ""
        )
    }
}