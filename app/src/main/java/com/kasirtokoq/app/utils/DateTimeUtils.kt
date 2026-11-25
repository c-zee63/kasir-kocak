package com.kasirtokoq.app.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateTimeUtils {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private val dateOnly = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val invoiceFmt = DateTimeFormatter.ofPattern("yyyyMMdd")

    fun now(): String = LocalDateTime.now().format(formatter)

    fun today(): String = LocalDateTime.now().format(dateOnly)

    fun generateInvoiceNumber(prefix: String = "KTQ"): String {
        val datePart = LocalDateTime.now().format(invoiceFmt)
        val random = (100..999).random()
        return "$prefix$datePart$random"
    }

    fun formatCurrency(amount: Double): String {
        return "Rp ${"%,.0f".format(amount).replace(',', '.')}"
    }
}
