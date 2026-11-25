# KasirTokoQ — Database Schema (SQLite)

File database: **`kasirtokoq.db`**

## Ringkasan

Database SQLite lengkap untuk sistem kasir Android (POS) dengan fitur:

- Struk digital + data toko (logo, nama, alamat, footnote)
- Produk + barcode + foto + harga jual & modal
- Stok otomatis berkurang saat penjualan
- Pencatatan profit per item & per transaksi
- Resi pengiriman (shipping label)
- Riwayat pergerakan stok
- View siap pakai untuk laporan (harian, profit produk, stok menipis)
- Trigger otomatis untuk stok & void transaksi

Mode: **WAL** (Write-Ahead Logging) — aman & cepat untuk aplikasi Android.

---

## Daftar Tabel

| Tabel | Fungsi |
|-------|--------|
| `settings` | Data toko (nama, alamat, logo, footnote struk, pajak, dll) — hanya 1 baris |
| `categories` | Kategori produk |
| `products` | Master barang (nama, barcode, harga, stok, foto, dll) |
| `sales` | Header transaksi penjualan |
| `sale_items` | Detail item yang terjual |
| `stock_movements` | Riwayat masuk/keluar stok |
| `shipping_labels` | Resi pengiriman |
| `purchases` + `purchase_items` | Pembelian stok dari supplier (opsional) |

## View (Laporan Siap Pakai)

| View | Kegunaan |
|------|----------|
| `v_daily_sales` | Ringkasan penjualan per hari (omzet + profit) |
| `v_product_profit` | Profit per produk |
| `v_low_stock` | Daftar barang yang stoknya menipis |
| `v_sale_details` | Detail lengkap setiap transaksi |

## Trigger Otomatis

- `trg_sale_item_insert` → otomatis kurangi stok + catat `stock_movements` saat item dijual
- `trg_sale_void` → otomatis kembalikan stok saat transaksi di-void
- Trigger `updated_at` untuk products, settings, sales

---

## Cara Pakai di Android (Room / SQLite)

1. Copy file `kasirtokoq.db` ke folder assets atau download di first-run.
2. Buka dengan Room atau `SQLiteOpenHelper` / `SupportSQLiteDatabase`.
3. Selalu jalankan:
   ```kotlin
   db.execSQL("PRAGMA foreign_keys = ON")
   db.execSQL("PRAGMA journal_mode = WAL")
   ```
4. Untuk generate barcode/QR: simpan nilai di kolom `products.barcode`.
5. Foto produk: simpan path file di `products.photo_path` (jangan BLOB besar).

## Contoh Query Penting

```sql
-- Ambil data toko untuk struk
SELECT * FROM settings WHERE id = 1;

-- Cari produk by barcode (scan kamera)
SELECT * FROM products WHERE barcode = ? AND is_active = 1;

-- Laporan penjualan hari ini
SELECT * FROM v_daily_sales WHERE sale_date = date('now', 'localtime');

-- Stok menipis
SELECT * FROM v_low_stock;

-- Profit per produk
SELECT * FROM v_product_profit ORDER BY total_profit DESC LIMIT 20;
```

## Alur Transaksi Penjualan (disarankan di kode)

1. Mulai transaksi SQL (`BEGIN`)
2. Insert ke `sales` (dapatkan `sale_id`)
3. Insert semua item ke `sale_items`  
   → trigger otomatis mengurangi stok
4. Update `sales.total_profit` & `grand_total` jika perlu
5. `COMMIT`

Jika ingin void:
```sql
UPDATE sales SET is_void = 1, void_reason = '...' WHERE id = ?
```
→ trigger otomatis mengembalikan stok.

---

## Struktur File di Project

```
artifacts/
├── kasirtokoq.db                  ← database siap pakai (sudah ada sample data)
└── KasirTokoQ_Database_Schema.md  ← dokumentasi ini
```

Database sudah berisi:
- 1 pengaturan toko demo
- 5 kategori
- 12 produk contoh
- 1 transaksi penjualan contoh + 3 item
- 1 resi pengiriman contoh
- Stock movements otomatis tercatat

Silakan ganti data demo dengan data toko Anda.
