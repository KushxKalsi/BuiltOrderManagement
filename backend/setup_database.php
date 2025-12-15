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

$tables = [
    "users" => $sql_users,
    "categories" => $sql_categories,
    "products" => $sql_products,
    "orders" => $sql_orders,
    "order_items" => $sql_order_items
];

$results = [];

foreach ($tables as $name => $sql) {
    if ($conn->query($sql) === TRUE) {
        $results[$name] = "Created successfully";
    } else {
        $results[$name] = "Error: " . $conn->error;
    }
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

echo json_encode(["success" => true, "message" => "Database setup complete", "tables" => $results]);

$conn->close();
?>
