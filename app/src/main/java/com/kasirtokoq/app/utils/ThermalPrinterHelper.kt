package com.kasirtokoq.app.utils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import java.io.OutputStream
import java.util.UUID

/**
 * Helper sederhana untuk mencetak ke printer thermal Bluetooth ESC/POS.
 * Cocok untuk printer 58mm / 80mm (Xprinter, RPP02, dll).
 */
class ThermalPrinterHelper(private val context: Context) {

    companion object {
        private const val TAG = "ThermalPrinter"
        private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    private var socket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    fun getPairedPrinters(): List<BluetoothDevice> {
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return emptyList()
        return adapter.bondedDevices?.toList() ?: emptyList()
    }

    fun connect(device: BluetoothDevice): Boolean {
        return try {
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            socket?.connect()
            outputStream = socket?.outputStream
            true
        } catch (e: Exception) {
            Log.e(TAG, "Connect failed", e)
            disconnect()
            false
        }
    }

    fun disconnect() {
        try {
            outputStream?.close()
            socket?.close()
        } catch (_: Exception) {
        } finally {
            outputStream = null
            socket = null
        }
    }

    fun isConnected(): Boolean = socket?.isConnected == true

    // ESC/POS commands
    private fun write(bytes: ByteArray) {
        outputStream?.write(bytes)
        outputStream?.flush()
    }

    fun printText(text: String, bold: Boolean = false, center: Boolean = false) {
        val cmd = StringBuilder()
        if (center) cmd.append("\u001Ba\u0001") // center
        else cmd.append("\u001Ba\u0000") // left
        if (bold) cmd.append("\u001BE\u0001")
        cmd.append(text)
        if (bold) cmd.append("\u001BE\u0000")
        cmd.append("\n")
        write(cmd.toString().toByteArray(Charsets.UTF_8))
    }

    fun printLine(char: String = "-", width: Int = 32) {
        printText(char.repeat(width))
    }

    fun printReceipt(
        storeName: String,
        storeAddress: String,
        invoice: String,
        items: List<Triple<String, Int, Double>>, // name, qty, subtotal
        total: Double,
        payment: String,
        change: Double,
        footnote: String
    ) {
        printText(storeName, bold = true, center = true)
        printText(storeAddress, center = true)
        printLine()
        printText("No: $invoice")
        printText("Tanggal: ${DateTimeUtils.now()}")
        printLine()
        items.forEach { (name, qty, sub) ->
            printText(name)
            printText("  $qty x ${DateTimeUtils.formatCurrency(sub / qty)} = ${DateTimeUtils.formatCurrency(sub)}")
        }
        printLine()
        printText("TOTAL: ${DateTimeUtils.formatCurrency(total)}", bold = true)
        printText("Bayar ($payment): ${DateTimeUtils.formatCurrency(total + change)}")
        if (change > 0) printText("Kembali: ${DateTimeUtils.formatCurrency(change)}")
        printLine()
        printText(footnote, center = true)
        printText("\n\n\n") // feed paper
        // cut if supported
        write(byteArrayOf(0x1D, 0x56, 0x00))
    }

    fun openCashDrawer() {
        // Standard ESC/POS open drawer command
        write(byteArrayOf(0x1B, 0x70, 0x00, 0x19, 0xFA.toByte()))
    }
}
