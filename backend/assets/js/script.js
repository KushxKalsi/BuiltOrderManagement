const API_BASE = '../api';

// Check authentication
function checkAuth() {
    if (localStorage.getItem('admin_logged_in') !== 'true') {
        window.location.href = 'login.html';
    }
}

// Call on page load
checkAuth();

// Navigation
document.querySelectorAll('.nav-item').forEach(item => {
    item.addEventListener('click', (e) => {
        e.preventDefault();
        const page = item.dataset.page;
        switchPage(page);
    });
});

function switchPage(page) {
    // Update nav
    document.querySelectorAll('.nav-item').forEach(item => {
        item.classList.remove('active');
    });
    document.querySelector(`[data-page="${page}"]`).classList.add('active');
    
    // Update content
    document.querySelectorAll('.page').forEach(p => {
        p.classList.remove('active');
    });
    document.getElementById(`${page}-page`).classList.add('active');
    
    // Update title
    const titles = {
        dashboard: 'Dashboard',
        orders: 'Orders Management',
        products: 'Products Management',
        categories: 'Categories Management',
        users: 'Users Management'
    };
    document.getElementById('page-title').textContent = titles[page];
    
    // Load page data
    loadPageData(page);
}

function loadPageData(page) {
    switch(page) {
        case 'dashboard':
            loadDashboard();
            break;
        case 'orders':
            loadOrders();
            break;
        case 'products':
            loadProducts();
            break;
        case 'categories':
            loadCategories();
            break;
        case 'users':
            loadUsers();
            break;
    }
}

// Dashboard
async function loadDashboard() {
    try {
        const [ordersRes, productsRes] = await Promise.all([
            fetch(`${API_BASE}/admin.php?action=stats`),
            fetch(`${API_BASE}/products.php?action=list`)
        ]);
        
        const ordersData = await ordersRes.json();
        const productsData = await productsRes.json();
        
        if (ordersData.success) {
            document.getElementById('total-orders').textContent = ordersData.stats.total_orders || 0;
            document.getElementById('pending-orders').textContent = ordersData.stats.pending_orders || 0;
            document.getElementById('total-revenue').textContent = '₹' + (ordersData.stats.total_revenue || 0).toFixed(2);
        }
        
        if (productsData.success) {
            document.getElementById('total-products').textContent = productsData.total || 0;
        }
        
        loadRecentOrders();
    } catch (error) {
        console.error('Error loading dashboard:', error);
    }
}

async function loadRecentOrders() {
    try {
        const response = await fetch(`${API_BASE}/admin.php?action=recent_orders`);
        const data = await response.json();
        
        if (data.success) {
            const container = document.getElementById('recent-orders-list');
            container.innerHTML = data.orders.map(order => `
                <div class="order-card" onclick="viewOrder(${order.id})">
                    <div class="order-header">
                        <span class="order-id">Order #${order.id}</span>
                        <span class="status-badge status-${order.status}">${order.status}</span>
                    </div>
                    <div class="order-info">
                        <div>Amount: ₹${parseFloat(order.total_amount).toFixed(2)}</div>
                        <div>Date: ${new Date(order.created_at).toLocaleDateString()}</div>
                    </div>
                </div>
            `).join('');
        }
    } catch (error) {
        console.error('Error loading recent orders:', error);
    }
}

// Orders
async function loadOrders(status = '', search = '') {
    try {
        let url = `${API_BASE}/admin.php?action=all_orders`;
        if (status) url += `&status=${status}`;
        if (search) url += `&search=${search}`;
        
        const response = await fetch(url);
        const data = await response.json();
        
        if (data.success) {
            const container = document.getElementById('orders-list');
            if (data.orders.length === 0) {
                container.innerHTML = '<div class="empty-state"><h3>No orders found</h3></div>';
                return;
            }
            
            container.innerHTML = `
                <table class="table">
                    <thead>
                        <tr>
                            <th>Order ID</th>
                            <th>User</th>
                            <th>Amount</th>
                            <th>Status</th>
                            <th>Date</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${data.orders.map(order => `
                            <tr>
                                <td>#${order.id}</td>
                                <td>${order.user_name || 'User #' + order.user_id}</td>
                                <td>₹${parseFloat(order.total_amount).toFixed(2)}</td>
                                <td><span class="status-badge status-${order.status}">${order.status}</span></td>
                                <td>${new Date(order.created_at).toLocaleDateString()}</td>
                                <td>
                                    <button class="btn-primary" onclick="viewOrder(${order.id})">View</button>
                                    <button class="btn-warning" onclick="updateOrderStatus(${order.id})">Update</button>
                                </td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            `;
        }
    } catch (error) {
        console.error('Error loading orders:', error);
    }
}

function filterOrders() {
    const status = document.getElementById('order-status-filter').value;
    const search = document.getElementById('order-search').value;
    loadOrders(status, search);
}

function searchOrders() {
    filterOrders();
}

async function viewOrder(orderId) {
    try {
        const response = await fetch(`${API_BASE}/orders.php?action=detail&order_id=${orderId}`);
        const data = await response.json();
        
        if (data.success) {
            const order = data.order;
            const modal = document.getElementById('order-modal');
            const details = document.getElementById('order-details');
            
            details.innerHTML = `
                <div style="margin-bottom: 20px;">
                    <h3>Order #${order.id}</h3>
                    <p><strong>Status:</strong> <span class="status-badge status-${order.status}">${order.status}</span></p>
                    <p><strong>Date:</strong> ${new Date(order.created_at).toLocaleString()}</p>
                    <p><strong>Total Amount:</strong> ₹${parseFloat(order.total_amount).toFixed(2)}</p>
                    <p><strong>Payment Method:</strong> ${order.payment_method}</p>
                    <p><strong>Shipping Address:</strong> ${order.shipping_address}</p>
                    ${order.notes ? `<p><strong>Notes:</strong> ${order.notes}</p>` : ''}
                </div>
                
                <h4>Order Items</h4>
                <table class="table">
                    <thead>
                        <tr>
                            <th>Product</th>
                            <th>Quantity</th>
                            <th>Price</th>
                            <th>Subtotal</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${order.items.map(item => `
                            <tr>
                                <td>${item.name}</td>
                                <td>${item.quantity}</td>
                                <td>₹${parseFloat(item.price).toFixed(2)}</td>
                                <td>₹${(item.quantity * parseFloat(item.price)).toFixed(2)}</td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
                
                <div style="margin-top: 20px;">
                    <label><strong>Update Status:</strong></label>
                    <select id="order-status-update" style="margin: 10px 0; padding: 8px; width: 100%;">
                        <option value="pending" ${order.status === 'pending' ? 'selected' : ''}>Pending</option>
                        <option value="processing" ${order.status === 'processing' ? 'selected' : ''}>Processing</option>
                        <option value="shipped" ${order.status === 'shipped' ? 'selected' : ''}>Shipped</option>
                        <option value="delivered" ${order.status === 'delivered' ? 'selected' : ''}>Delivered</option>
                        <option value="cancelled" ${order.status === 'cancelled' ? 'selected' : ''}>Cancelled</option>
                    </select>
                    <button class="btn-primary" onclick="saveOrderStatus(${order.id})">Update Status</button>
                </div>
            `;
            
            modal.classList.add('active');
        }
    } catch (error) {
        console.error('Error viewing order:', error);
        alert('Error loading order details');
    }
}

async function saveOrderStatus(orderId) {
    const status = document.getElementById('order-status-update').value;
    
    try {
        const response = await fetch(`${API_BASE}/admin.php?action=update_order_status`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ order_id: orderId, status })
        });
        
        const data = await response.json();
        
        if (data.success) {
            alert('Order status updated successfully');
            closeModal('order-modal');
            loadOrders();
        } else {
            alert('Error: ' + data.message);
        }
    } catch (error) {
        console.error('Error updating order:', error);
        alert('Error updating order status');
    }
}

// Products
async function loadProducts(search = '') {
    try {
        let url = `${API_BASE}/products.php?action=list&limit=100`;
        if (search) url += `&search=${search}`;
        
        const response = await fetch(url);
        const data = await response.json();
        
        if (data.success) {
            const container = document.getElementById('products-list');
            if (data.products.length === 0) {
                container.innerHTML = '<div class="empty-state"><h3>No products found</h3></div>';
                return;
            }
            
            container.innerHTML = `
                <div class="products-grid">
                    ${data.products.map(product => {
                        const hasDiscount = product.discount_price && parseFloat(product.discount_price) < parseFloat(product.price);
                        const displayPrice = hasDiscount ? parseFloat(product.discount_price) : parseFloat(product.price);
                        return `
                        <div class="product-card">
                            <img src="${product.image_url || 'https://via.placeholder.com/250'}" 
                                 alt="${product.name}" class="product-image">
                            <div class="product-info">
                                <h3>${product.name}</h3>
                                <p style="font-size: 12px; color: #666;">${product.category_name || 'Uncategorized'}</p>
                                <p class="product-price">
                                    ${hasDiscount ? `<span style="text-decoration: line-through; color: #999; font-size: 14px;">$${parseFloat(product.price).toFixed(2)}</span> ` : ''}
                                    ₹${displayPrice.toFixed(2)}
                                    ${hasDiscount ? '<span class="status-badge" style="background: #ff4444; color: white; margin-left: 5px;">SALE</span>' : ''}
                                </p>
                                <p style="font-size: 14px;">Stock: ${product.stock}</p>
                                ${product.is_featured == 1 ? '<span class="status-badge status-delivered">Featured</span>' : ''}
                            </div>
                            <div class="product-actions">
                                <button class="btn-primary" onclick="editProduct(${product.id})">Edit</button>
                                <button class="btn-danger" onclick="deleteProduct(${product.id})">Delete</button>
                            </div>
                        </div>
                    `}).join('')}
                </div>
            `;
        }
    } catch (error) {
        console.error('Error loading products:', error);
    }
}

function searchProducts() {
    const search = document.getElementById('product-search').value;
    loadProducts(search);
}

async function showAddProductModal() {
    document.getElementById('product-modal-title').textContent = 'Add Product';
    document.getElementById('product-form').reset();
    document.getElementById('product-id').value = '';
    document.getElementById('product-image-preview').style.display = 'none';
    
    await loadCategoriesForSelect();
    document.getElementById('product-modal').classList.add('active');
}

function previewProductImage() {
    const imageUrl = document.getElementById('product-image').value;
    const preview = document.getElementById('product-image-preview');
    const previewImg = document.getElementById('product-preview-img');
    
    if (imageUrl) {
        previewImg.src = imageUrl;
        preview.style.display = 'block';
        
        // Hide preview if image fails to load
        previewImg.onerror = function() {
            preview.style.display = 'none';
        };
    } else {
        preview.style.display = 'none';
    }
}

async function editProduct(productId) {
    try {
        const response = await fetch(`${API_BASE}/products.php?action=detail&id=${productId}`);
        const data = await response.json();
        
        if (data.success) {
            const product = data.product;
            document.getElementById('product-modal-title').textContent = 'Edit Product';
            document.getElementById('product-id').value = product.id;
            document.getElementById('product-name').value = product.name;
            document.getElementById('product-description').value = product.description;
            document.getElementById('product-price').value = product.price;
            document.getElementById('product-discount-price').value = product.discount_price || '';
            document.getElementById('product-stock').value = product.stock;
            document.getElementById('product-image').value = product.image_url || '';
            document.getElementById('product-featured').checked = product.is_featured == 1;
            
            // Show image preview if image exists
            if (product.image_url) {
                const preview = document.getElementById('product-image-preview');
                const previewImg = document.getElementById('product-preview-img');
                previewImg.src = product.image_url;
                preview.style.display = 'block';
            } else {
                document.getElementById('product-image-preview').style.display = 'none';
            }
            
            await loadCategoriesForSelect();
            document.getElementById('product-category').value = product.category_id;
            
            document.getElementById('product-modal').classList.add('active');
        }
    } catch (error) {
        console.error('Error loading product:', error);
    }
}

async function saveProduct(event) {
    event.preventDefault();
    
    const productId = document.getElementById('product-id').value;
    const discountPrice = document.getElementById('product-discount-price').value;
    const productData = {
        name: document.getElementById('product-name').value,
        description: document.getElementById('product-description').value,
        price: document.getElementById('product-price').value,
        discount_price: discountPrice ? discountPrice : null,
        stock: document.getElementById('product-stock').value,
        category_id: document.getElementById('product-category').value,
        image_url: document.getElementById('product-image').value,
        is_featured: document.getElementById('product-featured').checked ? 1 : 0
    };
    
    if (productId) {
        productData.id = productId;
    }
    
    try {
        const action = productId ? 'update' : 'create';
        const response = await fetch(`${API_BASE}/admin.php?action=${action}_product`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(productData)
        });
        
        const data = await response.json();
        
        if (data.success) {
            alert(productId ? 'Product updated successfully' : 'Product created successfully');
            closeModal('product-modal');
            loadProducts();
        } else {
            alert('Error: ' + data.message);
        }
    } catch (error) {
        console.error('Error saving product:', error);
        alert('Error saving product');
    }
}

async function deleteProduct(productId) {
    if (!confirm('Are you sure you want to delete this product?')) return;
    
    try {
        const response = await fetch(`${API_BASE}/admin.php?action=delete_product`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id: productId })
        });
        
        const data = await response.json();
        
        if (data.success) {
            alert('Product deleted successfully');
            loadProducts();
        } else {
            alert('Error: ' + data.message);
        }
    } catch (error) {
        console.error('Error deleting product:', error);
        alert('Error deleting product');
    }
}

// Categories
async function loadCategories() {
    try {
        const response = await fetch(`${API_BASE}/categories.php?action=list`);
        const data = await response.json();
        
        if (data.success) {
            const container = document.getElementById('categories-list');
            if (data.categories.length === 0) {
                container.innerHTML = '<div class="empty-state"><h3>No categories found</h3></div>';
                return;
            }
            
            container.innerHTML = `
                <table class="table">
                    <thead>
                        <tr>
                            <th>Image</th>
                            <th>Name</th>
                            <th>Description</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${data.categories.map(category => `
                            <tr>
                                <td>
                                    <img src="${category.image_url || 'https://via.placeholder.com/50'}" 
                                         alt="${category.name}" 
                                         style="width: 50px; height: 50px; object-fit: cover; border-radius: 4px;">
                                </td>
                                <td>${category.name}</td>
                                <td>${category.description || '-'}</td>
                                <td>
                                    <button class="btn-primary" onclick="editCategory(${category.id})">Edit</button>
                                    <button class="btn-danger" onclick="deleteCategory(${category.id})">Delete</button>
                                </td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            `;
        }
    } catch (error) {
        console.error('Error loading categories:', error);
    }
}

async function loadCategoriesForSelect() {
    try {
        const response = await fetch(`${API_BASE}/categories.php?action=list`);
        const data = await response.json();
        
        if (data.success) {
            const select = document.getElementById('product-category');
            select.innerHTML = data.categories.map(cat => 
                `<option value="${cat.id}">${cat.name}</option>`
            ).join('');
        }
    } catch (error) {
        console.error('Error loading categories:', error);
    }
}

function showAddCategoryModal() {
    document.getElementById('category-modal-title').textContent = 'Add Category';
    document.getElementById('category-form').reset();
    document.getElementById('category-id').value = '';
    document.getElementById('category-image-preview').style.display = 'none';
    document.getElementById('category-modal').classList.add('active');
}

function previewCategoryImage() {
    const imageUrl = document.getElementById('category-image').value;
    const preview = document.getElementById('category-image-preview');
    const previewImg = document.getElementById('category-preview-img');
    
    if (imageUrl) {
        previewImg.src = imageUrl;
        preview.style.display = 'block';
        
        // Hide preview if image fails to load
        previewImg.onerror = function() {
            preview.style.display = 'none';
        };
    } else {
        preview.style.display = 'none';
    }
}

async function editCategory(categoryId) {
    try {
        const response = await fetch(`${API_BASE}/categories.php?action=list`);
        const data = await response.json();
        
        if (data.success) {
            const category = data.categories.find(c => c.id == categoryId);
            if (category) {
                document.getElementById('category-modal-title').textContent = 'Edit Category';
                document.getElementById('category-id').value = category.id;
                document.getElementById('category-name').value = category.name;
                document.getElementById('category-description').value = category.description || '';
                document.getElementById('category-image').value = category.image_url || '';
                
                // Show image preview if image exists
                if (category.image_url) {
                    const preview = document.getElementById('category-image-preview');
                    const previewImg = document.getElementById('category-preview-img');
                    previewImg.src = category.image_url;
                    preview.style.display = 'block';
                } else {
                    document.getElementById('category-image-preview').style.display = 'none';
                }
                
                document.getElementById('category-modal').classList.add('active');
            }
        }
    } catch (error) {
        console.error('Error loading category:', error);
    }
}

async function saveCategory(event) {
    event.preventDefault();
    
    const categoryId = document.getElementById('category-id').value;
    const categoryData = {
        name: document.getElementById('category-name').value,
        description: document.getElementById('category-description').value,
        image_url: document.getElementById('category-image').value
    };
    
    if (categoryId) {
        categoryData.id = categoryId;
    }
    
    try {
        const action = categoryId ? 'update' : 'create';
        const response = await fetch(`${API_BASE}/admin.php?action=${action}_category`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(categoryData)
        });
        
        const data = await response.json();
        
        if (data.success) {
            alert(categoryId ? 'Category updated successfully' : 'Category created successfully');
            closeModal('category-modal');
            loadCategories();
        } else {
            alert('Error: ' + data.message);
        }
    } catch (error) {
        console.error('Error saving category:', error);
        alert('Error saving category');
    }
}

async function deleteCategory(categoryId) {
    if (!confirm('Are you sure you want to delete this category?')) return;
    
    try {
        const response = await fetch(`${API_BASE}/admin.php?action=delete_category`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id: categoryId })
        });
        
        const data = await response.json();
        
        if (data.success) {
            alert('Category deleted successfully');
            loadCategories();
        } else {
            alert('Error: ' + data.message);
        }
    } catch (error) {
        console.error('Error deleting category:', error);
        alert('Error deleting category');
    }
}

// Users
async function loadUsers() {
    try {
        const response = await fetch(`${API_BASE}/admin.php?action=users`);
        const data = await response.json();
        
        if (data.success) {
            const container = document.getElementById('users-list');
            if (data.users.length === 0) {
                container.innerHTML = '<div class="empty-state"><h3>No users found</h3></div>';
                return;
            }
            
            container.innerHTML = `
                <table class="table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Name</th>
                            <th>Email</th>
                            <th>Phone</th>
                            <th>Registered</th>
                            <th>Orders</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${data.users.map(user => `
                            <tr>
                                <td>${user.id}</td>
                                <td>${user.name}</td>
                                <td>${user.email}</td>
                                <td>${user.phone || '-'}</td>
                                <td>${new Date(user.created_at).toLocaleDateString()}</td>
                                <td>${user.order_count || 0}</td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            `;
        }
    } catch (error) {
        console.error('Error loading users:', error);
    }
}

// Modal functions
function closeModal(modalId) {
    document.getElementById(modalId).classList.remove('active');
}

function logout() {
    if (confirm('Are you sure you want to logout?')) {
        window.location.href = 'login.html';
    }
}

// Initialize
loadDashboard();
