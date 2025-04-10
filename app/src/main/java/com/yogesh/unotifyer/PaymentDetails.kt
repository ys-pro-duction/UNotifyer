package com.yogesh.unotifyer

import java.io.Serializable


data class PaymentDetails(val amount: String, val name: String? = "", val txn: String? = "", var time: String = "",var notificationText: String) : Serializable