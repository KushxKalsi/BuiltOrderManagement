<?php
require_once 'config.php';

// Add discount_price column to products table if it doesn't exist
$sql = "ALTER TABLE products ADD COLUMN IF NOT EXISTS discount_price DECIMAL(10, 2) DEFAULT NULL AFTER price";

if ($conn->query($sql) === TRUE) {
    echo json_encode([
        "success" => true, 
        "message" => "Migration successful: discount_price column added to products table"
    ]);
} else {
    // Check if column already exists
    $check = $conn->query("SHOW COLUMNS FROM products LIKE 'discount_price'");
    if ($check->num_rows > 0) {
        echo json_encode([
            "success" => true, 
            "message" => "Column already exists, no migration needed"
        ]);
    } else {
        echo json_encode([
            "success" => false, 
            "message" => "Migration failed: " . $conn->error
        ]);
    }
}

$conn->close();
?>
