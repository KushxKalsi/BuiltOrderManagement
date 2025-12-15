<?php
require_once '../config.php';

$method = $_SERVER['REQUEST_METHOD'];
$action = isset($_GET['action']) ? $_GET['action'] : '';

// Simple authentication check (you should implement proper authentication)
// For now, we'll skip authentication for development purposes

switch ($action) {
    case 'stats':
        getStats();
        break;
    case 'recent_orders':
        getRecentOrders();
        break;
    case 'all_orders':
        getAllOrders();
        break;
    case 'update_order_status':
        if ($method === 'POST') {
            updateOrderStatus();
        }
        break;
    case 'create_product':
        if ($method === 'POST') {
            createProduct();
        }
        break;
    case 'update_product':
        if ($method === 'POST') {
            updateProduct();
        }
        break;
    case 'delete_product':
        if ($method === 'POST') {
            deleteProduct();
        }
        break;
    case 'create_category':
        if ($method === 'POST') {
            createCategory();
        }
        break;
    case 'update_category':
        if ($method === 'POST') {
            updateCategory();
        }
        break;
    case 'delete_category':
        if ($method === 'POST') {
            deleteCategory();
        }
        break;
    case 'users':
        getUsers();
        break;
    default:
        echo json_encode(["success" => false, "message" => "Invalid action"]);
}

function getStats() {
    global $conn;
    
    // Total orders
    $totalOrders = $conn->query("SELECT COUNT(*) as count FROM orders")->fetch_assoc()['count'];
    
    // Pending orders
    $pendingOrders = $conn->query("SELECT COUNT(*) as count FROM orders WHERE status = 'pending'")->fetch_assoc()['count'];
    
    // Total revenue
    $totalRevenue = $conn->query("SELECT SUM(total_amount) as total FROM orders WHERE status != 'cancelled'")->fetch_assoc()['total'];
    
    echo json_encode([
        "success" => true,
        "stats" => [
            "total_orders" => intval($totalOrders),
            "pending_orders" => intval($pendingOrders),
            "total_revenue" => floatval($totalRevenue)
        ]
    ]);
}

function getRecentOrders() {
    global $conn;
    
    $sql = "SELECT o.*, u.name as user_name FROM orders o 
            LEFT JOIN users u ON o.user_id = u.id 
            ORDER BY o.created_at DESC 
            LIMIT 10";
    
    $result = $conn->query($sql);
    $orders = [];
    
    while ($row = $result->fetch_assoc()) {
        $orders[] = $row;
    }
    
    echo json_encode(["success" => true, "orders" => $orders]);
}

function getAllOrders() {
    global $conn;
    
    $status = isset($_GET['status']) ? $conn->real_escape_string($_GET['status']) : '';
    $search = isset($_GET['search']) ? $conn->real_escape_string($_GET['search']) : '';
    
    $sql = "SELECT o.*, u.name as user_name FROM orders o 
            LEFT JOIN users u ON o.user_id = u.id 
            WHERE 1=1";
    
    if ($status) {
        $sql .= " AND o.status = '$status'";
    }
    
    if ($search) {
        $sql .= " AND (o.id LIKE '%$search%' OR u.name LIKE '%$search%' OR o.shipping_address LIKE '%$search%')";
    }
    
    $sql .= " ORDER BY o.created_at DESC";
    
    $result = $conn->query($sql);
    $orders = [];
    
    while ($row = $result->fetch_assoc()) {
        $orders[] = $row;
    }
    
    echo json_encode(["success" => true, "orders" => $orders]);
}

function updateOrderStatus() {
    global $conn;
    
    $data = json_decode(file_get_contents("php://input"), true);
    $orderId = intval($data['order_id']);
    $status = $conn->real_escape_string($data['status']);
    
    $validStatuses = ['pending', 'processing', 'shipped', 'delivered', 'cancelled'];
    if (!in_array($status, $validStatuses)) {
        echo json_encode(["success" => false, "message" => "Invalid status"]);
        return;
    }
    
    $sql = "UPDATE orders SET status = '$status' WHERE id = $orderId";
    
    if ($conn->query($sql)) {
        echo json_encode(["success" => true, "message" => "Order status updated successfully"]);
    } else {
        echo json_encode(["success" => false, "message" => "Failed to update order status"]);
    }
}

function createProduct() {
    global $conn;
    
    $data = json_decode(file_get_contents("php://input"), true);
    
    $name = $conn->real_escape_string($data['name']);
    $description = $conn->real_escape_string($data['description']);
    $price = floatval($data['price']);
    $discountPrice = isset($data['discount_price']) && $data['discount_price'] ? floatval($data['discount_price']) : null;
    $stock = intval($data['stock']);
    $categoryId = intval($data['category_id']);
    $imageUrl = isset($data['image_url']) ? $conn->real_escape_string($data['image_url']) : '';
    $isFeatured = isset($data['is_featured']) ? intval($data['is_featured']) : 0;
    
    $discountPriceValue = $discountPrice ? $discountPrice : 'NULL';
    
    $sql = "INSERT INTO products (name, description, price, discount_price, stock, category_id, image_url, is_featured) 
            VALUES ('$name', '$description', $price, $discountPriceValue, $stock, $categoryId, '$imageUrl', $isFeatured)";
    
    if ($conn->query($sql)) {
        echo json_encode([
            "success" => true, 
            "message" => "Product created successfully",
            "product_id" => $conn->insert_id
        ]);
    } else {
        echo json_encode(["success" => false, "message" => "Failed to create product: " . $conn->error]);
    }
}

function updateProduct() {
    global $conn;
    
    $data = json_decode(file_get_contents("php://input"), true);
    
    $id = intval($data['id']);
    $name = $conn->real_escape_string($data['name']);
    $description = $conn->real_escape_string($data['description']);
    $price = floatval($data['price']);
    $discountPrice = isset($data['discount_price']) && $data['discount_price'] ? floatval($data['discount_price']) : null;
    $stock = intval($data['stock']);
    $categoryId = intval($data['category_id']);
    $imageUrl = isset($data['image_url']) ? $conn->real_escape_string($data['image_url']) : '';
    $isFeatured = isset($data['is_featured']) ? intval($data['is_featured']) : 0;
    
    $discountPriceValue = $discountPrice ? $discountPrice : 'NULL';
    
    $sql = "UPDATE products SET 
            name = '$name',
            description = '$description',
            price = $price,
            discount_price = $discountPriceValue,
            stock = $stock,
            category_id = $categoryId,
            image_url = '$imageUrl',
            is_featured = $isFeatured
            WHERE id = $id";
    
    if ($conn->query($sql)) {
        echo json_encode(["success" => true, "message" => "Product updated successfully"]);
    } else {
        echo json_encode(["success" => false, "message" => "Failed to update product: " . $conn->error]);
    }
}

function deleteProduct() {
    global $conn;
    
    $data = json_decode(file_get_contents("php://input"), true);
    $id = intval($data['id']);
    
    // Check if product is in any orders
    $check = $conn->query("SELECT COUNT(*) as count FROM order_items WHERE product_id = $id");
    if ($check->fetch_assoc()['count'] > 0) {
        echo json_encode(["success" => false, "message" => "Cannot delete product that has been ordered"]);
        return;
    }
    
    $sql = "DELETE FROM products WHERE id = $id";
    
    if ($conn->query($sql)) {
        echo json_encode(["success" => true, "message" => "Product deleted successfully"]);
    } else {
        echo json_encode(["success" => false, "message" => "Failed to delete product"]);
    }
}

function createCategory() {
    global $conn;
    
    $data = json_decode(file_get_contents("php://input"), true);
    
    $name = $conn->real_escape_string($data['name']);
    $description = isset($data['description']) ? $conn->real_escape_string($data['description']) : '';
    $imageUrl = isset($data['image_url']) ? $conn->real_escape_string($data['image_url']) : '';
    
    $sql = "INSERT INTO categories (name, description, image_url) VALUES ('$name', '$description', '$imageUrl')";
    
    if ($conn->query($sql)) {
        echo json_encode([
            "success" => true, 
            "message" => "Category created successfully",
            "category_id" => $conn->insert_id
        ]);
    } else {
        echo json_encode(["success" => false, "message" => "Failed to create category: " . $conn->error]);
    }
}

function updateCategory() {
    global $conn;
    
    $data = json_decode(file_get_contents("php://input"), true);
    
    $id = intval($data['id']);
    $name = $conn->real_escape_string($data['name']);
    $description = isset($data['description']) ? $conn->real_escape_string($data['description']) : '';
    $imageUrl = isset($data['image_url']) ? $conn->real_escape_string($data['image_url']) : '';
    
    $sql = "UPDATE categories SET name = '$name', description = '$description', image_url = '$imageUrl' WHERE id = $id";
    
    if ($conn->query($sql)) {
        echo json_encode(["success" => true, "message" => "Category updated successfully"]);
    } else {
        echo json_encode(["success" => false, "message" => "Failed to update category: " . $conn->error]);
    }
}

function deleteCategory() {
    global $conn;
    
    $data = json_decode(file_get_contents("php://input"), true);
    $id = intval($data['id']);
    
    // Check if category has products
    $check = $conn->query("SELECT COUNT(*) as count FROM products WHERE category_id = $id");
    if ($check->fetch_assoc()['count'] > 0) {
        echo json_encode(["success" => false, "message" => "Cannot delete category that has products"]);
        return;
    }
    
    $sql = "DELETE FROM categories WHERE id = $id";
    
    if ($conn->query($sql)) {
        echo json_encode(["success" => true, "message" => "Category deleted successfully"]);
    } else {
        echo json_encode(["success" => false, "message" => "Failed to delete category"]);
    }
}

function getUsers() {
    global $conn;
    
    $sql = "SELECT u.*, COUNT(o.id) as order_count 
            FROM users u 
            LEFT JOIN orders o ON u.id = o.user_id 
            GROUP BY u.id 
            ORDER BY u.created_at DESC";
    
    $result = $conn->query($sql);
    $users = [];
    
    while ($row = $result->fetch_assoc()) {
        $users[] = $row;
    }
    
    echo json_encode(["success" => true, "users" => $users]);
}

$conn->close();
?>
