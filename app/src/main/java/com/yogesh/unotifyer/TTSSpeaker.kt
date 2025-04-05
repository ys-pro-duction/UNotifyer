package com.yogesh.unotifyer

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.HashMap
import java.util.Locale

private const val TAG = "TTSSpeaker"
class TTSSpeaker(private val context: Context, private val totalRupees: Int) :
    TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "onInit: totalRupees $totalRupees")
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale("hi", "IN"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    println("Language not supported")
                } else {
                    val rupeeMap = HashMap<Int, Int>()
                    val rupeeSArray = arrayOf(
                        R.raw.s1,
                        R.raw.s2,
                        R.raw.s3,
                        R.raw.s4,
                        R.raw.s5,
                        R.raw.s6,
                        R.raw.s7,
                        R.raw.s8,
                        R.raw.s9,
                        R.raw.s10,
                        R.raw.s11,
                        R.raw.s12,
                        R.raw.s13,
                        R.raw.s14,
                        R.raw.s15,
                        R.raw.s16,
                        R.raw.s17,
                        R.raw.s18,
                        R.raw.s19,
                        R.raw.s20,
                        R.raw.s21,
                        R.raw.s22,
                        R.raw.s23,
                        R.raw.s24,
                        R.raw.s25,
                        R.raw.s26,
                        R.raw.s27,
                        R.raw.s28,
                        R.raw.s29,
                        R.raw.s30,
                        R.raw.s31,
                        R.raw.s32,
                        R.raw.s33,
                        R.raw.s34,
                        R.raw.s35,
                        R.raw.s36,
                        R.raw.s37,
                        R.raw.s38,
                        R.raw.s39,
                        R.raw.s40,
                        R.raw.s41,
                        R.raw.s42,
                        R.raw.s43,
                        R.raw.s44,
                        R.raw.s45,
                        R.raw.s46,
                        R.raw.s47,
                        R.raw.s48,
                        R.raw.s49,
                        R.raw.s50,
                        R.raw.s51,
                        R.raw.s52,
                        R.raw.s53,
                        R.raw.s54,
                        R.raw.s55,
                        R.raw.s56,
                        R.raw.s57,
                        R.raw.s58,
                        R.raw.s59,
                        R.raw.s60,
                        R.raw.s61,
                        R.raw.s62,
                        R.raw.s63,
                        R.raw.s64,
                        R.raw.s65,
                        R.raw.s66,
                        R.raw.s67,
                        R.raw.s68,
                        R.raw.s69,
                        R.raw.s70,
                        R.raw.s71,
                        R.raw.s72,
                        R.raw.s73,
                        R.raw.s74,
                        R.raw.s75,
                        R.raw.s76,
                        R.raw.s77,
                        R.raw.s78,
                        R.raw.s79,
                        R.raw.s80,
                        R.raw.s81,
                        R.raw.s82,
                        R.raw.s83,
                        R.raw.s84,
                        R.raw.s85,
                        R.raw.s86,
                        R.raw.s87,
                        R.raw.s88,
                        R.raw.s89,
                        R.raw.s90,
                        R.raw.s91,
                        R.raw.s92,
                        R.raw.s93,
                        R.raw.s94,
                        R.raw.s95,
                        R.raw.s96,
                        R.raw.s97,
                        R.raw.s98,
                        R.raw.s99
                    )
                    for (i in rupeeSArray.indices) {
                        rupeeMap[i + 1] = rupeeSArray[i]
                    }
                    rupeeMap[100] = R.raw.s100
                    rupeeMap[1000] = R.raw.s1000
                    addSpeech("pre", R.raw.pre)
                    addSpeech("post", R.raw.post)
                    addSpeech("phonepe", R.raw.phonepe)

                    playRupee(totalRupees, rupeeMap)
                }
            } else {
                println("Initialization failed")
            }
        }
    }

    private fun playRupee(amount: Int, rupeeMap: HashMap<Int, Int>) {
        speak("phonepe")
        speak("pre")
        if (amount < 100) {
            rupee(amount, rupeeMap)
        } else if (amount < 1000) {
            val rupeeText = amount.toString()
            val hundreds = rupeeText[0].toString().toInt()
            val nums = rupeeText.substring(1).toInt()
            hundreds(hundreds,rupeeMap)
            rupee(nums,rupeeMap)
        } else if (amount < 100000) {
            val rupeeText = amount.toString()
            val thousands = if (rupeeText.length < 5) rupeeText[0].toString().toInt() else rupeeText.substring(0, 2).toInt()
            val hundred = rupeeText[rupeeText.length-3].toString().toInt()
            val rupee = rupeeText.substring(rupeeText.length-2).toInt()
            thousands(thousands,rupeeMap)
            hundreds(hundred,rupeeMap)
            rupee(rupee,rupeeMap)
        }
        tts?.speak("post", TextToSpeech.QUEUE_ADD, null, "post")
    }

    private fun thousands(amount: Int, rupeeMap: HashMap<Int, Int>) {
        Log.d(TAG, "thousands: $amount")
        if (amount == 0) return
        rupee(amount, rupeeMap)
        addSpeech("1000", rupeeMap[1000]!!)
        speak("1000")
    }

    private fun rupee(amount: Int, rupeeMap: HashMap<Int, Int>) {
        Log.d(TAG, "rupee: $amount")
        if (amount == 0) return
        addSpeech("$amount", rupeeMap[amount]!!)
        speak("$amount")
    }
    private fun hundreds(amount: Int, rupeeMap: HashMap<Int, Int>){
        Log.d(TAG, "hundreds: $amount")
        rupee(amount, rupeeMap)
        if (amount == 0) return
        addSpeech("100", rupeeMap[100]!!)
        speak("100")
    }

    private fun addSpeech(text: String, resourceId: Int) {
        tts?.addSpeech(text, context.packageName, resourceId)
    }

    private fun speak(s: String) {
        tts?.speak(s, TextToSpeech.QUEUE_ADD, null, s)
    }

}