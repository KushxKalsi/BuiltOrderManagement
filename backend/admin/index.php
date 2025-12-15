<?php require_once '../config/version.php'; ?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard - Order Management</title>
    <link rel="stylesheet" href="../assets/css/styles.css?v=<?= $v ?>">
</head>
<body>
    <div class="admin-container">
        <!-- Sidebar -->
        <aside class="sidebar">
            <div class="logo">
                <h2>Admin Panel</h2>
            </div>
            <nav class="nav-menu">
                <a href="#" class="nav-item active" data-page="dashboard">
                    <span class="icon">📊</span>
                    Dashboard
                </a>
                <a href="#" class="nav-item" data-page="orders">
                    <span class="icon">📦</span>
                    Orders
                </a>
                <a href="#" class="nav-item" data-page="products">
                    <span class="icon">🛍️</span>
                    Products
                </a>
                <a href="#" class="nav-item" data-page="categories">
                    <span class="icon">📁</span>
                    Categories
                </a>
                <a href="#" class="nav-item" data-page="users">
                    <span class="icon">👥</span>
                    Users
                </a>
            </nav>
        </aside>

        <!-- Main Content -->
        <main class="main-content">
            <header class="header">
                <h1 id="page-title">Dashboard</h1>
                <div class="user-info">
                    <span>Admin User</span>
                    <button class="btn-logout" onclick="logout()">Logout</button>
                </div>
            </header>

            <div class="content-area">
                <!-- Dashboard Page -->
                <div id="dashboard-page" class="page active">
                    <div class="stats-grid">
                        <div class="stat-card">
                            <h3>Total Orders</h3>
                            <p class="stat-value" id="total-orders">0</p>
                        </div>
                        <div class="stat-card">
                            <h3>Pending Orders</h3>
                            <p class="stat-value" id="pending-orders">0</p>
                        </div>
                        <div class="stat-card">
                            <h3>Total Products</h3>
                            <p class="stat-value" id="total-products">0</p>
                        </div>
                        <div class="stat-card">
                            <h3>Total Revenue</h3>
                            <p class="stat-value" id="total-revenue">₹0</p>
                        </div>
                    </div>
                    <div class="recent-orders">
                        <h2>Recent Orders</h2>
                        <div id="recent-orders-list"></div>
                    </div>
                </div>

                <!-- Orders Page -->
                <div id="orders-page" class="page">
                    <div class="page-header">
                        <div class="filters">
                            <select id="order-status-filter" onchange="filterOrders()">
                                <option value="">All Status</option>
                                <option value="pending">Pending</option>
                                <option value="processing">Processing</option>
                                <option value="shipped">Shipped</option>
                                <option value="delivered">Delivered</option>
                                <option value="cancelled">Cancelled</option>
                            </select>
                            <input type="text" id="order-search" placeholder="Search orders..." onkeyup="searchOrders()">
                        </div>
                    </div>
                    <div id="orders-list"></div>
                </div>

                <!-- Products Page -->
                <div id="products-page" class="page">
                    <div class="page-header">
                        <button class="btn-primary" onclick="showAddProductModal()">+ Add Product</button>
                        <input type="text" id="product-search" placeholder="Search products..." onkeyup="searchProducts()">
                    </div>
                    <div id="products-list"></div>
                </div>

                <!-- Categories Page -->
                <div id="categories-page" class="page">
                    <div class="page-header">
                        <button class="btn-primary" onclick="showAddCategoryModal()">+ Add Category</button>
                    </div>
                    <div id="categories-list"></div>
                </div>

                <!-- Users Page -->
                <div id="users-page" class="page">
                    <div id="users-list"></div>
                </div>
            </div>
        </main>
    </div>

    <!-- Modals -->
    <div id="order-modal" class="modal">
        <div class="modal-content">
            <span class="close" onclick="closeModal('order-modal')">&times;</span>
            <h2>Order Details</h2>
            <div id="order-details"></div>
        </div>
    </div>

    <div id="product-modal" class="modal">
        <div class="modal-content">
            <span class="close" onclick="closeModal('product-modal')">&times;</span>
            <h2 id="product-modal-title">Add Product</h2>
            <form id="product-form" onsubmit="saveProduct(event)">
                <input type="hidden" id="product-id">
                <div class="form-group">
                    <label>Product Name</label>
                    <input type="text" id="product-name" required>
                </div>
                <div class="form-group">
                    <label>Description</label>
                    <textarea id="product-description" rows="3" required></textarea>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label>Price</label>
                        <input type="number" id="product-price" step="0.01" required>
                    </div>
                    <div class="form-group">
                        <label>Discount Price (Optional)</label>
                        <input type="number" id="product-discount-price" step="0.01" placeholder="Leave empty for no discount">
                    </div>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label>Stock</label>
                        <input type="number" id="product-stock" required>
                    </div>
                    <div class="form-group">
                        <label>Category</label>
                        <select id="product-category" required></select>
                    </div>
                </div>
                <div class="form-group">
                    <label>Image URL</label>
                    <input type="text" id="product-image" oninput="previewProductImage()">
                    <div id="product-image-preview" style="margin-top: 10px; display: none;">
                        <img id="product-preview-img" src="" alt="Preview" style="max-width: 200px; max-height: 200px; border-radius: 8px; border: 1px solid #ddd;">
                    </div>
                </div>
                <div class="form-group">
                    <label>
                        <input type="checkbox" id="product-featured">
                        Featured Product
                    </label>
                </div>
                <div class="form-actions">
                    <button type="button" class="btn-secondary" onclick="closeModal('product-modal')">Cancel</button>
                    <button type="submit" class="btn-primary">Save</button>
                </div>
            </form>
        </div>
    </div>

    <div id="category-modal" class="modal">
        <div class="modal-content">
            <span class="close" onclick="closeModal('category-modal')">&times;</span>
            <h2 id="category-modal-title">Add Category</h2>
            <form id="category-form" onsubmit="saveCategory(event)">
                <input type="hidden" id="category-id">
                <div class="form-group">
                    <label>Category Name</label>
                    <input type="text" id="category-name" required>
                </div>
                <div class="form-group">
                    <label>Description</label>
                    <textarea id="category-description" rows="2"></textarea>
                </div>
                <div class="form-group">
                    <label>Image URL</label>
                    <input type="text" id="category-image" placeholder="https://example.com/image.jpg" oninput="previewCategoryImage()">
                    <div id="category-image-preview" style="margin-top: 10px; display: none;">
                        <img id="category-preview-img" src="" alt="Preview" style="max-width: 200px; max-height: 200px; border-radius: 8px; border: 1px solid #ddd;">
                    </div>
                </div>
                <div class="form-actions">
                    <button type="button" class="btn-secondary" onclick="closeModal('category-modal')">Cancel</button>
                    <button type="submit" class="btn-primary">Save</button>
                </div>
            </form>
        </div>
    </div>

    <script src="../assets/js/script.js?v=<?= $v ?>"></script>
</body>
</html>
