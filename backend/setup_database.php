<?php
require_once 'config.php';

// Create users table
$sql_users = "CREATE TABLE IF NOT EXISTS users (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB";

// Create categories table
$sql_categories = "CREATE TABLE IF NOT EXISTS categories (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    image_url VARCHAR(500),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB";

// Create products table
$sql_products = "CREATE TABLE IF NOT EXISTS products (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    category_id INT UNSIGNED,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    discount_price DECIMAL(10, 2),
    image_url VARCHAR(500),
    stock INT DEFAULT 0,
    rating DECIMAL(2, 1) DEFAULT 0,
    is_featured BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB";

// Create orders table
$sql_orders = "CREATE TABLE IF NOT EXISTS orders (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNSIGNED NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    discount_amount DECIMAL(10, 2) DEFAULT 0,
    coupon_code VARCHAR(50) DEFAULT NULL,
    final_amount DECIMAL(10, 2) NOT NULL,
    status ENUM('pending', 'confirmed', 'shipped', 'delivered', 'cancelled') DEFAULT 'pending',
    shipping_address TEXT NOT NULL,
    payment_method VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB";

// Create order_items table
$sql_order_items = "CREATE TABLE IF NOT EXISTS order_items (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    order_id INT UNSIGNED NOT NULL,
    product_id INT UNSIGNED NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB";

// Create coupons table
$sql_coupons = "CREATE TABLE IF NOT EXISTS coupons (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    discount_type ENUM('percentage', 'fixed') NOT NULL DEFAULT 'percentage',
    discount_value DECIMAL(10, 2) NOT NULL,
    min_order_amount DECIMAL(10, 2) DEFAULT 0,
    max_discount DECIMAL(10, 2) DEFAULT NULL,
    usage_limit INT DEFAULT NULL,
    used_count INT DEFAULT 0,
    start_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    end_date DATETIME DEFAULT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB";

// Create coupon_usage table WITHOUT foreign keys first (to avoid constraint issues with existing tables)
$sql_coupon_usage = "CREATE TABLE IF NOT EXISTS coupon_usage (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    coupon_id INT UNSIGNED NOT NULL,
    user_id INT UNSIGNED NOT NULL,
    order_id INT UNSIGNED NOT NULL,
    discount_amount DECIMAL(10, 2) NOT NULL,
    used_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_coupon_id (coupon_id),
    INDEX idx_user_id (user_id),
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB";

// First create tables without coupon_usage
$tables = [
    "users" => $sql_users,
    "categories" => $sql_categories,
    "products" => $sql_products,
    "coupons" => $sql_coupons
];

$results = [];

foreach ($tables as $name => $sql) {
    if ($conn->query($sql) === TRUE) {
        $results[$name] = "Created successfully";
    } else {
        $results[$name] = "Error: " . $conn->error;
    }
}

// Check if orders table exists and has old structure - drop and recreate if needed
$ordersCheck = $conn->query("SHOW COLUMNS FROM orders LIKE 'final_amount'");
$ordersExists = $conn->query("SHOW TABLES LIKE 'orders'")->num_rows > 0;

if ($ordersExists && $ordersCheck && $ordersCheck->num_rows == 0) {
    // Old orders table exists without new columns - add them
    $conn->query("ALTER TABLE orders ADD COLUMN discount_amount DECIMAL(10, 2) DEFAULT 0 AFTER total_amount");
    $conn->query("ALTER TABLE orders ADD COLUMN coupon_code VARCHAR(50) DEFAULT NULL AFTER discount_amount");
    $conn->query("ALTER TABLE orders ADD COLUMN final_amount DECIMAL(10, 2) DEFAULT 0 AFTER coupon_code");
    $conn->query("UPDATE orders SET final_amount = total_amount WHERE final_amount = 0");
    $results["orders"] = "Updated with new columns";
} else {
    // Create orders table
    if ($conn->query($sql_orders) === TRUE) {
        $results["orders"] = "Created successfully";
    } else {
        $results["orders"] = "Error: " . $conn->error;
    }
}

// Create order_items
if ($conn->query($sql_order_items) === TRUE) {
    $results["order_items"] = "Created successfully";
} else {
    $results["order_items"] = "Error: " . $conn->error;
}

// Create coupon_usage
if ($conn->query($sql_coupon_usage) === TRUE) {
    $results["coupon_usage"] = "Created successfully";
} else {
    $results["coupon_usage"] = "Error: " . $conn->error;
}

// Insert sample categories
$sample_categories = [
    ["Electronics", "https://images.unsplash.com/photo-1498049794561-7780e7231661?w=400", "Latest electronic gadgets"],
    ["Fashion", "https://images.unsplash.com/photo-1445205170230-053b83016050?w=400", "Trendy fashion items"],
    ["Home & Living", "https://images.unsplash.com/photo-1484101403633-562f891dc89a?w=400", "Home decor and furniture"],
    ["Sports", "https://images.unsplash.com/photo-1461896836934-ffe607ba8211?w=400", "Sports equipment and gear"]
];

foreach ($sample_categories as $cat) {
    $stmt = $conn->prepare("INSERT IGNORE INTO categories (name, image_url, description) VALUES (?, ?, ?)");
    $stmt->bind_param("sss", $cat[0], $cat[1], $cat[2]);
    $stmt->execute();
}

// Insert sample products
$sample_products = [
    [1, "Wireless Headphones", "Premium noise-cancelling wireless headphones", 149.99, 129.99, "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400", 50, 4.5, true],
    [1, "Smart Watch", "Feature-rich smartwatch with health tracking", 299.99, 249.99, "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400", 30, 4.3, true],
    [1, "Bluetooth Speaker", "Portable waterproof speaker", 79.99, null, "https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=400", 100, 4.2, false],
    [2, "Designer Jacket", "Premium leather jacket", 199.99, 159.99, "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=400", 25, 4.6, true],
    [2, "Running Shoes", "Comfortable athletic shoes", 129.99, null, "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400", 80, 4.4, false],
    [3, "Modern Lamp", "Minimalist desk lamp", 49.99, 39.99, "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=400", 60, 4.1, false],
    [3, "Cozy Blanket", "Soft fleece blanket", 34.99, null, "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=400", 120, 4.7, true],
    [4, "Yoga Mat", "Non-slip exercise mat", 29.99, 24.99, "https://images.unsplash.com/photo-1601925260368-ae2f83cf8b7f?w=400", 200, 4.5, false]
];

foreach ($sample_products as $prod) {
    $stmt = $conn->prepare("INSERT IGNORE INTO products (category_id, name, description, price, discount_price, image_url, stock, rating, is_featured) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
    $stmt->bind_param("issddsidb", $prod[0], $prod[1], $prod[2], $prod[3], $prod[4], $prod[5], $prod[6], $prod[7], $prod[8]);
    $stmt->execute();
}

// Insert sample coupons
$sample_coupons = [
    ["WELCOME10", "Welcome discount - 10% off on your first order", "percentage", 10.00, 0, 50.00, 100, "2024-01-01 00:00:00", "2025-12-31 23:59:59"],
    ["FLAT20", "Flat $20 off on orders above $100", "fixed", 20.00, 100.00, null, 50, "2024-01-01 00:00:00", "2025-12-31 23:59:59"],
    ["SAVE15", "Save 15% on all products", "percentage", 15.00, 50.00, 30.00, null, "2024-01-01 00:00:00", "2025-12-31 23:59:59"],
    ["SUMMER25", "Summer sale - 25% off", "percentage", 25.00, 75.00, 100.00, 200, "2024-06-01 00:00:00", "2025-08-31 23:59:59"]
];

foreach ($sample_coupons as $coupon) {
    $stmt = $conn->prepare("INSERT IGNORE INTO coupons (code, description, discount_type, discount_value, min_order_amount, max_discount, usage_limit, start_date, end_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
    $stmt->bind_param("sssdddiis", $coupon[0], $coupon[1], $coupon[2], $coupon[3], $coupon[4], $coupon[5], $coupon[6], $coupon[7], $coupon[8]);
    $stmt->execute();
}

echo json_encode(["success" => true, "message" => "Database setup complete", "tables" => $results]);

$conn->close();
?>
