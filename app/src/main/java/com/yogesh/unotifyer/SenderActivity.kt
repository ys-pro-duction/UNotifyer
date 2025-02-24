package com.yogesh.unotifyer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.semantics.Role
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
                            MODE_PRIVATE
                        ).getString("number", "")
                            .toString(),
                        saveNumber = { number: String -> saveNumber(number) },
                        saveSimChoosen = { sim: Int -> saveSimChoosen(sim) },
                        selectedSimCard = getSimChoosen(),
                        simCardNumbers = Utils.getSimCardNumbers(this),
                    )
                }
            }
        }
    }

    private fun getSimChoosen(): Int {
        return getSharedPreferences("sender", MODE_PRIVATE).getInt("sim",0)
    }

    private fun saveSimChoosen(sim: Int) {
        getSharedPreferences("sender", MODE_PRIVATE).edit().putInt("sim", sim)
            .apply()
    }

    private fun saveNumber(number: String) {
        getSharedPreferences("sender", MODE_PRIVATE).edit().putString("number", number)
            .apply()
    }
}

@Composable
fun SenderMainView(
    modifier: Modifier = Modifier,
    saveNumber: (number: String) -> Unit,
    receiverNumber: String,
    saveSimChoosen: (sim: Int) -> Unit,
    simCardNumbers: ArrayList<String>?,
    selectedSimCard: Int
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
        Button(onClick = {
            saveNumber(mobileNumber)
        }, modifier = Modifier.padding(top = 12.dp)) {
            Text("Save")
        }
        Text(
            text = "From Sim card",
            modifier = modifier.padding(top = 12.dp)
        )
        simCardNumbers?.let { RadioButtonSingleSelection(it,selectedSimCard,saveSimChoosen) }
    }
}

@Composable
fun RadioButtonSingleSelection(
    simNumber: ArrayList<String>,
    selectedSimCard: Int,
    saveSimChoosen: (sim: Int) -> Unit
) {
    val simCard = if (selectedSimCard > simNumber.size-1){
        saveSimChoosen(simNumber.size-1)
        simNumber.size-1
    }else selectedSimCard
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(simNumber[simCard]) }

    Row(Modifier.selectableGroup()) {
        simNumber.forEach { text ->
            Row(
                Modifier
                    .height(56.dp)
                    .selectable(
                        selected = (text == selectedOption),
                        onClick = { },
                        role = Role.RadioButton
                    ).clickable(onClick = {onOptionSelected(text)
                                          saveSimChoosen(simNumber.indexOf(text))},indication = null, interactionSource = remember { MutableInteractionSource() })
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest,
                        shape = RoundedCornerShape(CornerSize(16.dp))
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (text == selectedOption),
                    onClick = null,
                    modifier = Modifier.padding(start = 8.dp)
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 4.dp,end = 8.dp)
                )
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
            receiverNumber = "",
            saveSimChoosen = { },
            simCardNumbers = arrayListOf("+911234567890","+919876543210"),
            selectedSimCard = 1,
        )
    }
}