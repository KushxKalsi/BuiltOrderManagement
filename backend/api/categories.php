<?php
require_once '../config.php';

$method = $_SERVER['REQUEST_METHOD'];

$sql = "SELECT * FROM categories ORDER BY name ASC";
$result = $conn->query($sql);
$categories = [];

while ($row = $result->fetch_assoc()) {
    $categories[] = $row;
}

echo json_encode(["success" => true, "categories" => $categories]);

$conn->close();
?>
