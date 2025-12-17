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
    // Coupon management
    case 'coupons':
        getCoupons();
        break;
    case 'create_coupon':
        if ($method === 'POST') {
            createCoupon();
        }
        break;
    case 'update_coupon':
        if ($method === 'POST') {
            updateCoupon();
        }
        break;
    case 'delete_coupon':
        if ($method === 'POST') {
            deleteCoupon();
        }
        break;
    case 'toggle_coupon':
        if ($method === 'POST') {
            toggleCouponStatus();
        }
        break;
    case 'assign_coupon':
        if ($method === 'POST') {
            assignCouponToUser();
        }
        break;
    case 'user_coupons':
        getUserCoupons();
        break;
    case 'remove_user_coupon':
        if ($method === 'POST') {
            removeUserCoupon();
        }
        break;
    case 'coupon_stats':
        getCouponStats();
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

// Coupon Management Functions
function getCoupons() {
    global $conn;
    
    $sql = "SELECT c.*, 
            (SELECT COUNT(*) FROM coupon_usage WHERE coupon_id = c.id) as times_used
            FROM coupons c 
            ORDER BY c.created_at DESC";
    
    $result = $conn->query($sql);
    $coupons = [];
    
    while ($row = $result->fetch_assoc()) {
        $coupons[] = $row;
    }
    
    echo json_encode(["success" => true, "coupons" => $coupons]);
}

function createCoupon() {
    global $conn;
    
    $data = json_decode(file_get_contents("php://input"), true);
    
    $code = strtoupper($conn->real_escape_string(trim($data['code'])));
    $description = $conn->real_escape_string($data['description'] ?? '');
    $discountType = $conn->real_escape_string($data['discount_type']);
    $discountValue = floatval($data['discount_value']);
    $minOrderAmount = floatval($data['min_order_amount'] ?? 0);
    $maxDiscount = isset($data['max_discount']) && $data['max_discount'] ? floatval($data['max_discount']) : null;
    $usageLimit = isset($data['usage_limit']) && $data['usage_limit'] ? intval($data['usage_limit']) : null;
    $startDate = $conn->real_escape_string($data['start_date'] ?? date('Y-m-d H:i:s'));
    $endDate = isset($data['end_date']) && $data['end_date'] ? $conn->real_escape_string($data['end_date']) : null;
    $isActive = isset($data['is_active']) ? intval($data['is_active']) : 1;
    
    $maxDiscountSql = $maxDiscount ? $maxDiscount : 'NULL';
    $usageLimitSql = $usageLimit ? $usageLimit : 'NULL';
    $endDateSql = $endDate ? "'$endDate'" : 'NULL';
    
    $sql = "INSERT INTO coupons (code, description, discount_type, discount_value, min_order_amount, max_discount, usage_limit, start_date, end_date, is_active) 
            VALUES ('$code', '$description', '$discountType', $discountValue, $minOrderAmount, $maxDiscountSql, $usageLimitSql, '$startDate', $endDateSql, $isActive)";
    
    if ($conn->query($sql)) {
        echo json_encode(["success" => true, "message" => "Coupon created successfully", "coupon_id" => $conn->insert_id]);
    } else {
        echo json_encode(["success" => false, "message" => "Failed to create coupon: " . $conn->error]);
    }
}

function updateCoupon() {
    global $conn;
    
    $data = json_decode(file_get_contents("php://input"), true);
    
    $id = intval($data['id']);
    $code = strtoupper($conn->real_escape_string(trim($data['code'])));
    $description = $conn->real_escape_string($data['description'] ?? '');
    $discountType = $conn->real_escape_string($data['discount_type']);
    $discountValue = floatval($data['discount_value']);
    $minOrderAmount = floatval($data['min_order_amount'] ?? 0);
    $maxDiscount = isset($data['max_discount']) && $data['max_discount'] ? floatval($data['max_discount']) : null;
    $usageLimit = isset($data['usage_limit']) && $data['usage_limit'] ? intval($data['usage_limit']) : null;
    $startDate = $conn->real_escape_string($data['start_date']);
    $endDate = isset($data['end_date']) && $data['end_date'] ? $conn->real_escape_string($data['end_date']) : null;
    $isActive = isset($data['is_active']) ? intval($data['is_active']) : 1;
    
    $maxDiscountSql = $maxDiscount ? $maxDiscount : 'NULL';
    $usageLimitSql = $usageLimit ? $usageLimit : 'NULL';
    $endDateSql = $endDate ? "'$endDate'" : 'NULL';
    
    $sql = "UPDATE coupons SET 
            code = '$code',
            description = '$description',
            discount_type = '$discountType',
            discount_value = $discountValue,
            min_order_amount = $minOrderAmount,
            max_discount = $maxDiscountSql,
            usage_limit = $usageLimitSql,
            start_date = '$startDate',
            end_date = $endDateSql,
            is_active = $isActive
            WHERE id = $id";
    
    if ($conn->query($sql)) {
        echo json_encode(["success" => true, "message" => "Coupon updated successfully"]);
    } else {
        echo json_encode(["success" => false, "message" => "Failed to update coupon: " . $conn->error]);
    }
}

function deleteCoupon() {
    global $conn;
    
    $data = json_decode(file_get_contents("php://input"), true);
    $id = intval($data['id']);
    
    // Check if coupon has been used
    $check = $conn->query("SELECT COUNT(*) as count FROM coupon_usage WHERE coupon_id = $id");
    if ($check->fetch_assoc()['count'] > 0) {
        echo json_encode(["success" => false, "message" => "Cannot delete coupon that has been used. Deactivate it instead."]);
        return;
    }
    
    $conn->query("DELETE FROM user_coupons WHERE coupon_id = $id");
    
    if ($conn->query("DELETE FROM coupons WHERE id = $id")) {
        echo json_encode(["success" => true, "message" => "Coupon deleted successfully"]);
    } else {
        echo json_encode(["success" => false, "message" => "Failed to delete coupon"]);
    }
}

function toggleCouponStatus() {
    global $conn;
    
    $data = json_decode(file_get_contents("php://input"), true);
    $id = intval($data['id']);
    
    $sql = "UPDATE coupons SET is_active = NOT is_active WHERE id = $id";
    
    if ($conn->query($sql)) {
        echo json_encode(["success" => true, "message" => "Coupon status updated"]);
    } else {
        echo json_encode(["success" => false, "message" => "Failed to update coupon status"]);
    }
}

function assignCouponToUser() {
    global $conn;
    
    $data = json_decode(file_get_contents("php://input"), true);
    $userId = intval($data['user_id']);
    $couponId = intval($data['coupon_id']);
    
    // Check if already assigned
    $check = $conn->query("SELECT id FROM user_coupons WHERE user_id = $userId AND coupon_id = $couponId");
    if ($check->num_rows > 0) {
        echo json_encode(["success" => false, "message" => "Coupon already assigned to this user"]);
        return;
    }
    
    $sql = "INSERT INTO user_coupons (user_id, coupon_id) VALUES ($userId, $couponId)";
    
    if ($conn->query($sql)) {
        echo json_encode(["success" => true, "message" => "Coupon assigned to user successfully"]);
    } else {
        echo json_encode(["success" => false, "message" => "Failed to assign coupon: " . $conn->error]);
    }
}

function getUserCoupons() {
    global $conn;
    
    $userId = isset($_GET['user_id']) ? intval($_GET['user_id']) : 0;
    
    if ($userId > 0) {
        // Get coupons for specific user
        $sql = "SELECT uc.*, c.code, c.description, c.discount_type, c.discount_value, c.min_order_amount, c.max_discount
                FROM user_coupons uc
                JOIN coupons c ON uc.coupon_id = c.id
                WHERE uc.user_id = $userId
                ORDER BY uc.assigned_at DESC";
    } else {
        // Get all user-coupon assignments
        $sql = "SELECT uc.*, c.code, c.description, u.name as user_name, u.email as user_email
                FROM user_coupons uc
                JOIN coupons c ON uc.coupon_id = c.id
                JOIN users u ON uc.user_id = u.id
                ORDER BY uc.assigned_at DESC";
    }
    
    $result = $conn->query($sql);
    $userCoupons = [];
    
    while ($row = $result->fetch_assoc()) {
        $userCoupons[] = $row;
    }
    
    echo json_encode(["success" => true, "user_coupons" => $userCoupons]);
}

function removeUserCoupon() {
    global $conn;
    
    $data = json_decode(file_get_contents("php://input"), true);
    $id = intval($data['id']);
    
    if ($conn->query("DELETE FROM user_coupons WHERE id = $id")) {
        echo json_encode(["success" => true, "message" => "Coupon removed from user"]);
    } else {
        echo json_encode(["success" => false, "message" => "Failed to remove coupon"]);
    }
}

function getCouponStats() {
    global $conn;
    
    $totalCoupons = $conn->query("SELECT COUNT(*) as count FROM coupons")->fetch_assoc()['count'];
    $activeCoupons = $conn->query("SELECT COUNT(*) as count FROM coupons WHERE is_active = 1")->fetch_assoc()['count'];
    $totalUsage = $conn->query("SELECT COUNT(*) as count FROM coupon_usage")->fetch_assoc()['count'];
    $totalDiscount = $conn->query("SELECT SUM(discount_amount) as total FROM coupon_usage")->fetch_assoc()['total'];
    $assignedCoupons = $conn->query("SELECT COUNT(*) as count FROM user_coupons WHERE is_used = 0")->fetch_assoc()['count'];
    
    echo json_encode([
        "success" => true,
        "stats" => [
            "total_coupons" => intval($totalCoupons),
            "active_coupons" => intval($activeCoupons),
            "total_usage" => intval($totalUsage),
            "total_discount" => floatval($totalDiscount ?? 0),
            "assigned_coupons" => intval($assignedCoupons)
        ]
    ]);
}

$conn->close();
?>
