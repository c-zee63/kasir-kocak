# KasirTokoQ

Aplikasi Kasir (POS) Android **offline-first** untuk toko & UMKM.

## Fitur yang sudah diimplementasikan

### Database (Room + SQLite)
- Settings toko (nama, alamat, telepon, footnote struk)
- Kategori & Produk (nama, barcode, harga jual, harga modal, stok, unit, foto path)
- Transaksi penjualan + detail item + profit otomatis
- Stock movements (riwayat masuk/keluar stok)
- Shipping labels (resi pengiriman)

### Layar Utama
- **Beranda** — ringkasan omzet, profit, jumlah transaksi, stok menipis (real-time)
- **Kasir / POS** — cari produk, tambah ke keranjang, ubah qty, bayar (tunai/transfer/QRIS), stok otomatis berkurang
- **Produk** — daftar produk + form tambah produk baru
- **Laporan** — stok menipis, riwayat transaksi, export produk ke CSV (bisa dibuka Excel)
- **Pengaturan** — simpan data toko & footnote struk

### Utilitas
- `ThermalPrinterHelper` — siap cetak struk ESC/POS via Bluetooth + perintah buka cash drawer
- `ExcelExporter` — export CSV
- `DateTimeUtils` — format tanggal & mata uang Rupiah

## Cara Build

### Android Studio (disarankan)
1. Buka folder `KasirTokoQ` di Android Studio
2. Biarkan Gradle sync (akan generate wrapper jika belum ada)
3. Run di emulator / device (minSdk 26)

### GitHub Actions
Push ke repository GitHub → workflow `build-apk.yml` akan otomatis build APK debug.  
Unduh dari tab **Actions** → Artifacts.

### Command line
```bash
# Generate wrapper dulu jika belum ada
gradle wrapper --gradle-version 8.9

./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

## Struktur Utama

```
app/src/main/java/com/kasirtokoq/app/
├── data/
│   ├── local/          # Room entities, DAO, AppDatabase
│   └── repository/     # KasirRepository + CartItem
├── viewmodel/          # PosViewModel, ProductViewModel, HomeViewModel, SettingsViewModel
├── ui/screens/         # Semua layar Compose
├── utils/              # Printer, Excel, DateTime
├── MainActivity.kt
└── KasirTokoQApp.kt
```

## Langkah Selanjutnya (opsional)

- Integrasi CameraX + ML Kit untuk scan barcode kamera sungguhan (dependency sudah ada)
- Panggil `ThermalPrinterHelper` setelah checkout berhasil
- Tambah form edit produk & void transaksi
- Generate barcode image untuk produk baru

---

Project siap di-build. Semua alur inti (tambah produk → jual → stok berkurang → laporan → export) sudah berjalan.
