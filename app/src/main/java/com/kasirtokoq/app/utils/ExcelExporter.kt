package com.kasirtokoq.app.utils

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileWriter

/**
 * Export data ke file CSV (bisa dibuka di Excel).
 * Lebih ringan & stabil di Android dibanding full Apache POI.
 */
object ExcelExporter {

    fun exportSalesToCsv(
        context: Context,
        rows: List<List<String>>,
        fileName: String = "laporan_penjualan_${DateTimeUtils.today()}.csv"
    ): File? {
        return try {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                ?: context.filesDir
            val file = File(dir, fileName)
            FileWriter(file).use { writer ->
                rows.forEach { row ->
                    writer.append(row.joinToString(",") { "\"${it.replace("\"", "\"\"")}\"" })
                    writer.append("\n")
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportProductsToCsv(
        context: Context,
        products: List<com.kasirtokoq.app.data.local.entities.ProductEntity>
    ): File? {
        val header = listOf("ID", "Nama", "Barcode", "Harga Jual", "Harga Modal", "Stok", "Unit")
        val rows = mutableListOf(header)
        products.forEach { p ->
            rows.add(
                listOf(
                    p.id.toString(),
                    p.name,
                    p.barcode ?: "",
                    p.price.toString(),
                    p.costPrice.toString(),
                    p.stock.toString(),
                    p.unit
                )
            )
        }
        return exportSalesToCsv(context, rows, "produk_${DateTimeUtils.today()}.csv")
    }
}
