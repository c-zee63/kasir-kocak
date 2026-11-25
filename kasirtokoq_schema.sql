-- KasirTokoQ Complete Schema
-- Generated for Android POS

PRAGMA foreign_keys = ON;
PRAGMA journal_mode = WAL;

CREATE TABLE categories (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT NOT NULL UNIQUE,
        description TEXT DEFAULT '',
        color TEXT DEFAULT '#3B82F6',
        is_active INTEGER NOT NULL DEFAULT 1,
        created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
        updated_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
    );

CREATE TABLE products (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        category_id INTEGER REFERENCES categories(id) ON DELETE SET NULL,
        name TEXT NOT NULL,
        barcode TEXT UNIQUE,
        sku TEXT,
        description TEXT DEFAULT '',
        price REAL NOT NULL CHECK (price >= 0),           -- harga jual
        cost_price REAL NOT NULL DEFAULT 0 CHECK (cost_price >= 0),  -- harga modal
        stock INTEGER NOT NULL DEFAULT 0,
        min_stock INTEGER NOT NULL DEFAULT 0,
        unit TEXT NOT NULL DEFAULT 'pcs',
        photo_path TEXT DEFAULT '',
        is_active INTEGER NOT NULL DEFAULT 1,
        created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
        updated_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
    );

CREATE TABLE purchase_items (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        purchase_id INTEGER NOT NULL REFERENCES purchases(id) ON DELETE CASCADE,
        product_id INTEGER NOT NULL REFERENCES products(id),
        qty INTEGER NOT NULL CHECK (qty > 0),
        unit_cost REAL NOT NULL,
        subtotal REAL NOT NULL,
        created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
    );

CREATE TABLE purchases (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        reference_number TEXT UNIQUE,
        supplier_name TEXT DEFAULT '',
        total_cost REAL NOT NULL DEFAULT 0,
        notes TEXT DEFAULT '',
        created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
    );

CREATE TABLE sale_items (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        sale_id INTEGER NOT NULL REFERENCES sales(id) ON DELETE CASCADE,
        product_id INTEGER REFERENCES products(id) ON DELETE SET NULL,
        product_name TEXT NOT NULL,               -- denormalized (untuk history)
        barcode TEXT DEFAULT '',
        qty INTEGER NOT NULL CHECK (qty > 0),
        unit_price REAL NOT NULL,                 -- harga jual saat transaksi
        unit_cost REAL NOT NULL DEFAULT 0,        -- harga modal saat transaksi
        subtotal REAL NOT NULL,                   -- qty * unit_price
        profit REAL NOT NULL DEFAULT 0,           -- (unit_price - unit_cost) * qty
        created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
    );

CREATE TABLE sales (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        invoice_number TEXT NOT NULL UNIQUE,
        total_amount REAL NOT NULL DEFAULT 0,      -- sebelum diskon & pajak
        discount REAL NOT NULL DEFAULT 0,
        tax_amount REAL NOT NULL DEFAULT 0,
        grand_total REAL NOT NULL DEFAULT 0,       -- yang dibayar pelanggan
        total_profit REAL NOT NULL DEFAULT 0,      -- keuntungan kotor transaksi
        payment_method TEXT DEFAULT 'cash',       -- cash, transfer, qris, mixed
        amount_paid REAL DEFAULT 0,
        change_amount REAL DEFAULT 0,
        customer_name TEXT DEFAULT '',
        customer_phone TEXT DEFAULT '',
        notes TEXT DEFAULT '',
        cashier_name TEXT DEFAULT 'Kasir',
        is_void INTEGER NOT NULL DEFAULT 0,
        void_reason TEXT DEFAULT '',
        created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
        updated_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
    );

CREATE TABLE settings (
        id INTEGER PRIMARY KEY CHECK (id = 1),
        store_name TEXT NOT NULL DEFAULT 'Toko Saya',
        store_address TEXT DEFAULT '',
        store_phone TEXT DEFAULT '',
        store_email TEXT DEFAULT '',
        store_logo_path TEXT DEFAULT '',
        receipt_footnote TEXT DEFAULT 'Terima kasih telah berbelanja!',
        currency TEXT NOT NULL DEFAULT 'IDR',
        tax_rate REAL NOT NULL DEFAULT 0.0,
        receipt_prefix TEXT DEFAULT 'INV',
        low_stock_threshold INTEGER DEFAULT 5,
        created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
        updated_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
    );

CREATE TABLE shipping_labels (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        sale_id INTEGER REFERENCES sales(id) ON DELETE SET NULL,
        invoice_number TEXT DEFAULT '',
        recipient_name TEXT NOT NULL,
        recipient_phone TEXT DEFAULT '',
        recipient_address TEXT NOT NULL,
        recipient_city TEXT DEFAULT '',
        recipient_postal_code TEXT DEFAULT '',
        courier TEXT DEFAULT '',                  -- JNE, J&T, SiCepat, dll
        service_type TEXT DEFAULT '',             -- REG, YES, OKE, dll
        tracking_number TEXT DEFAULT '',
        shipping_cost REAL NOT NULL DEFAULT 0,
        weight_gram INTEGER DEFAULT 0,
        status TEXT NOT NULL DEFAULT 'draft',     -- draft, printed, shipped, delivered, cancelled
        notes TEXT DEFAULT '',
        printed_at TEXT,
        shipped_at TEXT,
        created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
        updated_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
    );

CREATE TABLE stock_movements (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        product_id INTEGER NOT NULL REFERENCES products(id) ON DELETE CASCADE,
        type TEXT NOT NULL CHECK (type IN ('in', 'out', 'sale', 'adjust', 'return', 'void')),
        qty INTEGER NOT NULL,                     -- positif = masuk, negatif = keluar
        qty_before INTEGER NOT NULL,
        qty_after INTEGER NOT NULL,
        reference_type TEXT DEFAULT '',           -- 'sale', 'purchase', 'adjust', 'void'
        reference_id INTEGER,
        notes TEXT DEFAULT '',
        created_by TEXT DEFAULT 'system',
        created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
    );

CREATE INDEX idx_products_active ON products(is_active);

CREATE INDEX idx_products_barcode ON products(barcode);

CREATE INDEX idx_products_category ON products(category_id);

CREATE INDEX idx_products_name ON products(name);

CREATE INDEX idx_products_stock ON products(stock);

CREATE INDEX idx_sale_items_product ON sale_items(product_id);

CREATE INDEX idx_sale_items_sale ON sale_items(sale_id);

CREATE INDEX idx_sales_created ON sales(created_at);

CREATE INDEX idx_sales_invoice ON sales(invoice_number);

CREATE INDEX idx_sales_void ON sales(is_void);

CREATE INDEX idx_shipping_sale ON shipping_labels(sale_id);

CREATE INDEX idx_shipping_status ON shipping_labels(status);

CREATE INDEX idx_shipping_tracking ON shipping_labels(tracking_number);

CREATE INDEX idx_stock_movements_created ON stock_movements(created_at);

CREATE INDEX idx_stock_movements_product ON stock_movements(product_id);

CREATE INDEX idx_stock_movements_type ON stock_movements(type);

CREATE VIEW v_daily_sales AS
    SELECT 
        date(created_at) AS sale_date,
        COUNT(*) AS total_transactions,
        SUM(CASE WHEN is_void = 0 THEN grand_total ELSE 0 END) AS total_revenue,
        SUM(CASE WHEN is_void = 0 THEN total_profit ELSE 0 END) AS total_profit,
        SUM(CASE WHEN is_void = 1 THEN 1 ELSE 0 END) AS void_count
    FROM sales
    GROUP BY date(created_at)
    ORDER BY sale_date DESC;

CREATE VIEW v_low_stock AS
    SELECT 
        p.id,
        p.name,
        p.barcode,
        p.stock,
        p.min_stock,
        c.name AS category_name
    FROM products p
    LEFT JOIN categories c ON c.id = p.category_id
    WHERE p.is_active = 1 AND p.stock <= p.min_stock
    ORDER BY p.stock ASC;

CREATE VIEW v_product_profit AS
    SELECT 
        p.id,
        p.name,
        p.barcode,
        p.stock,
        COALESCE(SUM(si.qty), 0) AS total_sold,
        COALESCE(SUM(si.subtotal), 0) AS total_revenue,
        COALESCE(SUM(si.profit), 0) AS total_profit,
        CASE 
            WHEN COALESCE(SUM(si.qty), 0) > 0 
            THEN ROUND(COALESCE(SUM(si.profit), 0) / SUM(si.qty), 2)
            ELSE 0 
        END AS avg_profit_per_unit
    FROM products p
    LEFT JOIN sale_items si ON si.product_id = p.id
    LEFT JOIN sales s ON s.id = si.sale_id AND s.is_void = 0
    GROUP BY p.id
    ORDER BY total_profit DESC;

CREATE VIEW v_sale_details AS
    SELECT 
        s.id AS sale_id,
        s.invoice_number,
        s.created_at,
        s.grand_total,
        s.total_profit,
        s.payment_method,
        s.customer_name,
        s.is_void,
        si.product_name,
        si.qty,
        si.unit_price,
        si.subtotal,
        si.profit AS item_profit
    FROM sales s
    JOIN sale_items si ON si.sale_id = s.id
    ORDER BY s.created_at DESC, si.id;

CREATE TRIGGER trg_products_updated
    AFTER UPDATE ON products
    FOR EACH ROW
    BEGIN
        UPDATE products SET updated_at = datetime('now', 'localtime') WHERE id = OLD.id;
    END;

CREATE TRIGGER trg_sale_item_insert
    AFTER INSERT ON sale_items
    FOR EACH ROW
    WHEN NEW.product_id IS NOT NULL
    BEGIN
        -- Kurangi stok
        UPDATE products 
        SET stock = stock - NEW.qty,
            updated_at = datetime('now', 'localtime')
        WHERE id = NEW.product_id;

        -- Catat pergerakan stok
        INSERT INTO stock_movements (
            product_id, type, qty, qty_before, qty_after,
            reference_type, reference_id, notes, created_by
        )
        SELECT 
            NEW.product_id,
            'sale',
            -NEW.qty,
            p.stock + NEW.qty,   -- sebelum dikurangi
            p.stock,             -- setelah
            'sale',
            NEW.sale_id,
            'Penjualan ' || (SELECT invoice_number FROM sales WHERE id = NEW.sale_id),
            'system'
        FROM products p WHERE p.id = NEW.product_id;
    END;

CREATE TRIGGER trg_sale_void
    AFTER UPDATE OF is_void ON sales
    FOR EACH ROW
    WHEN NEW.is_void = 1 AND OLD.is_void = 0
    BEGIN
        -- Kembalikan stok untuk setiap item
        UPDATE products
        SET stock = stock + (
            SELECT COALESCE(SUM(qty), 0) FROM sale_items WHERE sale_id = NEW.id AND product_id = products.id
        ),
        updated_at = datetime('now', 'localtime')
        WHERE id IN (SELECT product_id FROM sale_items WHERE sale_id = NEW.id AND product_id IS NOT NULL);

        -- Catat movement return
        INSERT INTO stock_movements (
            product_id, type, qty, qty_before, qty_after,
            reference_type, reference_id, notes, created_by
        )
        SELECT 
            si.product_id,
            'void',
            si.qty,
            p.stock - si.qty,
            p.stock,
            'void',
            NEW.id,
            'Void transaksi ' || NEW.invoice_number,
            'system'
        FROM sale_items si
        JOIN products p ON p.id = si.product_id
        WHERE si.sale_id = NEW.id AND si.product_id IS NOT NULL;
    END;

CREATE TRIGGER trg_sales_updated
    AFTER UPDATE ON sales
    FOR EACH ROW
    BEGIN
        UPDATE sales SET updated_at = datetime('now', 'localtime') WHERE id = OLD.id;
    END;

CREATE TRIGGER trg_settings_updated
    AFTER UPDATE ON settings
    FOR EACH ROW
    BEGIN
        UPDATE settings SET updated_at = datetime('now', 'localtime') WHERE id = 1;
    END;

