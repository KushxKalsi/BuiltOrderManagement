<?php
require_once '../config.php';

$method = $_SERVER['REQUEST_METHOD'];
$action = isset($_GET['action']) ? $_GET['action'] : '';

switch ($action) {
    case 'register':
        if ($method === 'POST') {
            register();
        }
        break;
    case 'login':
        if ($method === 'POST') {
            login();
        }
        break;
    case 'update':
        if ($method === 'POST') {
            updateProfile();
        }
        break;
    default:
        echo json_encode(["success" => false, "message" => "Invalid action"]);
}

function hashPassword($password) {
    return hash('sha256', $password . 'store_salt_2024');
}

function register() {
    global $conn;
    
    $data = json_decode(file_get_contents("php://input"), true);
    
    $name = $conn->real_escape_string($data['name']);
    $email = $conn->real_escape_string($data['email']);
    $phone = isset($data['phone']) ? $conn->real_escape_string($data['phone']) : '';
    $password = hashPassword($data['password']);
    $address = isset($data['address']) ? $conn->real_escape_string($data['address']) : '';
    
    // Check if email exists
    $check = $conn->query("SELECT id FROM users WHERE email = '$email'");
    if ($check->num_rows > 0) {
        echo json_encode(["success" => false, "message" => "Email already registered"]);
        return;
    }
    
    $sql = "INSERT INTO users (name, email, phone, password_hash, address) VALUES ('$name', '$email', '$phone', '$password', '$address')";
    
    if ($conn->query($sql) === TRUE) {
        $userId = $conn->insert_id;
        $result = $conn->query("SELECT id, name, email, phone, address, created_at FROM users WHERE id = $userId");
        $user = $result->fetch_assoc();
        echo json_encode(["success" => true, "message" => "Registration successful", "user" => $user]);
    } else {
        echo json_encode(["success" => false, "message" => "Registration failed: " . $conn->error]);
    }
}

function login() {
    global $conn;
    
    $data = json_decode(file_get_contents("php://input"), true);
    
    $email = $conn->real_escape_string($data['email']);
    $password = hashPassword($data['password']);
    
    $sql = "SELECT id, name, email, phone, address, created_at FROM users WHERE email = '$email' AND password_hash = '$password'";
    $result = $conn->query($sql);
    
    if ($result->num_rows > 0) {
        $user = $result->fetch_assoc();
        echo json_encode(["success" => true, "message" => "Login successful", "user" => $user]);
    } else {
        echo json_encode(["success" => false, "message" => "Invalid email or password"]);
    }
}

function updateProfile() {
    global $conn;
    
    $data = json_decode(file_get_contents("php://input"), true);
    
    $userId = intval($data['user_id']);
    $name = $conn->real_escape_string($data['name']);
    $phone = isset($data['phone']) ? $conn->real_escape_string($data['phone']) : '';
    $address = isset($data['address']) ? $conn->real_escape_string($data['address']) : '';
    
    $sql = "UPDATE users SET name = '$name', phone = '$phone', address = '$address' WHERE id = $userId";
    
    if ($conn->query($sql) === TRUE) {
        $result = $conn->query("SELECT id, name, email, phone, address, created_at FROM users WHERE id = $userId");
        $user = $result->fetch_assoc();
        echo json_encode(["success" => true, "message" => "Profile updated", "user" => $user]);
    } else {
        echo json_encode(["success" => false, "message" => "Update failed: " . $conn->error]);
    }
}

$conn->close();
?>
