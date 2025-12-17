<?php
require_once '../config.php';

$method = $_SERVER['REQUEST_METHOD'];
$action = isset($_GET['action']) ? $_GET['action'] : '';

switch ($action) {
    case 'create':
        if ($method === 'POST') {
            createOrder();
        }
        break;
    case 'list':
        getUserOrders();
        break;
    case 'detail':
        getOrderDetail();
        break;
    case 'cancel':
        if ($method === 'POST') {
            cancelOrder();
        }
        break;
    default:
        echo json_encode(["success" => false, "message" => "Invalid action"]);
}

function createOrder() {
    global $conn;
    
    $data = json_decode(file_get_contents("php://input"), true);
    
    $userId = intval($data['user_id']);
    $totalAmount = floatval($data['total_amount']);
    $discountAmount = isset($data['discount_amount']) ? floatval($data['discount_amount']) : 0;
    $couponCode = isset($data['coupon_code']) ? $conn->real_escape_string($data['coupon_code']) : null;
    $finalAmount = $totalAmount - $discountAmount;
    $shippingAddress = $conn->real_escape_string($data['shipping_address']);
    $paymentMethod = isset($data['payment_method']) ? $conn->real_escape_string($data['payment_method']) : 'COD';
    $notes = isset($data['notes']) ? $conn->real_escape_string($data['notes']) : '';
    $items = $data['items'];
    
    $conn->begin_transaction();
    
    try {
        // Create order
        $couponCodeSql = $couponCode ? "'$couponCode'" : "NULL";
        $sql = "INSERT INTO orders (user_id, total_amount, discount_amount, coupon_code, final_amount, shipping_address, payment_method, notes) 
                VALUES ($userId, $totalAmount, $discountAmount, $couponCodeSql, $finalAmount, '$shippingAddress', '$paymentMethod', '$notes')";
        
        if (!$conn->query($sql)) {
            throw new Exception("Failed to create order");
        }
        
        $orderId = $conn->insert_id;
        
        // Add order items
        foreach ($items as $item) {
            $productId = intval($item['product_id']);
            $quantity = intval($item['quantity']);
            $price = floatval($item['price']);
            
            $itemSql = "INSERT INTO order_items (order_id, product_id, quantity, price) 
                        VALUES ($orderId, $productId, $quantity, $price)";
            
            if (!$conn->query($itemSql)) {
                throw new Exception("Failed to add order item");
            }
            
            // Update stock
            $conn->query("UPDATE products SET stock = stock - $quantity WHERE id = $productId");
        }
        
        // Record coupon usage if coupon was applied
        if ($couponCode) {
            $couponResult = $conn->query("SELECT id FROM coupons WHERE code = '$couponCode'");
            if ($couponResult->num_rows > 0) {
                $couponId = $couponResult->fetch_assoc()['id'];
                $conn->query("INSERT INTO coupon_usage (coupon_id, user_id, order_id, discount_amount) VALUES ($couponId, $userId, $orderId, $discountAmount)");
                $conn->query("UPDATE coupons SET used_count = used_count + 1 WHERE id = $couponId");
                
                // Mark user-specific coupon as used if applicable
                $conn->query("UPDATE user_coupons SET is_used = 1, used_at = NOW() WHERE coupon_id = $couponId AND user_id = $userId");
            }
        }
        
        $conn->commit();
        
        echo json_encode([
            "success" => true, 
            "message" => "Order placed successfully", 
            "order_id" => $orderId
        ]);
        
    } catch (Exception $e) {
        $conn->rollback();
        echo json_encode(["success" => false, "message" => $e->getMessage()]);
    }
}

function getUserOrders() {
    global $conn;
    
    $userId = isset($_GET['user_id']) ? intval($_GET['user_id']) : 0;
    
    $sql = "SELECT * FROM orders WHERE user_id = $userId ORDER BY created_at DESC";
    $result = $conn->query($sql);
    $orders = [];
    
    while ($row = $result->fetch_assoc()) {
        // Get order items count
        $itemsResult = $conn->query("SELECT COUNT(*) as count FROM order_items WHERE order_id = " . $row['id']);
        $row['items_count'] = $itemsResult->fetch_assoc()['count'];
        $orders[] = $row;
    }
    
    echo json_encode(["success" => true, "orders" => $orders]);
}

function getOrderDetail() {
    global $conn;
    
    $orderId = isset($_GET['order_id']) ? intval($_GET['order_id']) : 0;
    
    $sql = "SELECT * FROM orders WHERE id = $orderId";
    $result = $conn->query($sql);
    
    if ($result->num_rows === 0) {
        echo json_encode(["success" => false, "message" => "Order not found"]);
        return;
    }
    
    $order = $result->fetch_assoc();
    
    // Get order items with product details
    $itemsSql = "SELECT oi.*, p.name, p.image_url FROM order_items oi 
                 JOIN products p ON oi.product_id = p.id 
                 WHERE oi.order_id = $orderId";
    $itemsResult = $conn->query($itemsSql);
    $items = [];
    
    while ($item = $itemsResult->fetch_assoc()) {
        $items[] = $item;
    }
    
    $order['items'] = $items;
    
    echo json_encode(["success" => true, "order" => $order]);
}

function cancelOrder() {
    global $conn;
    
    $data = json_decode(file_get_contents("php://input"), true);
    $orderId = intval($data['order_id']);
    
    // Check if order can be cancelled
    $result = $conn->query("SELECT status FROM orders WHERE id = $orderId");
    if ($result->num_rows === 0) {
        echo json_encode(["success" => false, "message" => "Order not found"]);
        return;
    }
    
    $order = $result->fetch_assoc();
    if ($order['status'] !== 'pending') {
        echo json_encode(["success" => false, "message" => "Order cannot be cancelled"]);
        return;
    }
    
    // Restore stock
    $items = $conn->query("SELECT product_id, quantity FROM order_items WHERE order_id = $orderId");
    while ($item = $items->fetch_assoc()) {
        $conn->query("UPDATE products SET stock = stock + " . $item['quantity'] . " WHERE id = " . $item['product_id']);
    }
    
    // Update order status
    $conn->query("UPDATE orders SET status = 'cancelled' WHERE id = $orderId");
    
    echo json_encode(["success" => true, "message" => "Order cancelled successfully"]);
}

$conn->close();
?>
