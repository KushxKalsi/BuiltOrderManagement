<?php
require_once '../config.php';

$method = $_SERVER['REQUEST_METHOD'];
$action = isset($_GET['action']) ? $_GET['action'] : '';

switch ($action) {
    case 'validate':
        if ($method === 'POST') {
            validateCoupon();
        }
        break;
    case 'list':
        getActiveCoupons();
        break;
    case 'apply':
        if ($method === 'POST') {
            applyCoupon();
        }
        break;
    default:
        echo json_encode(["success" => false, "message" => "Invalid action"]);
}

function validateCoupon() {
    global $conn;
    
    $data = json_decode(file_get_contents("php://input"), true);
    
    $code = strtoupper(trim($conn->real_escape_string($data['code'])));
    $orderAmount = floatval($data['order_amount']);
    $userId = isset($data['user_id']) ? intval($data['user_id']) : 0;
    
    // Find coupon
    $sql = "SELECT * FROM coupons WHERE code = '$code' AND is_active = 1";
    $result = $conn->query($sql);
    
    if ($result->num_rows === 0) {
        echo json_encode(["success" => false, "message" => "Invalid coupon code"]);
        return;
    }
    
    $coupon = $result->fetch_assoc();
    
    // Check if coupon is within valid date range
    $now = date('Y-m-d H:i:s');
    if ($coupon['start_date'] && $now < $coupon['start_date']) {
        echo json_encode(["success" => false, "message" => "Coupon is not yet active"]);
        return;
    }
    if ($coupon['end_date'] && $now > $coupon['end_date']) {
        echo json_encode(["success" => false, "message" => "Coupon has expired"]);
        return;
    }
    
    // Check usage limit
    if ($coupon['usage_limit'] !== null && $coupon['used_count'] >= $coupon['usage_limit']) {
        echo json_encode(["success" => false, "message" => "Coupon usage limit reached"]);
        return;
    }
    
    // Check minimum order amount
    if ($orderAmount < $coupon['min_order_amount']) {
        echo json_encode([
            "success" => false, 
            "message" => "Minimum order amount of $" . number_format($coupon['min_order_amount'], 2) . " required"
        ]);
        return;
    }
    
    // Check if user already used this coupon (if user_id provided)
    if ($userId > 0) {
        $usageCheck = $conn->query("SELECT id FROM coupon_usage WHERE coupon_id = " . $coupon['id'] . " AND user_id = $userId");
        if ($usageCheck->num_rows > 0) {
            echo json_encode(["success" => false, "message" => "You have already used this coupon"]);
            return;
        }
    }
    
    // Calculate discount
    $discountAmount = 0;
    if ($coupon['discount_type'] === 'percentage') {
        $discountAmount = ($orderAmount * $coupon['discount_value']) / 100;
        // Apply max discount cap if set
        if ($coupon['max_discount'] !== null && $discountAmount > $coupon['max_discount']) {
            $discountAmount = $coupon['max_discount'];
        }
    } else {
        // Fixed discount
        $discountAmount = $coupon['discount_value'];
    }
    
    // Ensure discount doesn't exceed order amount
    if ($discountAmount > $orderAmount) {
        $discountAmount = $orderAmount;
    }
    
    $finalAmount = $orderAmount - $discountAmount;
    
    echo json_encode([
        "success" => true,
        "message" => "Coupon applied successfully",
        "coupon" => [
            "id" => intval($coupon['id']),
            "code" => $coupon['code'],
            "description" => $coupon['description'],
            "discount_type" => $coupon['discount_type'],
            "discount_value" => floatval($coupon['discount_value']),
            "discount_amount" => round($discountAmount, 2),
            "final_amount" => round($finalAmount, 2)
        ]
    ]);
}

function getActiveCoupons() {
    global $conn;
    
    $now = date('Y-m-d H:i:s');
    
    $sql = "SELECT id, code, description, discount_type, discount_value, min_order_amount, max_discount 
            FROM coupons 
            WHERE is_active = 1 
            AND (start_date IS NULL OR start_date <= '$now')
            AND (end_date IS NULL OR end_date >= '$now')
            AND (usage_limit IS NULL OR used_count < usage_limit)
            ORDER BY discount_value DESC";
    
    $result = $conn->query($sql);
    $coupons = [];
    
    while ($row = $result->fetch_assoc()) {
        $coupons[] = [
            "id" => intval($row['id']),
            "code" => $row['code'],
            "description" => $row['description'],
            "discount_type" => $row['discount_type'],
            "discount_value" => floatval($row['discount_value']),
            "min_order_amount" => floatval($row['min_order_amount']),
            "max_discount" => $row['max_discount'] ? floatval($row['max_discount']) : null
        ];
    }
    
    echo json_encode(["success" => true, "coupons" => $coupons]);
}

function applyCoupon() {
    global $conn;
    
    $data = json_decode(file_get_contents("php://input"), true);
    
    $couponId = intval($data['coupon_id']);
    $userId = intval($data['user_id']);
    $orderId = intval($data['order_id']);
    $discountAmount = floatval($data['discount_amount']);
    
    // Record coupon usage
    $sql = "INSERT INTO coupon_usage (coupon_id, user_id, order_id, discount_amount) 
            VALUES ($couponId, $userId, $orderId, $discountAmount)";
    
    if ($conn->query($sql)) {
        // Increment used_count
        $conn->query("UPDATE coupons SET used_count = used_count + 1 WHERE id = $couponId");
        echo json_encode(["success" => true, "message" => "Coupon usage recorded"]);
    } else {
        echo json_encode(["success" => false, "message" => "Failed to record coupon usage"]);
    }
}

$conn->close();
?>
