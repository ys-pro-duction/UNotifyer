package com.yogesh.unotifyer

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    private fun extractTransactionDetails(notification: String): PaymentDetails {
        // Updated regex with lookahead to stop capture before keywords
        val amountRegex = Regex(
            """(?:Rs\.?|₹)\s*(\X+?)(?=\s+(?:from|via|for|txn)\b|$)""",
            RegexOption.IGNORE_CASE
        )

        val senderRegex =
            Regex("""(?:from|via)\s+([A-Za-z\s.'*&]+?|\*{6}\d{4})(?:\s+(?:via|for|txn)|$)""")
        val txnIdRegex = Regex("""txn\s+([A-Za-z0-9]+)""")

        val amountMatch = amountRegex.find(notification)
        val amountStr = amountMatch?.groupValues?.getOrNull(1)?.replace(",", "")

        val amountInt: Int? = amountStr?.filter { it.isDigit() }?.toIntOrNull() ?: amountStr?.let {
            val normalDigits = mapOf(
                "0️⃣" to '0', "1️⃣" to '1', "2️⃣" to '2', "3️⃣" to '3', "4️⃣" to '4',
                "5️⃣" to '5', "6️⃣" to '6', "7️⃣" to '7', "8️⃣" to '8', "9️⃣" to '9',
                "𝟎" to '0', "𝟏" to '1', "𝟐" to '2', "𝟑" to '3', "𝟒" to '4',
                "𝟓" to '5', "𝟔" to '6', "𝟕" to '7', "𝟖" to '8', "𝟗" to '9',
                "\uD835\uDFF6" to '0', "\uD835\uDFF7" to '1', "\uD835\uDFF8" to '2',
                "\uD835\uDFF9" to '3', "\uD835\uDFFA" to '4', "\uD835\uDFFB" to '5',
                "\uD835\uDFFC" to '6', "\uD835\uDFFD" to '7', "\uD835\uDFFE" to '8',
                "\uD835\uDFFF" to '9', "𝟘" to '0', "𝟙" to '1', "𝟚" to '2',
                "𝟛" to '3', "𝟜" to '4', "𝟝" to '5', "𝟞" to '6', "𝟟" to '7',
                "𝟠" to '8', "𝟡" to '9', "𝟬" to '0', "𝟭" to '1', "𝟮" to '2', "𝟯" to '3',
                "𝟰" to '4', "𝟱" to '5', "𝟲" to '6', "𝟳" to '7', "𝟴" to '8', "𝟵" to '9'
            )

            Regex("""\X""")  // split into grapheme clusters
                .findAll(it)
                .mapNotNull { g -> normalDigits[g.value] }
                .joinToString("")
                .toIntOrNull()
        }

        val senderMatch = senderRegex.find(notification)
        val sender = senderMatch?.groupValues?.getOrNull(1)?.trim()

        val txnIdMatch = txnIdRegex.find(notification)
        val txnId = txnIdMatch?.groupValues?.getOrNull(1)?.trim()
        if (amountInt == null || amountInt <= 0) throw Exception("Amount is null or <= zero")
        return PaymentDetails(amountInt.toString(), sender, txnId,notification)
    }


    fun main() {
        val notifications = mapOf(
            Pair("You've received Rs.5 from Yogesh swami", 5),
            Pair("You've received Rs.𝟒𝟎 from Yogesh swami", 40),
            Pair("You've received Rs.2️⃣5️⃣ from Yogesh swami", 25),
            Pair("You've received Rs.𝟸 from Yogesh swami", 2),
            Pair("You've received Rs.50 from Yogesh Swami", 50),
            Pair("You've received Rs.𝟓 from Yogesh Swami", 5),
            Pair("You've received Rs.𝟰𝟬 from Yogesh Swami", 40),
            Pair(
                "You've received Rs 5 from ******3082 via PhonePe for txn T2503262136193430608151.",
                5
            ),
            Pair(
                "You've received Rs 35 from Yogesh swami via PhonePe for txn T2503282015099540319782.",
                35
            ),
            Pair(
                "You've received Rs 𝟙𝟛𝟝 from Yogesh swami via PhonePe for txn T2504051656194141152025.",
                135
            ),Pair(
                "You've received Rs 𝟷𝟶𝟼 from ******8253 via PhonePe for txn T2504061007266922191903.",
                106
            )
        )

        notifications.forEach { notification ->
            val (amount, sender, txnId) = extractTransactionDetails(notification.key)
            println("Notification: $notification")
            print(" Amount: $amount")
            assert(amount.toInt() == notification.value)
            print(" Sender: $sender")
            print(" Transaction ID: $txnId")
            println()
            println()
        }
//        data.forEach { notification ->
//            val (amount, sender, txnId) = extractTransactionDetails(notification.key)
//            println(notification)
//            println("                   $amount")
//            println()
//        }
    }


    @Test
    fun addition_isCorrect() {
        main()

    }

}

