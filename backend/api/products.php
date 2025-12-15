<?php
require_once '../config.php';

$method = $_SERVER['REQUEST_METHOD'];
$action = isset($_GET['action']) ? $_GET['action'] : 'list';

switch ($action) {
    case 'list':
        getProducts();
        break;
    case 'featured':
        getFeaturedProducts();
        break;
    case 'category':
        getProductsByCategory();
        break;
    case 'search':
        searchProducts();
        break;
    case 'detail':
        getProductDetail();
        break;
    default:
        echo json_encode(["success" => false, "message" => "Invalid action"]);
}

function getProducts() {
    global $conn;
    
    $page = isset($_GET['page']) ? intval($_GET['page']) : 1;
    $limit = isset($_GET['limit']) ? intval($_GET['limit']) : 20;
    $offset = ($page - 1) * $limit;
    $search = isset($_GET['search']) ? $conn->real_escape_string($_GET['search']) : '';
    
    $sql = "SELECT p.*, c.name as category_name FROM products p 
            LEFT JOIN categories c ON p.category_id = c.id";
    
    if ($search) {
        $sql .= " WHERE p.name LIKE '%$search%' OR p.description LIKE '%$search%'";
    }
    
    $sql .= " ORDER BY p.created_at DESC LIMIT $limit OFFSET $offset";
    
    $result = $conn->query($sql);
    $products = [];
    
    while ($row = $result->fetch_assoc()) {
        $products[] = $row;
    }
    
    // Get total count
    $countSql = "SELECT COUNT(*) as total FROM products";
    if ($search) {
        $countSql .= " WHERE name LIKE '%$search%' OR description LIKE '%$search%'";
    }
    $countResult = $conn->query($countSql);
    $total = $countResult->fetch_assoc()['total'];
    
    echo json_encode([
        "success" => true, 
        "products" => $products,
        "total" => intval($total),
        "page" => $page,
        "limit" => $limit
    ]);
}

function getFeaturedProducts() {
    global $conn;
    
    $sql = "SELECT p.*, c.name as category_name FROM products p 
            LEFT JOIN categories c ON p.category_id = c.id 
            WHERE p.is_featured = 1 
            ORDER BY p.rating DESC 
            LIMIT 10";
    
    $result = $conn->query($sql);
    $products = [];
    
    while ($row = $result->fetch_assoc()) {
        $products[] = $row;
    }
    
    echo json_encode(["success" => true, "products" => $products]);
}

function getProductsByCategory() {
    global $conn;
    
    $categoryId = isset($_GET['category_id']) ? intval($_GET['category_id']) : 0;
    $page = isset($_GET['page']) ? intval($_GET['page']) : 1;
    $limit = isset($_GET['limit']) ? intval($_GET['limit']) : 20;
    $offset = ($page - 1) * $limit;
    
    $sql = "SELECT p.*, c.name as category_name FROM products p 
            LEFT JOIN categories c ON p.category_id = c.id 
            WHERE p.category_id = $categoryId 
            ORDER BY p.created_at DESC 
            LIMIT $limit OFFSET $offset";
    
    $result = $conn->query($sql);
    $products = [];
    
    while ($row = $result->fetch_assoc()) {
        $products[] = $row;
    }
    
    echo json_encode(["success" => true, "products" => $products]);
}

function searchProducts() {
    global $conn;
    
    $query = isset($_GET['q']) ? $conn->real_escape_string($_GET['q']) : '';
    
    $sql = "SELECT p.*, c.name as category_name FROM products p 
            LEFT JOIN categories c ON p.category_id = c.id 
            WHERE p.name LIKE '%$query%' OR p.description LIKE '%$query%' 
            ORDER BY p.rating DESC 
            LIMIT 50";
    
    $result = $conn->query($sql);
    $products = [];
    
    while ($row = $result->fetch_assoc()) {
        $products[] = $row;
    }
    
    echo json_encode(["success" => true, "products" => $products]);
}

function getProductDetail() {
    global $conn;
    
    $productId = isset($_GET['id']) ? intval($_GET['id']) : 0;
    
    $sql = "SELECT p.*, c.name as category_name FROM products p 
            LEFT JOIN categories c ON p.category_id = c.id 
            WHERE p.id = $productId";
    
    $result = $conn->query($sql);
    
    if ($result->num_rows > 0) {
        $product = $result->fetch_assoc();
        echo json_encode(["success" => true, "product" => $product]);
    } else {
        echo json_encode(["success" => false, "message" => "Product not found"]);
    }
}

$conn->close();
?>
